package com.adp.esi.digitech.ds.config.file.schema.service;

import com.adp.esi.digitech.ds.config.exception.ConfigurationException;
import com.adp.esi.digitech.ds.config.model.XSDNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.*;
import java.util.*;

/**
 * Service for parsing XSD files to a generic XsdNode tree structure. Ignores
 * namespaces and documentation for simplicity and broad compatibility.
 * 
 * @author rhidau
 */
@Service
@Slf4j
public class XSDParseService {

	// private final ObjectMapper objectMapper;

	/**
	 * Parses the provided XSD file and returns the root XsdNode tree.
	 *
	 * @param file XSD file as MultipartFile.
	 * @return Root XsdNode of the parsed schema.
	 * @throws ConfigurationException if parsing fails.
	 */
	public XSDNode parseXsd(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ConfigurationException("Provided XSD file is null or empty.");
		}
		try {
			byte[] xsdBytes = file.getBytes();
			Map<String, List<XMLEvent>> typeDefMap = new HashMap<>();
			collectTypes(xsdBytes, typeDefMap);

			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);

			try (InputStream is = new ByteArrayInputStream(xsdBytes)) {
				XMLEventReader reader = factory.createXMLEventReader(is);
				while (reader.hasNext()) {
					XMLEvent event = reader.nextEvent();
					if (event.isStartElement() && isTag(event.asStartElement(), "element")) {
						Set<String> recursionGuard = new HashSet<>();
						XSDNode rootNode = parseElement(reader, event.asStartElement(), typeDefMap, recursionGuard);
						log.info("Successfully parsed XSD root node: {}", rootNode.getName());
						return rootNode;
					}
				}
			}
			throw new ConfigurationException("No <element> found in root XSD file.");
		} catch (Exception ex) {
			log.error("Failed to parse XSD: {}", ex.getMessage(), ex);
			throw new ConfigurationException("Error parsing XSD: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Collects all global type (complexType/simpleType) definitions from the XSD.
	 *
	 * @param xsdBytes   The XSD file as bytes.
	 * @param typeDefMap Map to store type names and their event streams.
	 * @throws XMLStreamException if XML parsing fails.
	 */
	private void collectTypes(byte[] xsdBytes, Map<String, List<XMLEvent>> typeDefMap) throws XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);

		try (InputStream is = new ByteArrayInputStream(xsdBytes)) {
			XMLEventReader reader = factory.createXMLEventReader(is);
			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
				if (event.isStartElement()) {
					StartElement start = event.asStartElement();
					if (isTag(start, "complexType") || isTag(start, "simpleType")) {
						String typeName = getAttr(start, "name");
						if (typeName != null) {
							List<XMLEvent> typeEvents = new ArrayList<>();
							int depth = 1;
							typeEvents.add(event);
							String tag = start.getName().getLocalPart();
							while (reader.hasNext() && depth > 0) {
								XMLEvent next = reader.nextEvent();
								typeEvents.add(next);
								if (next.isStartElement() && tag.equals(next.asStartElement().getName().getLocalPart()))
									depth++;
								else if (next.isEndElement()
										&& tag.equals(next.asEndElement().getName().getLocalPart()))
									depth--;
							}
							typeDefMap.put(typeName, typeEvents);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	/**
	 * Parses an <element> node and its children recursively.
	 * 
	 * @param reader         The XML event reader.
	 * @param start          The starting <element> tag.
	 * @param typeDefMap     Map of type names to their event streams.
	 * @param recursionGuard Set for detecting cycles.
	 * @return Parsed XsdNode.
	 * @throws XMLStreamException if parsing fails.
	 */
	private XSDNode parseElement(XMLEventReader reader, StartElement start, Map<String, List<XMLEvent>> typeDefMap,
			Set<String> recursionGuard) throws XMLStreamException {
		String name = getAttr(start, "name");
		String type = getAttr(start, "type");
		String defaultValue = getAttr(start, "default");

		XSDNode node = XSDNode.builder().name(name).type(type).selectable(true).build();

		for (Iterator<?> it = start.getAttributes(); it.hasNext();) {
			Attribute attr = (Attribute) it.next();
			String k = attr.getName().getLocalPart();
			if (!"name".equals(k) && !"type".equals(k))
				node.getAttrs().put(k, attr.getValue());
		}
		if (defaultValue != null)
			node.getAttrs().put("default", defaultValue);

		String guardKey = name + ":" + type;
		if (type != null && !recursionGuard.add(guardKey)) {
			log.warn("Cycle detected for type '{}'. Skipping expansion.", type);
			return node;
		}

		String typeKey = stripPrefix(type);
		if (type != null && typeDefMap.containsKey(typeKey)) {
			List<XMLEvent> events = typeDefMap.get(typeKey);
			XSDNode typed = parseComplexType(events.iterator(), typeDefMap, recursionGuard);
			node.setChildren(typed.getChildren());
			node.getAttrs().putAll(typed.getAttrs());
			recursionGuard.remove(guardKey);
			return node;
		}

		// Inline complexType/simpleType/attribute/choice/all/sequence, etc.
		while (reader.hasNext()) {
			XMLEvent event = reader.peek();
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				String local = se.getName().getLocalPart();
				switch (local) {
				case "complexType":
					reader.nextEvent();
					XSDNode complexTypeNode = parseComplexType(reader, typeDefMap, recursionGuard);
					node.setChildren(complexTypeNode.getChildren());
					node.getAttrs().putAll(complexTypeNode.getAttrs());
					break;
				case "simpleType":
					reader.nextEvent();
					processSimpleType(reader, se, node);
					break;
				case "attribute":
					reader.nextEvent();
					processAttribute(se, node);
					break;
				case "choice":
				case "sequence":
				case "all":
					reader.nextEvent();
					parseGroup(reader, node, typeDefMap, recursionGuard);
					break;
				default:
					reader.nextEvent();
					break;
				}
			} else if (event.isEndElement() && isTag(event.asEndElement(), "element")) {
				reader.nextEvent();
				break;
			} else {
				reader.nextEvent();
			}
		}
		recursionGuard.remove(guardKey);
		return node;
	}

	/**
	 * Parses a <complexType> node and collects its children and attributes.
	 *
	 * @param iter           Iterator of XMLEvents for the complex type.
	 * @param typeDefMap     Map of type definitions.
	 * @param recursionGuard Set for detecting cycles.
	 * @return XsdNode representing the complex type.
	 * @throws XMLStreamException if parsing fails.
	 */
	private XSDNode parseComplexType(Iterator<XMLEvent> iter, Map<String, List<XMLEvent>> typeDefMap,
			Set<String> recursionGuard) throws XMLStreamException {
		XSDNode parent = XSDNode.builder().selectable(false).build();
		while (iter.hasNext()) {
			XMLEvent event = iter.next();
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				String local = se.getName().getLocalPart();
				switch (local) {
				case "sequence":
				case "choice":
				case "all":
					parseGroup(iter, parent, typeDefMap, recursionGuard);
					break;
				case "element":
					parent.getChildren().add(parseElementFromIterator(iter, se, typeDefMap, recursionGuard));
					break;
				case "attribute":
					processAttribute(se, parent);
					break;
				case "simpleType":
					processSimpleType(iter, se, parent);
					break;
				}
			} else if (event.isEndElement() && isTag(event.asEndElement(), "complexType")) {
				break;
			}
		}
		return parent;
	}

	/**
	 * Parses a <complexType> node using an event reader.
	 *
	 * @param reader         XML event reader within the complexType.
	 * @param typeDefMap     Map of type definitions.
	 * @param recursionGuard Set for detecting cycles.
	 * @return XsdNode representing the complex type.
	 * @throws XMLStreamException if parsing fails.
	 */
	private XSDNode parseComplexType(XMLEventReader reader, Map<String, List<XMLEvent>> typeDefMap,
			Set<String> recursionGuard) throws XMLStreamException {
		XSDNode parent = XSDNode.builder().selectable(false).build();
		while (reader.hasNext()) {
			XMLEvent event = reader.peek();
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				String local = se.getName().getLocalPart();
				switch (local) {
				case "sequence":
				case "choice":
				case "all":
					reader.nextEvent();
					parseGroup(reader, parent, typeDefMap, recursionGuard);
					break;
				case "element":
					reader.nextEvent();
					parent.getChildren().add(parseElement(reader, se, typeDefMap, recursionGuard));
					break;
				case "attribute":
					reader.nextEvent();
					processAttribute(se, parent);
					break;
				case "simpleType":
					reader.nextEvent();
					processSimpleType(reader, se, parent);
					break;
				}
			} else if (event.isEndElement() && isTag(event.asEndElement(), "complexType")) {
				reader.nextEvent();
				break;
			} else {
				reader.nextEvent();
			}
		}
		return parent;
	}

	/**
	 * Parses a group construct (sequence/choice/all) using XMLEventReader.
	 *
	 * @param reader         XML event reader.
	 * @param parent         Parent XsdNode to attach children to.
	 * @param typeDefMap     Map of type definitions.
	 * @param recursionGuard Set for detecting cycles.
	 * @throws XMLStreamException if parsing fails.
	 */
	private void parseGroup(XMLEventReader reader, XSDNode parent, Map<String, List<XMLEvent>> typeDefMap,
			Set<String> recursionGuard) throws XMLStreamException {
		while (reader.hasNext()) {
			XMLEvent event = reader.peek();
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				if (isTag(se, "element")) {
					reader.nextEvent();
					parent.getChildren().add(parseElement(reader, se, typeDefMap, recursionGuard));
				} else if (isTag(se, "choice") || isTag(se, "sequence") || isTag(se, "all")) {
					reader.nextEvent();
					parseGroup(reader, parent, typeDefMap, recursionGuard);
				} else {
					reader.nextEvent();
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();
				String name = ee.getName().getLocalPart();
				if ("choice".equals(name) || "sequence".equals(name) || "all".equals(name)) {
					reader.nextEvent();
					break;
				}
				reader.nextEvent();
			} else {
				reader.nextEvent();
			}
		}
	}

	/**
	 * Parses a group construct (sequence/choice/all) using an iterator.
	 *
	 * @param iter           Iterator of XMLEvents.
	 * @param parent         Parent XsdNode to attach children to.
	 * @param typeDefMap     Map of type definitions.
	 * @param recursionGuard Set for detecting cycles.
	 * @throws XMLStreamException if parsing fails.
	 */
	private void parseGroup(Iterator<XMLEvent> iter, XSDNode parent, Map<String, List<XMLEvent>> typeDefMap,
			Set<String> recursionGuard) throws XMLStreamException {
		while (iter.hasNext()) {
			XMLEvent event = iter.next();
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				if (isTag(se, "element")) {
					parent.getChildren().add(parseElementFromIterator(iter, se, typeDefMap, recursionGuard));
				} else if (isTag(se, "choice") || isTag(se, "sequence") || isTag(se, "all")) {
					parseGroup(iter, parent, typeDefMap, recursionGuard);
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();
				String name = ee.getName().getLocalPart();
				if ("choice".equals(name) || "sequence".equals(name) || "all".equals(name))
					break;
			}
		}
	}

	/**
	 * Parses an <element> node from an iterator, for in-memory type expansion.
	 *
	 * @param iter           Iterator of XMLEvents.
	 * @param start          The element's start tag.
	 * @param typeDefMap     Map of type definitions.
	 * @param recursionGuard Set for detecting cycles.
	 * @return Parsed XsdNode.
	 * @throws XMLStreamException if parsing fails.
	 */
	private XSDNode parseElementFromIterator(Iterator<XMLEvent> iter, StartElement start,
			Map<String, List<XMLEvent>> typeDefMap, Set<String> recursionGuard) throws XMLStreamException {
		String name = getAttr(start, "name");
		String type = getAttr(start, "type");
		String defaultValue = getAttr(start, "default");
		XSDNode node = XSDNode.builder().name(name).type(type).selectable(true).build();

		for (Iterator<?> attrs = start.getAttributes(); attrs.hasNext();) {
			Attribute attr = (Attribute) attrs.next();
			String k = attr.getName().getLocalPart();
			if (!"name".equals(k) && !"type".equals(k))
				node.getAttrs().put(k, attr.getValue());
		}
		if (defaultValue != null)
			node.getAttrs().put("default", defaultValue);

		String guardKey = name + ":" + type;
		if (type != null && !recursionGuard.add(guardKey)) {
			log.warn("Cycle detected for type '{}'. Skipping expansion.", type);
			return node;
		}

		String typeKey = stripPrefix(type);
		if (type != null && typeDefMap.containsKey(typeKey)) {
			List<XMLEvent> events = typeDefMap.get(typeKey);
			XSDNode typed = parseComplexType(events.iterator(), typeDefMap, recursionGuard);
			node.setChildren(typed.getChildren());
			node.getAttrs().putAll(typed.getAttrs());
			recursionGuard.remove(guardKey);
			return node;
		}

		int depth = 0;
		while (iter.hasNext()) {
			XMLEvent event = iter.next();
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				if (isTag(se, "complexType")) {
					XSDNode complexTypeNode = parseComplexType(iter, typeDefMap, recursionGuard);
					node.setChildren(complexTypeNode.getChildren());
					node.getAttrs().putAll(complexTypeNode.getAttrs());
				} else if (isTag(se, "simpleType")) {
					processSimpleType(iter, se, node);
				} else if (isTag(se, "attribute")) {
					processAttribute(se, node);
				} else if (isTag(se, "choice") || isTag(se, "sequence") || isTag(se, "all")) {
					parseGroup(iter, node, typeDefMap, recursionGuard);
				}
			} else if (event.isEndElement()) {
				EndElement ee = event.asEndElement();
				if (isTag(ee, "element")) {
					if (depth == 0)
						break;
					depth--;
				}
			}
		}
		recursionGuard.remove(guardKey);
		return node;
	}

	/**
	 * Parses a <simpleType> definition and adds restrictions to the node.
	 *
	 * @param reader XML event reader.
	 * @param start  StartElement for the simpleType.
	 * @param node   XsdNode to populate with restrictions.
	 * @throws XMLStreamException if parsing fails.
	 */
	private void processSimpleType(XMLEventReader reader, StartElement start, XSDNode node) throws XMLStreamException {
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				if ("restriction".equals(se.getName().getLocalPart())) {
					String base = getAttr(se, "base");
					if (base != null)
						node.getAttrs().put("base", base);
					List<String> enums = new ArrayList<>();
					while (reader.hasNext()) {
						XMLEvent e = reader.nextEvent();
						if (e.isStartElement() && "enumeration".equals(e.asStartElement().getName().getLocalPart())) {
							String val = getAttr(e.asStartElement(), "value");
							if (val != null)
								enums.add(val);
						} else if (e.isEndElement()
								&& "restriction".equals(e.asEndElement().getName().getLocalPart())) {
							break;
						}
					}
					if (!enums.isEmpty())
						node.getAttrs().put("enum", String.join(",", enums));
				}
			} else if (event.isEndElement() && "simpleType".equals(event.asEndElement().getName().getLocalPart())) {
				break;
			}
		}
	}

	/**
	 * Parses a <simpleType> definition from an iterator and adds restrictions to
	 * the node.
	 *
	 * @param iter  Iterator of XMLEvents.
	 * @param start StartElement for the simpleType.
	 * @param node  XsdNode to populate with restrictions.
	 * @throws XMLStreamException if parsing fails.
	 */
	private void processSimpleType(Iterator<XMLEvent> iter, StartElement start, XSDNode node)
			throws XMLStreamException {
		while (iter.hasNext()) {
			XMLEvent event = iter.next();
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				if ("restriction".equals(se.getName().getLocalPart())) {
					String base = getAttr(se, "base");
					if (base != null)
						node.getAttrs().put("base", base);
					List<String> enums = new ArrayList<>();
					while (iter.hasNext()) {
						XMLEvent e = iter.next();
						if (e.isStartElement() && "enumeration".equals(e.asStartElement().getName().getLocalPart())) {
							String val = getAttr(e.asStartElement(), "value");
							if (val != null)
								enums.add(val);
						} else if (e.isEndElement()
								&& "restriction".equals(e.asEndElement().getName().getLocalPart())) {
							break;
						}
					}
					if (!enums.isEmpty())
						node.getAttrs().put("enum", String.join(",", enums));
				}
			} else if (event.isEndElement() && "simpleType".equals(event.asEndElement().getName().getLocalPart())) {
				break;
			}
		}
	}

	/**
	 * Processes a single <attribute> node and adds it as an attribute to the parent
	 * node.
	 *
	 * @param se   StartElement for the attribute.
	 * @param node Parent XsdNode.
	 */
	private void processAttribute(StartElement se, XSDNode node) {
		String attrName = getAttr(se, "name");
		String type = getAttr(se, "type");
		String def = getAttr(se, "default");
		if (attrName != null) {
			node.getAttrs().put(attrName, type != null ? type : "string");
			if (def != null)
				node.getAttrs().put(attrName + "_default", def);
		}
	}

	/**
	 * Gets the value of a specified attribute from a StartElement.
	 *
	 * @param el  StartElement to inspect.
	 * @param key Name of the attribute.
	 * @return Attribute value, or null if not present.
	 */
	private String getAttr(StartElement el, String key) {
		Attribute attr = el.getAttributeByName(new QName(key));
		return attr != null ? attr.getValue() : null;
	}

	/**
	 * Removes the namespace prefix from a QName string.
	 *
	 * @param type The QName string.
	 * @return The type name without namespace prefix.
	 */
	private String stripPrefix(String type) {
		if (type == null)
			return null;
		int idx = type.indexOf(':');
		return (idx != -1) ? type.substring(idx + 1) : type;
	}

	/**
	 * Checks if a StartElement matches the given tag name (ignores namespace).
	 *
	 * @param el      StartElement to check.
	 * @param tagName Tag name to match.
	 * @return True if local name matches.
	 */
	private boolean isTag(StartElement el, String tagName) {
		return tagName.equals(el.getName().getLocalPart());
	}

	/**
	 * Checks if an EndElement matches the given tag name (ignores namespace).
	 *
	 * @param el      EndElement to check.
	 * @param tagName Tag name to match.
	 * @return True if local name matches.
	 */
	private boolean isTag(EndElement el, String tagName) {
		return tagName.equals(el.getName().getLocalPart());
	}
}
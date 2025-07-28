package com.adp.esi.digitech.ds.config.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a node in a file (XSD/XML) element tree for generic schema
 * exploration and rendering. Each node may have a name, type, attributes, child
 * nodes, uuid, and documentation. Suitable for UI tree rendering and further
 * processing.
 * 
 * @author rhidau
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XSDNode {
	/** Name of the XML element or type */
	private String name;

	/** Type of the XML element (from XSD, if present) */
	private String type;

	/**
	 * Attributes of the element (e.g., minOccurs, maxOccurs, custom attrs, etc.)
	 */
	@Builder.Default
	private Map<String, String> attrs = new HashMap<>();

	/** Child nodes of the element or type definition */
	@Builder.Default
	private List<XSDNode> children = new ArrayList<>();

	/** Documentation or annotation extracted from XSD, if any */
	private String documentation;

	/** Whether this node is selectable in a UI (e.g., for checkbox trees) */
	@Builder.Default
	private boolean selectable = true;

	/** Unique identifier for this node (for UI and backend reference) */
	@Builder.Default
	private String uuid = UUID.randomUUID().toString();
}
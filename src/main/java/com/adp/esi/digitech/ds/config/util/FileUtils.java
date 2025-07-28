package com.adp.esi.digitech.ds.config.util;

import org.springframework.stereotype.Service;

@Service
public class FileUtils {

	public String getFileName(String fileName) {

		if (!ValidationUtil.isHavingValue(fileName)) {
			throw new IllegalArgumentException("fileName can't be null");
		}

		return fileName.substring(0, fileName.lastIndexOf("."));
	}

	public String getFileExtension(String fileName) {

		if (!ValidationUtil.isHavingValue(fileName)) {
			throw new IllegalArgumentException("fileName can't be null");
		}

		return fileName.substring(fileName.lastIndexOf(".") + 1);
	}

	public String getContentType(String fileName) {
		String type = fileName.substring(fileName.lastIndexOf(".") + 1);

		String mimeType = "";

		switch (type.toLowerCase()) {
		case "pdf":
			mimeType = "application/pdf";
			break;
		case "doc":
			mimeType = "application/msword";
			break;
		case "docx":
			mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
			break;
		case "xlsx":
			mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			break;
		case "tif":
			mimeType = "image/tiff";
			break;
		case "tiff":
			mimeType = "image/tiff";
			break;
		case "jpg":
			mimeType = "image/jpeg";
			break;
		case "jpeg":
			mimeType = "image/jpeg";
			break;
		case "htm":
			mimeType = "text/html";
			break;
		case "html":
			mimeType = "text/html";
			break;
		case "gif":
			mimeType = "image/gif";
			break;
		case "png":
			mimeType = "image/png";
			break;
		case "txt":
			mimeType = "text/plain";
			break;
		case "json":
			mimeType = "application/json";
			break;
		case "sig":
			mimeType = "application/pgp-signature";
			break;
		default:
			mimeType = "application/octet-stream";
			break;
		}

		return mimeType;
	}
}

package com.adp.esi.digitech.ds.config.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtils {

	@Autowired
	StandardPBEStringEncryptor standardPBEStringEncryptor;

	public String encrypt(final String message) {
		if (message == null) {
			return null;
		}

		return standardPBEStringEncryptor.encrypt(message);
	}

	public String decrypt(final String encryptedMessage) {

		if (encryptedMessage == null) {
			return null;
		}
		return standardPBEStringEncryptor.decrypt(encryptedMessage);
	}

}

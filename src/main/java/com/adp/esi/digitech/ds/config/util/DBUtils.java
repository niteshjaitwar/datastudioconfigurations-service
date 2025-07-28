package com.adp.esi.digitech.ds.config.util;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Objects;

public class DBUtils {
	
	public static String convertClobToString(Clob clob) {
		if(Objects.isNull(clob)) return null;
		
		try(Reader reader = clob.getCharacterStream()) {
			StringBuilder sb = new StringBuilder();
			char[] buffer = new char[2048];
			int bytes;
			while ((bytes = reader.read(buffer)) != -1) {
				sb.append(buffer, 0, bytes);
			}
			
			return sb.toString();
		} catch (SQLException | IOException e) {
			return null;
		}
	}

}

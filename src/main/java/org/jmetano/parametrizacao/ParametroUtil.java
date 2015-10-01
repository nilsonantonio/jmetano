package org.jmetano.parametrizacao;

import java.util.ResourceBundle;

public class ParametroUtil {

	private static final String FILE_NAME = "parametros";

	public static boolean getValueAsBoolean(String chave) {
		ResourceBundle resourceBundle = ResourceBundle.getBundle(FILE_NAME);
		String valor = resourceBundle.getString(chave).toLowerCase().trim();
		if (!valor.equals("true") && !valor.equals("false")) {
			throw new IllegalArgumentException("The key \"" + chave
					+ "\" is not boolean.");
		}
		return valor.equals("true");
	}

	public static String getValueAsString(String chave) {
		ResourceBundle resourceBundle = ResourceBundle.getBundle(FILE_NAME);
		return resourceBundle.getString(chave).toLowerCase().trim();
	}

	public static int getValueAsInteger(String chave) {
		ResourceBundle resourceBundle = ResourceBundle.getBundle(FILE_NAME);
		String valor = resourceBundle.getString(chave).toLowerCase().trim();
		try {
			return Integer.parseInt(valor);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("The key \"" + chave
					+ "\" is not integer.");
		}
	}
}

package org.jmetano.dadosdeteste;

import java.util.ArrayList;
import java.util.List;

public class DDTUtils {

	private static List<String> dados = new ArrayList<String>();

	public static void setDados(Object[] dados) {
		DDTUtils.dados.clear();
		for (Object dado : dados) {
			DDTUtils.dados.add(dado.toString());
		}
	}

	public static List<String> getDados() {
		return DDTUtils.dados;
	}
}

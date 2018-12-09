/*
package precedence;


import llkAnalyzer.Grammar;
import service.Types;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Data implements Serializable {
	
	private final Grammar grammar;
	private final int[] f, g;
	private final HashMap<String, Integer> mapping;
	
	private final HashMap<Types, String> reverse = new HashMap<>();
	
	public Data(Grammar _grammar, int[] _f, int[] _g, HashMap<String, Integer> map) {
		grammar = _grammar; f = _f; g = _g; mapping = map;
		
		for(Map.Entry<String, Types> e : grammar.terminals.entrySet()) reverse.put(e.getValue(), e.getKey());
	}
	
	public int f(String s) {
		return f[mapping.get(s)];
	}
	
	public int g(String s) {
		return g[mapping.get(s)];
	}
	
	public String convertLexeme(Types type) {
		return reverse.get(type);
	}
	
	public Grammar getGrammar() {
		return grammar;
	}
	
}
*/

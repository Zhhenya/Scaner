package llkAnalyzer;


import org.apache.commons.lang3.SerializationUtils;
import scanner.Scanner;

import java.io.File;
import java.io.FileInputStream;
import java.util.Stack;



public class LLkAnalyzer  {
	
	private static final boolean DEBUG = false;
	
	private final Table controlTable;
	private final Scanner scanner;
	public StringBuilder lexeme;


	public LLkAnalyzer(Scanner scanner, StringBuilder lexeme){
		this.scanner = scanner;
		this.lexeme = lexeme;
	}
	
	public LLkAnalyzer(File table, Scanner source) throws Exception {
		controlTable = SerializationUtils.deserialize(new FileInputStream(table));
		scanner = source;
	}
	

	public void program() {
		Stack<Table.Element> stack = new Stack<>();
		stack.add(controlTable.getEndTerminal());
		stack.add(controlTable.getAxiom());
		
		scanner.scanner(lexeme);
		for(;;) {
		//	if(DEBUG) System.out.format("%-17s %-2d %s\n", lexeme.type.name(), lexeme.line + 1, stack);
			Table.Element current = stack.pop();
			
			if(current instanceof Table.Terminal) {
				Table.Terminal terminal = (Table.Terminal)current;
				
				if(lexeme.type != terminal.type) {
					String line1 = "expected " + terminal.type, line2 = "but found " + lexeme.type;
					throw new AnalyzeError(scanner, lexeme, line1, line2);
				}
				if(lexeme.type == Type.End) break;
				lexeme = scanner.next();
			} else {
				NonTerminal nonTerminal = (NonTerminal)current;
				
				Cell cell = controlTable.get(nonTerminal, lexeme.type);
				if(cell.isEmpty()) {
					String line1 = "wrong character " + lexeme.type, line2 = "when analyzing " + nonTerminal;
					throw new AnalyzeError(scanner, lexeme, line1, line2);
				}
				stack.addAll(cell.get(0));
			}
		}
	}

}

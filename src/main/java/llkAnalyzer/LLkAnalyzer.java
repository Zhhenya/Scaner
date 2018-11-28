package llkAnalyzer;


import org.apache.commons.lang3.SerializationUtils;
import scanner.Scanner;
import service.DiagramsException;
import service.Types;

import java.io.File;
import java.io.FileInputStream;
import java.util.Stack;



public class LLkAnalyzer  {
	
	private static final boolean DEBUG = false;
	
	private final Table controlTable;
	private final Scanner scanner;
	
	public LLkAnalyzer(File table, Scanner source) throws Exception {
		controlTable = SerializationUtils.deserialize(new FileInputStream(table));
		scanner = source;
	}
	

	public void program() {
		Stack<Table.Element> stack = new Stack<>();
		stack.add(controlTable.getEndTerminal());
		stack.add(controlTable.getAxiom());
		
		scanner.scanner();
		for(;;) {
		//	if(DEBUG) System.out.format("%-17s %-2d %s\n", scanner.getLexeme()..type.name(), scanner.getLexeme()..line + 1, stack);
			Table.Element current = stack.pop();
			
			if(current instanceof Table.Terminal) {
				Table.Terminal terminal = (Table.Terminal)current;
				
				if(scanner.getLexeme().type != terminal.type) {
					String line1 = "ожидался " + terminal.type, line2 = "но найден " + scanner.getLexeme().type;
						//throw new DiagramsException("Ошибка LLK", scanner.getLexeme().lexeme, scanner);
					throw new AnalyzeError(scanner, scanner.getLexeme(), line1, line2);

				}
				if(scanner.getLexeme().type == Types.TypeEnd) break;
				scanner.scanner();
			} else {
				Table.NonTerminal nonTerminal = (Table.NonTerminal)current;
				
				Table.Cell cell = controlTable.get(nonTerminal, scanner.getLexeme().type);
				if(cell.isEmpty()) {
					String line1 = "ошибочная комбинация " + scanner.getLexeme().type, line2 = "при анализе " + nonTerminal;
					throw new AnalyzeError(scanner, scanner.getLexeme(), line1, line2);
				//	throw new DiagramsException("Ошибка ", scanner.getLexeme().lexeme, scanner);
				}
				stack.addAll(cell.get(0));
			}
		}
	}

}

package llkAnalyzer;


import scanner.Scanner;
import service.Types;

import java.util.function.Supplier;

public class AnalyzeError extends RuntimeException {
	
	private final String message;
	private final String additional;
	
	public AnalyzeError(Scanner scanner, Scanner.Lexeme lexeme, Types... expected) {
		this(scanner, lexeme, ((Supplier<String>)(() -> {
			StringBuilder builder = new StringBuilder();
			builder.append("Найдено ");
			if(lexeme.type == Types.TypeEnd) {
				builder.append("конец файла");
			} else {
				if(lexeme.type == Types.TypeError) builder.append("invalid ");
				builder.append("символ \"");
				builder.append(lexeme.lexeme);
				builder.append("\"");
			}
			return builder.toString();
		})).get(), "ожидался " + asList(expected));
	}
	
	public AnalyzeError(Scanner scanner, Scanner.Lexeme lexeme, String first, String... lines) {
		StringBuilder builder = new StringBuilder();
		builder.append("парсер грамматики");
		builder.append("(строка ");
		builder.append(scanner.getNumberOfRow());
		builder.append("): ");
		int length = builder.length();
		
		builder.append(first);
		message = builder.toString();
		builder = new StringBuilder();
		builder.append("\n");
		
		for(String s : lines) {
			for(int i = 0; i < length; i++) builder.append(" ");
			builder.append(s);
			builder.append("\n");
		}
		builder.append("\n");
		
//		if(lexeme.type != Types.TypeEnd) {
//			builder.append(scanner.getCurrentIndexPosition());
//			for(int i = 0; i < lexeme.lexeme.length(); i++) builder.append(" ");
//			builder.append("^");
//		}

		additional = builder.toString();
	}
	
	@Override
	public String getMessage() {
		return message;
	}
	
	public String getDisplayMessage() {
		return message + additional;
	}

	private static String asList(Types[] types) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < types.length - 1; i++) {
			if(builder.length() == 0) {
				builder.append(types[i]);
			} else {
				builder.append(", ");
				builder.append(types[i]);
			}
		}
		if(types.length > 1) builder.append(" или ");
		builder.append(types[types.length - 1]);
		
		return builder.toString();
	}

}
package service;

import scanner.Scanner;

public class DiagramsException extends RuntimeException {
    private String MESSAGE = "строка %s:";

    public DiagramsException(String message, Scanner scanner) {
        scanner.printError(String.format(MESSAGE, scanner.getCurrentLine()) + message, scanner.getLexeme().lexeme,
                scanner.getCurrentItem());
    }

    public DiagramsException(String message) {
        System.out.println(message);
    }
}
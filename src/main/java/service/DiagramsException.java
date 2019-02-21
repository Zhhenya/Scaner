package service;

import scanner.Scanner;

public class DiagramsException extends RuntimeException {
    private String MESSAGE = "строка %s:";

    public DiagramsException(String message, StringBuilder lexeme, Scanner scanner) {
        scanner.printError(String.format(MESSAGE, scanner.getNumberOfRow()) + message, lexeme, scanner.getCurrentItem());
    }

    public DiagramsException(String message) {
        System.out.println(message);
    }
}
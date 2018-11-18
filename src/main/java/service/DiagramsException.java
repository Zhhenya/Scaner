package service;

import scanner.Scanner;

public class DiagramsException extends Exception {
    public DiagramsException(String message, StringBuilder lexeme, Scanner scanner){
        scanner.printError(message, lexeme, scanner.getCurrentItem());
    }
}
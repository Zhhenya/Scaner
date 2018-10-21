package service;

import scanner.Scaner;

public class SemanticsException extends Exception {
    public SemanticsException(String message, StringBuilder lexeme){
        System.out.println(message + ": " + lexeme);
    }

    public SemanticsException(String message, String lexeme){
        System.out.println(message + ": " + lexeme);
    }

    public SemanticsException(String message){
        System.out.println(message);
    }
}
package service;

import scanner.Lexeme;
import scanner.Scanner;

public class SemanticsException extends Exception {
    public SemanticsException(String message, StringBuilder lexeme){
        System.out.println(message + ": " + lexeme);
    }

    public SemanticsException(String message, Lexeme lexeme){
        System.out.println(message + ": " + lexeme.lexeme.toString());
    }
    public SemanticsException(String message, String lexeme){
        System.out.println(message + ": " + lexeme);
    }


    public SemanticsException(String message){
        System.out.println(message);
    }
}
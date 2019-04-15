package scanner;

import service.Types;

public class Lexeme {
    public StringBuilder lexeme = new StringBuilder();
    public Types type;
    public int line = 0, ptr = 0;

    public Lexeme(StringBuilder lexeme, Types type) {
        this.lexeme.append(lexeme); this.type = type;
    }

    public void delete() {
        this.lexeme.delete(0, lexeme.length());
    }

    public void delete(int start, int end) {
        this.lexeme.delete(start, end);
    }

    public void append(String symbol) {
        lexeme.append(symbol);
    }

    public void append(Character symbol) {
        lexeme.append(symbol);
    }

    public int length() {
        return lexeme.length();
    }

    public void setType(Types type) {
        this.type = type;
    }

    public Types setGetType(Types type) {
        this.type = type; return type;
    }
    public String getName(){
        return lexeme.toString();
    }

    public Lexeme() {
    }
}
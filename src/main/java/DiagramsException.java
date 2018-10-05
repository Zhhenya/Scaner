public class DiagramsException extends Exception {
    public DiagramsException(String message, StringBuilder lexeme, Scaner scaner){
        scaner.printError(message, lexeme, scaner.getCurrentItem());
    }
}
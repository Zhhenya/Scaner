package interpreter;

import diagrams.Diagrams;
import org.apache.log4j.BasicConfigurator;
import scanner.Lexeme;
import scanner.Scanner;
import service.DataType;
import service.DiagramsException;
import service.SemanticsException;
import tree.Node;
import tree.Tree;

import java.io.IOException;

public class Interpreter {
    private boolean interpreting = false, analyzing = true;
    private Diagrams diagrams;
    private Scanner scanner;


    public static void main(String[] args) {
        (new Interpreter()).start();
    }

    private Interpreter(){
        String filePath = "src/main/resources/program1.java";
        scanner = new Scanner();
        Lexeme lexeme = new Lexeme();
        try {
            scanner.createLines(filePath);
            diagrams = new Diagrams(scanner, lexeme.lexeme, this);
            diagrams.setRoot();
        } catch (DiagramsException | IOException e) {
            e.printStackTrace();
        }
    }

    private void start(){
        BasicConfigurator.configure();
        program();
        findAndRunMainClass();

    }

    private void program() {
        try {
            diagrams.S();
            diagrams.printTree();
        } catch (SemanticsException e) {
            e.printStackTrace();
        }
    }

    private void findAndRunMainClass(){

        /*
        * Реализовать поиск класса по имени (
        * Main  не обязательно корневой)
        * */

        Node node = diagrams.getRoot().findUpName("Main").getNode();
        if(node.type != DataType.TClass)
            throw new DiagramsException("Тип не является классом");
        if(node.lexemeName.equals("Main"))
            throw new DiagramsException("Класс Main не найден");
        findAndRunMainMethod(node);
    }

    private void findAndRunMainMethod(Node node){
        /*
         * Реализовать оиск класса по имени (
         * Main  не обязательно корневой)
         * */
        if(!node.lexemeName.equals("min"))
            throw new DiagramsException("Метод main н найден");
    }

    private void printGlobalVariables() {
        System.out.println("Global variables values:");
        Tree tree = diagrams.getRoot();
        while(tree != null) {
            if(tree.node != null && tree.right == null) {
                System.out.print("* " + tree.node.lexemeName + " = ");
                if(tree.node.dataValue.value == null)
                    System.out.println("null");
                else
                    System.out.println(tree.node.dataValue + " (" + tree.node.dataValue.getClass().getSimpleName() + ")");
            }
            tree = tree.left;
        }
    }

    public boolean isInterpreting() {
        return interpreting;
    }

    public void setInterpreting(boolean interpreting) {
        this.interpreting = interpreting;
    }

    public boolean isAnalyzing() {
        return analyzing;
    }

    public void setAnalyzing(boolean analyzing) {
        this.analyzing = analyzing;
    }
}
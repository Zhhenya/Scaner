package interpreter;

import diagrams.Diagrams;
import org.apache.log4j.BasicConfigurator;
import scanner.Lexeme;
import scanner.Scanner;
import service.DataType;
import service.DiagramsException;
import service.SemanticsException;
import tree.DataValue;
import tree.Node;
import tree.Tree;

import java.io.IOException;
import java.util.Stack;

public class Interpreter {
    private boolean interpreting = false, analyzing = true, callFunction = false;
    private Diagrams diagrams;
    private Scanner scanner;
    private Stack<DataValue> middleValues = new Stack<>();
    private Stack<Tree> functionCallInterpreter = new Stack<>();

    public void setFunctionCallInterpreterClear(){
        functionCallInterpreter = new Stack<>();
    }

    public static void main(String[] args) {
        (new Interpreter()).start();
    }

    private Interpreter() {
        String filePath = "src/main/resources/program2.java";
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

    private void start() {
        BasicConfigurator.configure();
        program();
        findAndRunMainClass();

        interpreting = true;
        analyzing = false;
        scanner.setCurrentLine(0);
        scanner.setPtr(0);
        scanner.initLine(0);
        diagrams.clear();
        program();

   //     printGlobalVariables();

    }

    private void program() {
        try {
            diagrams.S();
            diagrams.getRoot().printValueTree();
        } catch (SemanticsException e) {
            e.printStackTrace();
        }
    }

    private void findAndRunMainClass() {

        /*
         * Реализовать поиск класса по имени (
         * Main  не обязательно корневой)
         * */

        Node node = null;
        try {
            node = diagrams.getRoot().getClass("TestClass").getNode();
        } catch (SemanticsException e) {
            e.printStackTrace();
        }
        if (node.type != DataType.TClass) {
            throw new DiagramsException("Тип не является классом");
        }
        if (!node.lexemeName.equals("TestClass")) {
            throw new DiagramsException("Класс TestClass не найден");
        }
     //   findAndRunMainMethod();
    }

    private void findAndRunMainMethod() {
        /*
         * Реализовать оиск класса по имени (
         * Main  не обязательно корневой)
         * */
        Node mainNode = null;
        Tree from = diagrams.getRoot().left;
        mainNode = diagrams.getRoot().findRightLeft(from, "main").getNode();
        if (!mainNode.lexemeName.equals("main")) {
            throw new DiagramsException("Метод main н найден");
        }
    }



    private void printGlobalVariables() {
        System.out.println("Global variables values:");
        Tree tree = diagrams.getRoot();
        while (tree != null) {
            if (tree.node != null && tree.right == null) {
                System.out.print(tree.node.lexemeName + " = ");
                if (tree.node.dataValue.value == null) {
                    System.out.println("null");
                } else {
                    switch(tree.node.type){
                        case TBoolean:
                            System.out.println(tree.node.dataValue.value.constant);
                        case TInt:
                            System.out.println(tree.node.dataValue.value.valueInt);
                        case TUserType:
                            System.out.println(tree.node.dataValue.value.clazz.node.lexemeName);
                    }
                }
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

    public boolean isCallFunction() {
        return callFunction;
    }

    public void setCallFunction(boolean callFunction) {
        this.callFunction = callFunction;
    }

    public Stack<Tree> getFunctionCallInterpreter() {
        return functionCallInterpreter;
    }

    public void setFunctionCallInterpreter(Stack<Tree> functionCallInterpreter) {
        this.functionCallInterpreter = functionCallInterpreter;
    }
    public Tree peek(){
        return functionCallInterpreter.peek();
    }
    public void put(Tree tree){
        functionCallInterpreter.push(tree);
    }
}
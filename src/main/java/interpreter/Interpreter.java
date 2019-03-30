package interpreter;

import diagrams.Diagrams;
import org.apache.log4j.BasicConfigurator;
import scanner.Lexeme;
import scanner.Scanner;
import service.DataType;
import service.DiagramsException;
import service.Pos;
import service.SemanticsException;
import tree.DataValue;
import tree.Node;
import tree.Tree;

import java.io.IOException;
import java.util.Stack;

public class Interpreter {
    private boolean interpreting = false, analyzing = true, callFunction = false;
    private boolean interpretingIf = false, execute = false;
    private Diagrams diagrams;
    private Scanner scanner;
    private Stack<DataValue> middleValues = new Stack<>();
    private Stack<Tree> functionCallInterpreter = new Stack<>();
    private Stack<Pos> functionCallPos = new Stack<>();

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
        interpreting = true;
        analyzing = false;
        scanner.setCurrentLine(0);
        scanner.setPtr(0);
        scanner.initLine(0);
        diagrams.clear();
        findAndRunMainClass();

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
        findAndRunMainMethod();
    }

    private void findAndRunMainMethod() {
        /*
         * Реализовать поиск класса по имени (
         * Main  не обязательно корневой)
         * */
        Tree from = diagrams.getRoot().left;
        Tree main = diagrams.getRoot().findByName(from, "main");
        if (main == null) {
            throw new DiagramsException("Метод main не найден");
        }

        scanner.setCurrentLine(main.node.line);
        scanner.setPtr(main.node.ptr);
        try {
            callFunction = true;
            functionCallInterpreter.push(main.clone(main));
            functionCallInterpreter.peek().node.dataValue.value.valueInt = 0;
            diagrams.Method();
        } catch (SemanticsException e) {
            e.printStackTrace();
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

    public boolean isInterpretingIf() {
        return interpretingIf;
    }

    public void setInterpretingIf(boolean interpretingIf) {
        this.interpretingIf = interpretingIf;
    }

    public boolean isExecute() {
        return execute;
    }

    public void setExecute(boolean execute) {
        this.execute = execute;
    }

    public Stack<Pos> getFunctionCallPos() {
        return functionCallPos;
    }

    public void setFunctionCallPos(Stack<Pos> functionCallPos) {
        this.functionCallPos = functionCallPos;
    }
    public void pushFunctionCallPos(Lexeme lexeme){
        Pos pos = new Pos();
        pos.callMethodPointAddr.type = lexeme.type;
        pos.callMethodPointAddr.lexeme.append(lexeme.lexeme);
        pos.callMethodPointAddr.ptr = lexeme.ptr;
        pos.callMethodPointAddr.line = lexeme.line;
        functionCallPos.push(pos);
    }
}
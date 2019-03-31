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
    private Stack<DataValue> returnValues = new Stack<>();
    private Stack<Tree> functionCallInterpreter = new Stack<>();
    private Stack<Pos> functionCallPos = new Stack<>();

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
    }

    private void program() {
        try {
            diagrams.S();
            diagrams.printTree();
        } catch (SemanticsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Найти класс TestClass
     */
    private void findAndRunMainClass() {
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

    /**
     * Найти метод main в корневом классе
     */
    private void findAndRunMainMethod() {
        Tree from = diagrams.getRoot().left;
        Tree main = diagrams.getRoot().findByNameLeft(from, "main");
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

    public Tree peek() {
        return functionCallInterpreter.peek();
    }

    public void put(Tree tree) {
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

    public void pushFunctionCallPos(Lexeme lexeme) {
        Pos pos = new Pos();
        pos.callMethodPointAddr.type = lexeme.type;
        pos.callMethodPointAddr.lexeme.append(lexeme.lexeme);
        pos.callMethodPointAddr.line = lexeme.line;
        pos.callMethodPointAddr.ptr = lexeme.ptr;
        functionCallPos.push(pos);
    }

    public Stack<DataValue> getReturnValues() {
        return returnValues;
    }

    public void pushReturnValue(DataValue dataValue) {
        returnValues.push(dataValue);
    }

    public DataValue peekReturnValue() {
        return returnValues.peek();
    }

    public DataValue popReturnValue() {
        return returnValues.pop();
    }

}
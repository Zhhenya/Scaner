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
    private Stack<Tree> prevFunctionCallInterpreter = new Stack<>();
    private Stack<Tree> functionCallInterpreter = new Stack<>();
    private Stack<Tree> classCallInterpreter = new Stack<>();
    private Stack<Pos> functionCallPos = new Stack<>();
    private Stack<Pos> varCallPos = new Stack<>();
    private Stack<Tree> varCallInterpreter = new Stack<>();
    private Stack<Boolean> nesting = new Stack<>();
    private int countOfReturn = 0;

    public static void main(String[] args) {
        (new Interpreter()).start();
    }

    private Interpreter() {
        String filePath = "src/main/resources/program3.java";
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
        scanner.setPtr(main.node.ptrStart);
        try {
            callFunction = true;
            functionCallInterpreter.push(main.clone(main));
            functionCallInterpreter.peek().node.dataValue.value.valueInt = 0;
            diagrams.Method();
            diagrams.getRoot().printValueTree();
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

    public Tree peekFunctionCall() {
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
        if (isInterpreting()) {
            Pos pos = new Pos();
            pos.callMethodPointAddr.type = lexeme.type;
            pos.callMethodPointAddr.lexeme.append(lexeme.lexeme);
            pos.callMethodPointAddr.line = lexeme.line;
            pos.callMethodPointAddr.ptr = lexeme.ptr;
            functionCallPos.push(pos);
        }
    }

    public void pushFunctionCallPos(Pos pos) {
        if (isInterpreting()) {
            functionCallPos.push(pos);
        }
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

    public Boolean isEmptyReturnValue() {
        return returnValues.isEmpty();
    }

    public DataValue popReturnValue() {
        return returnValues.pop();
    }

    public int getCountOfReturn() {
        return countOfReturn;
    }

    public void setCountOfReturn(int countOfReturn) {
        this.countOfReturn = countOfReturn;
    }

    public void incCountOfReturn() {
        countOfReturn++;
    }

    public void decCountOfReturn() {
        countOfReturn--;
    }

    public void checkReturn() {
        if (isInterpreting() && countOfReturn != 0) {
            decCountOfReturn();
        }
    }

    public void addNesting(Boolean isNesting) {
        if (isNesting) {
            nesting.push(isNesting);
        }
    }

    public void changeNesting(Boolean isNesting) {
        if (isNesting) {
            nesting.pop();
            nesting.push(isNesting);
        }
    }

    public Boolean popNesting() {
        if (isInterpreting()) {
            return nesting.pop();
        }
        return null;
    }

    public Tree popFunctionCall(){
        return functionCallInterpreter.pop();
    }

    public Pos popVarCall() {
        return varCallPos.pop();
    }

    public void pushVarCall(Pos pos) {
        varCallPos.push(pos);
    }

    public Pos peekVarCall() {
        return varCallPos.peek();
    }

    public Boolean peekNesting() {
        if (isInterpreting()) {
            return nesting.peek();
        }
        return null;
    }

    public void changeFunctionCallInterpreter(DataValue dataValue) {
        Tree tree = functionCallInterpreter.pop();
        tree.node.dataValue = dataValue;
        functionCallInterpreter.push(tree);
    }

    public void changePrevFunctionCallInterpreter(DataValue dataValue) {
        Tree tree = prevFunctionCallInterpreter.pop();
        tree.node.dataValue = dataValue;
        prevFunctionCallInterpreter.push(tree);
    }

    public boolean isClassCallInterpreterEmpty() {
        return classCallInterpreter.isEmpty();
    }

    public Tree penultimateFunctionCall() {
        if (functionCallInterpreter.size() > 1) {
            return functionCallInterpreter.get(functionCallInterpreter.size() - 2);
        }
        return null;
    }

    public Stack<Tree> getPrevFunctionCallInterpreter() {
        return prevFunctionCallInterpreter;
    }

    public void setPrevFunctionCallInterpreter(Stack<Tree> prevFunctionCallInterpreter) {
        this.prevFunctionCallInterpreter = prevFunctionCallInterpreter;
    }

    public Tree peekPrevFunctionCall(){
        return prevFunctionCallInterpreter.peek();
    }

    public Tree popPrevFunctionCall(){
        return prevFunctionCallInterpreter.pop();
    }

    public void pushPrevFuctionCall(Tree tree){
        prevFunctionCallInterpreter.push(tree);
    }
    public Tree penultimatePrevFunctionCall(){
        if (prevFunctionCallInterpreter.size() > 1) {
            return prevFunctionCallInterpreter.get(prevFunctionCallInterpreter.size() - 2);
        }
        return null;
    }
}
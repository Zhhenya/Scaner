import diagrams.Diagrams;
import llkAnalyzer.AnalyzeError;
import llkAnalyzer.Generator;
import llkAnalyzer.Table;
import org.apache.commons.lang3.SerializationUtils;
import scanner.Lexeme;
import scanner.Scanner;
import service.DataType;
import service.Types;
import translator.Translator;
import tree.Tree;
import triad.Reference;
import triad.Reference.ConstantReference;
import triad.Reference.FunctionReference;
import triad.Reference.TriadReference;
import triad.Reference.VariableReference;
import triad.Triad;
import triad.Triad.Action;
import triad.Triad.Transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Compiler {

    private static final boolean DEBUG = false;
    private static final HashMap<String, Method> reflectionsCache = new HashMap<>();

    private final Table controlTable;
    private final Scanner scanner;
    private Lexeme prevLexeme;

    private final Tree semantic;

    private final Stack<Lexeme> identifiers = new Stack<>();
    private final Stack<DataType> dataTypes = new Stack<>();
    private final Stack<TriadReference> addresses = new Stack<>();
    private final Stack<Reference> references = new Stack<>();

    private final Stack<Object> returns = new Stack<>();
    private final Stack<Triad> functionArguments = new Stack<>();

    private final ArrayList<Triad> triads = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Generator.main(new String[]{"silent"});

        Compiler compiler = new Compiler(new File("table.llk"), new Scanner(new File("input2.cpp")));
        if (!compiler.compile(false)) {
            return;
        }

        Triad[] optimized = (new Optimizer(compiler.triads)).optimize(true);

        (new Translator(optimized, compiler.semantic)).translate(true);
    }

    public Compiler(File table, Scanner source) throws Exception {
        controlTable = SerializationUtils.deserialize(new FileInputStream(table));
        scanner = source;

        semantic = new Diagrams(scanner);
    }

    public boolean compile(boolean silent) throws Exception {
        try {
            program();
            System.out.println("Source file analyzed without errors.\n");

            if (!silent) {
                for (int i = 0; i < triads.size(); i++) {
                    System.out.println(triads.get(i).toString(i));
                }
            }
        } catch (AnalyzeError e) {
            System.out.println(e.getDisplayMessage());
            return false;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }

        PrintWriter out = new PrintWriter(new FileWriter("triads.txt"));
        for (int i = 0; i < triads.size(); i++) {
            out.println(triads.get(i).toString(i));
        }
        out.close();

        if (!silent) {
            System.out.println();
        }
        return true;
    }

    public void program() throws Throwable {
        Stack<Table.Element> stack = new Stack<>();
        stack.add(controlTable.getEndTerminal());
        stack.add(controlTable.getAxiom());

        Lexeme lexeme = scanner.scanner();
        for (; ; ) {
            if (DEBUG) {
                System.out.format("%-17s %-2d %s\n", lexeme.type.name(), lexeme.line + 1, stack);
            }
            Table.Element current = stack.pop();

            if (current instanceof Table.Delta) {
                callDeltaFunction(current.value);
            } else if (current instanceof Table.Terminal) {
                Table.Terminal terminal = (Table.Terminal) current;

                if (lexeme.type != terminal.type) {
                    String line1 = "expected " + terminal.type, line2 = "but found " + lexeme.type;
                    throw new AnalyzeError(scanner, lexeme, line1, line2);
                }
                if (lexeme.type == Types.TypeEnd) {
                    break;
                }

                if (lexeme.type == Types.TypeIdent || lexeme.lexeme.toString().equals("main")) {
                    identifiers.push(lexeme);

                    if (DEBUG) {
                        System.out.println(">>> PUSH IDENTIFIER " + lexeme.value + " <<<");
                        System.out.println(identifiers + " " + dataTypes + " " + references);
                    }
                } else if (lexeme.type == Types.TypeInt || lexeme.type == Types.TypeConstInt) {
                    SemanticAnalyzer.Value value = semantic.getConstValue(lexeme);

                    references.push(new ConstantReference(value.value));
                    dataTypes.push(value.type);

                    if (DEBUG) {
                        System.out.println(">>> PUSH CONSTANT " + value.value + " <<<");
                        System.out.println(identifiers + " " + dataTypes + " " + references);
                    }
                } else if (lexeme.type == Type.KeyReturn) {
                    returns.push(lexeme);
                }

                prevLexeme = lexeme;
                lexeme = scanner.scanner();
            } else {
                Table.NonTerminal nonTerminal = (Table.NonTerminal) current;

                Table.Cell cell = controlTable.get(nonTerminal, lexeme.type);
                if (cell.isEmpty()) {
                    String line1 = "wrong character " + lexeme.type, line2 = "when analyzing " + nonTerminal;
                    throw new AnalyzeError(scanner, lexeme, line1, line2);
                }
                stack.addAll(cell.get(0));
            }
        }
    }

    private void callDeltaFunction(String name) throws Throwable {
        if (DEBUG) {
            System.out.println(">>> CALL TO DELTA FUNCTION " + name + " <<<");
        }

        Method method = reflectionsCache.get(name);
        if (method == null) {
            method = this.getClass().getDeclaredMethod(name);
            method.setAccessible(true);

            reflectionsCache.put(name, method);
        }

        try {
            method.invoke(this);
            if (DEBUG) {
                System.out.println(identifiers + " " + dataTypes + " " + references);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private TriadReference addTriad(Triad t) {
        triads.add(t);
        if (DEBUG) {
            System.out.println("\n" + t + "\n");
        }
        return new TriadReference(triads.size() - 1);
    }

    private VariableReference getVariableReference(Lexeme lexeme) {
        return new VariableReference(semantic.getVariable(lexeme));
    }

    /* Delta functions */

    private void pushTypeI() {
        dataTypes.push(DataType.tInt);
    }

    private void pushTypeLI() {
        dataTypes.push(DataType.tLongInt);
    }

    private void pushTypeLLI() {
        dataTypes.push(DataType.tLongLongInt);
    }

    private void popType() {
        dataTypes.pop();
    }

    private void startFunction() {
        Lexeme identifier = identifiers.pop();
        addTriad(new Triad(Action.proc, new FunctionReference(identifier.value)));

        semantic.startFunction(dataTypes.peek(), identifier);
    }

    private void prolog() {
        Triad t = new Triad(Action.call, new FunctionReference("#prolog"));
        addresses.push(addTriad(t.setTransferControl(Transfer.FUNCTION)));
    }

    private void epilog() {
        Triad update = triads.get(addresses.pop().index);
        update.ref2 = new ConstantReference(semantic.getMaximumPointer());

        int args = semantic.getArgumentsSize();
        Triad t = new Triad(Action.call, new FunctionReference("#epilog"), new ConstantReference(args));
        TriadReference ref = addTriad(t.setTransferControl(Transfer.FUNCTION));

        addTriad(new Triad(Action.endp));

        for (Object o : returns) {
            triads.get(((TriadReference) o).index).ref1 = ref;
        }
        returns.clear();
    }

    private void ref() {
        Lexeme identifier = identifiers.pop();

        dataTypes.push(semantic.getVariable(identifier).type);
        references.push(getVariableReference(identifier));
    }

    private void addVariable() {
        semantic.addVariable(dataTypes.peek(), identifiers.peek());
    }

    private void popVar() {
        identifiers.pop();
    }

    private void checkAssignment() {
        SemanticAnalyzer.Node val = semantic.getVariable(identifiers.peek());

        semantic.checkAssignment(identifiers.peek(), val.type, dataTypes.peek());
        triads.add(new Triad(Action.assign, getVariableReference(identifiers.peek()), references.peek()));

        dataTypes.pop();
        references.pop();
    }

    private void cEQ() {
        cmp(Action.eq);
    }

    private void cNE() {
        cmp(Action.ne);
    }

    private void cLT() {
        cmp(Action.lt);
    }

    private void cLE() {
        cmp(Action.le);
    }

    private void cGT() {
        cmp(Action.gt);
    }

    private void cGE() {
        cmp(Action.ge);
    }

    private void cmp(Action action) {
        Reference second = references.pop(), first = references.pop();
        references.push(addTriad(new Triad(action, first, second)));

        dataTypes.pop(); dataTypes.pop();
        dataTypes.push(DataType.tInt);
    }

    private void add() {
        cast(Action.add);
    }

    private void sub() {
        cast(Action.sub);
    }

    private void mul() {
        cast(Action.mul);
    }

    private void div() {
        cast(Action.div);
    }

    private void mod() {
        cast(Action.mod);
    }

    private void cast(Action action) {
        Reference second = references.pop(), first = references.pop();
        references.push(addTriad(new Triad(action, first, second)));

        dataTypes.push(semantic.cast(dataTypes.pop(), dataTypes.pop()));
    }

    private void startBlock() {
        semantic.startBlock();
    }

    private void goToParentLevel() {
        semantic.goToParentLevel();
    }

    private void _do() {
        addresses.push(addTriad(new Triad(Action.nop).setTransferControl(Transfer.LOOP)));
    }

    private void _while() {
        Reference ref = references.pop();
        dataTypes.pop();

        if (ref instanceof VariableReference || ref instanceof ConstantReference) {
            addTriad(new Triad(Action.cmp, ref, new ConstantReference(0)));
        }

        addTriad(new Triad(Action.jg, addresses.pop()).setTransferControl(Transfer.LOOPBACK));
    }

    private void checkReturnType() {
        semantic.checkReturnType((Lexeme) returns.pop(), dataTypes.pop());

        addTriad(new Triad(Action.ret, references.pop()));
        returns.add(addTriad(new Triad(Action.jmp, null)));
    }

    private void startFunctionCall() {
        semantic.startFunctionCall(identifiers.peek());
    }

    private void checkFunctionArgument() {
        semantic.checkFunctionArgument(prevLexeme, dataTypes.pop());
        functionArguments.push(new Triad(Action.push, references.pop()));
    }

    private void finishFunctionCall() {
        while (!functionArguments.isEmpty()) {
            addTriad(functionArguments.pop());
        }

        dataTypes.push(semantic.finishFunctionCall(prevLexeme));
        references.push(addTriad(new Triad(Action.call, new FunctionReference(identifiers.pop().value))));
    }

    private void unary() {
        Reference ref = references.pop();
        if (ref instanceof ConstantReference) {
            references.push(new ConstantReference(-((ConstantReference) ref).value));
        } else {
            references.push(addTriad(new Triad(Action.neg, ref)));
        }
    }

}
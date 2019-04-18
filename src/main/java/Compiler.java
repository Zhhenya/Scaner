import llkAnalyzer.AnalyzeError;
import llkAnalyzer.Table;
import optimizer.IfOptimizer;
import optimizer.Optimizer;
import org.apache.commons.lang3.SerializationUtils;
import scanner.Lexeme;
import scanner.Scanner;
import service.DataType;
import service.Types;
import translator.Translator;
import tree.SemanticAnalyzer;
import tree.Tree;
import tree.Value;
import triad.Reference;
import triad.Reference.ConstantReference;
import triad.Reference.ClassReference;
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
import java.util.*;

import static org.apache.commons.lang3.ArrayUtils.toArray;

public class Compiler {

    private static final boolean DEBUG = false;
    private static final HashMap<String, Method> reflectionsCache = new HashMap<>();

    private final Table controlTable;
    private final Scanner scanner;
    private Lexeme prevLexeme;

    private final SemanticAnalyzer semantic;

    private final Stack<Lexeme> identifiers = new Stack<>();
    private final Stack<DataType> dataTypes = new Stack<>();
    private final Stack<TriadReference> addresses = new Stack<>();
    private final Stack<Reference> references = new Stack<>();

    private final Stack<Object> returns = new Stack<>();
    private final Stack<Triad> functionArguments = new Stack<>();

    private final List<Triad> triads = new ArrayList<>();

    public static void main(String[] args) throws Exception {
//        Generator.main(new String[]{"silent"});

        Scanner scanner = new Scanner();
        scanner.createLines("src/main/resources/program5.java");

        Compiler compiler = new Compiler(new File("table.llk"), scanner);
        if (!compiler.compile(false)) {
            return;
        }

        List<Triad> original = compiler.triads;

        PrintWriter out = new PrintWriter(new FileWriter("triads.txt"));
        for (int i = 0; i < original.size(); i++) {
            out.println(original.get(i).toString(i));
        }
        out.close();

        Triad[] optimizedExpression = (new Optimizer(compiler.triads)).optimize(true);

        List<Triad> ifOptimized = new IfOptimizer(Arrays.asList(optimizedExpression)).optimize();

        Triad[] optimized = ifOptimized.toArray(new Triad[0]);





        out = new PrintWriter(new FileWriter("optimization.txt"));
        for (int k = 0; k < original.size(); k++) {
            String x = original.get(k).toString(k);
            if (k < optimized.length) {
                out.println(x + "\t\t\t" + optimized[k].toString(k));
            } else {
                out.println(x);
            }
        }
        out.close();

      //  Triad[] triads = compiler.triads.toArray(new Triad[compiler.triads.size()]);
        (new Translator(optimized, compiler.semantic)).translate(true);
    }

    public Compiler(File table, Scanner source) throws Exception {
        controlTable = SerializationUtils.deserialize(new FileInputStream(table));
        scanner = source;

        semantic = new SemanticAnalyzer(scanner);
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
                        System.out.println(">>> PUSH IDENTIFIER " + lexeme.getName() + " <<<");
                        System.out.println(identifiers + " " + dataTypes + " " + references);
                    }
                } else if (lexeme.type == Types.TypeConstInt || lexeme.type == Types.TypeBoolean) {
                    Value value = semantic.getVariableValue(lexeme);

                    references.push(new ConstantReference(value.value));
                    dataTypes.push(value.type);

                    if (DEBUG) {
                        System.out.println(">>> PUSH CONSTANT " + value.value + " <<<");
                        System.out.println(identifiers + " " + dataTypes + " " + references);
                    }
                } else if (lexeme.type == Types.TypeReturn) {
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

    private void pushTypeInt() {
        dataTypes.push(DataType.TInt);
    }

    private void pushTypeBool() {
        dataTypes.push(DataType.TBoolean);
    }

    private void pushTypeUser() {
        dataTypes.push(DataType.TUserType);
    }

    private void popType() {
        dataTypes.pop();
    }

    private void startFunction() {
        Lexeme identifier = identifiers.pop();
        addTriad(new Triad(Action.proc, new FunctionReference(identifier.getName())));

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

    private void startClass() {
        Lexeme identifier = identifiers.pop();
      //  addTriad(new Triad(Action.clazz, new ClassReference(identifier.getName())));

        semantic.startClass(identifier);
    }

    private void popVar() {
        identifiers.pop();
    }

    private void checkAssignment() {
        Tree val = semantic.getVariable(identifiers.peek());

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
        dataTypes.push(DataType.TInt);
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

    //прописать правильно
    private void _if() {
        addresses.push(addTriad(new Triad(Action.jg).setTransferControl(Transfer.IF)));

      /*  Reference ref = references.pop();
        dataTypes.pop();

        if (ref instanceof VariableReference || ref instanceof ConstantReference) {
            addTriad(new Triad(Action.cmp, ref, new ConstantReference(0)));
        }

        addresses.push(addTriad(new Triad(Action.jg).setTransferControl(Transfer.IF)));*/

      //  addresses.push(addTriad(new Triad(Action.nop).setTransferControl(Transfer.IF)));
    }
    private void end_else(){
        addTriad(new Triad(Action.nothing).setTransferControl(Transfer.END_ELSE));
    }
    private void start_if(){
        addTriad(new Triad(Action.nothing).setTransferControl(Transfer.START_IF));
    }

    private void _else() {
        Triad update = triads.get(addresses.pop().index);

        update.ref1 = addTriad(new Triad(Action.nop).setTransferControl(Transfer.ELSE));

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
        references.push(addTriad(new Triad(Action.call, new FunctionReference(identifiers.pop().getName()))));
    }

    private void unary() {
        Reference ref = references.pop();
        if (ref instanceof ConstantReference) {
            references.push(new ConstantReference(((Integer) ((ConstantReference) ref).value) * -1));
        } else {
            references.push(addTriad(new Triad(Action.neg, ref)));
        }
    }

}

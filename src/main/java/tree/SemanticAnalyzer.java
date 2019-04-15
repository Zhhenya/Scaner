package tree;

import llkAnalyzer.AnalyzeError;
import scanner.Lexeme;
import scanner.Scanner;
import service.DataType;
import service.SemanticsException;
import service.Types;

import java.util.Stack;

public class SemanticAnalyzer {

    private static final int ARGUMENTS_OFFSET = 8;

    private final Scanner scanner;

    private final Tree root = new Tree();
    private Tree current = root;

    private Tree functionPointer, functionArgumentPointer;

    private int global = 0;

    private int localPointer = 0, maximumPointer, argumentsSize;
    private Stack<Integer> localPointerStack = new Stack<>();
    private boolean arguments;

    public SemanticAnalyzer(Scanner s) {
        scanner = s;
        root.setCurrent(root);
    }

    public void addVariable(DataType type, Lexeme identifier) {
        Tree node = findScope(identifier.getName());
        if (node != null) {
            alreadyDefined(identifier, node);
        }

        include(type, identifier);

        if (global == 0) {
            current.global = true;
        } else if (arguments) {
            current.address = localPointer;
        } else {
            maximumPointer = Math.max(maximumPointer, -localPointer);
            current.address = localPointer;
        }
    }

    private void include(DataType type, Lexeme identifier) {
        try {
            root.include(identifier, type);
            current = root.getCurrent();
        } catch (SemanticsException e) {
            e.printStackTrace();
        }
    }

    private void include(DataType type, Lexeme identifier, DataType returnType) {
        try {
            root.include(identifier, type, returnType);
            current = root.getCurrent();
        } catch (SemanticsException e) {
            e.printStackTrace();
        }
    }

    public void startFunction(DataType type, Lexeme identifier) {
        Tree node = findScope(identifier.getName());
        if (node != null) {
            alreadyDefined(identifier, node);
        }
        include(type, identifier, type);

//        left(type, identifier);
//        right();

        maximumPointer = 0;
        argumentsSize = localPointer = ARGUMENTS_OFFSET;

        global++;
        arguments = true;
    }

    public void startClass(Lexeme identifier) {
        Tree node = findScope(identifier.getName());
        if (node != null) {
            alreadyDefined(identifier, node);
        }

        include(DataType.TClass, identifier);
  //      current = root.getCurrent();
//		left(DataType.TUserType, identifier);
//		right();

        maximumPointer = 0;
        argumentsSize = localPointer = ARGUMENTS_OFFSET;

        global++;
        arguments = true;
    }

    private void alreadyDefined(Lexeme identifier, Tree found) {
        String s1 = "Identifier '" + identifier.getName() + "' is already defined in the scope";
        String s2 = "Previous declaration at line " + (found.lexeme.line + 1);
        throw new AnalyzeError(scanner, identifier, s1, s2);
    }

    public void startBlock() {
//        left(DataType.TBlock, null);
//        right();
        include(DataType.TBlock, new Lexeme());

        right();



        if (arguments) {
            arguments = false;
            localPointer = 0;
        }

        global++;
        localPointerStack.push(localPointer);
    }

    public void goToParentLevel() {
        while (current.type != null) {
            current = current.parent;
        }
        current = current.parent;

        global--;
        if (!localPointerStack.isEmpty()) {
            localPointer = localPointerStack.pop();
        }
    }

    private void left(DataType type, Lexeme lexeme) {
        current.left = new Tree(current, type, lexeme);
        current = current.left;
    }

    private void right() {
        current.right = new Tree(current, DataType.Empty, new Lexeme());
        current = current.right;
    }

    public Tree findScope(String identifier) {
        return current.findByName(root.left, identifier);
    }

    public Tree findBuName(String name) {
        return current.findByName(root.left, name);
    }

//	public Node find(String identifier) {
//		Node node = current;
//		do {
//			if(node.lexeme != null && node.lexeme.value.equals(identifier)) return node;
//			node = node.parent;
//		} while(node != null);
//		return null;
//	}

    public DataType cast(DataType a, DataType b) {
        if (a == DataType.TInt || b == DataType.TInt) {
            return DataType.TInt;
        }
        if (a == DataType.TBoolean || b == DataType.TBoolean) {
            return DataType.TBoolean;
        }
        //if(a == DataType. || b == DataType.tLongInt) return DataType.tLongInt;
//		if(a == DataType.tInt || b == DataType.tInt) return DataType.tInt;
        return null;
    }

//	public Value getConstValue(Lexeme lexeme) {
//		BigInteger value = new BigInteger(lexeme.value, lexeme.type == Types.ConstInt10 ? 10 : 16);
//
//		if(value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0)
//			return new Value(DataType.tInt, value.intValue());
//		if(value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0)
//			return new Value(DataType.tLongLongInt, value.longValue());
//
//		throw new AnalyzeError(scanner, lexeme, "Integer constant is too big");
//	}
//

    public tree.Value getVariableValue(Lexeme lexeme) {
        Types type = lexeme.type;
        if (type == Types.TypeInt || type == Types.TypeConstInt) {
            Integer value = Integer.parseInt(lexeme.lexeme.toString());

            return new tree.Value(DataType.TInt, value);

        }
        if (type == Types.TypeBoolean || type == Types.TypeTrue || type == Types.TypeFalse) {
            Boolean value = Boolean.valueOf(lexeme.lexeme.toString());

            return new tree.Value(DataType.TBoolean, value);
        }

        throw new AnalyzeError(scanner, lexeme, "Неверное значение переменной");

    }

    public Tree getVariable(Lexeme lexeme) {
        Tree node = root.findByName(root.left, lexeme.getName());
        if (node == null) {
            throw new AnalyzeError(scanner, lexeme, "Variable '" + lexeme.getName() + "' is not defined in the scope");
        }
        if (node.right != null) {
            throw new AnalyzeError(scanner, lexeme, "'" + lexeme.getName() + "' is not a variable");
        }

        return node;
    }

    public void checkAssignment(Lexeme lexeme, DataType variable, DataType expression) {
        if (cast(variable, expression) != variable) {
            String line1 = "Incompatibility of types during operation";
            String line2 = "'" + variable + "' = '" + expression + "'";
            throw new AnalyzeError(scanner, lexeme, line1, line2);
        }
    }

    public void checkReturnType(Lexeme lexeme, DataType ret) {
        Tree node = current;
        while (node.right == null || node.type == null || node.type == DataType.TBlock) {
            node = node.parent;
        }
        DataType required = node.type;

        if (cast(required, ret) != required) {
            String message = "Incompatible return type '" + ret + "' when required '" + required + "'";
            throw new AnalyzeError(scanner, lexeme, message);
        }
    }

    public void startFunctionCall(Lexeme lexeme) {
        Tree node = root.findByName(root.left, lexeme.getName());
        if (node == null) {
            throw new AnalyzeError(scanner, lexeme, "Function '" + lexeme.getName() + "' is not defined in the scope");
        }
        if (node.right == null) {
            throw new AnalyzeError(scanner, lexeme, "'" + lexeme.getName() + "' is not a function");
        }

        functionPointer = node;
        functionArgumentPointer = node.right.left;
    }

    public void checkFunctionArgument(Lexeme lexeme, DataType type) {
        if (functionArgumentPointer == null || functionArgumentPointer.type == DataType.TBlock) {
            throw new AnalyzeError(scanner, lexeme, "Too many arguments for function");
        }

        DataType required = functionArgumentPointer.type;
        if (cast(required, type) != required) {
            String message = "Incompatible argument type '" + type + "' when required '" + required + "'";
            throw new AnalyzeError(scanner, lexeme, message);
        }
        functionArgumentPointer = functionArgumentPointer.left;
    }

    public DataType finishFunctionCall(Lexeme lexeme) {
        if (functionArgumentPointer != null && functionArgumentPointer.type != DataType.TBlock) {
            throw new AnalyzeError(scanner, lexeme, "Not enough arguments for function");
        }
        return functionPointer.type;
    }

    public String treeToString() {
        StringBuilder builder = new StringBuilder();
        printTree(builder, root, 0);
        return builder.toString();
    }

    private void printTree(StringBuilder builder, Tree node, int level) {
        if (node == null) {
            return;
        }

        for (int i = 0; i < level; i++) {
            builder.append("|");
            for (int j = 0; j < 6; j++) {
                builder.append(" ");
            }
        }

        String value = node.lexeme == null ? null : node.lexeme.getName();
        builder.append(node.type == null ? "♦" : (node.type + " " + value));
        if (node.value != null) {
            builder.append(" (");
            builder.append(node.value.getClass().getSimpleName());
            builder.append(" : ");
            builder.append(node.value.toString());
            builder.append(")");
        }
        if (node == current) {
            builder.append("  <--");
        }

        builder.append("\n");
        printTree(builder, node.right, level + 1);
        printTree(builder, node.left, level);
    }

    public Tree getRoot() {
        return root;
    }

    public int getMaximumPointer() {
        return maximumPointer;
    }

    public int getArgumentsSize() {
        return argumentsSize - ARGUMENTS_OFFSET;
    }
}

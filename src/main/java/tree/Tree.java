package tree;

import llkAnalyzer.AnalyzeError;
import org.apache.log4j.Logger;
import scanner.Lexeme;
import scanner.Scanner;
import service.DataType;
import service.SemanticsException;
import service.Types;

public class Tree {
    protected Logger LOGGER = Logger.getLogger(Tree.class);
    public Lexeme lexeme = new Lexeme(new StringBuilder(), Types.TypeVoid);
    public DataType type;
    public tree.Tree classLink;
    public Value value;
    public DataType returnType;
    public Tree parent, left, right;
    public static Tree current;
    public boolean global = false;
    public int address;
    private Scanner scanner;

    public Tree(Tree p, DataType t, Lexeme l) {
        parent = p;
        type = t;
        lexeme = l;
    }


    public Tree getRoot() {
        return this;
    }

    public Tree(Tree parent, Tree left, Tree right) {
        this.parent = parent;
        this.left = left;
        this.right = right;
    }

    public Tree() {
        parent = this;
        left = null;
        right = null;
    }

    public void setLeft(Tree leftNode) {
        left = leftNode;
    }

    public void setRight(Tree rightNode) {
        right = new Tree(this, null, rightNode.lexeme);
    }

    public Value getIntValue(Lexeme lexeme) {
        Integer value = Integer.parseInt(lexeme.lexeme.toString());

        if (lexeme.type.equals(Types.TypeInt) || lexeme.type.equals(Types.TypeConstInt)) {
            return new Value(DataType.TInt, value);
        }

        throw new AnalyzeError(scanner, lexeme, "Неверное число типа int");
    }

    public Tree findUp(String name) {
        return findUp(this, name);
    }

    public Tree findUp(Tree from, String name) {
        Tree vertex = from;
        Tree vertex1;
        while ((vertex != null) && (vertex != vertex.parent) && !name.equals(vertex.lexeme.getName())) {
            vertex = vertex.parent;
            vertex1 = findByName(vertex, name);
            if (vertex1 != null) {
                return vertex1;
            }
        }
        if (vertex == vertex.parent) {
            return null;
        }

        return vertex;
    }

    public Tree findByNameLeft(Tree vertex, String lexeme) {

        if (vertex == null || vertex == vertex.parent) {
            return null;
        }
        if (vertex.lexeme.getName().equals(lexeme)) {
            return vertex;
        }
        if (vertex.left != null) {
            return findByNameLeft(vertex.left, lexeme);
        } else {
            return findByNameLeft(vertex.right, lexeme);
        }
    }

    public Tree findByNameRight(Tree vertex, String lexeme) {

        if (vertex == null || vertex == vertex.parent) {
            return null;
        }
        if (vertex.lexeme.getName().equals(lexeme)) {
            return vertex;
        }
        if (vertex.right != null) {
            return findByNameRight(vertex.right, lexeme);
        } else {
            return findByNameRight(vertex.left, lexeme);
        }
    }

    public Tree findByName(Tree vertex, String lexeme) {
        Tree foundedVertex = findByNameLeft(vertex, lexeme);
        if (foundedVertex == null) {
            foundedVertex = findByNameRight(vertex, lexeme);
        }
        return foundedVertex;
    }

    public Tree findUp() {
        Tree vertex = current;
        while (vertex != null && vertex.type != DataType.TClass && vertex.type != DataType.TFunction && (vertex != vertex.parent)) {
            vertex = vertex.parent;
        }
        if (vertex == vertex.parent) {
            return null;
        }
        return vertex;
    }

    public Tree findUpFunction() {
        Tree vertex = current;
        while (vertex != null && vertex.type != DataType.TFunction && (vertex != vertex.parent)) {
            vertex = vertex.parent;
        }
        if (vertex == vertex.parent) {
            return null;
        }
        return vertex;
    }

    public Tree retrieveClassLick() {
        Tree vertex = current;
        if (current.classLink != null) {
            return current.classLink;
        } else {
            return findUpFunction();
        }
    }


    public Tree findUpName(String className) {
        Tree vertex = current;
        while (vertex != null && (vertex != vertex.parent) &&
                ((vertex.type == DataType.TClass || vertex.type == DataType.TFunction) && !vertex.lexeme.getName().equals(className)) ||
                (vertex.type != DataType.TClass && vertex.type != DataType.TFunction) && !vertex.lexeme.getName().equals(className)) {
            vertex = vertex.parent;
        }
        if (vertex == vertex.parent) {
            return null;
        }
        return vertex;
    }

    public Tree findRightLeft(String lexemeID) {
        return findRightLeft(this, lexemeID);
    }

    public Tree findRightLeft(Tree from, String lexemeID) {
        Tree vertex = from.right;
        while ((vertex != null) && !lexemeID.equals(vertex.lexeme.getName())) {
            vertex = vertex.left;
        }
        return vertex;
    }


    public Tree findParameter(Tree from, String typeOfParameter) throws SemanticsException {
        Tree vertex = from;
        if (from.right != null) {
            vertex = from.right;
        }

        vertex = vertex.left;

        if (vertex.type.toString() != typeOfParameter) {
            if (vertex.classLink != null) {
                if (vertex.classLink.lexeme.getName() != typeOfParameter) {
                    throw new SemanticsException("Тип формальной и фактической переменной не совпадают: ",
                                                 vertex.classLink.lexeme.getName() + " " + typeOfParameter);
                }
            } else {
                throw new SemanticsException("Тип формальной и фактической переменной не совпадают: ",
                                             vertex.type.toString() + " " + typeOfParameter);
            }
        }

        return vertex;
    }

    public void printTree() {
        System.out.println("Вершина: " + lexeme.getName());
        if (left != null) {
            System.out.println("     слева: " + left.lexeme.getName());
        }
        if (right != null) {
            System.out.println("     справа: " + right.lexeme.getName());
        }
        System.out.println();
        if (left != null) {
            left.printTree();
        }
        if (right != null) {
            right.printTree();
        }
    }

    private void print(Tree node) {
        System.out.println(node.value.value);
       /* switch (node.type) {
            case TBoolean:
                System.out.println(node.value.value);
            case TInt:
                System.out.println(node..value.valueInt);
            case TUserType:
                System.out.println(node.dataValue.value.clazz.lexeme.getName());
        }*/
    }

    public void printVariables() {
        System.out.println("Вершина: " + lexeme.getName());
        if (left != null) {
            System.out.println("     слева: " + left.lexeme.getName());
            print(left);
        }
        if (right != null) {
            System.out.println("     справа: " + right.lexeme.getName());
            //  print(right);
        }
        System.out.println();
        if (left != null) {
            left.printVariables();
        }
        if (right != null) {
            right.printVariables();
        }
    }

    public void printValueTree() {
        System.out.println("Вершина: " + lexeme);
        if (left != null) {
            System.out.println("     слева: " + left.lexeme.getName() + " = " + left.value.value);

        }
        if (right != null) {
            System.out.println("     справа: " + right.lexeme.getName());
        }
        System.out.println();
        if (left != null) {
            left.printValueTree();
        }
        if (right != null) {
            right.printValueTree();
        }
    }

    public Tree findUpOnLevel(Tree from, String lexemeID) {
        Tree vertex = from;
        while ((vertex != null) && vertex.parent.right != vertex) {
            if (lexemeID.equals(vertex.lexeme.getName())) {
                return vertex;
            }
            vertex = vertex.parent;
        }
        return null;
    }




    /*
     * семантические подпрограммы
     * */

    public void setCurrent(Tree vertex) {
        current = vertex;
    }

    public Tree getCurrent() {
        return current;
    }

    public Tree include(Lexeme lexeme, DataType lexemeType) throws SemanticsException {
     //   duplicateControl(current, lexeme.lexeme.toString());
        Tree vertex = new Tree();
        vertex.initLexeme(lexeme);
        vertex.type = lexemeType;
        vertex.parent = current;
        current.setLeft(vertex);
        current = current.left;
        if (lexemeType != DataType.TFunction && lexemeType != DataType.TClass) {
            Tree link;
            link = findUp();
            vertex.classLink = link;
            if (link != null && (link.type == DataType.TClass || link.type == DataType.TUserType)) {
                vertex.global = true;
            }
            return current;
        } else {
            current.setRight(new Tree());
            current = current.right;
            return vertex;
        }
    }

    private void initLexeme(Lexeme lexeme) {
        this.lexeme = new Lexeme();
        this.lexeme.type = lexeme.type;
        this.lexeme.lexeme.append(lexeme.lexeme.toString());
        this.lexeme.ptr = lexeme.ptr;
        this.lexeme.line = lexeme.line;
    }


    public Tree include(Lexeme lexeme, DataType lexemeType, DataType returnType) throws
            SemanticsException {
      //  duplicateControl(current, lexeme.lexeme.toString());
        Tree vertex = new Tree();
        vertex.initLexeme(lexeme);
        vertex.parent = current;
        vertex.type = lexemeType;
        vertex.returnType = returnType;
        Tree link = findUp();
        this.classLink = link;

        if (link != null && (link.type == DataType.TClass || link.type == DataType.TUserType)) {
            global = true;
        }

        /*и еще ссылка на значение*/
        current.setLeft(vertex);
        current = current.left;
        current.setRight(new Tree());
        current = current.right;
        return vertex;
    }

    public void setType(Tree vertex, DataType type) {
        vertex.type = type;
    }


//
//    public void controlNumberOfParameters(Tree vertex, Integer numberOfParameters) throws SemanticsException {
//        if (!numberOfParameters.equals(vertex.numberOfParameters)) {
//            throw new SemanticsException("Число параметров не совпадает");
//        }
//    }


    public DataType getType(String lexeme) throws SemanticsException {
//        LOGGER.info("Метод getType()");
        //  LOGGER.info(lexeme);
        switch (lexeme) {
            case "int":
                return DataType.TInt;
            case "boolean":
                return DataType.TBoolean;
            case "void":
                return DataType.TVoid;
            case "TypeInt":
                return DataType.TInt;
            case "TypeBoolean":
                return DataType.TBoolean;
            case "TypeConstInt":
                return DataType.TInt;
            default:
                Tree vertex = findUp(current, lexeme);
                if (vertex != null) {
                    return vertex.type;
                }
                getClass(lexeme); return DataType.TClass;
        }
    }

    public Tree getTypeOfLexeme(String lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
        if (vertex == null) {
            throw new SemanticsException("Отсутствует описание идентификатора", lexeme);
        }
        if (vertex.type == DataType.TFunction) {
            throw new SemanticsException("Неверное использование вызова функции", lexeme);
        }
        if (vertex.type == DataType.TClass) {
            throw new SemanticsException("Неверное использование вызова объекта класса", lexeme);
        }
        return vertex;
    }

    public Tree getFunction(String lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
        if (vertex == null) {
            throw new SemanticsException("Отсутствует описание функции", lexeme);
        }
        if (vertex.type != DataType.TFunction) {
            throw new SemanticsException("Неверный вызов функции", lexeme);
        }

        return vertex;
    }

    public Tree getFunction() throws SemanticsException {
        Tree vertex = findUpFunction();
        if (vertex == null) {
            throw new SemanticsException("Отсутствует описание функции");
        }
        if (vertex.type != DataType.TFunction) {
            throw new SemanticsException("Неверный вызов функции");
        }

        return vertex;
    }

//    public Tree getFunction(String lexeme, Tree from) throws SemanticsException {
//        Tree vertex = findRightLeft(from, lexeme);
//        if(vertex == null)
//            throw new SemanticsException("Отсутствует описание функции", lexeme);
//        if(vertex.type != DataType.TFunction)
//            throw new SemanticsException("Неверный вызов функции", lexeme);
//
//        return vertex;
//    }

    public Tree getClass(String lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
        if (vertex == null) {
            throw new SemanticsException("Отсутствует описание класса", lexeme);
        }
        if (vertex.type != DataType.TClass) {
            throw new SemanticsException("Неверное использование вызова объекта класса", lexeme);
        }

        return vertex;
    }


    public Tree getObjectName(String lexeme, Tree from) throws SemanticsException {
        Tree vertex = findUp(from, lexeme);
        // Tree vertex = findRightLeft(from, lexeme);

        if (vertex == null) {
            throw new SemanticsException("Отсутствует описание идентфикатора", lexeme);
        }
        return vertex;
    }

    public Tree getObjectNameForClass(String lexeme, Tree from) throws SemanticsException {
        // Tree vertex = findUp(from, lexeme);
        Tree vertex = findRightLeft(from, lexeme);

        if (vertex == null) {
            throw new SemanticsException("Отсутствует описание идентфикатора", lexeme);
        }
        return vertex;
    }


    public void duplicateControl(Tree vertex, String lexeme) throws SemanticsException {
        if (findUp(vertex, lexeme) != null) {
            throw new SemanticsException("Повторное объявление идентификатора", lexeme);
        }
    }

}
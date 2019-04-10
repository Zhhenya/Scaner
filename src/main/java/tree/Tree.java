package tree;

import org.apache.log4j.Logger;
import scanner.Lexeme;
import service.DataType;
import service.SemanticsException;

public class Tree {
    protected Logger LOGGER = Logger.getLogger(Tree.class);
    public Node node;
    public Tree parent, left, right;
    public static Tree current;

    public Tree(Tree parent, Tree left, Tree right, Node node) {
        this.node = node;
        this.parent = parent;
        this.left = left;
        this.right = right;
    }

    public Tree() {
        node = new Node();
        parent = this;
        left = null;
        right = null;
        //   current = this;
    }

    public void setLeft(Node leftNode) {
        Tree vertex = new Tree(this, null, null, leftNode);
        left = vertex;
    }

    public void setRight(Node rightNode) {
        Tree vertex = new Tree(this, null, null, rightNode);
        right = vertex;
    }

    public Tree findUp(String lexemeID) {
        return findUp(this, lexemeID);
    }

    public Tree findUp(Tree from, String lexemeID) {
        Tree vertex = from;
        Tree vertex1 = null;
        while ((vertex != null) && (vertex != vertex.parent) && !lexemeID.equals(vertex.node.lexemeName)) {
            vertex = vertex.parent;
            vertex1 = findByName(vertex, lexemeID);
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
        if (vertex.node.lexemeName.equals(lexeme)) {
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
        if (vertex.node.lexemeName.equals(lexeme)) {
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
        while (vertex != null && vertex.node.type != DataType.TClass && vertex.node.type != DataType.TFunction && (vertex != vertex.parent)) {
            vertex = vertex.parent;
        }
        if (vertex == vertex.parent) {
            return null;
        }
        return vertex;
    }

    public Tree findUpFunction() {
        Tree vertex = current;
        while (vertex != null && vertex.node.type != DataType.TFunction && (vertex != vertex.parent)) {
            vertex = vertex.parent;
        }
        if (vertex == vertex.parent) {
            return null;
        }
        return vertex;
    }

    public Tree retrieveClassLick() {
        Tree vertex = current;
        if (current.node.classLink != null) {
            return current.node.classLink;
        } else {
            return findUpFunction();
        }
    }


    public Tree findUpName(String className) {
        Tree vertex = current;
        while (vertex != null && (vertex != vertex.parent) &&
                ((vertex.node.type == DataType.TClass || vertex.node.type == DataType.TFunction) && !vertex.node.lexemeName.equals(className)) ||
                (vertex.node.type != DataType.TClass && vertex.node.type != DataType.TFunction) && !vertex.node.lexemeName.equals(className)) {
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
        while ((vertex != null) && !lexemeID.equals(vertex.node.lexemeName)) {
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

        if (vertex.node.type.toString() != typeOfParameter) {
            if (vertex.node.classLink != null) {
                if (vertex.node.classLink.node.lexemeName != typeOfParameter) {
                    throw new SemanticsException("Тип формальной и фактической переменной не совпадают: ",
                                                 vertex.node.classLink.node.lexemeName + " " + typeOfParameter);
                }
            } else {
                throw new SemanticsException("Тип формальной и фактической переменной не совпадают: ",
                                             vertex.node.type.toString() + " " + typeOfParameter);
            }
        }

        return vertex;
    }

    public void printTree() {
        System.out.println("Вершина: " + node.lexemeName);
        if (left != null) {
            System.out.println("     слева: " + left.node.lexemeName);
        }
        if (right != null) {
            System.out.println("     справа: " + right.node.lexemeName);
        }
        System.out.println();
        if (left != null) {
            left.printTree();
        }
        if (right != null) {
            right.printTree();
        }
    }

    private void print(Node node) {
        switch (node.type) {
            case TBoolean:
                System.out.println(node.dataValue.value.constant);
            case TInt:
                System.out.println(node.dataValue.value.valueInt);
            case TUserType:
                System.out.println(node.dataValue.value.clazz.node.lexemeName);
        }
    }

    public void printVariables() {
        System.out.println("Вершина: " + node.lexemeName);
        if (left != null) {
            System.out.println("     слева: " + left.node.lexemeName);
            print(left.node);
        }
        if (right != null) {
            System.out.println("     справа: " + right.node.lexemeName);
            //  print(right.node);
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
        System.out.println("Вершина: " + node.lexemeName);
        if (left != null) {
            if (left.node.type == DataType.TInt) {
                System.out.println("     слева: " + left.node.lexemeName + " = " + left.node.dataValue.value.valueInt);
            } else {
                System.out.println("     слева: " + left.node.lexemeName);
            }
        }
        if (right != null) {
            System.out.println("     справа: " + right.node.lexemeName);
        }
        System.out.println();
        if (left != null) {
            left.printValueTree();
        }
        if (right != null) {
            right.printValueTree();
        }
    }

  /*  public Tree find(Tree from, String lexeme) {
        Tree vertex = from;
        do {
            if (vertex.node.lexemeName.equals(lexeme)) {
                return vertex;
            }
            vertex = vertex.parent;
        }while(vertex != null);
    }*/

    public Tree findUpOnLevel(Tree from, String lexemeID) {
        Tree vertex = from;
        while ((vertex != null) && vertex.parent.right != vertex) {
            if (lexemeID.equals(vertex.node.lexemeName)) {
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

    public Tree include(Lexeme lexeme, DataType lexemeType, String className) throws SemanticsException {
        duplicateControl(current, lexeme.lexeme.toString());
        Tree vertex = new Tree();
        Node newNode = new Node();
        node.dataValue = new DataValue();
        newNode.lexemeName = lexeme.lexeme.toString();
        newNode.ptrStart = lexeme.ptr;
        newNode.line = lexeme.line;
        newNode.type = lexemeType;
        /*и еще ссылка на значение*/
        current.setLeft(newNode);
        current = current.left;
        if (lexemeType != DataType.TFunction && lexemeType != DataType.TClass) {
            if (className != null) {
                newNode.classLink = findUpName(className);
            } else {
                newNode.classLink = findUp();
            }
            return current;
        } else {
            vertex = current;
            /*page 141*/
            current.setRight(new Node());
            current = current.right;
            return vertex;
        }
    }


    public Tree include(Lexeme lexeme, DataType lexemeType, DataType returnType, String className) throws
            SemanticsException {
        duplicateControl(current, lexeme.lexeme.toString());
        Tree vertex;
        Node newNode = new Node();
        newNode.dataValue = new DataValue();
        newNode.lexemeName = lexeme.lexeme.toString();
        newNode.ptrStart = lexeme.ptr;
        newNode.line = lexeme.line;
        newNode.type = lexemeType;
        newNode.dataValue.type = returnType;
        if (className != null) {
            newNode.classLink = findUpName(className);
        } else {
            newNode.classLink = findUp();
        }


        /*и еще ссылка на значение*/
        current.setLeft(newNode);
        current = current.left;
        vertex = current;
        /*page 141*/
        current.setRight(new Node());
        current = current.right;
        return vertex;
    }

    public void setType(Tree vertex, DataType type) {
        vertex.node.type = type;
    }

    public void setNumberOfParameters(Tree vertex, Integer numberOfParameters) {
        vertex.node.numberOfParameters = numberOfParameters;
    }

    public void controlNumberOfParameters(Tree vertex, Integer numberOfParameters) throws SemanticsException {
        if (numberOfParameters != vertex.node.numberOfParameters) {
            throw new SemanticsException("Число параметров не совпадает");
        }
    }


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
                    return vertex.node.type;
                }
                getClass(lexeme); return DataType.TClass;
        }
    }

    public Tree getTypeOfLexeme(String lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
        if (vertex == null) {
            throw new SemanticsException("Отсутствует описание идентификатора", lexeme);
        }
        if (vertex.node.type == DataType.TFunction) {
            throw new SemanticsException("Неверное использование вызова функции", lexeme);
        }
        if (vertex.node.type == DataType.TClass) {
            throw new SemanticsException("Неверное использование вызова объекта класса", lexeme);
        }
        return vertex;
    }

    public Tree getFunction(String lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
        if (vertex == null) {
            throw new SemanticsException("Отсутствует описание функции", lexeme);
        }
        if (vertex.node.type != DataType.TFunction) {
            throw new SemanticsException("Неверный вызов функции", lexeme);
        }

        return vertex;
    }

    public Tree getFunction() throws SemanticsException {
        Tree vertex = findUpFunction();
        if (vertex == null) {
            throw new SemanticsException("Отсутствует описание функции");
        }
        if (vertex.node.type != DataType.TFunction) {
            throw new SemanticsException("Неверный вызов функции");
        }

        return vertex;
    }

//    public Tree getFunction(String lexeme, Tree from) throws SemanticsException {
//        Tree vertex = findRightLeft(from, lexeme);
//        if(vertex == null)
//            throw new SemanticsException("Отсутствует описание функции", lexeme);
//        if(vertex.node.type != DataType.TFunction)
//            throw new SemanticsException("Неверный вызов функции", lexeme);
//
//        return vertex;
//    }

    public Tree getClass(String lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
//        LOGGER.info("Метод getClass()");
//        LOGGER.info(vertex);
//        LOGGER.info(lexeme);
        if (vertex == null) {
            throw new SemanticsException("Отсутствует описание класса", lexeme);
        }
        if (vertex.node.type != DataType.TClass) {
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
      /*  if(vertex.node.type != DataType.TClass && vertex.node.type != DataType.TBoolean && vertex.node.type !=
      DataType.TInt && vertex.node.type != DataType.TFunction)
            throw new SemanticsException("Такой идентификатор не объявлен", lexeme);*/


        return vertex;
    }

    public Tree getObjectNameForClass(String lexeme, Tree from) throws SemanticsException {
        // Tree vertex = findUp(from, lexeme);
        Tree vertex = findRightLeft(from, lexeme);

        if (vertex == null) {
            throw new SemanticsException("Отсутствует описание идентфикатора", lexeme);
        }
      /*  if(vertex.node.type != DataType.TClass && vertex.node.type != DataType.TBoolean && vertex.node.type !=
      DataType.TInt && vertex.node.type != DataType.TFunction)
            throw new SemanticsException("Такой идентификатор не объявлен", lexeme);*/


        return vertex;
    }


    public Tree forReset(Tree vertex) {
        Tree newVertex = new Tree();

        return newVertex;
    }

    public void duplicateControl(Tree vertex, String lexeme) throws SemanticsException {
        if (findUp(vertex, lexeme) != null) {
            throw new SemanticsException("Повторное объявление идентификатора", lexeme);
        }
    }


    public Node getNode() {
        return node;
    }

    public Tree getParent() {
        return parent;
    }

    public Tree getLeft() {
        return left;
    }

    public Tree getRight() {
        return right;
    }

    public Tree clone(Tree vertex) {
        Tree tree = new Tree();
        tree.node = vertex.node.clone();
        tree.right = new Tree();
        tree.right.node = vertex.right.node.clone();
        tree.right.parent = tree;
        tree.right.left = vertex.cloneLeft(vertex.right.left);
        tree.right.left.parent = tree.right;
        return tree;
    }

    public Tree cloneLeft(Tree vertex) {
        Tree tree = new Tree();
        tree.node = vertex.node.clone();
        if (vertex.left != null) {
            tree.left = vertex.cloneLeft(vertex.left);
            tree.left.parent = tree;
        }
        if (vertex.right != null) {
            tree.right = vertex.cloneLeft(vertex.right);
            tree.right.parent = tree;
        }
        if (vertex.right == null && vertex.left == null) {
            return tree;
        }
        return tree;
    }
}
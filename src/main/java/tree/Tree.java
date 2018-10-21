package tree;

import service.DateType;
import service.SemanticsException;

import java.util.Collections;

public class Tree {
    public Node node;
    public Tree parent, left, right;
    public static Tree current;

    public Tree(Tree parent, Tree left, Tree right, Node node){
        this.node = node;
        this.parent = parent;
        this.left = left;
        this.right = right;
    }

    public Tree(){
        node = new Node();
        parent = this;
        left = null;
        right = null;
     //   current = this;
    }

    public  void setLeft(Node leftNode){
        Tree vertex = new Tree(this, null, null, leftNode);
        left = vertex;
    }

    public void setRight(Node rightNode){
        Tree vertex = new Tree(this, null, null, rightNode);
        right = vertex;
    }

    public Tree findUp(String lexemeID){
        return findUp(this, lexemeID);
    }

    public Tree findUp(Tree from, String lexemeID){
        Tree vertex = from;
        while((vertex != null) /*&& (vertex.parent != null)*/ && (vertex != vertex.parent)
                && lexemeID.toString().compareTo(vertex.node.lexemeID.toString()) != 0)
            vertex = vertex.parent;
        if(vertex == vertex.parent)
            return null;

        return vertex;
    }

    public Tree findUp(){
        Tree vertex = current;
        while(vertex != null && vertex.node.type != DateType.TClass && vertex.node.type != DateType.TFunction && (vertex != vertex.parent))
            vertex = vertex.parent;
        if(vertex == vertex.parent)
            return null;
        return vertex;
    }

    public Tree findUpFunction(){
        Tree vertex = current;
        while(vertex != null && vertex.node.type != DateType.TFunction && (vertex != vertex.parent))
            vertex = vertex.parent;
        if(vertex == vertex.parent)
            return null;
        return vertex;
    }


    public Tree findUpName(String className){
        Tree vertex = current;
        while(vertex != null && (vertex != vertex.parent) &&
                ((vertex.node.type == DateType.TClass || vertex.node.type == DateType.TFunction)   && vertex.node.lexemeID.compareTo(className) != 0) ||
                (vertex.node.type != DateType.TClass && vertex.node.type != DateType.TFunction)   && vertex.node.lexemeID.compareTo(className) != 0) {
                vertex = vertex.parent;
        }
        if(vertex == vertex.parent)
            return null;
        return vertex;
    }

    public Tree findRightLeft(String lexemeID){
        return findRightLeft(this, lexemeID);
    }

    public Tree findRightLeft(Tree from, String lexemeID){
        Tree vertex = from.right;
        while((vertex != null) && lexemeID.toString().compareTo(vertex.node.lexemeID.toString()) != 0)
            vertex = vertex.left;
        return vertex;
    }


    public Tree findParameter(Tree from, String typeOfParameter) throws SemanticsException {
        Tree vertex = from;
        if(from.right != null)
            vertex = from.right;

        vertex = vertex.left;

        if(vertex.node.type.toString() != typeOfParameter)
            if(vertex.node.classLink != null ) {
                if (vertex.node.classLink.node.lexemeID != typeOfParameter)
                    throw new SemanticsException("Тип формальной и фактической переменной не совпадают: ", vertex.node.classLink.node.lexemeID + " " + typeOfParameter);
            }else
                throw new SemanticsException("Тип формальной и фактической переменной не совпадают: ", vertex.node.type.toString() + " " + typeOfParameter);

        return vertex;
    }

    public void printTree(){
        System.out.println("Вершина: " + node.lexemeID);
        if(left != null)
            System.out.println("     слева: " + left.node.lexemeID);
        if(right != null)
            System.out.println("     справа: " + right.node.lexemeID);
        System.out.println();
        if(left != null)
            left.printTree();
        if(right != null)
            right.printTree();
    }

    public Tree findUpOnLevel(Tree from, String lexemeID){
        Tree vertex = from;
        while((vertex != null) && vertex.parent.right != vertex){
            if(lexemeID.toString().compareTo(vertex.node.lexemeID.toString()) == 0)
                return vertex;
            vertex = vertex.parent;
        }
        return null;
    }




    /*
    * семантические подпрограммы
    * */

    public void setCurrent(Tree vertex){
        current = vertex;
    }

    public Tree getCurrent(){
        return current;
    }

    public Tree include(String lexeme, DateType lexemeType, String className) throws SemanticsException {
        duplicateControl(current, lexeme);
        Tree vertex = new Tree();
        Node newNode = new Node();
        newNode.lexemeID = lexeme.toString();
        newNode.type = lexemeType;
        /*и еще ссылка на значение*/
        current.setLeft(newNode);
        current = current.left;
        if(lexemeType != DateType.TFunction && lexemeType !=DateType.TClass) {
            if(className != null)
                newNode.classLink = findUpName(className);
            else
                newNode.classLink = findUp();
            return current;
        }
        else{
            vertex = current;
            /*page 141*/
            current.setRight(new Node());
            current = current.right;
            return vertex;
        }
    }


    public Tree include(String lexeme, DateType lexemeType, DateType returnType, String className) throws SemanticsException {
        duplicateControl(current, lexeme);
        Tree vertex = new Tree();
        Node newNode = new Node();
        newNode.lexemeID = lexeme.toString();
        newNode.type = lexemeType;
        newNode.returnType = returnType;
        if(className != null)
            newNode.classLink = findUpName(className);
        else
            newNode.classLink = findUp();


        /*и еще ссылка на значение*/
        current.setLeft(newNode);
        current = current.left;
        vertex = current;
        /*page 141*/
        current.setRight(new Node());
        current = current.right;
        return vertex;
    }

    public void setType(Tree vertex, DateType type){
        vertex.node.type = type;
    }

    public void setNumberOfParameters(Tree vertex, Integer numberOfParameters){
        vertex.node.numberOfParameters = numberOfParameters;
    }

    public void controlNumberOfParameters(Tree vertex, Integer numberOfParameters) throws SemanticsException {
        if(numberOfParameters != vertex.node.numberOfParameters)
            throw new SemanticsException("Число параметров не совпадает");
    }


    public DateType getType(String lexeme) throws SemanticsException {
        switch(lexeme.toString()){
            case "int" : return DateType.TInt;
            case "boolean": return DateType.TBoolean;
            case "void": return DateType.TVoid;
            default: getClass(lexeme); return DateType.TClass;
        }
    }

    public Tree getTypeOfLexeme(String lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
        if(vertex == null)
            throw new SemanticsException("Отсутствует описание идентификатора", lexeme);
        if(vertex.node.type == DateType.TFunction)
            throw new SemanticsException("Неверное использование вызова функции", lexeme);
        if(vertex.node.type == DateType.TClass)
            throw new SemanticsException("Неверное использование вызова объекта класса", lexeme);
      return vertex;
    }

    public Tree getFunction(String lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
        if(vertex == null)
            throw new SemanticsException("Отсутствует описание функции", lexeme);
        if(vertex.node.type != DateType.TFunction)
            throw new SemanticsException("Неверный вызов функции", lexeme);

        return vertex;
    }

    public Tree getFunction() throws SemanticsException {
        Tree vertex = findUpFunction();
        if(vertex == null)
            throw new SemanticsException("Отсутствует описание функции");
        if(vertex.node.type != DateType.TFunction)
            throw new SemanticsException("Неверный вызов функции");

        return vertex;
    }

    public Tree getFunction(String lexeme, Tree from) throws SemanticsException {
        Tree vertex = findRightLeft(from, lexeme);
        if(vertex == null)
            throw new SemanticsException("Отсутствует описание функции", lexeme);
        if(vertex.node.type != DateType.TFunction)
            throw new SemanticsException("Неверный вызов функции", lexeme);

        return vertex;
    }

    public Tree getClass(String lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
        if(vertex == null)
            throw new SemanticsException("Отсутствует описание класса", lexeme);
        if(vertex.node.type != DateType.TClass)
            throw new SemanticsException("Неверное использование вызова объекта класса", lexeme);

        return vertex;
    }


    public Tree getObjectName(String lexeme, Tree from) throws SemanticsException {
        Tree vertex = findUp(from, lexeme);
       // Tree vertex = findRightLeft(from, lexeme);

        if(vertex == null)
            throw new SemanticsException("Отсутствует описание идентфикатора", lexeme);
      /*  if(vertex.node.type != DateType.TClass && vertex.node.type != DateType.TBoolean && vertex.node.type != DateType.TInt && vertex.node.type != DateType.TFunction)
            throw new SemanticsException("Такой идентификатор не объявлен", lexeme);*/


        return vertex;
    }

    public Tree getObjectNameForClass(String lexeme, Tree from) throws SemanticsException {
        // Tree vertex = findUp(from, lexeme);
        Tree vertex = findRightLeft(from, lexeme);

        if(vertex == null)
            throw new SemanticsException("Отсутствует описание идентфикатора", lexeme);
      /*  if(vertex.node.type != DateType.TClass && vertex.node.type != DateType.TBoolean && vertex.node.type != DateType.TInt && vertex.node.type != DateType.TFunction)
            throw new SemanticsException("Такой идентификатор не объявлен", lexeme);*/


        return vertex;
    }



    public Tree forReset(Tree vertex){
        Tree newVertex = new Tree();

        return newVertex;
    }

    public void duplicateControl(Tree vertex, String lexeme) throws SemanticsException {
        if(findUp(vertex, lexeme) != null)
            throw new SemanticsException("Повторное объявление идентификатора", lexeme);
    }






}
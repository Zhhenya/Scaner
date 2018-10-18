package tree;

import service.DateType;
import service.SemanticsException;

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
        parent = null;
        left = null;
        right = null;
    }

    public  void setLeft(Node leftNode){
        Tree vertex = new Tree(this, null, null, leftNode);
        left = vertex;
    }

    public void setRight(Node rightNode){
        Tree vertex = new Tree(this, null, null, rightNode);
        right = vertex;
    }

    public Tree findUp(StringBuilder lexemeID){
        return findUp(this, lexemeID);
    }

    public Tree findUp(Tree from, StringBuilder lexemeID){
        Tree vertex = from;
        while((vertex != null) /*page 127 */){

        }
        return vertex;
    }

    public Tree findRightLeft(StringBuilder lexemeID){
        return findRightLeft(this, lexemeID);
    }

    public Tree findRightLeft(Tree from, StringBuilder lexemeID){
        Tree vertex = from.right;
        while((vertex != null) /*page 127*/)
            vertex = vertex.left;
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

    public Tree findUpOnLevel(Tree from, StringBuilder lexemeID){
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

    public Tree include(StringBuilder lexeme, DateType lexemeType) throws SemanticsException {
        if(duplicateControl(current, lexeme))
            throw new SemanticsException("Повтроное объявление идентификатора", lexeme);
        Tree vertex = new Tree();
        Node newNode = new Node();
        newNode.lexemeID = lexeme;
        newNode.type = lexemeType;
        /*и еще ссылка на значение*/
        current.setLeft(newNode);
        current = current.left;
        if(lexemeType != DateType.TFunction && lexemeType !=DateType.TClass)
            return current;
        else{
            vertex = current;
            /*page 141*/
            current.setRight(new Node());
            current = current.right;
            return vertex;
        }
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

    public Tree getTypeOfLexeme(StringBuilder lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
        if(vertex == null)
            throw new SemanticsException("Отсутствует описание идентификатора", lexeme);
        if(vertex.node.type == DateType.TFunction)
            throw new SemanticsException("Неверное использование вызова функции", lexeme);
        if(vertex.node.type == DateType.TClass)
            throw new SemanticsException("Неверное использование вызова объекта класса", lexeme);
      return vertex;
    }

    public Tree getFunction(StringBuilder lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
        if(vertex == null)
            throw new SemanticsException("Отсутствует описание функции", lexeme);
        if(vertex.node.type != DateType.TFunction)
            throw new SemanticsException("Неверный вызов функции", lexeme);

        return vertex;
    }

    public Tree getClass(StringBuilder lexeme) throws SemanticsException {
        Tree vertex = findUp(current, lexeme);
        if(vertex == null)
            throw new SemanticsException("Отсутствует описание класса", lexeme);
        if(vertex.node.type != DateType.TClass)
            throw new SemanticsException("Неверное использование вызова объекта класса", lexeme);

        return vertex;
    }

 /*   public Tree reset(Tree vertex){

    }*/

    public boolean duplicateControl(Tree vertex, StringBuilder lexeme){
        if(findUp(vertex, lexeme) == null)
            return false;
        return true;
    }






}
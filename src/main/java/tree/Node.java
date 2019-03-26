package tree;

import service.DataType;
/*
* lexemeName - ID лексеммц
* type - тип значение (int, boolean, userType)
* */

public class Node {
    public String lexemeName;
    public int ptr;
    public int line;
    public DataType type;
    public Boolean constantFlag;
    public Constant constantValue;
    public Integer numberOfParameters;
    public DataType returnType;
    public Tree classLink;
    public DataValue dataValue;

    public Node(){
        lexemeName = "";
        ptr = -1;
        line = -1;
        type = DataType.TUnknown;
        constantFlag = false;
        constantValue = new Constant();
        numberOfParameters = 0;
        returnType = DataType.TVoid;
        classLink = null;
        dataValue = new DataValue();
    }

    public Node clone(){
        Node clone = new Node();
        clone.dataValue = this.dataValue.clone();
        clone.line = this.line;
        clone.ptr = this.ptr;
        clone.type = this.type;
        clone.lexemeName = this.lexemeName;
        clone.returnType = this.returnType;
        clone.classLink = this.classLink;
        clone.constantFlag = this.constantFlag;
        clone.constantValue = this.constantValue;
        return clone;
    }
}
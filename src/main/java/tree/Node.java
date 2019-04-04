package tree;

import service.DataType;
/*
* lexemeName - ID лексеммц
* type - тип значение (int, boolean, userType)
* */

public class Node {
    public String lexemeName;
    public int ptrStart;
    public int ptrEnd;
    public int lineEnd;
    public int line;
    public DataType type;
    public Boolean constantFlag;
    public Constant constantValue;
    public Integer numberOfParameters;
    public Tree classLink;
    public DataValue dataValue;

    public Node(){
        lexemeName = "";
        ptrStart = -1;
        ptrEnd = -1;
        line = -1;
        lineEnd = -1;
        type = DataType.TUnknown;
        constantFlag = false;
        constantValue = new Constant();
        numberOfParameters = 0;
        classLink = null;
        dataValue = new DataValue();
    }

    public Node clone(){
        Node clone = new Node();
        clone.dataValue = this.dataValue.clone();
        clone.line = this.line;
        clone.ptrStart = this.ptrStart;
        clone.ptrEnd = this.ptrEnd;
        clone.type = this.type;
        clone.lexemeName = this.lexemeName;
        clone.classLink = this.classLink;
        clone.constantFlag = this.constantFlag;
        clone.constantValue = this.constantValue;
        return clone;
    }
}
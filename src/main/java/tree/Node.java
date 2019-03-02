package tree;

import service.DataType;
/*
* lexemeName - ID лексеммц
* type - тип значение (int, boolean, userType)
* */

public class Node {
    public String lexemeName;
    public DataType type;
    public Boolean constantFlag;
    public Constant constantValue;
    public Integer numberOfParameters;
    public DataType returnType;
    public Tree classLink;
    public DataValue dataValue;

    public Node(){
        lexemeName = "";
        type = DataType.TUnknown;
        constantFlag = false;
        constantValue = new Constant();
        numberOfParameters = 0;
        returnType = DataType.TVoid;
        classLink = null;
        dataValue = new DataValue();
    }
}
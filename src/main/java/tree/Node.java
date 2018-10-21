package tree;

import service.DateType;
/*
* lexemeID - ID лексеммц
* type - тип значение (int, boolean, userType)
* */

public class Node {
    public String lexemeID;
    public DateType type;
    public Boolean constantFlag;
    public Constant constantValue;
    public Integer numberOfParameters;
    public DateType returnType;
    public Tree classLink;

    public Node(){
        lexemeID = "";
        type = DateType.TUnknown;
        constantFlag = false;
        constantValue = new Constant();
        numberOfParameters = 0;
        returnType = DateType.TVoid;
        classLink = null;
    }
}
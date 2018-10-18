package tree;

import service.DateType;
/*
* lexemeID - ID лексеммц
* type - тип значение (int, boolean, userType)
* */

public class Node {
    public StringBuilder lexemeID;
    public DateType type;
    public Boolean constantFlag;
    public Constant constantValue;
    public Integer numberOfParameters;

    public Node(){
        lexemeID = null;
        type = null;
        constantFlag = null;
        constantValue = null;
        numberOfParameters = null;
    }
}
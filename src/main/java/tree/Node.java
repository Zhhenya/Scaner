package tree;

import service.DataType;
import service.Types;
/*
* lexemeName - ID лексеммц
* type - тип значение (int, boolean, userType)
* */

public class Node {
    public String lexemeName;
    public DataType type;
    public Tree classLink;
    public Value value;
    public DataType returnType;

    public Node(){
        lexemeName = "";

        type = DataType.TUnknown;
        classLink = null;
        value = new Value();
    }
}
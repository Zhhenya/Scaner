package service;

import scanner.Lexeme;
import tree.Tree;

public class Pos {
    public Lexeme callVarPointAddr;
    public Tree classLinkVar;
    public Lexeme callMethodPointAddr;
    public Tree classLinkFunction;
    public Lexeme callClassPointAddr;
    public Tree classLinkClass;

    public Lexeme descriptionMethodAddr;
    public Lexeme descriptionClassAddr;

    public Pos() {
        callVarPointAddr = new Lexeme();
        callMethodPointAddr = new Lexeme();
        callClassPointAddr = new Lexeme();

        descriptionMethodAddr = new Lexeme();
        descriptionClassAddr = new Lexeme();
    }

    public void setCallMethodPointAddr(Lexeme lexeme, Tree classLinkFunction) {
        callMethodPointAddr.ptr = lexeme.ptr;
        callMethodPointAddr.line = lexeme.line;
        callMethodPointAddr.lexeme = lexeme.lexeme;
        if (!classLinkFunction.node.lexemeName.equals("TestClass")) {
            this.classLinkFunction = classLinkFunction.clone(classLinkFunction);
        }else
            this.classLinkFunction = classLinkFunction;
    }

    public void setDescriptionMethodAddr(int ptr, int line) {
        descriptionMethodAddr.ptr = ptr;
        descriptionMethodAddr.line = line;
    }

    public void setCallVarPointAddr(Lexeme lexeme, Tree classLinkVar) {
        callVarPointAddr.ptr = lexeme.ptr;
        callVarPointAddr.line = lexeme.line;
        callVarPointAddr.lexeme = lexeme.lexeme;
        if (!classLinkVar.node.lexemeName.equals("TestClass")) {
            this.classLinkVar = classLinkVar.clone(classLinkVar);
        }else
            this.classLinkVar = classLinkVar;
    }

}
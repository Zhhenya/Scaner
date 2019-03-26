package service;

import scanner.Lexeme;

public class Pos {
   public Lexeme callVarPointAddr;
   public Lexeme callMethodPointAddr;
   public Lexeme callClassPointAddr;

   public Lexeme descriptionMethodAddr;
   public Lexeme descriptionClassAddr;


   public Pos(){
      callVarPointAddr = new Lexeme();
      callMethodPointAddr = new Lexeme();
      callClassPointAddr = new Lexeme();

      descriptionMethodAddr= new Lexeme();
      descriptionClassAddr = new Lexeme();
   }

   public void setCallMethodPointAddr(int ptr, int line){
      callMethodPointAddr.ptr = ptr;
      callMethodPointAddr.line = line;
   }

   public void setDescriptionMethodAddr(int ptr, int line){
      descriptionMethodAddr.ptr = ptr;
      descriptionMethodAddr.line = line;
   }

}
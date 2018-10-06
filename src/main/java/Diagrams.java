/*
import java.net.Proxy;

public class Diagrams {
    private Scaner scaner;
    private StringBuilder lexeme;

    public Diagrams(Scaner scaner, StringBuilder lexeme){
        this.scaner = scaner;
        this.lexeme = lexeme;
    }

    public Scaner getScaner() {
        return scaner;
    }

    public void setScaner(Scaner scaner) {
        this.scaner = scaner;
    }

    public StringBuilder getLexeme() {
        return lexeme;
    }

    public void setLexeme(StringBuilder lexeme) {
        this.lexeme = lexeme;
    }
*/
/*
    // public class Main{ [C]}
    public Types S(){
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.TypePublic)
            return printError("Ожидалось 'public'");

        if((t = scaner.scaner(lexeme)) != Types.TypeClass)
            return printError("Ожидалось 'class'");

        if((t = scaner.scaner(lexeme)) != Types.TypeMain && t != Types.TypeIdent)
          return printError("Ожидалось Main");

        if((t = scaner.scaner(lexeme)) != Types.TypeOpenBrace)
          return printError("Ожидалось {");


        if(C() == Types.TypeError)
            return Types.TypeError;

        if((t = scaner.scaner(lexeme)) != Types.TypeCloseBrace)
          return  printError("Ожидалось }");


        return Types.TypeForReturn;
    }

    public Types C(){
        Types t = null;

        int startPosition, currentPosition = -1;
        do {
            startPosition = scaner.getCurrentIndexPosition();
                if((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeInt || (t) == Types.TypeBoolean ||t == Types.TypeVoid) {
                    startPosition = scaner.getCurrentIndexPosition();
                    if((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.Typemain) {
                        scaner.setCurrentIndexPosition(startPosition);
                        t = M();
                    }
                }
                else {
                    scaner.setCurrentIndexPosition(startPosition);
                    t = Types.TypeError;
                }

                if(t == Types.TypeError) {
                    scaner.setCurrentIndexPosition(startPosition);

                    t = I();
                    currentPosition = scaner.getCurrentIndexPosition();
                }
                if(t == Types.TypeError) {
                    scaner.setCurrentIndexPosition(startPosition);
                    if ((t = scaner.scaner(lexeme)) !=Types.TypePublic)
                        return printError("Ожидалось 'public'");
                    else {
                        scaner.setCurrentIndexPosition(startPosition);
                        t = S();
                    }

                }
                else
                    scaner.setCurrentIndexPosition(currentPosition);
            }while(t == Types.TypeForReturn);

     *//*
*/
/*   if((t = scaner.scaner(lexeme)) != Types.TypeCloseBrace)
            return printError("Ожидалось '}'");*//*
*/
/*
        if(t == Types.TypeComma)
            return printError("Ожидалось ';'");
        if(t == Types.TypeError)
            return Types.TypeError;

        return Types.TypeForReturn;

    }*//*



   */
/* public Types M() {
        Types t;

        if (R() == Types.TypeForReturn) {
            if ((t = scaner.scaner(lexeme)) != Types.TypeIdent && t != Types.Typemain)
                return Types.TypeError;
               // return printError("Ожидалось идентификатор или main");
            if (scaner.scaner(lexeme) != Types.TypeOpenParenthesis)
                return Types.TypeError;
              //  return printError("Ожидалось (");

            if (F() == Types.TypeError)
                return Types.TypeError;

            if (scaner.scaner(lexeme) != Types.TypeCloseParenthesis)
                return printError("Ожидалось )");

            if (B() == Types.TypeError)
                return Types.TypeError;
        }
        else
            return Types.TypeError;
        return Types.TypeForReturn;
    }*//*


   */
/* public Types R(){
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeVoid &&
            t != Types.TypeIdent && t != Types.TypeBoolean &&
            t != Types.TypeInt)
         //   return printError("Ожидался тип возвращаемого значения или идентификатор");
            return Types.TypeError;
        return Types.TypeForReturn;
    }*//*


    public Types F(){
        Types t;
        int startPosition = 0;
        do{
            if((t = scaner.scaner(lexeme)) != Types.TypeBoolean &&
                    t != Types.TypeInt && t != Types.TypeIdent)
                return printError("Ожидался тип или идентификатор");
            else
                if((t =scaner.scaner(lexeme)) != Types.TypeIdent)
                    return Types.TypeError;
                  //  return printError("Ожидался идентификатор");
            startPosition = scaner.getCurrentIndexPosition();
            t = scaner.scaner(lexeme);
        }while(t == Types.TypeComma);
        scaner.setCurrentIndexPosition(startPosition);

        return Types.TypeForReturn;
    }

   */
/* public Types B(){
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.TypeOpenBrace)
            return printError("Ожидалось {");

        O();

        if((t = scaner.scaner(lexeme)) != Types.TypeCloseBrace)
            return printError("Ожидалось }");

        return null;
    }*//*


    public Types O(){
        Types t = null;
        int startPosition = scaner.getCurrentIndexPosition();

        while(t != Types.TypeError && t != Types.TypeForReturn) {
            startPosition = scaner.getCurrentIndexPosition();
            if ((t = scaner.scaner(lexeme)) == Types.TypeIdent || t != Types.TypeBoolean || t != Types.TypeInt) {
                scaner.setCurrentIndexPosition(startPosition);
                    t = V1();
            } else
                if (t == Types.TypePublic && t == Types.TypeClass) {
                    scaner.setCurrentIndexPosition(startPosition);
                    t = S();
                }
                else
                    t = O1();
        }

        if(t == Types.TypeError)
            return Types.TypeError;



        return Types.TypeForReturn;
    }

   */
/* public Types V1(){
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.TypeIdent && t != Types.TypeBoolean && t != Types.TypeInt)
            return printError("Ожидался тип");
        if(I() == Types.TypeError)
            return Types.TypeError;

        if(scaner.scaner(lexeme) != Types.TypeSemicolon)
            return printError("Ожидалась ;");

        return Types.TypeForReturn;
    }*//*


    */
/*public Types I(){
        Types t = null;
        int startPosition  = -1, count = 0;
         do{
            if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
                return Types.TypeError;
               // return printError("Ожидался идентификатор");
             startPosition = scaner.getCurrentIndexPosition();
             if((t = scaner.scaner(lexeme)) == Types.TypeAssign) {
                 if (V() == Types.TypeError)
                     return Types.TypeError;
                  count++;             //   startPosition = scaner.getCurrentIndexPosition();
             }else
                 scaner.setCurrentIndexPosition(startPosition);
             startPosition = scaner.getCurrentIndexPosition();
         }while((t = scaner.scaner(lexeme)) == Types.TypeComma);
        if(t != Types.TypeSemicolon && count == 0)
             return Types.TypeError;
         if(t != Types.TypeSemicolon)
            scaner.setCurrentIndexPosition(startPosition);

        return Types.TypeForReturn;
    }*//*


    public Types O1(){
        Types t = null;
        int startPosition = scaner.getCurrentIndexPosition();
        if((t = scaner.scaner(lexeme)) == Types.TypeIf) {
            if ((t = scaner.scaner(lexeme)) != Types.TypeOpenParenthesis)
                return printError("Ожидалось (");
            else {
                scaner.setCurrentIndexPosition(startPosition);
                return U();
            }
        }else
            if(t == Types.TypeIdent){
                if((t = scaner.scaner(lexeme)) == Types.TypeDot) {
                    scaner.setCurrentIndexPosition(startPosition);
                    return N2();
                }
                else
                    if(t == Types.TypeOpenBrace){
                        scaner.setCurrentIndexPosition(startPosition);
                        return H();
                    }
            }
            else
                if(t == Types.TypeOpenBrace){
                    scaner.setCurrentIndexPosition(startPosition);
                    return B();
                }

        return t;
    }

    public Types N2(){
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
            return Types.TypeError;
          //  return printError("Ожидался идентификатор");
        while((t = scaner.scaner(lexeme))== Types.TypeDot ){
            t = scaner.scaner(lexeme);
            if(t != Types.TypeIdent)
                return printError("Ожидался идентификатор");
        }

        return Types.TypeForReturn;
    }

    public Types H(){
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
            return Types.TypeError;
          //  return printError("Ожидался идентификатор");
        if((t = scaner.scaner(lexeme)) != Types.TypeOpenParenthesis)
            return Types.TypeError;

        //  return printError("Ожидалось (");

        F1();

        if((t = scaner.scaner(lexeme)) != Types.TypeCloseParenthesis)
            return Types.TypeError;

        //   return printError("Ожидалось )");

        return Types.TypeForReturn;
    }

   */
/* public Types F1(){
        Types t;
        do{
            V();
        }while((t = scaner.scaner(lexeme)) == Types.TypeComma);
        return Types.TypeForReturn;
    }*//*


    public Types V(){
        Types t;
        int start = scaner.getCurrentIndexPosition();
        if(scaner.scaner(lexeme) == Types.TypeNegation)
            V();
        else {
            scaner.setCurrentIndexPosition(start);
            if ((t = scaner.scaner(lexeme)) == Types.TypeOr || t == Types.TypeAnd)
                return A2();
            else
                scaner.setCurrentIndexPosition(start);
        }


        return A2();
    }

    public Types U() {
        Types t;
        int startPosition = 0;
        if ((t = scaner.scaner(lexeme)) == Types.TypeIf)
            return printError("Ожидалось if");

        if ((t = scaner.scaner(lexeme)) != Types.TypeOpenParenthesis)
            return printError("Ожидалось (");
         V();

        if ((t = scaner.scaner(lexeme)) != Types.TypeCloseParenthesis)
            return printError("Ожидалось )");

        O1();
        startPosition = scaner.getCurrentIndexPosition();

        if(scaner.scaner(lexeme) != Types.TypeElse) {
            scaner.setCurrentIndexPosition(startPosition);
            return Types.TypeForReturn;
        }
        else
           return O();
    }


    public Types A2(){
        A3();
        Types t;
        int startPosition = scaner.getCurrentIndexPosition();
        while((t = scaner.scaner(lexeme)) == Types.TypeLe || t == Types.TypeGe || t == Types.TypeGt || t == Types.TypeLt){
            startPosition = scaner.getCurrentIndexPosition();
            if(A3() == Types.TypeError)
                return printError("Ожидалось выражение");
            startPosition = scaner.getCurrentIndexPosition();
        }

       // scaner.setCurrentIndexPosition(startPosition);
        if ((t != Types.TypeSemicolon))
            return Types.TypeForReturn;
        return Types.TypeForReturn;
    }

    public Types A3(){
        A4();
        Types t;
        int startPosition = scaner.getCurrentIndexPosition();

        while((t = scaner.scaner(lexeme)) == Types.TypePlus || t == Types.TypeMinus){
            startPosition = scaner.getCurrentIndexPosition();
            if(A4() == Types.TypeError)
                return printError("Ожидалось выражение");
            startPosition = scaner.getCurrentIndexPosition();
        }
        scaner.setCurrentIndexPosition(startPosition);

        return Types.TypeForReturn;
    }

    public Types A4(){
        A5();
        Types t;
        int startPosition = scaner.getCurrentIndexPosition();

        while((t = scaner.scaner(lexeme)) == Types.TypeMultiply ||
                t == Types.TypeDivision || t == Types.TypeMod) {
            startPosition = scaner.getCurrentIndexPosition();
            if (A5() == Types.TypeError)
                return printError("Ожидалось выражение");
            startPosition = scaner.getCurrentIndexPosition();
        }
        scaner.setCurrentIndexPosition(startPosition);
        return  Types.TypeForReturn;
    }

    public Types A5() {
        Types t;
        int start = scaner.getCurrentIndexPosition();
        if((t = scaner.scaner(lexeme)) == Types.TypePlusPlus || t == Types.TypeMinusMinus)
            if(A6() == Types.TypeError)
                return printError("Ожидалось выражение");
            scaner.setCurrentIndexPosition(start);
        return A6();
    }

    public Types A6() {
        Types t;
        A7();
        int start = scaner.getCurrentIndexPosition();

        while ((t = scaner.scaner(lexeme)) == Types.TypePlusPlus || t == Types.TypeMinusMinus){
            */
/*if ()
                return printError("Ожидалось выражение");
            else
                scaner.setCurrentIndexPosition(start);*//*

    }
    scaner.setCurrentIndexPosition(start);
       */
/* if ((t == Types.TypeSemicolon))
            return Types.TypeForReturn;*//*

        return Types.TypeForReturn;
    }

    public Types A7(){
        Types t;
        int startPosition = scaner.getCurrentIndexPosition();
        if((t = scaner.scaner(lexeme)) == Types.TypeIdent)
            return t;
        else {
            if (t == Types.TypeConstInt || t == Types.TypeBoolean || t == Types.TypeInt)
                return t;
            else if (t == Types.TypeOpenParenthesis) {
                scaner.setCurrentIndexPosition(startPosition);
                V();
            } else
                return printError("Ожидалось: идентификатор/константа/(");
        }
        if((t = scaner.scaner(lexeme)) != Types.TypeCloseParenthesis)
            return printError("Ожидалось )");
        return t;
    }





    private Types printError(String messege){
        scaner.printError(messege, lexeme, scaner.getCurrentItem());
        return Types.TypeError;
    }
}*/

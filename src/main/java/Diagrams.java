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

        if((t = scaner.scaner(lexeme)) != Types.Type_public)
            return printError("Ожидалось 'public'");

        if((t = scaner.scaner(lexeme)) != Types.Type_class)
            return printError("Ожидалось 'class'");

        if((t = scaner.scaner(lexeme)) != Types.Type_Main && t != Types.Type_ident)
          return printError("Ожидалось Main");

        if((t = scaner.scaner(lexeme)) != Types.Type_openBrace)
          return printError("Ожидалось {");


        if(C() == Types.Type_error)
            return Types.Type_error;

        if((t = scaner.scaner(lexeme)) != Types.Type_closeBrace)
          return  printError("Ожидалось }");


        return Types.Type_forReturn;
    }

    public Types C(){
        Types t = null;

        int startPosition, currentPosition = -1;
        do {
            startPosition = scaner.getCurrentIndexPosition();
                if((t = scaner.scaner(lexeme)) == Types.Type_ident || t == Types.Type_int || (t) == Types.Type_boolean ||t == Types.Type_void) {
                    startPosition = scaner.getCurrentIndexPosition();
                    if((t = scaner.scaner(lexeme)) == Types.Type_ident || t == Types.Type_main) {
                        scaner.setCurrentIndexPosition(startPosition);
                        t = M();
                    }
                }
                else {
                    scaner.setCurrentIndexPosition(startPosition);
                    t = Types.Type_error;
                }

                if(t == Types.Type_error) {
                    scaner.setCurrentIndexPosition(startPosition);

                    t = I();
                    currentPosition = scaner.getCurrentIndexPosition();
                }
                if(t == Types.Type_error) {
                    scaner.setCurrentIndexPosition(startPosition);
                    if ((t = scaner.scaner(lexeme)) !=Types.Type_public)
                        return printError("Ожидалось 'public'");
                    else {
                        scaner.setCurrentIndexPosition(startPosition);
                        t = S();
                    }

                }
                else
                    scaner.setCurrentIndexPosition(currentPosition);
            }while(t == Types.Type_forReturn);

     *//*
*/
/*   if((t = scaner.scaner(lexeme)) != Types.Type_closeBrace)
            return printError("Ожидалось '}'");*//*
*/
/*
        if(t == Types.Type_comma)
            return printError("Ожидалось ';'");
        if(t == Types.Type_error)
            return Types.Type_error;

        return Types.Type_forReturn;

    }*//*



   */
/* public Types M() {
        Types t;

        if (R() == Types.Type_forReturn) {
            if ((t = scaner.scaner(lexeme)) != Types.Type_ident && t != Types.Type_main)
                return Types.Type_error;
               // return printError("Ожидалось идентификатор или main");
            if (scaner.scaner(lexeme) != Types.Type_openParenthesis)
                return Types.Type_error;
              //  return printError("Ожидалось (");

            if (F() == Types.Type_error)
                return Types.Type_error;

            if (scaner.scaner(lexeme) != Types.Type_closeParenthesis)
                return printError("Ожидалось )");

            if (B() == Types.Type_error)
                return Types.Type_error;
        }
        else
            return Types.Type_error;
        return Types.Type_forReturn;
    }*//*


   */
/* public Types R(){
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.Type_void &&
            t != Types.Type_ident && t != Types.Type_boolean &&
            t != Types.Type_int)
         //   return printError("Ожидался тип возвращаемого значения или идентификатор");
            return Types.Type_error;
        return Types.Type_forReturn;
    }*//*


    public Types F(){
        Types t;
        int startPosition = 0;
        do{
            if((t = scaner.scaner(lexeme)) != Types.Type_boolean &&
                    t != Types.Type_int && t != Types.Type_ident)
                return printError("Ожидался тип или идентификатор");
            else
                if((t =scaner.scaner(lexeme)) != Types.Type_ident)
                    return Types.Type_error;
                  //  return printError("Ожидался идентификатор");
            startPosition = scaner.getCurrentIndexPosition();
            t = scaner.scaner(lexeme);
        }while(t == Types.Type_comma);
        scaner.setCurrentIndexPosition(startPosition);

        return Types.Type_forReturn;
    }

   */
/* public Types B(){
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.Type_openBrace)
            return printError("Ожидалось {");

        O();

        if((t = scaner.scaner(lexeme)) != Types.Type_closeBrace)
            return printError("Ожидалось }");

        return null;
    }*//*


    public Types O(){
        Types t = null;
        int startPosition = scaner.getCurrentIndexPosition();

        while(t != Types.Type_error && t != Types.Type_forReturn) {
            startPosition = scaner.getCurrentIndexPosition();
            if ((t = scaner.scaner(lexeme)) == Types.Type_ident || t != Types.Type_boolean || t != Types.Type_int) {
                scaner.setCurrentIndexPosition(startPosition);
                    t = V1();
            } else
                if (t == Types.Type_public && t == Types.Type_class) {
                    scaner.setCurrentIndexPosition(startPosition);
                    t = S();
                }
                else
                    t = O1();
        }

        if(t == Types.Type_error)
            return Types.Type_error;



        return Types.Type_forReturn;
    }

   */
/* public Types V1(){
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.Type_ident && t != Types.Type_boolean && t != Types.Type_int)
            return printError("Ожидался тип");
        if(I() == Types.Type_error)
            return Types.Type_error;

        if(scaner.scaner(lexeme) != Types.Type_Semicolon)
            return printError("Ожидалась ;");

        return Types.Type_forReturn;
    }*//*


    */
/*public Types I(){
        Types t = null;
        int startPosition  = -1, count = 0;
         do{
            if((t = scaner.scaner(lexeme)) != Types.Type_ident)
                return Types.Type_error;
               // return printError("Ожидался идентификатор");
             startPosition = scaner.getCurrentIndexPosition();
             if((t = scaner.scaner(lexeme)) == Types.Type_assign) {
                 if (V() == Types.Type_error)
                     return Types.Type_error;
                  count++;             //   startPosition = scaner.getCurrentIndexPosition();
             }else
                 scaner.setCurrentIndexPosition(startPosition);
             startPosition = scaner.getCurrentIndexPosition();
         }while((t = scaner.scaner(lexeme)) == Types.Type_comma);
        if(t != Types.Type_Semicolon && count == 0)
             return Types.Type_error;
         if(t != Types.Type_Semicolon)
            scaner.setCurrentIndexPosition(startPosition);

        return Types.Type_forReturn;
    }*//*


    public Types O1(){
        Types t = null;
        int startPosition = scaner.getCurrentIndexPosition();
        if((t = scaner.scaner(lexeme)) == Types.Type_if) {
            if ((t = scaner.scaner(lexeme)) != Types.Type_openParenthesis)
                return printError("Ожидалось (");
            else {
                scaner.setCurrentIndexPosition(startPosition);
                return U();
            }
        }else
            if(t == Types.Type_ident){
                if((t = scaner.scaner(lexeme)) == Types.Type_dot) {
                    scaner.setCurrentIndexPosition(startPosition);
                    return N2();
                }
                else
                    if(t == Types.Type_openBrace){
                        scaner.setCurrentIndexPosition(startPosition);
                        return H();
                    }
            }
            else
                if(t == Types.Type_openBrace){
                    scaner.setCurrentIndexPosition(startPosition);
                    return B();
                }

        return t;
    }

    public Types N2(){
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.Type_ident)
            return Types.Type_error;
          //  return printError("Ожидался идентификатор");
        while((t = scaner.scaner(lexeme))== Types.Type_dot ){
            t = scaner.scaner(lexeme);
            if(t != Types.Type_ident)
                return printError("Ожидался идентификатор");
        }

        return Types.Type_forReturn;
    }

    public Types H(){
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.Type_ident)
            return Types.Type_error;
          //  return printError("Ожидался идентификатор");
        if((t = scaner.scaner(lexeme)) != Types.Type_openParenthesis)
            return Types.Type_error;

        //  return printError("Ожидалось (");

        F1();

        if((t = scaner.scaner(lexeme)) != Types.Type_closeParenthesis)
            return Types.Type_error;

        //   return printError("Ожидалось )");

        return Types.Type_forReturn;
    }

   */
/* public Types F1(){
        Types t;
        do{
            V();
        }while((t = scaner.scaner(lexeme)) == Types.Type_comma);
        return Types.Type_forReturn;
    }*//*


    public Types V(){
        Types t;
        int start = scaner.getCurrentIndexPosition();
        if(scaner.scaner(lexeme) == Types.Type_Negation)
            V();
        else {
            scaner.setCurrentIndexPosition(start);
            if ((t = scaner.scaner(lexeme)) == Types.Type_or || t == Types.Type_and)
                return A2();
            else
                scaner.setCurrentIndexPosition(start);
        }


        return A2();
    }

    public Types U() {
        Types t;
        int startPosition = 0;
        if ((t = scaner.scaner(lexeme)) == Types.Type_if)
            return printError("Ожидалось if");

        if ((t = scaner.scaner(lexeme)) != Types.Type_openParenthesis)
            return printError("Ожидалось (");
         V();

        if ((t = scaner.scaner(lexeme)) != Types.Type_closeParenthesis)
            return printError("Ожидалось )");

        O1();
        startPosition = scaner.getCurrentIndexPosition();

        if(scaner.scaner(lexeme) != Types.Type_else) {
            scaner.setCurrentIndexPosition(startPosition);
            return Types.Type_forReturn;
        }
        else
           return O();
    }


    public Types A2(){
        A3();
        Types t;
        int startPosition = scaner.getCurrentIndexPosition();
        while((t = scaner.scaner(lexeme)) == Types.Type_le || t == Types.Type_ge || t == Types.Type_gt || t == Types.Type_lt){
            startPosition = scaner.getCurrentIndexPosition();
            if(A3() == Types.Type_error)
                return printError("Ожидалось выражение");
            startPosition = scaner.getCurrentIndexPosition();
        }

       // scaner.setCurrentIndexPosition(startPosition);
        if ((t != Types.Type_Semicolon))
            return Types.Type_forReturn;
        return Types.Type_forReturn;
    }

    public Types A3(){
        A4();
        Types t;
        int startPosition = scaner.getCurrentIndexPosition();

        while((t = scaner.scaner(lexeme)) == Types.Type_Plus || t == Types.Type_Minus){
            startPosition = scaner.getCurrentIndexPosition();
            if(A4() == Types.Type_error)
                return printError("Ожидалось выражение");
            startPosition = scaner.getCurrentIndexPosition();
        }
        scaner.setCurrentIndexPosition(startPosition);

        return Types.Type_forReturn;
    }

    public Types A4(){
        A5();
        Types t;
        int startPosition = scaner.getCurrentIndexPosition();

        while((t = scaner.scaner(lexeme)) == Types.Type_Multiply ||
                t == Types.Type_division || t == Types.Type_mod) {
            startPosition = scaner.getCurrentIndexPosition();
            if (A5() == Types.Type_error)
                return printError("Ожидалось выражение");
            startPosition = scaner.getCurrentIndexPosition();
        }
        scaner.setCurrentIndexPosition(startPosition);
        return  Types.Type_forReturn;
    }

    public Types A5() {
        Types t;
        int start = scaner.getCurrentIndexPosition();
        if((t = scaner.scaner(lexeme)) == Types.Type_PlusPlus || t == Types.Type_MinusMinus)
            if(A6() == Types.Type_error)
                return printError("Ожидалось выражение");
            scaner.setCurrentIndexPosition(start);
        return A6();
    }

    public Types A6() {
        Types t;
        A7();
        int start = scaner.getCurrentIndexPosition();

        while ((t = scaner.scaner(lexeme)) == Types.Type_PlusPlus || t == Types.Type_MinusMinus){
            */
/*if ()
                return printError("Ожидалось выражение");
            else
                scaner.setCurrentIndexPosition(start);*//*

    }
    scaner.setCurrentIndexPosition(start);
       */
/* if ((t == Types.Type_Semicolon))
            return Types.Type_forReturn;*//*

        return Types.Type_forReturn;
    }

    public Types A7(){
        Types t;
        int startPosition = scaner.getCurrentIndexPosition();
        if((t = scaner.scaner(lexeme)) == Types.Type_ident)
            return t;
        else {
            if (t == Types.Type_ConstInt || t == Types.Type_boolean || t == Types.Type_int)
                return t;
            else if (t == Types.Type_openParenthesis) {
                scaner.setCurrentIndexPosition(startPosition);
                V();
            } else
                return printError("Ожидалось: идентификатор/константа/(");
        }
        if((t = scaner.scaner(lexeme)) != Types.Type_closeParenthesis)
            return printError("Ожидалось )");
        return t;
    }





    private Types printError(String messege){
        scaner.printError(messege, lexeme, scaner.getCurrentItem());
        return Types.Type_error;
    }
}*/

public class Diagram{

    private Scaner scaner;
    private StringBuilder lexeme;
    private int position;
    private int row;

    public Diagram(Scaner scaner, StringBuilder lexeme){
        this.scaner = scaner;
        this.lexeme = lexeme;
    }

    private Types printError(String messege){
        scaner.printError(messege, lexeme, scaner.getCurrentItem());
        return Types.Type_error;
    }

    private void setPositionAndLine(int position, int numberOfRow){
        scaner.setCurrentIndexPosition(position);
        scaner.setNumberOfRow(row);
    }
    private void getPositionAndLine(){
        position = scaner.getCurrentIndexPosition();
        row = scaner.getNumberOfRow();
    }


    // public class Main{ [C]}
    public Types S() throws DiagramsException{
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.Type_public)
            throw new DiagramsException("Ожидалось 'public", lexeme, scaner);

        if((t = scaner.scaner(lexeme)) != Types.Type_class)
            return printError("Ожидалось 'class'");

        if((t = scaner.scaner(lexeme)) != Types.Type_Main && t != Types.Type_ident)
            return printError("Ожидалось Main");

        if((t = scaner.scaner(lexeme)) != Types.Type_openBrace)
           throw new DiagramsException("Ожидалось {", lexeme, scaner);


        if((t = C()) == Types.Type_error)
            throw new DiagramsException("Ошибка: ", lexeme, scaner);

        if((t = scaner.scaner(lexeme)) != Types.Type_closeBrace)
            throw new DiagramsException("Ожидалось }", lexeme, scaner);


        return Types.Type_forReturn;
    }

    public Types C() throws DiagramsException{
        Types t = null;
        do {
            getPositionAndLine();

            if((t = scaner.scaner(lexeme)) == Types.Type_ident || t == Types.Type_int || (t) == Types.Type_boolean ||t == Types.Type_void) {
                if((t = scaner.scaner(lexeme)) == Types.Type_ident || t == Types.Type_main)
                    if((t = scaner.scaner(lexeme)) == Types.Type_openParenthesis) {
                        setPositionAndLine(position, row);
                        t = M();
                        setPositionAndLine(position, row);
                        continue;
                    }
            }

            setPositionAndLine(position, row);
            if((t = scaner.scaner(lexeme)) == Types.Type_ident || t == Types.Type_int || (t) == Types.Type_boolean)
                position = scaner.getCurrentIndexPosition();
                if((t = scaner.scaner(lexeme)) == Types.Type_ident ) {
                    if ((t = scaner.scaner(lexeme)) == Types.Type_comma || t == Types.Type_Semicolon || t == Types.Type_assign) {
                        setPositionAndLine(position, row);
                        t = I();
                        continue;
                    }
                }
                setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) ==Types.Type_public){
                setPositionAndLine(position, row);
                t = S();
                continue;
            }
        }while(t == Types.Type_forReturn);

        if(t == Types.Type_comma)
            throw new DiagramsException("Ожидалось ','", lexeme, scaner);
        if(t == Types.Type_error)
            throw new DiagramsException("Ошибка: ", lexeme, scaner);
        if(t == Types.Type_closeBrace )
            scaner.setCurrentIndexPosition(position);

        return Types.Type_forReturn;

    }

    public Types M() throws DiagramsException {
        Types t;

         R();
            if ((t = scaner.scaner(lexeme)) != Types.Type_ident && t != Types.Type_main)
                throw new DiagramsException("Ожидалось идентификатор или main", lexeme, scaner);
            if (scaner.scaner(lexeme) != Types.Type_openParenthesis)
                throw new DiagramsException("Ожидалось (", lexeme, scaner);

            F();

            if (scaner.scaner(lexeme) != Types.Type_closeParenthesis)
                throw new DiagramsException("Ожидалось )", lexeme, scaner);

            B();
            return Types.Type_forReturn;
    }

    public void R() throws DiagramsException{
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.Type_void &&
                t != Types.Type_ident && t != Types.Type_boolean &&
                t != Types.Type_int)
            throw new DiagramsException("Ожидался тип или идентификатор", lexeme, scaner);
    }

    public void F() throws DiagramsException{
        Types t;
        do{
            P();
            getPositionAndLine();
        }while((t = scaner.scaner(lexeme))== Types.Type_comma);
        setPositionAndLine(position, row);
    }

    public void P() throws DiagramsException{
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.Type_ident && t != Types.Type_boolean &&
                t != Types.Type_int)
            throw new DiagramsException("Ожидался тип", lexeme, scaner);
        if((t = scaner.scaner(lexeme)) != Types.Type_ident)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);

    }

    public Types B() throws DiagramsException{
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.Type_openBrace)
            throw new DiagramsException("Ожидалось {", lexeme, scaner);

        O();

        if((t = scaner.scaner(lexeme)) != Types.Type_closeBrace)
            throw new DiagramsException("Ожидалось }", lexeme, scaner);

        return Types.Type_forReturn;
    }

    public Types O() throws DiagramsException {
        Types t;
        int stop = 0;

        while (true) {
            getPositionAndLine();
            t = scaner.scaner(lexeme);
            if ((t == Types.Type_ident || t == Types.Type_boolean || t == Types.Type_int) && (t = scaner.scaner(lexeme)) != Types.Type_dot) {
                setPositionAndLine(position, row);
                t = V1();
                continue;
            }
            setPositionAndLine(position, row);
            if (t == Types.Type_public && t == Types.Type_class) {
                setPositionAndLine(position, row);
                t = S();
                continue;
            } else {//O1()
                setPositionAndLine(position, row);
                if ((t = scaner.scaner(lexeme)) == Types.Type_if) {
                    setPositionAndLine(position, row);
                    O1();
                } else if (t == Types.Type_ident)
                    if ((t = scaner.scaner(lexeme)) == Types.Type_dot) {
                        setPositionAndLine(position, row);
                        O1();
                    } else {
                        setPositionAndLine(position, row);
                        if ((t = scaner.scaner(lexeme)) == Types.Type_ident && t == Types.Type_openBrace) {
                            setPositionAndLine(position, row);
                            O1();
                        }
                    } else if (t == Types.Type_openBrace) {
                        setPositionAndLine(position, row);
                        O1();
                    }
                setPositionAndLine(position, row);
                if ((t = scaner.scaner(lexeme))== Types.Type_Semicolon)
                    return Types.Type_forReturn;
                return Types.Type_forReturn;
            }
        }
    }

    public Types V1() throws DiagramsException{
        Types t;


        if((t = scaner.scaner(lexeme)) != Types.Type_ident && t != Types.Type_boolean && t != Types.Type_int)
            throw new DiagramsException("Ожидался тип", lexeme, scaner);
        if(I() == Types.Type_error)
            throw new DiagramsException("Ожидался список идентификаторов", lexeme, scaner);

        return Types.Type_forReturn;
    }

    public Types I() throws DiagramsException{
        Types t = null;
        int count = 0;
        do{

            if((t = scaner.scaner(lexeme)) != Types.Type_ident)
             return printError("Ожидался идентификатор");

            getPositionAndLine();
            if((t = scaner.scaner(lexeme)) == Types.Type_assign) {
                if (V() == Types.Type_error)
                    return Types.Type_error;
                count++;
            }else
                setPositionAndLine(position , row);

            getPositionAndLine();
        }while((t = scaner.scaner(lexeme)) == Types.Type_comma);
        if(t != Types.Type_Semicolon && count == 0)
            throw new DiagramsException("Ожидалось ';'", lexeme, scaner);
        if(t != Types.Type_Semicolon)
            setPositionAndLine(position , row);

        return Types.Type_forReturn;
    }


    public Types O1() throws DiagramsException{
        Types t = null;
        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) == Types.Type_if) {
            if ((t = scaner.scaner(lexeme)) != Types.Type_openParenthesis)
                throw new DiagramsException("Ожидалось (", lexeme, scaner);
            else {
                setPositionAndLine(position, row);
                return U();
            }
        }
        if(t == Types.Type_ident) {
            if ((t = scaner.scaner(lexeme)) == Types.Type_dot) {
                setPositionAndLine(position, row);
                if (N2() == Types.Type_error)
                    throw new DiagramsException("Ожидался сложный идентификатор", lexeme, scaner);

            }
            return t = V();
        }
        if(t == Types.Type_return)
            return Types.Type_forReturn;
        if(t == Types.Type_Semicolon)
            return Types.Type_forReturn;

        if((t =  scaner.scaner(lexeme)) == Types.Type_ident && t == Types.Type_openBrace)
            return H();

        setPositionAndLine(position, row);
        if(t == Types.Type_openBrace){
            setPositionAndLine(position, row);
            return B();
        }


        return Types.Type_forReturn;
    }

    public Types N2() throws DiagramsException{
        Types t;
        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) != Types.Type_ident)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);
        while((t = scaner.scaner(lexeme))== Types.Type_dot ){
            t = scaner.scaner(lexeme);
            if(t != Types.Type_ident)
                throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);
            getPositionAndLine();
        }
        if(t != Types.Type_assign)
            setPositionAndLine(position, row);
        return Types.Type_forReturn;
    }

    public Types H() throws DiagramsException{
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.Type_ident)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);
        if((t = scaner.scaner(lexeme)) != Types.Type_openParenthesis)
            throw new DiagramsException("Ожидалось (", lexeme, scaner);

        if(F1() == Types.Type_error)
            throw new DiagramsException("Ошибка: ", lexeme, scaner);

        if((t = scaner.scaner(lexeme)) != Types.Type_closeParenthesis)
            throw new DiagramsException("Ожидалось )", lexeme, scaner);

        return Types.Type_forReturn;
    }

    public Types F1() throws DiagramsException{
        Types t;
        do{
            V();
            getPositionAndLine();
        }while((t = scaner.scaner(lexeme)) == Types.Type_comma);
        setPositionAndLine(position, row);
        return Types.Type_forReturn;
    }

    public Types U() throws DiagramsException{
        Types t;
        if ((t = scaner.scaner(lexeme)) != Types.Type_if)
            throw new DiagramsException("Ожидалось if", lexeme, scaner);

        if ((t = scaner.scaner(lexeme)) != Types.Type_openParenthesis)
           throw new DiagramsException("Ожидалось (", lexeme, scaner);
        V();

        if ((t = scaner.scaner(lexeme)) != Types.Type_closeParenthesis)
            throw new DiagramsException("Ожидалось )", lexeme, scaner);

        O1();

        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) != Types.Type_else && t == Types.Type_Semicolon) {
            setPositionAndLine(position, row);
            return Types.Type_forReturn;
        }
        else {
            getPositionAndLine();
            if (t == Types.Type_closeBrace && scaner.scaner(lexeme) == Types.Type_else)
                return O();
            else setPositionAndLine(position,row);

        }

        return Types.Type_forReturn;
    }

    public Types V() throws DiagramsException{
        Types t;
        getPositionAndLine();
        if(scaner.scaner(lexeme) == Types.Type_Negation)
           return V();
        else {
            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.Type_or || t == Types.Type_and)
                return A2();
            else
                setPositionAndLine(position, row);
        }

        return A2();
    }

    public Types A2() throws DiagramsException{
        A3();
        Types t;
        while((t = scaner.scaner(lexeme)) == Types.Type_le || t == Types.Type_ge || t == Types.Type_gt || t == Types.Type_lt){
            if(A3() == Types.Type_error)
                throw new DiagramsException("Ожидалось выражение", lexeme, scaner);
        }

        if ((t != Types.Type_Semicolon) && t != Types.Type_closeParenthesis)
            throw new DiagramsException("Ожидалось ';' или ')'", lexeme, scaner);
        if(t == Types.Type_closeParenthesis)
            setPositionAndLine(position, row);
        return Types.Type_forReturn;
    }

    public Types A3() throws DiagramsException{
        A4();
        Types t;
        getPositionAndLine();

        while((t = scaner.scaner(lexeme)) == Types.Type_Plus || t == Types.Type_Minus){
            if(A4() == Types.Type_error)
                throw new DiagramsException("Ожидалось выражение", lexeme, scaner);
            getPositionAndLine();
        }
        setPositionAndLine(position, row);

        return Types.Type_forReturn;
    }

    public Types A4() throws DiagramsException{
        A5();
        Types t;
        getPositionAndLine();

        while((t = scaner.scaner(lexeme)) == Types.Type_Multiply ||
                t == Types.Type_division || t == Types.Type_mod) {
            getPositionAndLine();
            if (A5() == Types.Type_error)
                throw new DiagramsException("Ожидалось выражение", lexeme, scaner);
            getPositionAndLine();
        }
       setPositionAndLine(position, row);
        return  Types.Type_forReturn;
    }

    public Types A5() throws DiagramsException {
        Types t;
        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) == Types.Type_PlusPlus || t == Types.Type_MinusMinus)
            if(A6() == Types.Type_error)
                throw new DiagramsException("Ожидалось выражение", lexeme, scaner);
        setPositionAndLine(position, row);
        return A6();
    }

    public Types A6()throws DiagramsException {
        Types t;
        A7();
        getPositionAndLine();

        while ((t = scaner.scaner(lexeme)) == Types.Type_PlusPlus || t == Types.Type_MinusMinus){
        }
        setPositionAndLine(position, row);
        return Types.Type_forReturn;
    }

    public Types A7() throws DiagramsException{
        Types t;
        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) == Types.Type_ident) {
            setPositionAndLine(position, row);
            return t = N2();
        }
        else {
            if (t == Types.Type_ConstInt || t == Types.Type_true || t == Types.Type_false || t == Types.Type_int)
                return t;
            else if (t == Types.Type_openParenthesis) {
                setPositionAndLine(position, row);
                V();
            } else
                throw new DiagramsException("" +
                        "(", lexeme, scaner);
        }
        if((t = scaner.scaner(lexeme)) != Types.Type_closeParenthesis)
            throw new DiagramsException("Ожидалось )", lexeme, scaner);
        return t;
    }




}
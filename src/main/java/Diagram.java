public class Diagram{

    private Scaner scaner;
    private StringBuilder lexeme;
    private int position;
    private int row;
    private boolean parenthesis = false;
    private boolean brace = false;

    public Diagram(Scaner scaner, StringBuilder lexeme){
        this.scaner = scaner;
        this.lexeme = lexeme;
    }

    private Types printError(String messege){
        scaner.printError(messege, lexeme, scaner.getCurrentItem());
        return Types.TypeError;
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

        if((t = scaner.scaner(lexeme)) != Types.TypePublic)
            throw new DiagramsException("Ожидалось 'public", lexeme, scaner);

        if((t = scaner.scaner(lexeme)) != Types.TypeClass)
            return printError("Ожидалось 'class'");

        if((t = scaner.scaner(lexeme)) != Types.TypeMain && t != Types.TypeIdent)
            return printError("Ожидалось Main");

        if((t = scaner.scaner(lexeme)) != Types.TypeOpenBrace)
           throw new DiagramsException("Ожидалось {", lexeme, scaner);

        C();

        if((t = scaner.scaner(lexeme)) != Types.TypeCloseBrace)
            throw new DiagramsException("Ожидалось }", lexeme, scaner);

        return Types.TypeForReturn;
    }

    public void C() throws DiagramsException{
        Types t = null;
        do {
            getPositionAndLine();

            if ((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeInt || (t) == Types.TypeBoolean || t == Types.TypeVoid) {
                if ((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.Typemain)
                    if ((t = scaner.scaner(lexeme)) == Types.TypeOpenParenthesis) {
                        setPositionAndLine(position, row);
                        M();
                        setPositionAndLine(position, row);
                        continue;
                    }
            }
            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeInt || (t) == Types.TypeBoolean)
                      getPositionAndLine();
                if ((t = scaner.scaner(lexeme)) == Types.TypeIdent) {
                    if ((t = scaner.scaner(lexeme)) == Types.TypeComma || t == Types.TypeSemicolon || t == Types.TypeAssign) {
                        setPositionAndLine(position, row);
                        I();
                        continue;
                    }
                }
            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypePublic) {
                setPositionAndLine(position, row);
                S();
                continue;
            }
            break;
        }while(true);

        if(t == Types.TypeComma)
            throw new DiagramsException("Ожидалось ','", lexeme, scaner);
        if(t == Types.TypeError)
            throw new DiagramsException("Ошибка: ", lexeme, scaner);
        if(t == Types.TypeCloseBrace)
            scaner.setCurrentIndexPosition(position);
    }

    public void M() throws DiagramsException {
        Types t;

            R();
            if ((t = scaner.scaner(lexeme)) != Types.TypeIdent && t != Types.Typemain)
                throw new DiagramsException("Ожидалось идентификатор или main", lexeme, scaner);
            if (scaner.scaner(lexeme) != Types.TypeOpenParenthesis)
                throw new DiagramsException("Ожидалось (", lexeme, scaner);

            F();

            if (scaner.scaner(lexeme) != Types.TypeCloseParenthesis)
                throw new DiagramsException("Ожидалось )", lexeme, scaner);

            B();
    }

    public void R() throws DiagramsException{
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeVoid &&
                t != Types.TypeIdent && t != Types.TypeBoolean &&
                t != Types.TypeInt)
            throw new DiagramsException("Ожидался тип или идентификатор", lexeme, scaner);
    }

    public void F() throws DiagramsException{
        Types t;
        do{
            P();
            getPositionAndLine();
        }while((t = scaner.scaner(lexeme))== Types.TypeComma);
        setPositionAndLine(position, row);
    }

    public void P() throws DiagramsException{
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent && t != Types.TypeBoolean &&
                t != Types.TypeInt)
            throw new DiagramsException("Ожидался тип", lexeme, scaner);
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);

    }

    public void B() throws DiagramsException{
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.TypeOpenBrace)
            throw new DiagramsException("Ожидалось {", lexeme, scaner);

        O();

        if((t = scaner.scaner(lexeme)) != Types.TypeCloseBrace)
            throw new DiagramsException("Ожидалось }", lexeme, scaner);

    }

    public void O() throws DiagramsException {
        Types t;

        while (true) {
            getPositionAndLine();
            t = scaner.scaner(lexeme);
            if (t == Types.TypeIdent && ((t = scaner.scaner(lexeme)) == Types.TypePlusPlus || t == Types.TypeMinusMinus)) {
                setPositionAndLine(position, row);
                t = V();
                continue;
            }
            setPositionAndLine(position, row);
            if (t == Types.TypePlusPlus  || t == Types.TypeMinusMinus || t == Types.TypeNegation) {
                setPositionAndLine(position, row);
                t = V();
                continue;
            }


            setPositionAndLine(position, row);
            if ((t == Types.TypeIdent || t == Types.TypeBoolean || t == Types.TypeInt) && (t = scaner.scaner(lexeme)) != Types.TypeDot) {
                setPositionAndLine(position, row);
                t = V1();
                continue;
            }


            setPositionAndLine(position, row);
            if (t == Types.TypePublic && t == Types.TypeClass) {
                setPositionAndLine(position, row);
                t = S();
                continue;
            }

            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypeIf) {
                setPositionAndLine(position, row);
                O1();
            } else if (t == Types.TypeIdent)
                if ((t = scaner.scaner(lexeme)) == Types.TypeDot) {
                    setPositionAndLine(position, row);
                    O1();
                } else {
                    setPositionAndLine(position, row);
                    if ((t = scaner.scaner(lexeme)) == Types.TypeIdent && t == Types.TypeOpenBrace) {
                        setPositionAndLine(position, row);
                        O1();
                    }
                } else if (t == Types.TypeOpenBrace) {
                    setPositionAndLine(position, row);
                    O1();
                    } else if(t == Types.TypeReturn){
                                continue;
                            } else if(t == Types.TypeSemicolon){
                                        continue;
                                    } else { setPositionAndLine(position, row);
                                            return;
                                            }

        }
    }

    public Types V1() throws DiagramsException{
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.TypeIdent && t != Types.TypeBoolean && t != Types.TypeInt)
            throw new DiagramsException("Ожидался тип", lexeme, scaner);
        I() ;


        return Types.TypeForReturn;
    }

    public void I() throws DiagramsException{
        Types t = null;
        int count = 0;
        do{

            if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
                throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);

            getPositionAndLine();
            if((t = scaner.scaner(lexeme)) == Types.TypeAssign) {
                V();
                count++;
            }else
                setPositionAndLine(position , row);

            getPositionAndLine();
        }while((t = scaner.scaner(lexeme)) == Types.TypeComma);
        if(t != Types.TypeSemicolon && count == 0)
            throw new DiagramsException("Ожидалось ';'", lexeme, scaner);
        if(t != Types.TypeSemicolon)
            setPositionAndLine(position , row);

    }


    public void O1() throws DiagramsException{
        Types t = null;
        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) == Types.TypeIf) {
            if ((t = scaner.scaner(lexeme)) != Types.TypeOpenParenthesis)
                throw new DiagramsException("Ожидалось (", lexeme, scaner);
            else {
                setPositionAndLine(position, row);
                U();
                return;
            }
        }
        if(t == Types.TypeIdent) {
            if ((t = scaner.scaner(lexeme)) == Types.TypeDot) {
                setPositionAndLine(position, row);
                N2();
            }
            V();
            return;
        }
        if(t == Types.TypeReturn || t == Types.TypeSemicolon)
           return ;

        if((t =  scaner.scaner(lexeme)) == Types.TypeIdent && t == Types.TypeOpenBrace) {
            H();
            return;
        }

        setPositionAndLine(position, row);
        if(t == Types.TypeOpenBrace){
            setPositionAndLine(position, row);
            B();
            return;
        }

    }

    public void N2() throws DiagramsException{
        Types t;
        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);
        getPositionAndLine();
        while((t = scaner.scaner(lexeme))== Types.TypeDot){
            t = scaner.scaner(lexeme);
            if(t != Types.TypeIdent)
                throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);
            getPositionAndLine();
        }
        if(t != Types.TypeAssign)
            setPositionAndLine(position, row);
        if(t == Types.TypeError)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);

    }

    public void H() throws DiagramsException{
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);
        if((t = scaner.scaner(lexeme)) != Types.TypeOpenParenthesis)
            throw new DiagramsException("Ожидалось (", lexeme, scaner);

        F1();

        if((t = scaner.scaner(lexeme)) != Types.TypeCloseParenthesis)
            throw new DiagramsException("Ожидалось )", lexeme, scaner);
    }

    public void F1() throws DiagramsException{
        Types t;
        do{
            V();
            getPositionAndLine();
        }while((t = scaner.scaner(lexeme)) == Types.TypeComma);
        setPositionAndLine(position, row);
    }

    public void U() throws DiagramsException{
        Types t;
        if ((t = scaner.scaner(lexeme)) != Types.TypeIf)
            throw new DiagramsException("Ожидалось if", lexeme, scaner);

        if ((t = scaner.scaner(lexeme)) != Types.TypeOpenParenthesis)
           throw new DiagramsException("Ожидалось (", lexeme, scaner);
        V();

        if ((t = scaner.scaner(lexeme)) != Types.TypeCloseParenthesis)
            throw new DiagramsException("Ожидалось )", lexeme, scaner);

        O1();

        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) != Types.TypeElse && t == Types.TypeSemicolon) {
            setPositionAndLine(position, row);
            return;
        }
        if (t == Types.TypeCloseBrace && scaner.scaner(lexeme) == Types.TypeElse || t == Types.TypeElse) {
            O();
            return;
        }
        else setPositionAndLine(position,row);
    }

    public Types V() throws DiagramsException{
        Types t;
        getPositionAndLine();
        if(scaner.scaner(lexeme) == Types.TypeNegation)
           return V();
        else {
            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypeOr || t == Types.TypeAnd)
                return A2();
            else
                setPositionAndLine(position, row);
        }

        return A2();
    }

    public Types A2() throws DiagramsException{
        A3();
        Types t;
        while((t = scaner.scaner(lexeme)) == Types.TypeLe || t == Types.TypeGe || t == Types.TypeGt || t == Types.TypeLt){
            if(A3() == Types.TypeError)
                throw new DiagramsException("Ожидалось выражение", lexeme, scaner);
        }

        if ((t != Types.TypeSemicolon) && t != Types.TypeCloseParenthesis)
            throw new DiagramsException("Ожидалось ';' или ')'", lexeme, scaner);
        if(t == Types.TypeCloseParenthesis)
            setPositionAndLine(position, row);
        return Types.TypeForReturn;
    }

    public Types A3() throws DiagramsException{
        A4();
        Types t;
        getPositionAndLine();

        while((t = scaner.scaner(lexeme)) == Types.TypePlus || t == Types.TypeMinus){
            if(A4() == Types.TypeError)
                throw new DiagramsException("Ожидалось выражение", lexeme, scaner);
            getPositionAndLine();
        }
        setPositionAndLine(position, row);

        return Types.TypeForReturn;
    }

    public Types A4() throws DiagramsException{
        A5();
        Types t;
        getPositionAndLine();

        while((t = scaner.scaner(lexeme)) == Types.TypeMultiply ||
                t == Types.TypeDivision || t == Types.TypeMod) {
            getPositionAndLine();
            if (A5() == Types.TypeError)
                throw new DiagramsException("Ожидалось выражение", lexeme, scaner);
            getPositionAndLine();
        }
       setPositionAndLine(position, row);
        return  Types.TypeForReturn;
    }

    public Types A5() throws DiagramsException {
        Types t;
        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) == Types.TypePlusPlus || t == Types.TypeMinusMinus)
            return A6();
        setPositionAndLine(position, row);
        return A6();
    }

    public Types A6()throws DiagramsException {
        Types t;
        A7();
        getPositionAndLine();

        while ((t = scaner.scaner(lexeme)) == Types.TypePlusPlus || t == Types.TypeMinusMinus){
            getPositionAndLine();
        }
        setPositionAndLine(position, row);
        return Types.TypeForReturn;
    }

    public Types A7() throws DiagramsException{
        Types t;
        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) == Types.TypeIdent) {
            setPositionAndLine(position, row);
            N2();
            return Types.TypeForReturn;
        }
        else {
            if (t == Types.TypeConstInt || t == Types.TypeTrue || t == Types.TypeFalse || t == Types.TypeInt)
                return t;
            else if (t == Types.TypeOpenParenthesis) {
                setPositionAndLine(position, row);
                V();
            } else
                throw new DiagramsException("Ожидалось (/числовая константа/true/false", lexeme, scaner);
        }
        if((t = scaner.scaner(lexeme)) != Types.TypeCloseParenthesis)
            throw new DiagramsException("Ожидалось )", lexeme, scaner);
        return t;
    }




}
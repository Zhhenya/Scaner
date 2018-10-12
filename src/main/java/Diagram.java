/*
import java.util.ArrayList;
import java.util.List;

public class Diagram{

    private Scaner scaner;
    private StringBuilder lexeme;
    private int position;
    private int row;
    private boolean parenthesis = false;
    private List<Boolean> brace = new ArrayList<Boolean>();

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
    private void getPositionAndLine () {
        position = scaner.getCurrentIndexPosition();
        row = scaner.getNumberOfRow();
    }


    // public class Main{ [Content]}
    public Types S() throws DiagramsException{
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.TypePublic)
            throw new DiagramsException("Ожидалось 'public", lexeme, scaner);

        if((t = scaner.scaner(lexeme)) != Types.TypeClass)
            throw new DiagramsException("Ожидалось 'class'", lexeme, scaner);

        if((t = scaner.scaner(lexeme)) != Types.TypeMain && t != Types.TypeIdent)
            throw new DiagramsException("Ожидалось имя класса", lexeme, scaner);

        if((t = scaner.scaner(lexeme)) != Types.TypeOpenBrace)
           throw new DiagramsException("Ожидалось {", lexeme, scaner);

        Content();


        if((t = scaner.scaner(lexeme)) != Types.TypeCloseBrace)
            throw new DiagramsException("Ожидалось }", lexeme, scaner);


        return Types.TypeForReturn;
    }

    public void Content() throws DiagramsException{
        Types t = null;
        do {
            getPositionAndLine();

            if ((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeInt || (t) == Types.TypeBoolean || t == Types.TypeVoid) {
                if ((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.Typemain)
                    if ((t = scaner.scaner(lexeme)) == Types.TypeOpenParenthesis) {
                        setPositionAndLine(position, row);
                        Method();
                        setPositionAndLine(position, row);
                        continue;
                    }
            }

            setPositionAndLine(position, row);
            t = scaner.scaner(lexeme);
            if ((t == Types.TypeIdent || t == Types.TypeInt) && ((t = scaner.scaner(lexeme)) == Types.TypeOpenBrace || t == Types.TypeOpenParenthesis))
                throw new DiagramsException("Ожидался объявление класса или функции", lexeme, scaner);



            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeInt || (t) == Types.TypeBoolean)
                      getPositionAndLine();
                if ((t = scaner.scaner(lexeme)) == Types.TypeIdent) {
                    if ((t = scaner.scaner(lexeme)) == Types.TypeComma || t == Types.TypeSemicolon || t == Types.TypeAssign) {
                        setPositionAndLine(position, row);
                        ListOfIdentifiers();
                        continue;
                    }
                     else
                         if(t == Types.TypeIdent || t == Types.TypeConstInt || t == Types.TypeFalse || t == Types.TypeTrue)
                            throw new DiagramsException("Ожидалось ',' , ';', '='", lexeme, scaner);
                }
            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypePublic || t == Types.TypeClass) {
                setPositionAndLine(position, row);
                S();
                continue;
            }
            break;
        }while(true);

        if(t == Types.TypeComma)
            throw new DiagramsException("Ожидалось ';'", lexeme, scaner);
        if(t == Types.TypeError)
            throw new DiagramsException("Ошибка: ", lexeme, scaner);
        if(t == Types.TypeCloseBrace)
            scaner.setCurrentIndexPosition(position);
    }

    public void Method() throws DiagramsException {
        Types t;

            ReturnType();
            if ((t = scaner.scaner(lexeme)) != Types.TypeIdent && t != Types.Typemain)
                throw new DiagramsException("Ожидалось идентификатор или main", lexeme, scaner);
            if (scaner.scaner(lexeme) != Types.TypeOpenParenthesis)
                throw new DiagramsException("Ожидалось (", lexeme, scaner);

            FormalParameterList();

            if (scaner.scaner(lexeme) != Types.TypeCloseParenthesis)
                throw new DiagramsException("Ожидалось )", lexeme, scaner);

            Block();
    }

    public void ReturnType() throws DiagramsException{
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeVoid &&
                t != Types.TypeIdent && t != Types.TypeBoolean &&
                t != Types.TypeInt)
            throw new DiagramsException("Ожидался тип или идентификатор", lexeme, scaner);
    }

    public void FormalParameterList() throws DiagramsException{
        Types t;
        do{
            Parameter();
            getPositionAndLine();
        }while((t = scaner.scaner(lexeme))== Types.TypeComma);
        setPositionAndLine(position, row);
    }

    public void Parameter() throws DiagramsException{
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent && t != Types.TypeBoolean &&
                t != Types.TypeInt)
            throw new DiagramsException("Ожидался тип", lexeme, scaner);
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);

    }

    public void Block() throws DiagramsException{
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.TypeOpenBrace)
            throw new DiagramsException("Ожидалось {", lexeme, scaner);

        Operators();

        if((t = scaner.scaner(lexeme)) != Types.TypeCloseBrace)
            throw new DiagramsException("Ожидалось }", lexeme, scaner);

    }

    public void Operators() throws DiagramsException {
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
            if ((t = scaner.scaner(lexeme))  == Types.TypePlusPlus  || t == Types.TypeMinusMinus || t == Types.TypeNegation) {
                setPositionAndLine(position, row);
                t = V();
                continue;
            }
            setPositionAndLine(position, row);
            if (((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeConstInt)&& ((t = scaner.scaner(lexeme)) == Types.TypePlus || t == Types.TypeMinus)) {
                setPositionAndLine(position, row);
                t = V();
                continue;
            }


            setPositionAndLine(position, row);
            if ((((t = scaner.scaner(lexeme))) == Types.TypeIdent || t == Types.TypeBoolean || t == Types.TypeInt) && (t = scaner.scaner(lexeme)) != Types.TypeDot) {
                setPositionAndLine(position, row);
                t = VariableDescription();
                continue;
            }


            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypePublic || t == Types.TypeClass) {
                setPositionAndLine(position, row);
                t = S();
                continue;
            }
            */
/*setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypePublic && t != Types.TypeClass)
                throw new DiagramsException("Ожидался 'class'", lexeme, scaner);
            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) != Types.TypePublic && t == Types.TypeClass)
                throw new DiagramsException("Ожидался 'public'", lexeme, scaner);

*//*



            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypeIf) {
                setPositionAndLine(position, row);
                Operator();
            } else if (t == Types.TypeIdent)
                if ((t = scaner.scaner(lexeme)) == Types.TypeDot) {
                    setPositionAndLine(position, row);
                    Operator();
                } else {
                    setPositionAndLine(position, row);
                    if ((t = scaner.scaner(lexeme)) == Types.TypeIdent && t == Types.TypeOpenBrace) {
                        setPositionAndLine(position, row);
                        Operator();
                    }
                } else if (t == Types.TypeOpenBrace) {
                  //  setPositionAndLine(position, row);
                    brace.add(true);
                    continue;
                 //   Operator();
                } else if(t == Types.TypeReturn){
                    getPositionAndLine();
                    if((t = scaner.scaner(lexeme)) == Types.TypeSemicolon)
                        continue;
                    setPositionAndLine(position, row);
                    if(((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeConstInt || t == Types.TypeFalse || t == Types.TypeFalse)
                            && (t = scaner.scaner(lexeme)) == Types.TypeSemicolon)
                        continue;

                    if((t = scaner.scaner(lexeme)) != Types.TypeSemicolon)
                        if (t != Types.TypeIdent && t != Types.TypePlusPlus && t != Types.TypeMinusMinus && t != Types.TypeNegation &&
                                t != Types.TypeTrue && t != Types.TypeFalse && t != Types.TypeConstInt)
                            throw new DiagramsException("Ожидалсась ';' или идентификатор или выражение", lexeme, scaner);


                    setPositionAndLine(position, row);

                    continue;
                } else if(t == Types.TypeSemicolon){
                    continue;
                }else if(t == Types.TypeCloseBrace && brace.size() != 0) {
                    brace.remove(brace.size() - 1);
                    continue;
                }
                else { setPositionAndLine(position, row);
                if(brace.size() != 0)
                    throw new DiagramsException("Ожидалось '}'", lexeme, scaner);


                        return;
                        }

        }
    }

    public Types VariableDescription() throws DiagramsException{
        Types t;
        int currentPos = -1;

        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent && t != Types.TypeBoolean && t != Types.TypeInt)
            throw new DiagramsException("Ожидался тип", lexeme, scaner);
        currentPos = scaner.getCurrentIndexPosition();
        if((t = scaner.scaner(lexeme)) == Types.TypeAssign)
            setPositionAndLine(position, row);
        else
            setPositionAndLine(currentPos, row);
        ListOfIdentifiers();


        return Types.TypeForReturn;
    }

    public void ListOfIdentifiers() throws DiagramsException{
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
            throw new DiagramsException("Ожидалось ';' или ',' или '='", lexeme, scaner);
        if(t != Types.TypeSemicolon)
            setPositionAndLine(position , row);

    }


    public void Operator() throws DiagramsException {
        Types t = null;
        getPositionAndLine();
        if ((t = scaner.scaner(lexeme)) == Types.TypeIf) {
            if ((t = scaner.scaner(lexeme)) != Types.TypeOpenParenthesis)
                throw new DiagramsException("Ожидалось (", lexeme, scaner);
            else {
                setPositionAndLine(position, row);
                OperatorIF();
                return;
            }
        }
        if (t == Types.TypeIdent) {
            if ((t = scaner.scaner(lexeme)) == Types.TypeDot) {
                setPositionAndLine(position, row);
                ObjectName();
            }
            V();
            return;
        }

        if(t == Types.TypeReturn || t == Types.TypeSemicolon) {
            setPositionAndLine(position, row);
            Operators();
            return;
        }

        setPositionAndLine(position, row);
        if((t =  scaner.scaner(lexeme)) == Types.TypeIdent && t == Types.TypeOpenBrace) {
            FunctionCall();
            return;
        }

        setPositionAndLine(position, row);
        if(t == Types.TypeOpenBrace){
            setPositionAndLine(position, row);
            Block();
            return;
        }

    }

    public void ObjectName() throws DiagramsException{
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

    public void FunctionCall() throws DiagramsException{
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);
        if((t = scaner.scaner(lexeme)) != Types.TypeOpenParenthesis)
            throw new DiagramsException("Ожидалось (", lexeme, scaner);

        ActualParametrList();

        if((t = scaner.scaner(lexeme)) != Types.TypeCloseParenthesis)
            throw new DiagramsException("Ожидалось )", lexeme, scaner);
    }

    public void ActualParametrList() throws DiagramsException{
        Types t;
        do{
            V();
            getPositionAndLine();
        }while((t = scaner.scaner(lexeme)) == Types.TypeComma);
        setPositionAndLine(position, row);
    }

    public void OperatorIF() throws DiagramsException{
        Types t;
        if ((t = scaner.scaner(lexeme)) != Types.TypeIf)
            throw new DiagramsException("Ожидалось if", lexeme, scaner);

        if ((t = scaner.scaner(lexeme)) != Types.TypeOpenParenthesis)
           throw new DiagramsException("Ожидалось (", lexeme, scaner);
        V();

        if ((t = scaner.scaner(lexeme)) != Types.TypeCloseParenthesis)
            throw new DiagramsException("Ожидалось )", lexeme, scaner);

        Operator();

            getPositionAndLine();
        if((t = scaner.scaner(lexeme)) != Types.TypeElse && t == Types.TypeSemicolon) {
            setPositionAndLine(position, row);
            return;
        }
        if (t == Types.TypeCloseBrace && scaner.scaner(lexeme) == Types.TypeElse || t == Types.TypeElse) {
            Operators();
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
            ObjectName();
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




}*/

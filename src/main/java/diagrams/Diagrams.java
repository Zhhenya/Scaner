package diagrams;

import scanner.Scaner;
import service.DateType;
import service.DiagramsException;
import service.SemanticsException;
import service.Types;
import tree.Tree;

import java.util.ArrayList;
import java.util.List;

public class Diagrams {
    private Scaner scaner;
    private StringBuilder lexeme;
    private int position;
    private int row;
    private List<Boolean> brace = new ArrayList<Boolean>();
    private List<Boolean> func = new ArrayList<Boolean>();
    Tree root = new Tree();
    Tree reset;
    DateType currentType = null;
    StringBuilder currentLexeme = new StringBuilder();
    Tree currentVertex = null;
    StringBuilder nameCurrentFunction = null;
    List<DateType> typeOfV = new ArrayList<DateType>();
    boolean functionCall = false;
    String typeName = "";


    public Diagrams(Scaner scaner, StringBuilder lexeme){
        this.scaner = scaner;
        this.lexeme = lexeme;
    }

    private void setPositionAndLine(int position, int numberOfRow){
        scaner.setCurrentIndexPosition(position);
        scaner.setNumberOfRow(row);
    }
    private void getPositionAndLine () {
        position = scaner.getCurrentIndexPosition();
        row = scaner.getNumberOfRow();
    }

    public void setRoot(){
        root.setCurrent(root);
    }

    public Types S() throws DiagramsException, SemanticsException {
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.TypePublic)
            throw new DiagramsException("Ожидалось 'public", lexeme, scaner);

        if((t = scaner.scaner(lexeme)) != Types.TypeClass)
            throw new DiagramsException("Ожидалось 'class'", lexeme, scaner);

        if((t = scaner.scaner(lexeme)) != Types.TypeMain && t != Types.TypeIdent)
            throw new DiagramsException("Ожидалось имя класса", lexeme, scaner);

        /*занести идентификатор с типом в дерево*/

        Tree vertex = root.include(lexeme.toString(), DateType.TClass, null);

        if((t = scaner.scaner(lexeme)) != Types.TypeOpenBrace)
            throw new DiagramsException("Ожидалось {", lexeme, scaner);

        Content();


        /*
        * возврат
        * */
        root.setCurrent(vertex);

        if((t = scaner.scaner(lexeme)) != Types.TypeCloseBrace)
            throw new DiagramsException("Ожидалось }", lexeme, scaner);

        getPositionAndLine();

        t = scaner.scaner(lexeme);
        if(t != Types.TypeEnd)
            setPositionAndLine(position, row);

        return t;
    }


    public void Content() throws DiagramsException, SemanticsException {
        Types t = null;
        do {
            getPositionAndLine();

            if ((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeInt || (t) == Types.TypeBoolean || t == Types.TypeVoid) {
                if ((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.Typemain)
                    if ((t = scaner.scaner(lexeme)) == Types.TypeOpenParenthesis) {
                     //   if(func.size() == 0 || ! func.get(func.size() - 1)) {
                            setPositionAndLine(position, row);
                            Method();
                            // setPositionAndLine(position, row);
                            continue;
                     //   }else
                       //     throw new service.DiagramsException("Здесь не может быть объявлена функция", lexeme, scaner);
                    }
            }

            setPositionAndLine(position, row);
            t = scaner.scaner(lexeme);
            if ((t == Types.TypeIdent || t == Types.TypeInt) && ((t = scaner.scaner(lexeme)) == Types.TypeOpenBrace || t == Types.TypeOpenParenthesis))
                throw new DiagramsException("Ожидалось объявление класса или функции", lexeme, scaner);



            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeInt || (t) == Types.TypeBoolean) {
                getPositionAndLine();
                currentLexeme.delete(0, currentLexeme.length());
                currentLexeme.append(lexeme.toString());



            }
            if ((t = scaner.scaner(lexeme)) == Types.TypeIdent) {
                /*
                 *найти лексемму с таким типом  ???????(как они там появятся)
                 * */
                currentType = root.getType(currentLexeme.toString());
                if ((t = scaner.scaner(lexeme)) == Types.TypeComma || t == Types.TypeSemicolon || t == Types.TypeAssign) {
                    setPositionAndLine(position, row);

                    /*
                    * затем последовательно заносить идентификаторы(переменные)
                    * с таким типом в дерево
                    * */
                    ListOfIdentifiers();
                    continue;
                }
                else
                if(t == Types.TypeIdent || t == Types.TypeConstInt || t == Types.TypeFalse || t == Types.TypeTrue)
                    throw new DiagramsException("Ожидалось ',' , ';', '='", lexeme, scaner);
            }
            setPositionAndLine(position, row);
            if (((t = scaner.scaner(lexeme)) == Types.TypePublic || t == Types.TypeClass) && (func.size() == 0 || !func.get(func.size() - 1))) {
                setPositionAndLine(position, row);
                S();
                continue;
            } else
                if((t == Types.TypePublic || t == Types.TypeClass) && func.size() != 0 && func.get(func.size() - 1))
                    throw new DiagramsException("Здесь не может быть объявление нового класса", lexeme, scaner);


            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypeOpenBrace)
                throw new DiagramsException("Блок может объявляться только в функции", lexeme, scaner);



            break;
        }while(true);

        if(t == Types.TypeComma)
            throw new DiagramsException("Ожидалось ';'", lexeme, scaner);
        if(t == Types.TypeError)
            throw new DiagramsException("Ошибка: ", lexeme, scaner);
        if(t == Types.TypeCloseBrace)
            scaner.setCurrentIndexPosition(position);
    }

    public void Method() throws DiagramsException, SemanticsException {
        Types t;

        ReturnType();

        /*
        * Определить тип лексеммы
        * */
        currentType = root.getType(lexeme.toString());


        if ((t = scaner.scaner(lexeme)) != Types.TypeIdent && t != Types.Typemain)
            throw new DiagramsException("Ожидалось идентификатор или main", lexeme, scaner);


        /*
        * занести  в дерево идентификатор с типом
        * */
        Tree vertex = root.include(lexeme.toString(), DateType.TFunction, currentType, null);
        nameCurrentFunction = lexeme;

        if (scaner.scaner(lexeme) != Types.TypeOpenParenthesis)
            throw new DiagramsException("Ожидалось (", lexeme, scaner);

        FormalParameterList();

        if (scaner.scaner(lexeme) != Types.TypeCloseParenthesis)
            throw new DiagramsException("Ожидалось )", lexeme, scaner);

        Block();


        /*
        * восстановить значение указателя
        * */
        root.setCurrent(vertex);
    }

    public void ReturnType() throws DiagramsException {
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeVoid &&
                t != Types.TypeIdent && t != Types.TypeBoolean &&
                t != Types.TypeInt)
            throw new DiagramsException("Ожидался тип или идентификатор", lexeme, scaner);
    }

    public void FormalParameterList() throws DiagramsException, SemanticsException {
        Types t;
        int numberOfParameters = 0;
        Tree vertex = root.getCurrent().parent;
        do{
            Parameter();

            /*
            * считаем количество параметров и присваиваем
            * переменной numberOfParameters в вершину
            * */
            numberOfParameters++;
            root.setNumberOfParameters(vertex, numberOfParameters);


            getPositionAndLine();
        }while((t = scaner.scaner(lexeme))== Types.TypeComma);
        setPositionAndLine(position, row);
    }

    public void Parameter() throws DiagramsException, SemanticsException {
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent && t != Types.TypeBoolean &&
                t != Types.TypeInt)
            throw new DiagramsException("Ожидался тип", lexeme, scaner);

        /*
        * определить тип лексеммы
        * */
        currentType = root.getType(lexeme.toString());
        if(currentType == DateType.TClass) {
            currentType = DateType.TUserType;
            currentVertex = root.getClass(lexeme.toString());
        }


        if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);

        /*
        * занести идентификатор с типом в дерево
        * определить ссылку на класс
        * */
        currentVertex = root.include(lexeme.toString(), currentType, currentType == DateType.TUserType ? currentVertex.node.lexemeID : null);

        /*
        * нужно ли делать возврат
        * */


    }

    public void Block() throws DiagramsException, SemanticsException {
        Types t;

        if((t = scaner.scaner(lexeme)) != Types.TypeOpenBrace)
            throw new DiagramsException("Ожидалось {", lexeme, scaner);

        Operators();

        if((t = scaner.scaner(lexeme)) != Types.TypeCloseBrace)
            throw new DiagramsException("Ожидалось }", lexeme, scaner);

    }

    public void Operators() throws DiagramsException, SemanticsException {
        Types t;

        while (true) {
            getPositionAndLine();
            t = scaner.scaner(lexeme);
            if (t == Types.TypeIdent && ((t = scaner.scaner(lexeme)) == Types.TypePlusPlus || t == Types.TypeMinusMinus)) {
                setPositionAndLine(position, row);
                typeOfV.clear();
                V();
                continue;
            }
            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme))  == Types.TypePlusPlus  || t == Types.TypeMinusMinus || t == Types.TypeNegation) {
                setPositionAndLine(position, row);
                typeOfV.clear();
                V();
                continue;
            }
            setPositionAndLine(position, row);
            if (((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeConstInt)&& ((t = scaner.scaner(lexeme)) == Types.TypePlus || t == Types.TypeMinus)) {
                setPositionAndLine(position, row);
                typeOfV.clear();
                V();
                continue;
            }


            setPositionAndLine(position, row);
            if (((((t = scaner.scaner(lexeme))) == Types.TypeIdent || t == Types.TypeBoolean || t == Types.TypeInt)) &&
                    (t = scaner.scaner(lexeme)) != Types.TypeDot && t != Types.TypeOpenParenthesis) {
                setPositionAndLine(position, row);
                VariableDescription();
                continue;
            }


            setPositionAndLine(position, row);
            if (((t = scaner.scaner(lexeme)) == Types.TypePublic || t == Types.TypeClass) && ( func.size() == 0 || !func.get(func.size() - 1)) ){
                setPositionAndLine(position, row);
                S();
                continue;
            } else
                if(t == Types.TypePublic || t == Types.TypeClass && func.get(func.size() - 1))
                    throw new DiagramsException("Здесь не может быть объявление другого класса", lexeme, scaner);
            setPositionAndLine(position, row);
            if ((t = scaner.scaner(lexeme)) == Types.TypeIf) {
                setPositionAndLine(position, row);
                Operator();
                continue;
            } else  if (t == Types.TypeIdent) {
                    if ((t = scaner.scaner(lexeme)) == Types.TypeDot) {
                        setPositionAndLine(position, row);
                        Operator();
                        continue;
                    } else
                        if (t  == Types.TypeOpenParenthesis) {
                            setPositionAndLine(position, row);
                            Operator();
                            continue;
                        }
                }else   if (t == Types.TypeOpenBrace) {
                            setPositionAndLine(position, row);
                            Block();
                         //   brace.add(true);
                            continue;
                        }    else if(t == Types.TypeReturn){
                            getPositionAndLine();
                            if((t = scaner.scaner(lexeme)) == Types.TypeSemicolon) {

                                Tree vertex = root.getFunction();
                                if(vertex.node.returnType != DateType.TVoid)
                                    throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeID);

                                continue;
                            }

                            setPositionAndLine(position, row);
                            if(((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeConstInt || t == Types.TypeFalse || t == Types.TypeFalse)
                                    && (scaner.scaner(lexeme)) == Types.TypeSemicolon) {

                                Tree vertex = root.getFunction();


                                if(t == Types.TypeConstInt || t == Types.TypeInt)
                                    if(vertex.node.returnType != DateType.TConstant && vertex.node.returnType != DateType.TInt)
                                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeID);

                                if(t == Types.TypeFalse || t == Types.TypeTrue)
                                    if(vertex.node.returnType != DateType.TBoolean)
                                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeID);

                                if(t == Types.TypeIdent) {
                                    setPositionAndLine(position, row);
                                    scaner.scaner(lexeme);
                                    Tree returnValue = root.getTypeOfLexeme(lexeme.toString());
                                    scaner.scaner(lexeme);
                                    if (returnValue.node.type != vertex.node.returnType)
                                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeID);
                                }
                                continue;
                            }


                            if((t = scaner.scaner(lexeme)) != Types.TypeSemicolon) {
                                if (t == Types.TypeIdent || t == Types.TypePlusPlus || t == Types.TypeMinusMinus || t == Types.TypeNegation || t == Types.TypeConstInt) {
                                    setPositionAndLine(position, row);
                                    typeOfV.clear();
                                    V();

                                    /*
                                     * проверить тип возвращаемого значения (выражения)
                                     * */
                                    if (currentType != currentVertex.node.returnType)
                                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + currentVertex.node.lexemeID);
                                    return;
                                }

                                if(t == Types.TypeTrue || t == Types.TypeFalse)
                                    if(currentType != DateType.TBoolean)
                                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + currentVertex.node.lexemeID);

                            }


                            if((t = scaner.scaner(lexeme)) != Types.TypeSemicolon)
                                if (t != Types.TypeIdent && t != Types.TypePlusPlus && t != Types.TypeMinusMinus && t != Types.TypeNegation &&
                                        t != Types.TypeTrue && t != Types.TypeFalse && t != Types.TypeConstInt)
                                    throw new DiagramsException("Ожидалсась ';' или идентификатор или выражение", lexeme, scaner);


                            setPositionAndLine(position, row);
                                     /*  setPositionAndLine(position, row);
                                       Operator();*/
                                       continue;
                        }   else if(t == Types.TypeSemicolon)
                                    continue;
                            else if(t == Types.TypeCloseBrace && brace.size() != 0) {
                                    brace.remove(brace.size() - 1);
                                    continue;
                                }
                            else {
                                setPositionAndLine(position, row);
                                return;
                            }
                    }
    }

    public void VariableDescription() throws DiagramsException, SemanticsException {
        Types t;
        int currentPos = -1;

        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent && t != Types.TypeBoolean && t != Types.TypeInt)
            throw new DiagramsException("Ожидался тип переменной", lexeme, scaner);


        /*
        * определить тип переменной
        * */
        currentType = root.getType(lexeme.toString());
        if(currentType == DateType.TClass) {
            currentVertex = root.getClass(lexeme.toString());
            currentType = DateType.TUserType;
        }

        currentPos = scaner.getCurrentIndexPosition();
        if((t = scaner.scaner(lexeme)) == Types.TypeAssign)
            setPositionAndLine(position, row);
        else
            setPositionAndLine(currentPos, row);
        ListOfIdentifiers();
    }

    public void ListOfIdentifiers() throws DiagramsException, SemanticsException {
        Types t = null;
        String typeIdent;
        Tree vertex = null;
        int count = 0;
        do{

            if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
                throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);

            /*
            * заносим идентификатор с типом currentType в дерево
            * */
            vertex = root.include(lexeme.toString(), currentType, currentType == DateType.TUserType ? currentVertex.node.lexemeID : null);
            typeIdent = currentType.toString();

            getPositionAndLine();
            if((t = scaner.scaner(lexeme)) == Types.TypeAssign) {
                typeOfV.clear();
                V();


                /*
                * в V() вычисляется новый currentType (тип выражения),
                * затем сравнивается с типом объявленной переменной(идентификатора)
                * */

                if(typeIdent != currentType.toString())
                    throw new SemanticsException("Тип переменной и тип присваемого значения не совпадают");


                count++;
            }else
                setPositionAndLine(position , row);

            getPositionAndLine();
        }while((t = scaner.scaner(lexeme)) == Types.TypeComma);



        /*
        * здесь нужно вернуться
        * */
        root.setCurrent(vertex);

        if(t != Types.TypeSemicolon && count == 0)
            throw new DiagramsException("Ожидалось ';' или ',' или '='", lexeme, scaner);
        if(t != Types.TypeSemicolon)
            setPositionAndLine(position , row);

    }

    public void Operator() throws DiagramsException, SemanticsException {
        Types t = null, nextT = null;
        getPositionAndLine();
        if ((t = scaner.scaner(lexeme)) == Types.TypeIf) {
            /*if ((t = scaner.scaner(lexeme)) != service.Types.TypeOpenParenthesis)
                throw new service.DiagramsException("Ожидалось (", lexeme, scaner);
            else {*/
                setPositionAndLine(position, row);
                OperatorIF();
                return;
          //  }
        }


        setPositionAndLine(position, row);
        if((t =  scaner.scaner(lexeme)) == Types.TypeIdent && (t =  scaner.scaner(lexeme)) == Types.TypeOpenParenthesis) {
            setPositionAndLine(position, row);
            FunctionCall();
            return;
        }

        setPositionAndLine(position, row);
        if ((t =  scaner.scaner(lexeme)) == Types.TypeIdent) {
            if ((t = scaner.scaner(lexeme)) == Types.TypeDot) {
                setPositionAndLine(position, row);
                ObjectName();
                if(functionCall){
                    functionCall = false;
                    return;
                }
            } else
                setPositionAndLine(position, row);
            typeOfV.clear();
            V();

            /*
            * проверить, что тип идентификатора и
            * тип выражения совпадают
            * */

            if(currentType != currentVertex.node.type)
                throw new SemanticsException("Тип присваиваемого значения не совпадает с типом переменной");

            return;
        }

       /* if(t == service.Types.TypeReturn || t == service.Types.TypeSemicolon) {
            setPositionAndLine(position, row);
            Operators();
            return;
        }*/

       if(t == Types.TypeReturn){
           getPositionAndLine();
           if((t = scaner.scaner(lexeme)) == Types.TypeSemicolon) {
               Tree vertex = root.getFunction();
               if(vertex.node.returnType!= DateType.TVoid)
                   throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeID);
               return;
           }

           setPositionAndLine(position, row);
           if(((t = scaner.scaner(lexeme)) == Types.TypeIdent || t == Types.TypeConstInt || t == Types.TypeFalse || t == Types.TypeTrue)
                   && (nextT = scaner.scaner(lexeme)) == Types.TypeSemicolon) {

               Tree vertex = root.getFunction();

                if(t == Types.TypeConstInt || t == Types.TypeInt)
                    if(vertex.node.returnType != DateType.TConstant && vertex.node.returnType != DateType.TInt)
                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeID);

               if(t == Types.TypeFalse || t == Types.TypeTrue)
                   if(vertex.node.returnType != DateType.TBoolean)
                       throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeID);


               return;
           }

           /*
           * сделать проверку типа выражение,
           * если возвращается выражение
           * */


           if((t = scaner.scaner(lexeme)) != Types.TypeSemicolon) {
               if (t == Types.TypeIdent || t == Types.TypePlusPlus || t == Types.TypeMinusMinus || t == Types.TypeNegation || t == Types.TypeConstInt) {
                   setPositionAndLine(position, row);
                   typeOfV.clear();
                   V();

                   /*
                    * проверить тип возвращаемого значения (выражения)
                    * */
                   if (currentType != currentVertex.node.returnType)
                       throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + currentVertex.node.lexemeID);
                   return;
               }

               if(t == Types.TypeTrue || t == Types.TypeFalse)
                   if(currentType != DateType.TBoolean)
                       throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + currentVertex.node.lexemeID);

           }

           /*if((t = scaner.scaner(lexeme)) != Types.TypeSemicolon)
               if (t != Types.TypeIdent && t != Types.TypePlusPlus && t != Types.TypeMinusMinus && t != Types.TypeNegation &&
                       t != Types.TypeTrue && t != Types.TypeFalse && t != Types.TypeConstInt)
                   throw new DiagramsException("Ожидалсась ';' или идентификатор или выражение", lexeme, scaner);*/
           return;
       }

        setPositionAndLine(position, row);
        if(t == Types.TypeOpenBrace){
            setPositionAndLine(position, row);
            Block();
            return;
        }

    }

    public void ObjectName() throws DiagramsException, SemanticsException {
        Types t;
        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);


        /*
        * нужно проверить, есть ли такой путь до этого объекта
        * для этого проверяем каждый идентификатор:определен ли он как тип класс
        * последний идентификатор проверяем как идентификатор (он только int или boolean)
        * */

        currentVertex = root.getObjectName(lexeme.toString(), root.getCurrent());

        getPositionAndLine();
        while((t = scaner.scaner(lexeme))== Types.TypeDot){
            getPositionAndLine();
            t = scaner.scaner(lexeme);
            if(t != Types.TypeIdent)
                throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);

            /*
            * проверяем, что такой класс объявлен
            * */
            currentVertex = root.getObjectNameForClass(lexeme.toString(), currentVertex.node.classLink);

           int currentPos = scaner.getCurrentIndexPosition();

            if(scaner.scaner(lexeme) == Types.TypeOpenParenthesis){
                setPositionAndLine(position, row);
                functionCall = true;
                break;
            } else setPositionAndLine(currentPos, row);

            /*
            * проверить, если встретилась скобка, то это вызов функции
            * вернуть позицию на имя функции
            * перенаправить на FunctionCall и выйти из ObjectName
            * */
            getPositionAndLine();
        }

        if(functionCall) {
            FunctionCall();
            functionCall = true;
            return;
        }


        if(t != Types.TypeAssign)
            setPositionAndLine(position, row);
        if(t == Types.TypeError)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);
    }

    public void FunctionCall() throws DiagramsException, SemanticsException {
        Types t;
        if((t = scaner.scaner(lexeme)) != Types.TypeIdent)
            throw new DiagramsException("Ожидался идентификатор", lexeme, scaner);


        /*
        * получить имя функции (получим всю вершину)
        * */
        if(!functionCall)
            currentVertex = root.getFunction(lexeme.toString());
        else
            functionCall = false;

        if((t = scaner.scaner(lexeme)) != Types.TypeOpenParenthesis)
            throw new DiagramsException("Ожидалось '('", lexeme, scaner);

        ActualParameterList();

        if((t = scaner.scaner(lexeme)) != Types.TypeCloseParenthesis)
            throw new DiagramsException("Ожидалось ')'", lexeme, scaner);
        if((t = scaner.scaner(lexeme)) != Types.TypeSemicolon)
            throw new DiagramsException("Ожидалось ';'", lexeme, scaner);
    }

    public void ActualParameterList() throws DiagramsException, SemanticsException {
        Types t;
        int numberOfActualParameter = 0, numberOfParameter = currentVertex.node.numberOfParameters;
        String functionName = currentVertex.node.lexemeID;
        Tree functionVertex = currentVertex;
        do{
            typeOfV.clear();
            V();
            numberOfActualParameter++;
         //   root.findRightLeft(functionName);
            if(currentType != DateType.TUserType)
                functionVertex = root.findParameter(functionVertex, currentType.toString());
            else
                functionVertex = root.findParameter(functionVertex, typeName);



            /*
            * проверить, что тип фактической и формальной переменных совпадает
            * */
          /*  if(currentVertex.node.type != currentType)
                throw new SemanticsException("Тип формальной и фактической переменной не совпадает: " + currentVertex.node.type + " , " + currentType);

*/

            getPositionAndLine();
        }while((t = scaner.scaner(lexeme)) == Types.TypeComma);

        if(numberOfActualParameter != numberOfParameter)
            throw new SemanticsException("Количество формальных и фактических переменных не совпадает");

        setPositionAndLine(position, row);
    }

    public void OperatorIF() throws DiagramsException, SemanticsException {
        Types t;
        func.add(true);
        if ((t = scaner.scaner(lexeme)) != Types.TypeIf)
            throw new DiagramsException("Ожидалось if", lexeme, scaner);

        if ((t = scaner.scaner(lexeme)) != Types.TypeOpenParenthesis)
            throw new DiagramsException("Ожидалось (", lexeme, scaner);
        typeOfV.clear();
        V();


        /*
        * проверить, что выражение в скобках
        * имеет тип boolean
        * */
        if(currentType != DateType.TBoolean)
            throw new SemanticsException("Выржение в скобках должно иметь тип boolean");

        if ((t = scaner.scaner(lexeme)) != Types.TypeCloseParenthesis)
            throw new DiagramsException("Ожидалось )", lexeme, scaner);

        if ((t = scaner.scaner(lexeme)) != Types.TypeOpenBrace)
            throw new DiagramsException("Ожидалось {", lexeme, scaner);

        Operators();

        if(func.size() != 0)
            func.remove(func.size() - 1);

        if ((t = scaner.scaner(lexeme)) != Types.TypeCloseBrace)
            throw new DiagramsException("Ожидалось }", lexeme, scaner);

        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) != Types.TypeElse && t == Types.TypeSemicolon) {
            setPositionAndLine(position, row);
            return;
        }
        if (t == Types.TypeElse) {

            if ((t = scaner.scaner(lexeme)) != Types.TypeOpenBrace)
                throw new DiagramsException("Ожидалось {", lexeme, scaner);

            func.add(true);

            Operators();

            if(func.size() != 0)
                func.remove(func.size() - 1);
            if ((t = scaner.scaner(lexeme)) != Types.TypeCloseBrace)
                throw new DiagramsException("Ожидалось }", lexeme, scaner);
            return;
        }
        else setPositionAndLine(position,row);
    }

    public Types V() throws DiagramsException, SemanticsException {
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

    public Types A2() throws DiagramsException, SemanticsException {
        A3();
        Types t;
        while((t = scaner.scaner(lexeme)) == Types.TypeLe || t == Types.TypeGe || t == Types.TypeGt || t == Types.TypeLt){
            if(A3() == Types.TypeError)
                throw new DiagramsException("Ожидалось выражение", lexeme, scaner);

            /*
            * так как эти знаки , значит тип boolean
            * предусмотреть также дл равно
            * сравнить тип выражения слева и справа от этих знаков
            * предусмотреть инициализацию (b = 0), ссылка на значение
            * */

            typeOfV.add(DateType.TBooleanSign);
        }

      /*  if((t == Types.TypeComma)){
            int currentPos = 0;
            scaner.scaner(lexeme);
            if()
        }*/


        if ((t != Types.TypeSemicolon) && t != Types.TypeCloseParenthesis && t != Types.TypeComma)
            throw new DiagramsException("Ожидалось ';' или ')' или ','", lexeme, scaner);
        if(t == Types.TypeCloseParenthesis || t == Types.TypeComma)
            setPositionAndLine(position, row);


        if(typeOfV.stream().anyMatch(i->i == DateType.TBoolean)) {
            if (!typeOfV.stream().allMatch(i -> i == DateType.TBoolean))
                throw new SemanticsException("В выражении учасвтвуют несколько типов");
            currentType = DateType.TBoolean;
        } else if(typeOfV.stream().anyMatch(i -> i == DateType.TBooleanSign))
            currentType = DateType.TBoolean;
        else if(typeOfV.stream().anyMatch(i->i == DateType.TInt) || typeOfV.stream().anyMatch(i->i == DateType.TConstant))
            currentType = DateType.TInt;
        else if (typeOfV.stream().anyMatch(i->i == DateType.TUserType)) {
            currentType = DateType.TUserType;
            typeName = currentVertex.node.classLink.node.lexemeID;
        }


        return Types.TypeForReturn;
    }

    public Types A3() throws DiagramsException, SemanticsException {
        A4();
        Types t;
        getPositionAndLine();

        while((t = scaner.scaner(lexeme)) == Types.TypePlus || t == Types.TypeMinus){
            if(A4() == Types.TypeError)
                throw new DiagramsException("Ожидалось выражение", lexeme, scaner);
            getPositionAndLine();


            if(typeOfV.stream().anyMatch(i -> i == DateType.TBoolean) ||
                            typeOfV.stream().anyMatch(i -> i == DateType.TUserType))
                throw new SemanticsException("В выражении участвуют несколько типов");
            /*
            * только типы TInt, TConstant
            * */
        }
        setPositionAndLine(position, row);

        return Types.TypeForReturn;
    }

    public Types A4() throws DiagramsException, SemanticsException {
        A5();
        Types t;
        getPositionAndLine();

        while((t = scaner.scaner(lexeme)) == Types.TypeMultiply ||
                t == Types.TypeDivision || t == Types.TypeMod) {
            getPositionAndLine();

            /*
            * только типы TInt, TConstant
            * */

        if( typeOfV.stream().anyMatch(i -> i == DateType.TBoolean) ||
            typeOfV.stream().anyMatch(i -> i == DateType.TUserType))
                throw new SemanticsException("В выражении участвуют несколько типов");


            if (A5() == Types.TypeError)
                throw new DiagramsException("Ожидалось выражение", lexeme, scaner);
            getPositionAndLine();
        }
        setPositionAndLine(position, row);
        return  Types.TypeForReturn;
    }

    public Types A5() throws DiagramsException, SemanticsException {
        Types t;
        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) == Types.TypePlusPlus || t == Types.TypeMinusMinus) {
            if( typeOfV.stream().anyMatch(i -> i == DateType.TBoolean) ||
                    typeOfV.stream().anyMatch(i -> i == DateType.TUserType) ||
                    typeOfV.stream().anyMatch(i -> i == DateType.TConstant))
                throw new SemanticsException("В выражении участвуют несколько типов");

            return A6();
        }

            if( typeOfV.stream().anyMatch(i -> i == DateType.TBoolean) ||
                typeOfV.stream().anyMatch(i -> i == DateType.TUserType))
            throw new SemanticsException("В выражении участвуют несколько типов");

        setPositionAndLine(position, row);
        return A6();
    }

    public Types A6() throws DiagramsException, SemanticsException {
        Types t;
        A7();
        getPositionAndLine();

        while ((t = scaner.scaner(lexeme)) == Types.TypePlusPlus || t == Types.TypeMinusMinus){
            getPositionAndLine();

            /*
            * проверять, должен быть только тип TInt
            * */

            if( typeOfV.stream().anyMatch(i -> i == DateType.TBoolean) ||
                typeOfV.stream().anyMatch(i -> i == DateType.TUserType) ||
                typeOfV.stream().anyMatch(i -> i == DateType.TConstant))
                throw new SemanticsException("В выражении участвуют несколько типов");

        }
        setPositionAndLine(position, row);
        return Types.TypeForReturn;
    }

    public Types A7() throws DiagramsException, SemanticsException {
        Types t;
        getPositionAndLine();
        if((t = scaner.scaner(lexeme)) == Types.TypeIdent) {
            setPositionAndLine(position, row);
            ObjectName();


            /*
            * найдем тип идентификатора
            * добавим его в список типов для выражения
            * */


            if(currentVertex.node.type == DateType.TBoolean || currentVertex.node.type == DateType.TInt || currentVertex.node.type == DateType.TUserType)
                typeOfV.add(currentVertex.node.type);

            return Types.TypeForReturn;
        }
        else {
            if (t == Types.TypeConstInt || t == Types.TypeTrue || t == Types.TypeFalse || t == Types.TypeInt) {

                if(t == Types.TypeInt)
                    currentType = DateType.TInt;
                else if(t == Types.TypeTrue || t == Types.TypeFalse )
                    currentType = DateType.TBoolean;
                else if(t == Types.TypeConstInt)
                    currentType = DateType.TConstant;

                typeOfV.add(currentType);

                return t;
            }
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
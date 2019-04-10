package diagrams;

import interpreter.Interpreter;
import org.apache.log4j.Logger;
import scanner.Lexeme;
import scanner.Scanner;
import service.*;
import tree.DataValue;
import tree.Tree;

import java.util.*;

import static service.InterpreterEnum.CALL_METHOD_POINT_ADDR;
import static service.InterpreterEnum.CALL_VAR_POINT_ADDR;

public class Diagrams {
    private static Logger LOGGER = Logger.getLogger(Diagrams.class);
    private Scanner scanner;
    private int position;
    private int row;
    private List<Boolean> brace = new ArrayList<>();
    private List<Boolean> func = new ArrayList<>();
    private Tree root = new Tree();
    private DataType currentType = null;
    private Lexeme currentLexeme = new Lexeme();
    private Tree currentVertex = null;
    private List<DataType> typeOfV = new ArrayList<>();
    private Stack<List<Lexeme>> dataOfV = new Stack<>();
    private List<Lexeme> currentDataOfV = new ArrayList<>();
    private boolean functionCall = false;
    private String typeName = "";
    private Interpreter interpreter;
    private Pos pos = new Pos();


    public void clear() {
        position = 0;
        row = 0;
        brace = new ArrayList<>();
        func = new ArrayList<>();
        currentType = null;
        currentLexeme = new Lexeme();
        currentVertex = null;
        typeOfV = new ArrayList<>();
        dataOfV = new Stack<>();
        functionCall = false;
        typeName = "";
    }

    public Diagrams(Scanner scanner, StringBuilder lexeme, Interpreter interpreter) {
        this.scanner = scanner;
        Lexeme lexeme1 = new Lexeme();
        lexeme1.lexeme = lexeme;
        this.interpreter = interpreter;
    }

    private void setPositionAndLine(int position, int numberOfRow) {
        scanner.setPtr(position);
        scanner.setCurrentLine(numberOfRow);
    }

    private void getPositionAndLine() {
        position = scanner.getPtr();
        row = scanner.getCurrentLine();
    }

    public void printTree() {
        root.printTree();
    }

    public void setRoot() {
        root.setCurrent(root);
    }

    public void S() throws DiagramsException, SemanticsException {

        if (scanner.scanner().type != Types.TypePublic) {
            throw new DiagramsException("Ожидалось 'public", scanner);
        }

        if (scanner.scanner().type != Types.TypeClass) {
            throw new DiagramsException("Ожидалось 'class'", scanner);
        }

        if (scanner.scanner().type != Types.TypeMain && scanner.getLexeme().type != Types.TypeIdent) {
            throw new DiagramsException("Ожидалось имя класса", scanner);
        }

        /*занести идентификатор с типом в дерево*/

        Tree vertex;
        if (interpreter.isAnalyzing()) {
            vertex = root.include(scanner.getLexeme(), DataType.TClass, null);
        } else {
            vertex = root.findByName(root.left, scanner.getLexeme().lexeme.toString());
            root.setCurrent(vertex);
        }

        if (scanner.scanner().type != Types.TypeOpenBrace) {
            throw new DiagramsException("Ожидалось {", scanner);
        }

        Content();

        /*
         * возврат
         * */
        root.setCurrent(vertex);

        if (scanner.scanner().type != Types.TypeCloseBrace) {
            throw new DiagramsException("Ожидалось }", scanner);
        }

        getPositionAndLine();

        if (scanner.scanner().type != Types.TypeEnd) {
            setPositionAndLine(position, row);
        }

    }


    private void Content() throws DiagramsException, SemanticsException {
        Types t;
        do {
            getPositionAndLine();

            if ((t = scanner.scanner().type) == Types.TypeIdent || t == Types.TypeInt ||
                    (t) == Types.TypeBoolean || t == Types.TypeVoid) {
                if ((t = scanner.scanner().type) == Types.TypeIdent || t == Types.Typemain) {
                    if (scanner.scanner().type == Types.TypeOpenParenthesis) {
                        setPositionAndLine(position, row);

                        Method();
                        continue;

                    }
                }
            }

            setPositionAndLine(position, row);
            t = scanner.scanner().type;
            if ((t == Types.TypeIdent || t == Types.TypeInt) && ((t = scanner.scanner().type) == Types.TypeOpenBrace || t == Types.TypeOpenParenthesis)) {
                throw new DiagramsException("Ожидалось объявление класса или функции", scanner);
            }


            setPositionAndLine(position, row);
            if ((t = scanner.scanner().type) == Types.TypeIdent || t == Types.TypeInt || (t) == Types.TypeBoolean) {
                getPositionAndLine();
                currentLexeme.delete(0, currentLexeme.length());
                currentLexeme.append(scanner.getLexeme().lexeme.toString());
            }
            String typeLexeme = scanner.getLexeme().lexeme.toString();
            if (scanner.scanner().type == Types.TypeIdent) {
                /*
                 *найти лексемму с таким типом  ???????(как они там появятся)
                 * */
                currentType = root.getType(typeLexeme);
                if ((t = scanner.scanner().type) == Types.TypeComma || t == Types.TypeSemicolon || t == Types.TypeAssign) {
                    setPositionAndLine(position, row);

                    /*
                     * затем последовательно заносить идентификаторы(переменные)
                     * с таким типом в дерево
                     * */
                    ListOfIdentifiers();
                    continue;
                }

            }
            setPositionAndLine(position, row);
            if (((t = scanner.scanner().type) == Types.TypePublic || t == Types.TypeClass) && (func.size() == 0 || !func.get(func.size() - 1))) {
                setPositionAndLine(position, row);
                S();
                continue;
            } else if ((t == Types.TypePublic || t == Types.TypeClass) && func.size() != 0 && func.get(func.size() - 1)) {
                throw new DiagramsException("Здесь не может быть объявление нового класса", scanner);
            }


            setPositionAndLine(position, row);
            if ((t = scanner.scanner().type) == Types.TypeOpenBrace) {
                throw new DiagramsException("Блок может объявляться только в функции", scanner);
            }


            break;
        }
        while (true);

        if (t == Types.TypeComma) {
            throw new DiagramsException("Ожидалось ';'", scanner);
        }
        if (t == Types.TypeError) {
            throw new DiagramsException("Ошибка: ", scanner);
        }
        if (t == Types.TypeCloseBrace) {
            setPositionAndLine(position, row);
        }
    }

    public DataValue Method() throws DiagramsException, SemanticsException {
        Types t;
        DataValue dataValue = new DataValue();

        if (interpreter.isAnalyzing()) {
            ReturnType();
            currentType = root.getType(scanner.getLexeme().lexeme.toString());
            if ((t = scanner.scanner().type) != Types.TypeIdent && t != Types.Typemain) {
                throw new DiagramsException("Ожидалось идентификатор или main", scanner);
            }
        }

        /*
         * Определить тип лексеммы
         * */
        if (interpreter.isInterpreting() && interpreter.isCallFunction()) {
            currentType = interpreter.peekFunctionCall().node.dataValue.type;
        }


        /*
         * занести  в дерево идентификатор с типом
         * */
        Tree vertex = null;
        if (interpreter.isAnalyzing()) {
            vertex = root.include(scanner.getLexeme(), DataType.TFunction, currentType, null);
        } else if (interpreter.isInterpreting()) {
            scanner.scanner();
            if (interpreter.isCallFunction()) {
                vertex = interpreter.peekFunctionCall();
            } else {
                scanner.scanner();
                vertex = root.findByName(root.left, scanner.getLexeme().lexeme.toString());
            }
            root.setCurrent(vertex.right);
        }

        if (scanner.scanner().type != Types.TypeOpenParenthesis) {
            throw new DiagramsException("Ожидалось (", scanner);
        }

        FormalParameterList();

        if (scanner.scanner().type != Types.TypeCloseParenthesis) {
            throw new DiagramsException("Ожидалось )", scanner);
        }

        Block();


        if (interpreter.isInterpreting() && !interpreter.isEmptyReturnValue()) {
            return dataValue = interpreter.popReturnValue();
        }

        /*
         * восстановить значение указателя
         * */
        root.setCurrent(vertex);
        return dataValue;
    }

    private void ReturnType() throws DiagramsException {
        Types t;
        if ((t = scanner.scanner().type) != Types.TypeVoid &&
                t != Types.TypeIdent && t != Types.TypeBoolean &&
                t != Types.TypeInt) {
            throw new DiagramsException("Ожидался тип или идентификатор", scanner);
        }
    }

    private void FormalParameterList() throws DiagramsException, SemanticsException {
        int numberOfParameters = 0;
        Tree vertex = root.getCurrent().parent;
        do {
            Parameter();

            /*
             * считаем количество параметров и присваиваем
             * переменной numberOfParameters в вершину
             * */
            numberOfParameters++;
            root.setNumberOfParameters(vertex, numberOfParameters);


            getPositionAndLine();
        }
        while (scanner.scanner().type == Types.TypeComma);
        setPositionAndLine(position, row);
    }

    public void Parameter() throws DiagramsException, SemanticsException {
        Types t;
        if ((t = scanner.scanner().type) != Types.TypeIdent && t != Types.TypeBoolean &&
                t != Types.TypeInt) {
            throw new DiagramsException("Ожидался тип", scanner);
        }

        /*
         * определить тип лексеммы
         * */
        currentType = root.getType(scanner.getLexeme().lexeme.toString());
        if (currentType == DataType.TClass) {
            currentType = DataType.TUserType;
            currentVertex = root.getClass(scanner.getLexeme().lexeme.toString());
        }


        if (scanner.scanner().type != Types.TypeIdent) {
            throw new DiagramsException("Ожидался идентификатор", scanner);
        }

        /*
         * занести идентификатор с типом в дерево
         * определить ссылку на класс
         * */
        if (interpreter.isAnalyzing()) {
            currentVertex = root.include(scanner.getLexeme(), currentType,
                                         currentType == DataType.TUserType ?
                                                 currentVertex.node.lexemeName : null);
        } else {
            currentVertex = root.findByName(root.left, scanner.getLexeme().lexeme.toString());
            root.setCurrent(currentVertex);
        }

        /*
         * нужно ли делать возврат
         * */
    }

    private void Block() throws DiagramsException, SemanticsException {
        if (scanner.scanner().type != Types.TypeOpenBrace) {
            throw new DiagramsException("Ожидалось {", scanner);
        }

        Operators();

        if (interpreter.isInterpreting() && !interpreter.isEmptyReturnValue()) {
            return;
        }

        if (scanner.scanner().type != Types.TypeCloseBrace) {
            throw new DiagramsException("Ожидалось }", scanner);
        }
    }

    public void Operators() throws DiagramsException, SemanticsException {
        Types t;

        while (true) {
            getPositionAndLine();
            Lexeme ident;
            ident = scanner.scanner();
            if (ident.type == Types.TypeIdent && ((t = scanner.scanner().type) == Types.TypePlusPlus || t == Types.TypeMinusMinus)) {
                setPositionAndLine(position, row);
                typeOfV.clear();
                remember(CALL_VAR_POINT_ADDR, ident);
                setVarValue(V());

                continue;
            }
            setPositionAndLine(position, row);
            if ((t = scanner.scanner().type) == Types.TypePlusPlus || t == Types.TypeMinusMinus || t == Types.TypeNegation) {
                setPositionAndLine(position, row);
                typeOfV.clear();
                setVarValue(V());

                continue;
            }
            setPositionAndLine(position, row);
            ident = scanner.scanner();
            if ((ident.type == Types.TypeIdent || ident.type == Types.TypeConstInt) && ((t =
                    scanner.scanner().type) == Types.TypePlus || t == Types.TypeMinus || t == Types.TypeAssign)) {
                setPositionAndLine(position, row);
                remember(CALL_VAR_POINT_ADDR, ident);
                typeOfV.clear();
                setVarValue(V());
                continue;
            }


            setPositionAndLine(position, row);
            ident = scanner.scanner();
            if ((ident.type == Types.TypeIdent || t == Types.TypeBoolean || t == Types.TypeInt) &&
                    (t = scanner.scanner().type) != Types.TypeDot && t != Types.TypeOpenParenthesis) {
                setPositionAndLine(position, row);
//                LOGGER.info("Operators: " + t);
//                LOGGER.info(scanner.getCurrentItem());
                VariableDescription();
                continue;
            }


            setPositionAndLine(position, row);
            if (((t = scanner.scanner().type) == Types.TypePublic || t == Types.TypeClass) && (func.size() == 0 || !func.get(func.size() - 1))) {
                setPositionAndLine(position, row);
                S();
                continue;
            } else if (t == Types.TypePublic || t == Types.TypeClass && func.get(func.size() - 1)) {
                throw new DiagramsException("Здесь не может быть объявление другого класса", scanner);
            }
            setPositionAndLine(position, row);
            if ((t = scanner.scanner().type) == Types.TypeIf) {
                setPositionAndLine(position, row);
                Operator();
                if (interpreter.isInterpreting() && !interpreter.isEmptyReturnValue()) {
                    return;
                }
            } else if (t == Types.TypeIdent) {
                if ((t = scanner.scanner().type) == Types.TypeDot) {
                    setPositionAndLine(position, row);
                    Operator();
                } else if (t == Types.TypeOpenParenthesis) {
                    setPositionAndLine(position, row);
                    Operator();
                }
            } else if (t == Types.TypeOpenBrace) {
                setPositionAndLine(position, row);
                Block();
            } else if (t == Types.TypeReturn) {
                getPositionAndLine();
                Lexeme returnLexeme = scanner.scanner();
                DataValue returnValue = new DataValue();
                if (returnLexeme.type == Types.TypeSemicolon) {

                    Tree vertex = root.getFunction();
                    if (vertex.node.dataValue.type != DataType.TVoid) {
                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                    }

                    continue;
                }

                setPositionAndLine(position, row);
                returnLexeme = scanner.scanner();
                if ((returnLexeme.type == Types.TypeIdent || returnLexeme.type == Types.TypeConstInt || returnLexeme.type == Types.TypeFalse || returnLexeme.type == Types.TypeTrue)
                        && (scanner.scanner().type) == Types.TypeSemicolon) {

                    Tree vertex = root.getFunction();

                    Tree returnTree = null;
                    if (returnLexeme.type == Types.TypeIdent) {

                        if (interpreter.isAnalyzing()) {
                            setPositionAndLine(position, row);
                            scanner.scanner();
                            Tree returnValueTree = root.getTypeOfLexeme(scanner.getLexeme().lexeme.toString());
                            scanner.scanner();
                            if (returnValueTree.node.type != vertex.node.dataValue.type) {
                                throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                            }
                            continue;
                        }

                        if (interpreter.isInterpreting()) {
                            returnTree = root.findByName(interpreter.getFunctionCallInterpreter().peek().right,
                                                         returnLexeme.lexeme.toString());
                        }
                        if (returnTree == null) {
                            returnTree = root.findByName(root.left, returnLexeme.lexeme.toString());
                        }
                        returnValue.type = returnTree.node.dataValue.type;
                        if (returnTree.node.type == DataType.TInt) {
                            returnValue.value.valueInt = returnTree.node.dataValue.value.valueInt;
                        }
                        if (returnTree.node.type == DataType.TBoolean) {
                            returnValue.value.constant = returnTree.node.dataValue.value.constant;
                        }
                    }

                    if (returnLexeme.type == Types.TypeConstInt || returnLexeme.type == Types.TypeInt) {
                        if (interpreter.isInterpreting()) {
                            returnValue.type = DataType.TInt;
                            returnValue.value.valueInt = Integer.parseInt(returnLexeme.lexeme.toString());
                        }
                        if (vertex.node.dataValue.type != DataType.TConstant && vertex.node.dataValue.type != DataType.TInt) {
                            throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                        }
                    }

                    if (returnLexeme.type == Types.TypeFalse || returnLexeme.type == Types.TypeTrue) {
                        if (interpreter.isInterpreting()) {
                            returnValue.type = DataType.TBoolean;
                            returnValue.value.constant = returnLexeme.type != Types.TypeFalse;
                        }
                        if (vertex.node.dataValue.type != DataType.TBoolean) {
                            throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                        }
                    }
                    if (interpreter.isInterpreting()) {
                        interpreter.changeFunctionCallInterpreter(returnValue);
                        interpreter.pushReturnValue(returnValue);
                        interpreter.popPrevFunctionCall();
                        if (!interpreter.getPrevFunctionCallInterpreter().isEmpty()) {
                            interpreter.changePrevFunctionCallInterpreter(returnValue);
                        }
                        return;
                    }

//                    if (t == Types.TypeIdent) {
//                        setPositionAndLine(position, row);
//                        scanner.scanner();
//                        Tree returnValueTree = root.getTypeOfLexeme(scanner.getLexeme().lexeme.toString());
//                        scanner.scanner();
//                        if (returnValueTree.node.type != vertex.node.returnType) {
//                            throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " +
//                            vertex.node.lexemeName);
//                        }
                    //                   }
                    continue;
                }


                if ((t = scanner.scanner().type) != Types.TypeSemicolon) {
                    if (t == Types.TypeIdent || t == Types.TypePlusPlus || t == Types.TypeMinusMinus || t == Types.TypeNegation || t == Types.TypeConstInt) {
                        setPositionAndLine(position, row);
                        typeOfV.clear();
                        V();

                        /*
                         * проверить тип возвращаемого значения (выражения)
                         * */
                        if (!currentType.equals(currentVertex.node.type)) {
                            throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + currentVertex.node.lexemeName);
                        }
                        return;
                    }

                    if (t == Types.TypeTrue || t == Types.TypeFalse) {
                        if (currentType != DataType.TBoolean) {
                            throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + currentVertex.node.lexemeName);
                        }
                    }

                }


                if (scanner.scanner().type == Types.TypeCloseBrace) {
                    setPositionAndLine(position, row);
                    continue;
                }

                setPositionAndLine(position, row);
                if ((t = scanner.scanner().type) != Types.TypeSemicolon) {
                    if (t != Types.TypeIdent && t != Types.TypePlusPlus && t != Types.TypeMinusMinus && t != Types.TypeNegation &&
                            t != Types.TypeTrue && t != Types.TypeFalse && t != Types.TypeConstInt) {
                        throw new DiagramsException("Ожидалсась ';' или идентификатор или выражение",
                                                    scanner);
                    }
                }


                setPositionAndLine(position, row);
            } else if (t == Types.TypeSemicolon) {
                continue;
            } else if (t == Types.TypeCloseBrace && brace.size() != 0) {
                brace.remove(brace.size() - 1);
            } else {
                setPositionAndLine(position, row);
                return;
            }
        }
    }

    private void VariableDescription() throws DiagramsException, SemanticsException {
        Types t;
        int currentPos = -1, currentRow = -1;

        getPositionAndLine();
        if ((t = scanner.scanner().type) != Types.TypeIdent && t != Types.TypeBoolean && t != Types.TypeInt) {
            throw new DiagramsException("Ожидался тип переменной", scanner);
        }


        /*
         * определить тип переменной
         * */
        currentType = root.getType(scanner.getLexeme().lexeme.toString());
        if (currentType == DataType.TClass) {
            currentVertex = root.getClass(scanner.getLexeme().lexeme.toString());
            currentType = DataType.TUserType;
        }

        currentPos = scanner.getPtr();
        currentRow = scanner.getCurrentLine();
        if (scanner.scanner().type == Types.TypeAssign) {
            setPositionAndLine(position, row);
        } else {
            setPositionAndLine(currentPos, currentRow);
        }
        ListOfIdentifiers();
    }

    private void ListOfIdentifiers() throws DiagramsException, SemanticsException {
        Types t;
        String typeIdent;
        Tree vertex;
        int count = 0;
        do {

            if (scanner.scanner().type != Types.TypeIdent) {
                throw new DiagramsException("Ожидался идентификатор", scanner);
            }

            /*
             * заносим идентификатор с типом currentType в дерево
             * */
            if (interpreter.isAnalyzing()) {
                vertex = root.include(scanner.getLexeme(), currentType,
                                      currentType == DataType.TUserType ?
                                              currentVertex.node.lexemeName : null);
            } else {
                vertex = root.findByName(root.left, scanner.getLexeme().lexeme.toString());
                root.setCurrent(vertex);
            }

            typeIdent = currentType.toString();
            remember(CALL_VAR_POINT_ADDR, scanner.getLexeme());

            getPositionAndLine();
            if (scanner.scanner().type == Types.TypeAssign) {
                typeOfV.clear();

                int tmpPos = scanner.getPtr();
                int tmpLine = scanner.getCurrentLine();
                Lexeme functionName = scanner.scanner();
                DataValue initIdentValue;

                if (functionName.type == Types.TypeIdent && scanner.scanner().type == Types.TypeOpenParenthesis) {
                    setPositionAndLine(tmpPos, tmpLine);
                    remember(CALL_METHOD_POINT_ADDR, functionName);
                    initIdentValue = setFunctionValue(FunctionCall());

                    if (interpreter.isInterpreting()) {
                        Pos tmp = interpreter.getFunctionCallPos().pop();
                        setPositionAndLine(tmp.callMethodPointAddr.ptr, tmp.callMethodPointAddr.line);
                        scanner.readForEndFunctionCall();
                        position = scanner.getPtr();
                        row = scanner.getCurrentLine();
                    }

                } else {
                    setPositionAndLine(tmpPos, tmpLine);
                    remember(CALL_VAR_POINT_ADDR, functionName);
                    initIdentValue = setVarValue(V());
                }

                if (interpreter.isInterpreting()) {
                    setVarValue(initIdentValue);
                }

                //вызов функции



                /*
                 * в V() вычисляется новый currentType (тип выражения),
                 * затем сравнивается с типом объявленной переменной(идентификатора)
                 * */

                if (interpreter.isAnalyzing() && !typeIdent.equals(currentType.toString())) {
                    if (currentVertex.node != null && currentVertex.node.type == DataType.TFunction) {
                        if (!currentVertex.node.dataValue.type.toString().equals(typeIdent)) {
                            throw new SemanticsException("Тип формальной и фактической переменной не совпадают: ",
                                                         currentVertex.node.dataValue.type + " " + typeIdent);
                        }
                    } else {
                        throw new SemanticsException("Тип формальной и фактической переменной не совпадают: "
                                                             + currentType + " " + typeIdent);
                    }
                }


                count++;
            } else {
                setPositionAndLine(position, row);
            }

            getPositionAndLine();
        }
        while ((t = scanner.scanner().type) == Types.TypeComma);



        /*
         * здесь нужно вернуться
         * */
        root.setCurrent(vertex);

        if (t != Types.TypeSemicolon && count == 0) {
            throw new DiagramsException("Ожидалось ';' или ',' или '='", scanner);
        }
        if (t != Types.TypeSemicolon) {
            setPositionAndLine(position, row);
        }

    }

    private void Operator() throws DiagramsException, SemanticsException {
        Types t;
        getPositionAndLine();
        if (scanner.scanner().type == Types.TypeIf) {
            setPositionAndLine(position, row);
            OperatorIF();
            return;
        }


        setPositionAndLine(position, row);
        if (scanner.scanner().type == Types.TypeIdent && scanner.scanner().type == Types.TypeOpenParenthesis) {
            setPositionAndLine(position, row);
            FunctionCall();
            return;
        }

        setPositionAndLine(position, row);
        if ((t = scanner.scanner().type) == Types.TypeIdent) {
            if (scanner.scanner().type == Types.TypeDot) {
                setPositionAndLine(position, row);
                ObjectName();
                if (functionCall) {
                    functionCall = false;
                    return;
                }
            } else {
                setPositionAndLine(position, row);
            }
            typeOfV.clear();
            remember(CALL_VAR_POINT_ADDR, scanner.getLexeme());
            setVarValue(V());

            /*
             * проверить, что тип идентификатора и
             * тип выражения совпадают
             * */

            if (currentType != currentVertex.node.type) {
                throw new SemanticsException("Тип присваиваемого значения не совпадает с типом переменной");
            }

            return;
        }

        if (t == Types.TypeReturn) {
            getPositionAndLine();
            DataValue returnValue = new DataValue();
            Lexeme returnLexeme = scanner.scanner();
            if (returnLexeme.type == Types.TypeSemicolon) {
                Tree vertex = root.getFunction();
                if (vertex.node.dataValue.type != DataType.TVoid) {
                    throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                }
                return;
            }

            setPositionAndLine(position, row);
            returnLexeme = scanner.scanner();
            if ((returnLexeme.type == Types.TypeIdent || returnLexeme.type == Types.TypeConstInt
                    || returnLexeme.type == Types.TypeFalse || returnLexeme.type == Types.TypeTrue)
                    && scanner.scanner().type == Types.TypeSemicolon) {

                Tree vertex = root.getFunction();

                Tree returnTree = null;
                if (returnLexeme.type == Types.TypeIdent) {
                    if (interpreter.isInterpreting()) {
                        returnTree = root.findByName(interpreter.getFunctionCallInterpreter().peek().right,
                                                     scanner.getLexeme().lexeme.toString());
                    }
                    if (interpreter.isAnalyzing() || returnValue == null) {
                        returnTree = root.findByName(root.left, scanner.getLexeme().lexeme.toString());
                    }

                    if (interpreter.isInterpreting() && returnTree.node.type == DataType.TInt) {
                        returnValue.value.valueInt = returnTree.node.dataValue.value.valueInt;
                    }
                    if (interpreter.isInterpreting() && returnTree.node.type == DataType.TBoolean) {
                        returnValue.value.constant = returnTree.node.dataValue.value.constant;
                    }
                }


                if (returnLexeme.type == Types.TypeConstInt || returnLexeme.type == Types.TypeInt) {
                    if (interpreter.isInterpreting()) {
                        returnValue.value.valueInt = Integer.parseInt(returnLexeme.lexeme.toString());
                    }
                    if (vertex.node.dataValue.type != DataType.TConstant && vertex.node.dataValue.type != DataType.TInt) {
                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                    }
                }

                if (returnLexeme.type == Types.TypeFalse || returnLexeme.type == Types.TypeTrue) {
                    if (interpreter.isInterpreting()) {
                        returnValue.value.constant = returnLexeme.type != Types.TypeFalse;
                    }
                    if (vertex.node.dataValue.type != DataType.TBoolean) {
                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                    }
                }

                if (interpreter.isInterpreting()) {
                    setPositionAndLine(pos.callMethodPointAddr.ptr, pos.callMethodPointAddr.line);
                }

                return;
            }

            /*
             * сделать проверку типа выражение,
             * если возвращается выражение
             * */


            if ((t = scanner.scanner().type) != Types.TypeSemicolon) {
                if (t == Types.TypeIdent || t == Types.TypePlusPlus || t == Types.TypeMinusMinus || t == Types.TypeNegation || t == Types.TypeConstInt) {
                    setPositionAndLine(position, row);
                    typeOfV.clear();
                    remember(CALL_VAR_POINT_ADDR, scanner.getLexeme());
                    setVarValue(V());

                    /*
                     * проверить тип возвращаемого значения (выражения)
                     * */
                    if (currentType != currentVertex.node.dataValue.type) {
                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + currentVertex.node.lexemeName);
                    }
                    return;
                }

                if (t == Types.TypeTrue || t == Types.TypeFalse) {
                    if (currentType != DataType.TBoolean) {
                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + currentVertex.node.lexemeName);
                    }
                }

            }
            return;
        }

        setPositionAndLine(position, row);
        if (t == Types.TypeOpenBrace) {
            setPositionAndLine(position, row);
            Block();
        }

    }

    private DataValue ObjectName() throws DiagramsException, SemanticsException {
        Types t;
        DataValue dataValue = new DataValue();
        getPositionAndLine();
        int startPosition = scanner.getPtr();
        Lexeme checkLexeme = scanner.scanner();
        if (checkLexeme.type != Types.TypeIdent) {
            throw new DiagramsException("Ожидался идентификатор", scanner);
        }


        /*
         * нужно проверить, есть ли такой путь до этого объекта
         * для этого проверяем каждый идентификатор:определен ли он как тип класс
         * последний идентификатор проверяем как идентификатор (он только int или boolean)
         * */

        currentVertex = root.getObjectName(scanner.getLexeme().lexeme.toString(), root.getCurrent());

        getPositionAndLine();
        while ((t = scanner.scanner().type) == Types.TypeDot) {
            getPositionAndLine();
            checkLexeme = scanner.scanner();
            if (checkLexeme.type != Types.TypeIdent) {
                throw new DiagramsException("Ожидался идентификатор", scanner);
            }

            /*
             * проверяем, что такой класс объявлен
             * */

            if (currentVertex.node.classLink == null) {
                throw new SemanticsException("Неверный вызов класса");
            }
            currentVertex = root.getObjectNameForClass(scanner.getLexeme().lexeme.toString(),
                                                       currentVertex.node.classLink);

            int currentPos = scanner.getPtr();

            if (scanner.scanner().type == Types.TypeOpenParenthesis) {
                setPositionAndLine(position, row);
                functionCall = true;
                break;
            } else {
                setPositionAndLine(currentPos, row);
            }

            /*
             * проверить, если встретилась скобка, то это вызов функции
             * вернуть позицию на имя функции
             * перенаправить на FunctionCall и выйти из ObjectName
             * */
            getPositionAndLine();
        }

        if (t == Types.TypeOpenParenthesis) {
            functionCall = true;
            setPositionAndLine(startPosition, row);
        }

        if (functionCall) {
            if (interpreter.isInterpreting()) {
                interpreter.pushFunctionCallPos(checkLexeme);
            }
            dataValue = FunctionCall();
            if (interpreter.isInterpreting()) {
                Pos tmp = interpreter.getFunctionCallPos().pop();
                setPositionAndLine(tmp.callMethodPointAddr.ptr, tmp.callMethodPointAddr.line);
                scanner.readForEndFunctionCall();
                position = scanner.getPtr();
                row = scanner.getCurrentLine();
            }
            typeOfV.clear();
            functionCall = false;
            return dataValue;
        }


        if (t != Types.TypeAssign) {
            setPositionAndLine(position, row);
        }
        if (t == Types.TypeError) {
            throw new DiagramsException("Ожидался идентификатор", scanner);
        }
        return dataValue;
    }

    public DataValue FunctionCall() throws DiagramsException, SemanticsException {
        Types t;
        DataValue dataValue = new DataValue();

        if ((t = scanner.scanner().type) != Types.TypeIdent) {
            throw new DiagramsException("Ожидался идентификатор", scanner);
        }

        if (interpreter.isInterpreting()) {
            pos.callMethodPointAddr = scanner.getLexeme();
            interpreter.setExecute(true);
        }

        //получить адрес функции
        if (functionCall) {
            functionCall = false;
        }

        currentVertex = root.getFunction(scanner.getLexeme().lexeme.toString());

        //получили координаты начала описания функции
        if (interpreter.isInterpreting()) {
            pos.setDescriptionMethodAddr(currentVertex.node.ptrStart, currentVertex.node.line);
            interpreter.setCallFunction(true);
        }

        dataValue.type = currentVertex.node.dataValue.type;

        if (scanner.scanner().type != Types.TypeOpenParenthesis) {
            throw new DiagramsException("Ожидалось '('", scanner);
        }

        if (interpreter.isInterpreting()) {
            interpreter.put(currentVertex.clone(currentVertex));
            if (interpreter.getPrevFunctionCallInterpreter().isEmpty()) {
                interpreter.pushPrevFuctionCall(currentVertex.clone(currentVertex));
            } else {
                interpreter.pushPrevFuctionCall(interpreter.peekPrevFunctionCall()
                                                           .clone(interpreter.peekPrevFunctionCall()));
            }
        }

        ActualParameterList();

        if ((t = scanner.scanner().type) != Types.TypeCloseParenthesis) {
            throw new DiagramsException("Ожидалось ')'", scanner);
        }


//        if (interpreter.isAnalyzing()) {
//            currentVertex.node.ptrEnd = scanner.getPtr();
//            currentVertex.node.lineEnd = scanner.getCurrentLine();
//        }

        getPositionAndLine();
        if ((t = scanner.scanner().type) != Types.TypeSemicolon) {
            throw new DiagramsException("Ожидалось ';'", scanner);
        }

        /*
         * Переходим на описание функции
         * */
        if (interpreter.isInterpreting()) {
            setPositionAndLine(pos.descriptionMethodAddr.ptr, pos.descriptionMethodAddr.line);
            dataValue = Method();
            interpreter.setExecute(false);
        }

        return dataValue;
    }

    private void ActualParameterList() throws DiagramsException, SemanticsException {
        Tree parameters = null;
        Tree prevParameters = null;

        if (interpreter.isCallFunction() && interpreter.isInterpreting()) {
            parameters = interpreter.peekFunctionCall().right;
            prevParameters = interpreter.peekPrevFunctionCall().right;
        }
        int numberOfActualParameter = 0, numberOfParameter = currentVertex.node.numberOfParameters;
        Tree functionVertex = currentVertex;
        do {
            typeOfV.clear();

            DataValue dataValue = V();

            numberOfActualParameter++;
            if (currentType != DataType.TUserType) {
                functionVertex = root.findParameter(functionVertex, currentType.toString());
            } else {
                functionVertex = root.findParameter(functionVertex, typeName);
            }

            if (interpreter.isInterpreting()) {
                functionVertex.node.dataValue = dataValue;
                if (interpreter.isCallFunction()) {
                    parameters.left.node.dataValue = dataValue;
                    prevParameters.left.node.dataValue = dataValue;
                    parameters = parameters.left;
                    prevParameters = prevParameters.left;
                }
            }

            /*
             * проверить, что тип фактической и формальной переменных совпадает
             * */
            getPositionAndLine();
        }
        while (scanner.scanner().type == Types.TypeComma);

        if (numberOfActualParameter != numberOfParameter) {
            throw new SemanticsException("Количество формальных и фактических переменных не совпадает");
        }

        setPositionAndLine(position, row);
    }

    private void OperatorIF() throws DiagramsException, SemanticsException {
        Types t;
        func.add(true);
        if (scanner.scanner().type != Types.TypeIf) {
            throw new DiagramsException("Ожидалось if", scanner);
        }

        if (scanner.scanner().type != Types.TypeOpenParenthesis) {
            throw new DiagramsException("Ожидалось (", scanner);
        }
        typeOfV.clear();
        DataValue dataValue = V();

        /*
         * проверить, что выражение в скобках
         * имеет тип boolean
         * */
        if (currentType != DataType.TBoolean) {
            throw new SemanticsException("Выржение в скобках должно иметь тип boolean");
        }

        if (scanner.scanner().type != Types.TypeCloseParenthesis) {
            throw new DiagramsException("Ожидалось )", scanner);
        }

        if (scanner.scanner().type != Types.TypeOpenBrace) {
            throw new DiagramsException("Ожидалось {", scanner);
        }

        if (interpreter.isInterpreting() && interpreter.isExecute()) {
            if (dataValue.value.constant) {
                interpreter.setInterpretingIf(true);
            } else {
                interpreter.setInterpretingIf(false);
            }
        }

        //выполняется ветка else
        if (interpreter.isInterpreting() && !interpreter.isInterpretingIf()) {
            interpreter.setAnalyzing(true);
            interpreter.setInterpreting(false);
            Operators();
            interpreter.setInterpreting(true);
            interpreter.setAnalyzing(false);
        } else {
            Operators();
        }

        if (interpreter.isInterpreting() && !interpreter.isEmptyReturnValue()) {
            return;
        }

        if (interpreter.isAnalyzing() && func.size() != 0) {
            func.remove(func.size() - 1);
        }

        t = scanner.scanner().type;
        if (interpreter.isAnalyzing() && t != Types.TypeCloseBrace) {
            throw new DiagramsException("Ожидалось }", scanner);
        }

        getPositionAndLine();
        t = scanner.scanner().type;
        if (interpreter.isAnalyzing() && t != Types.TypeElse && t == Types.TypeSemicolon) {
            setPositionAndLine(position, row);
            return;
        }
        if (t == Types.TypeElse) {

            t = scanner.scanner().type;
            if (interpreter.isAnalyzing() && t != Types.TypeOpenBrace) {
                throw new DiagramsException("Ожидалось {", scanner);
            }

            func.add(true);

            //выполняется if
            if (interpreter.isInterpreting() && interpreter.isInterpretingIf()) {
                interpreter.setAnalyzing(true);
                interpreter.setInterpreting(false);
                Operators();
                interpreter.setInterpreting(true);
                interpreter.setAnalyzing(false);
            } else {
                Operators();
            }

            if (interpreter.isAnalyzing() && func.size() != 0) {
                func.remove(func.size() - 1);
            }
            t = scanner.scanner().type;
            if (interpreter.isAnalyzing() && t != Types.TypeCloseBrace) {
                throw new DiagramsException("Ожидалось }", scanner);
            }
        } else {
            setPositionAndLine(position, row);
        }
    }

    public DataValue V() throws DiagramsException, SemanticsException {
        Types t;
        getPositionAndLine();
        dataOfV.push(new ArrayList<>());
        if (scanner.scanner().type == Types.TypeNegation) {
            return V();
        } else {
            setPositionAndLine(position, row);
            if ((t = scanner.scanner().type) == Types.TypeOr || t == Types.TypeAnd) {
                return A2();
            } else {
                setPositionAndLine(position, row);
            }
        }


        return A2();
    }

    private DataValue A2() throws DiagramsException, SemanticsException {
        A3();
        Types t;
        while ((t = scanner.scanner().type) == Types.TypeLe || t == Types.TypeGe || t == Types.TypeGt
                || t == Types.TypeLt || t == Types.TypeComparison || t == Types.TypeNegation) {
            dataOfV.lastElement().add(scanner.getLexeme());
            if (A3() == Types.TypeError) {
                throw new DiagramsException("Ожидалось выражение", scanner);
            }

            /*
             * так как эти знаки , значит тип boolean
             * предусмотреть также дл равно
             * сравнить тип выражения слева и справа от этих знаков
             * предусмотреть инициализацию (b = 0), ссылка на значение
             * */


            typeOfV.add(DataType.TBooleanSign);
        }
        DataValue dataValue = new DataValue();

        if ((t != Types.TypeSemicolon) && t != Types.TypeCloseParenthesis && t != Types.TypeComma) {
            throw new DiagramsException("Ожидалось ';' или ')' или ','", scanner);
        }

        if (t == Types.TypeCloseParenthesis || t == Types.TypeComma) {
            setPositionAndLine(position, row);
        }


        if (typeOfV.stream().anyMatch(i -> i == DataType.TFunction)) {
            for (int i = 0; i < typeOfV.size(); ) {
                if (typeOfV.get(i) == DataType.TFunction) {
                    typeOfV.remove(i);
                } else {
                    i++;
                }
            }
        }

        if (typeOfV.stream().anyMatch(i -> i == DataType.TBoolean)) {
            if (!typeOfV.stream().allMatch(i -> i == DataType.TBoolean)) {
                throw new SemanticsException("В выражении учасвтвуют несколько типов");
            }
            currentType = DataType.TBoolean;
        } else if (typeOfV.stream().anyMatch(i -> i == DataType.TBooleanSign)) {
            currentType = DataType.TBoolean;
        } else if (typeOfV.stream().anyMatch(i -> i == DataType.TInt) || typeOfV.stream().anyMatch(i -> i == DataType.TConstant)) {
            currentType = DataType.TInt;
        } else if (typeOfV.stream().anyMatch(i -> i == DataType.TUserType)) {
            currentType = DataType.TUserType;
            typeName = currentVertex.node.classLink.node.lexemeName;
        }


        if (interpreter.isInterpreting()) {
            dataValue = parseData();
        }
        dataValue.type = currentType;

        return dataValue;
    }

    private Types A3() throws DiagramsException, SemanticsException {
        A4();
        Types t;
        getPositionAndLine();

        while ((t = scanner.scanner().type) == Types.TypePlus || t == Types.TypeMinus) {
            dataOfV.lastElement().add(scanner.getLexeme());
            if (A4() == Types.TypeError) {
                throw new DiagramsException("Ожидалось выражение", scanner);
            }
            getPositionAndLine();


            if (typeOfV.stream().anyMatch(i -> i == DataType.TBoolean) ||
                    typeOfV.stream().anyMatch(i -> i == DataType.TUserType)) {
                throw new SemanticsException("В выражении участвуют несколько типов");
            }

            /*
             * только типы TInt, TConstant
             * */
        }
        setPositionAndLine(position, row);

        return Types.TypeForReturn;
    }

    private Types A4() throws DiagramsException, SemanticsException {
        A5();
        Types t;
        getPositionAndLine();

        while ((t = scanner.scanner().type) == Types.TypeMultiply ||
                t == Types.TypeDivision || t == Types.TypeMod) {
            dataOfV.lastElement().add(scanner.getLexeme());
            getPositionAndLine();

            /*
             * только типы TInt, TConstant
             * */

            if (typeOfV.stream().anyMatch(i -> i == DataType.TBoolean) ||
                    typeOfV.stream().anyMatch(i -> i == DataType.TUserType)) {
                throw new SemanticsException("В выражении участвуют несколько типов");
            }


            if (A5() == Types.TypeError) {
                throw new DiagramsException("Ожидалось выражение", scanner);
            }
            getPositionAndLine();
        }
        setPositionAndLine(position, row);
        return Types.TypeForReturn;
    }

    public Types A5() throws DiagramsException, SemanticsException {
        Types t;
        getPositionAndLine();
        if ((t = scanner.scanner().type) == Types.TypePlusPlus || t == Types.TypeMinusMinus) {
            dataOfV.lastElement().add(scanner.getLexeme());
            if (typeOfV.stream().anyMatch(i -> i == DataType.TBoolean) ||
                    typeOfV.stream().anyMatch(i -> i == DataType.TUserType) ||
                    typeOfV.stream().anyMatch(i -> i == DataType.TConstant)) {
                throw new SemanticsException("В выражении участвуют несколько типов");
            }

            return A6();
        }

        setPositionAndLine(position, row);
        return A6();
    }

    public Types A6() throws DiagramsException, SemanticsException {
        Types t;
        do {
            if (scanner.getLexeme().type == Types.TypeAssign) {
                dataOfV.lastElement().add(scanner.getLexeme());
            }
            A7();
        }
        while (scanner.getLexeme().type == Types.TypeAssign);
        getPositionAndLine();

        while ((t = scanner.scanner().type) == Types.TypePlusPlus || t == Types.TypeMinusMinus) {
            getPositionAndLine();

            /*
             * проверять, должен быть только тип TInt
             * */

            dataOfV.lastElement().add(scanner.getLexeme());
            if (typeOfV.stream().anyMatch(i -> i == DataType.TBoolean) ||
                    typeOfV.stream().anyMatch(i -> i == DataType.TUserType) ||
                    typeOfV.stream().anyMatch(i -> i == DataType.TConstant)) {
                throw new SemanticsException("В выражении участвуют несколько типов");
            }

        }
        setPositionAndLine(position, row);
        return Types.TypeForReturn;
    }

    public Types A7() throws DiagramsException, SemanticsException {
        Types t;
        DataValue returnValue;
        getPositionAndLine();

        Lexeme checkLexeme = scanner.scanner();
        t = checkLexeme.type;
        if (t == Types.TypeIdent) {
            setPositionAndLine(position, row);
            dataOfV.lastElement().add(scanner.getLexeme());

            int tmpPos = scanner.getPtr();
            int tmpLine = scanner.getCurrentLine();
            if (scanner.scanner().type == Types.TypeOpenParenthesis) {
                setPositionAndLine(tmpPos, tmpLine);
                if (interpreter.isInterpreting()) {
                    interpreter.pushFunctionCallPos(checkLexeme);
                }
                functionCall = false;

                FunctionCall();
                if (interpreter.isInterpreting()) {
                    Tree tmp = root.findByName(root.left, checkLexeme.lexeme.toString());
                    setPositionAndLine(tmp.node.ptrEnd, tmp.node.lineEnd);
                    position = tmp.node.ptrEnd;
                    row = tmp.node.lineEnd;
                }
            } else {
                setPositionAndLine(tmpPos, tmpLine);
            }

            returnValue = ObjectName();

            if (scanner.getLexeme().lexeme.toString().compareTo(";") == 0) {
                setPositionAndLine(position, row);
            }

            /*
             * найдем тип идентификатора
             * добавим его в список типов для выражения
             * */

            if (returnValue != null && returnValue.type != DataType.Empty) {
                typeOfV.add(returnValue.type);
                typeOfV.add(DataType.TFunction);
                /*
                 * обработать возвращаемое значение функции
                 *
                 * */
            } else if (currentVertex.node.type == DataType.TBoolean || currentVertex.node.type == DataType.TInt || currentVertex.node.type == DataType.TUserType) {
                typeOfV.add(currentVertex.node.type);
                //  dataOfV.add(scanner.getLexeme());
            }

            return Types.TypeForReturn;
        } else {
            if (t == Types.TypeConstInt || t == Types.TypeTrue || t == Types.TypeFalse || t == Types.TypeInt) {
                if (t == Types.TypeInt) {
                    currentType = DataType.TInt;
                } else if (t == Types.TypeTrue || t == Types.TypeFalse) {
                    currentType = DataType.TBoolean;
                } else if (t == Types.TypeConstInt) {
                    currentType = DataType.TConstant;
                }

                typeOfV.add(currentType);
                dataOfV.lastElement().add(scanner.getLexeme());
                return t;
            } else if (t == Types.TypeOpenParenthesis) {
                setPositionAndLine(position, row);
                V();
            } else {
                throw new DiagramsException("Ожидалось (/числовая константа/true/false", scanner);
            }
        }
        dataOfV.lastElement().add(scanner.getLexeme());
        if ((t = scanner.scanner().type) != Types.TypeCloseParenthesis) {
            throw new DiagramsException("Ожидалось )", scanner);
        }
        return t;
    }


    public Tree getRoot() {
        return root;
    }

    private DataValue parseData() {
        List<Lexeme> data = dataOfV.pop();
        if (data.stream().anyMatch(item -> item.type == Types.TypeAssign)) {
            for (int i = 0; i < data.size(); ) {
                if (data.get(i).type != Types.TypeAssign) {
                    data.remove(i);
                } else {
                    break;
                }
            }
            data.remove(0);
        }
        Boolean valueBoolean = null;
        Integer valueInt = null;
        if (data.size() == 1) {
            switch (data.get(0).type) {
                case TypeTrue:
                    valueBoolean = true;
                    break;
                case TypeFalse:
                    valueBoolean = true;
                    break;
                case TypeIdent:
                    Tree vertex = retrieveVariable(data.get(0).lexeme.toString(), 0);
                    switch (vertex.node.dataValue.type) {
                        case TInt:
                            valueInt = vertex.node.dataValue.value.valueInt;
                            break;
                        case TBoolean:
                            valueBoolean = vertex.node.dataValue.value.constant;
                            break;
                    }
                    break;
                case TypeInt:
                    valueInt = Integer.parseInt(data.get(0).lexeme.toString());
                    break;
                case TypeConstInt:
                    valueInt = Integer.parseInt(data.get(0).lexeme.toString());
                    break;

            }
        } else {
            for (int i = 0; i < data.size(); i += 3) {
                switch (data.get(i).type) {
                    case TypeConstInt:
                        switch (data.get(i + 2).type) {
                            case TypeInt:
                                if (i == 0) {
                                    valueInt = Integer.parseInt(data.get(i).lexeme.toString());
                                }
                                valueInt = executeOperationInt(
                                        valueInt, data.get(i + 2), data.get(i + 1).type);
                                break;
                            case TypeIdent:
                                valueInt = checkSecondVariableIdentAndExecute(
                                        data.get(i), data.get(i + 2), data.get(i + 1).type, 0);
                                break;
                        }
                        break;
                    case TypeIdent:
                        Tree vertex = null;
                        if (data.get(i + 2).type == Types.TypeIdent) {
                            vertex = retrieveVariable(data.get(i).lexeme.toString(), 1);
                            Lexeme intLexeme = new Lexeme();
                            intLexeme.lexeme.append(vertex.node.dataValue.value.valueInt);
                            intLexeme.type = Types.TypeInt;
                            if (isBooleanOperation(data.get(i + 1).type)) {
                                valueBoolean = checkSecondVariableTypeBool(data.get(i + 2), intLexeme,
                                                                           data.get(i + 1).type, 0);
                            } else {
                                valueInt = checkSecondVariableType(data.get(i + 2), intLexeme,
                                                                   data.get(i + 1).type, 0);
                                break;
                            }
                        } else {
                            vertex = retrieveVariable(data.get(i).lexeme.toString(), 0);
                            Lexeme intLexeme = new Lexeme();
                            intLexeme.lexeme.append(vertex.node.dataValue.value.valueInt);
                            intLexeme.type = Types.TypeInt;
                            if (isBooleanOperation(data.get(i + 1).type)) {
                                valueBoolean = checkSecondVariableTypeBool(data.get(i + 2), intLexeme,
                                                                           data.get(i + 1).type, 1);
                            } else {
                                valueInt = checkSecondVariableType(data.get(i + 2), intLexeme,
                                                                   data.get(i + 1).type, 1);
                                break;
                            }
                        }


                        break;


                }
            }
        }

        DataValue dataValue = new DataValue();
        dataValue.value.valueInt = valueInt;
        dataValue.value.constant = valueBoolean;
        return dataValue;
    }


    private Integer checkSecondVariableIdentAndExecute(Lexeme lexeme2, Lexeme lexeme1, Types operation, int mode) {
        Tree vertex = retrieveVariable(lexeme2.lexeme.toString(), mode);
        return retrieveExecutionWithIdent(vertex, lexeme1, lexeme2, operation);
    }

    private Tree retrieveVariable(String name, int mode) {
        if (interpreter.isInterpreting()) {
            Tree ident = null;
            switch (mode) {
                case 0:
                    if (!interpreter.getPrevFunctionCallInterpreter().isEmpty() &&
                            interpreter.getPrevFunctionCallInterpreter().size() > 1 &&
                            interpreter.penultimatePrevFunctionCall().node.lexemeName.equals(
                                    interpreter.peekPrevFunctionCall().node.lexemeName) ||
                            interpreter.getPrevFunctionCallInterpreter().size() == 1) {
                        ident = root.findByName(interpreter.peekPrevFunctionCall().right, name);
                    }
                    if (ident == null) {
                        ident = root.findByName(interpreter.peekFunctionCall().right, name);
                    }

                    if (ident == null) {
                        ident = root.findByName(root.left, name);
                    }


                    if (ident == null || ident.node.type == DataType.TFunction) {
                        if (!interpreter.getPrevFunctionCallInterpreter().isEmpty() && (interpreter.getPrevFunctionCallInterpreter().size() == 1 ||
                                interpreter.penultimatePrevFunctionCall().node.lexemeName.equals(
                                        interpreter.peekPrevFunctionCall().node.lexemeName))) {
                            return interpreter.peekPrevFunctionCall();
                        }
                        return interpreter.popFunctionCall();
                    }
                    break;
                case 1:
                    if (!interpreter.getPrevFunctionCallInterpreter().isEmpty() &&
                            interpreter.getPrevFunctionCallInterpreter().size() > 1 &&
                            interpreter.penultimatePrevFunctionCall().node.lexemeName.equals(
                                    interpreter.peekPrevFunctionCall().node.lexemeName) ||
                    interpreter.getPrevFunctionCallInterpreter().size() == 1) {
                        ident = root.findByName(interpreter.peekPrevFunctionCall().right, name);
                    }
                    if (ident == null) {
                        ident = root.findByName(interpreter.penultimateFunctionCall().right, name);
                    }

                    if (ident == null) {
                        ident = root.findByName(root.left, name);
                    }


                    if (ident == null || ident.node.type == DataType.TFunction) {
                        if (!interpreter.getPrevFunctionCallInterpreter().isEmpty() && (interpreter.getPrevFunctionCallInterpreter().size() == 1 ||
                                interpreter.penultimatePrevFunctionCall().node.lexemeName.equals(
                                        interpreter.peekPrevFunctionCall().node.lexemeName))) {
                            return interpreter.peekPrevFunctionCall();
                        }
                        return interpreter.popFunctionCall();
                    } break;
            }

            return ident;
        } else {
            return root.findByName(root.left, name);
        }
    }

    private Boolean checkSecondVariableIdentAndExecuteBool(Lexeme lexeme2, Lexeme lexeme1, Types operation, int mode) {
        Tree vertex = retrieveVariable(lexeme2.lexeme.toString(), mode);
        return retrieveExecutionWith(vertex, lexeme1, lexeme2, operation);
    }


    private Integer checkSecondVariableType(Lexeme lexeme2, Lexeme lexeme1, Types operation, int mode) {
        switch (lexeme2.type) {
            case TypeInt:
                return executeOperationInt(lexeme1, lexeme2, operation);
            case TypeConstInt:
                return executeOperationInt(lexeme1, lexeme2, operation);
            case TypeIdent:
                return checkSecondVariableIdentAndExecute(lexeme2, lexeme1, operation, mode);
            default:
                return null;
        }
    }

    private Boolean checkSecondVariableTypeBool(Lexeme lexeme2, Lexeme lexeme1, Types operation, int mode) {
        switch (lexeme2.type) {
            case TypeInt:
                return executeOperationBool(lexeme1, lexeme2, operation);
            case TypeConstInt:
                return executeOperationBool(lexeme1, lexeme2, operation);
            case TypeIdent:
                return checkSecondVariableIdentAndExecuteBool(lexeme1, lexeme2, operation, mode);
            default:
                return null;
        }
    }

    private Integer retrieveExecutionWithIdent(Tree vertex, Lexeme lexeme1, Lexeme lexeme2, Types operation) {
        switch (vertex.node.type) {
            case TInt:
                lexeme2.lexeme.delete(0, lexeme2.lexeme.length());
                lexeme2.lexeme.append(vertex.node.dataValue.value.valueInt);
                lexeme2.type = Types.TypeInt;
                return executeOperationInt(lexeme1, lexeme2, operation);
            case TConstant:
                lexeme2.lexeme.delete(0, lexeme2.lexeme.length());
                lexeme2.lexeme.append(vertex.node.dataValue.value.valueInt);
                lexeme2.type = Types.TypeConstInt;
                return executeOperationInt(lexeme1, lexeme2, operation);
            case TFunction:
                switch (vertex.node.dataValue.type) {
                    case TInt:
                        lexeme2 = new Lexeme();
                        lexeme2.type = Types.TypeInt;
                        lexeme2.lexeme.append(vertex.node.dataValue.value.valueInt);
                        return executeOperationInt(lexeme1, lexeme2, operation);
                    case TConstant:
                        lexeme2 = new Lexeme();
                        lexeme2.type = Types.TypeInt;
                        lexeme2.lexeme.append(vertex.node.dataValue.value.valueInt);
                        return executeOperationInt(lexeme1, lexeme2, operation);
                    /*
                     * добавить boolean
                     * */
                }
            default:
                return null;
        }
    }

    private Boolean retrieveExecutionWith(Tree vertex, Lexeme lexeme1, Lexeme lexeme2, Types operation) {
        switch (vertex.node.type) {
            case TInt:
                return executeOperationBool(lexeme1, lexeme2, operation);
            case TConstant:
                return executeOperationBool(lexeme1, lexeme2, operation);
            default:
                return null;
        }
    }

    private int executeOperationInt(Integer item, Lexeme lexeme2, Types operation) {
        switch (operation) {
            case TypePlus:
                return isInt(item, lexeme2, operation);
            case TypeMinus:
                return isInt(item, lexeme2, operation);
            case TypeDivision:
                return isInt(item, lexeme2, operation);
            case TypeMultiply:
                return isInt(item, lexeme2, operation);

            default:
                return Integer.MIN_VALUE;
        }
    }

    private Boolean executeOperationBool(Lexeme lexeme1, Lexeme lexeme2, Types operation) {
        switch (operation) {
            case TypeGe:
                return isBoolean(lexeme1, lexeme2, operation);
            case TypeLe:
                return isBoolean(lexeme1, lexeme2, operation);
            case TypeGt:
                return isBoolean(lexeme1, lexeme2, operation);
            case TypeComparison:
                return isBoolean(lexeme1, lexeme2, operation);
        }
        return null;
    }

    private int executeOperationInt(Lexeme lexeme1, Lexeme lexeme2, Types operation) {
        switch (operation) {
            case TypePlus:
                return isInt(lexeme1, lexeme2, operation);
            case TypeMinus:
                return isInt(lexeme1, lexeme2, operation);
            case TypeDivision:
                return isInt(lexeme1, lexeme2, operation);
            case TypeMultiply:
                return isInt(lexeme1, lexeme2, operation);
            default:
                return Integer.MIN_VALUE;
        }
    }

    private Integer isInt(Integer item, Lexeme lexeme2, Types operation) {
        if (lexeme2.type == Types.TypeConstInt) {
            return operationsInt(item, Integer.parseInt(lexeme2.lexeme.toString()), operation);
        }
        return null;
    }

    private Integer isInt(Lexeme lexeme1, Lexeme lexeme2, Types operation) {
        if (lexeme2.type == Types.TypeConstInt || lexeme2.type == Types.TypeInt) {
            return operationsInt(Integer.parseInt(lexeme1.lexeme.toString()),
                                 Integer.parseInt(lexeme2.lexeme.toString()), operation);
        }
        return null;
    }

    private boolean isBooleanOperation(Types type) {
        switch (type) {
            case TypeGe:
                return true;
            case TypeLe:
                return true;
            case TypeGt:
                return true;
            case TypeComparison:
                return true;
            default:
                return false;
        }
    }

    private Boolean isBoolean(Lexeme lexeme1, Lexeme lexeme2, Types operation) {
        if (lexeme1.type == Types.TypeInt || lexeme2.type == Types.TypeConstInt) {
            return operationsBoolean(Integer.parseInt(lexeme1.lexeme.toString()),
                                     Integer.parseInt(lexeme2.lexeme.toString()), operation);
        }
        return null;
    }

    private Integer operationsInt(Integer value1, Integer value2, Types operation) {
        switch (operation) {
            case TypePlus:
                return value1 + value2;
            case TypeMinus:
                return value1 - value2;
            case TypeDivision:
                return value1 / value2;
            case TypeMultiply:
                return value1 * value2;
            default:
                return null;

        }
    }

    private Boolean operationsBoolean(Integer value1, Integer value2, Types operation) {
        switch (operation) {
            case TypeGe:
                return value1 >= value2;
            case TypeLe:
                return value1 <= value2;
            case TypeGt:
                return value1 > value2;
            case TypeLt:
                return value1 < value2;
            case TypeComparison:
                return value1.equals(value2);
            default:
                return null;
        }
    }

    private void remember(InterpreterEnum interpreterEnum, Lexeme lexeme) {
        Pos pos = new Pos();
        if (interpreter.isInterpreting()) {
            Tree tree = root.findByName(root.left, lexeme.lexeme.toString());

            switch (interpreterEnum) {
                case CALL_VAR_POINT_ADDR:
                    pos.setCallVarPointAddr(lexeme, tree.node.classLink);
                    interpreter.pushVarCall(pos);
                    break;
                case CALL_METHOD_POINT_ADDR:
                    pos.setCallMethodPointAddr(lexeme, tree.node.classLink);
                    interpreter.pushFunctionCallPos(pos);
                    break;
                case CALL_CLASS_POINT_ADDR:
                    pos.callClassPointAddr = scanner.getLexeme(); break;
                case DESCRIPTION_CLASS_ADDR:
                    pos.descriptionMethodAddr = scanner.getLexeme(); break;
                case DESCRIPTION_METHOD_ADDR:
                    pos.descriptionClassAddr = scanner.getLexeme(); break;
            }
        }
    }

    /**
     * Установить значение переменной
     * Если переменная находится в функции - значение устанавливается в локальной копии из стека
     * если в классе - сслыка на склаа (елси это не TestClass)
     *
     * @param dataValue значение
     */
    public DataValue setVarValue(DataValue dataValue) {
        if (interpreter.isInterpreting()) {
            Tree tree = null;
            Tree prev = null;
            Pos pos = interpreter.popVarCall();
            if (!pos.callVarPointAddr.lexeme.toString().equals("main")) {
                if (pos.classLinkVar.node.type == DataType.TFunction) {
                    Collections.reverse(interpreter.getPrevFunctionCallInterpreter());
                    for (Tree item : interpreter.getPrevFunctionCallInterpreter()) {
                        prev = item.findByName(item.right, pos.callVarPointAddr.lexeme.toString());
                        if (prev != null) {
                            prev.node.dataValue = dataValue;
                            break;
                        }
                    }
                    Collections.reverse(interpreter.getPrevFunctionCallInterpreter());
                }
            }

            if (pos.classLinkVar.node.type == DataType.TFunction) {
                Collections.reverse(interpreter.getFunctionCallInterpreter());
                for (Tree item : interpreter.getFunctionCallInterpreter()) {
                    tree = item.findByName(item.right, pos.callVarPointAddr.lexeme.toString());
                    if (tree != null) {
                        break;
                    }
                }
                Collections.reverse(interpreter.getFunctionCallInterpreter());

            } else {
                tree = root.findByName(root.left, pos.callVarPointAddr.lexeme.toString());
            }
            tree.node.dataValue = dataValue;
        }
        return dataValue;
    }

    private DataValue setFunctionValue(DataValue dataValue) {
        if(dataValue.type == DataType.Empty)
            return dataValue;
        if (interpreter.isInterpreting()) {
            Tree tree = interpreter.getFunctionCallInterpreter().peek();
            Tree prev = interpreter.peekPrevFunctionCall();
            prev.node.dataValue = dataValue;
            tree.node.dataValue = dataValue;
        }
        return dataValue;
    }

    private DataValue setValue(DataValue dataValue, Tree tree) {
        if (interpreter.isInterpreting() && tree != null) {
            tree.node.dataValue = dataValue;
        }
        return dataValue;
    }

}
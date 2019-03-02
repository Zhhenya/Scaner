package diagrams;

import interpreter.Interpreter;
import org.apache.log4j.Logger;
import scanner.Lexeme;
import scanner.Scanner;
import service.DataType;
import service.DiagramsException;
import service.SemanticsException;
import service.Types;
import tree.DataValue;
import tree.Tree;
import tree.Value;

import java.util.ArrayList;
import java.util.List;

public class Diagrams {
    private static Logger LOGGER = Logger.getLogger(Diagrams.class);
    private Scanner scanner;
    private Lexeme lexeme = new Lexeme();
    private int position;
    private int row;
    private List<Boolean> brace = new ArrayList<Boolean>();
    private List<Boolean> func = new ArrayList<Boolean>();
    Tree root = new Tree();
    Tree reset;
    DataType currentType = null;
    Lexeme currentLexeme = new Lexeme();
    Tree currentVertex = null;
    StringBuilder nameCurrentFunction = new StringBuilder();
    List<DataType> typeOfV = new ArrayList<DataType>();
    List<String> dataOfV = new ArrayList<>();
    boolean functionCall = false;
    String typeName = "";
    Interpreter interpreter;
    Value value = new Value();


    public Diagrams(Scanner scanner, StringBuilder lexeme, Interpreter interpreter) {
        this.scanner = scanner;
        this.lexeme.lexeme = lexeme;
        this.interpreter = interpreter;
    }

    private void setPositionAndLine(int position, int numberOfRow) {
        scanner.setPtr(position);
        scanner.setCurrentLine(row);
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

    public Types S() throws DiagramsException, SemanticsException {
        Lexeme t = new Lexeme();

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

        Tree vertex = root.include(scanner.getLexeme().lexeme.toString(), DataType.TClass, null);

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

        return scanner.getLexeme().type;
    }


    public void Content() throws DiagramsException, SemanticsException {
        Types t = null;
        do {
            getPositionAndLine();

            if ((t = scanner.scanner().type) == Types.TypeIdent || t == Types.TypeInt || (t) == Types.TypeBoolean || t == Types.TypeVoid) {
                if ((t = scanner.scanner().type) == Types.TypeIdent || t == Types.Typemain) {
                    if ((t = scanner.scanner().type) == Types.TypeOpenParenthesis) {
                        setPositionAndLine(position, row);
                        Method();
                        continue;

                    }
                }
            }

            setPositionAndLine(position, row);
            t = scanner.scanner().type;
            if ((t == Types.TypeIdent || t == Types.TypeInt) && ((t = scanner.scanner().type) == Types.TypeOpenBrace || t == Types.TypeOpenParenthesis)) {
                throw new DiagramsException("Ожидалось объявление класса или функции",  scanner);
            }


            setPositionAndLine(position, row);
            if ((t = scanner.scanner().type) == Types.TypeIdent || t == Types.TypeInt || (t) == Types.TypeBoolean) {
                getPositionAndLine();
                currentLexeme.delete(0, currentLexeme.length());
                currentLexeme.append(lexeme.lexeme.toString());


            }
            if ((t = scanner.scanner().type) == Types.TypeIdent) {
                /*
                 *найти лексемму с таким типом  ???????(как они там появятся)
                 * */
                currentType = root.getType(scanner.getLexeme().lexeme.toString());
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
            scanner.setPtr(position);
        }
    }

    public void Method() throws DiagramsException, SemanticsException {
        Types t;

        ReturnType();

        /*
         * Определить тип лексеммы
         * */
        currentType = root.getType(lexeme.lexeme.toString());


        if ((t = scanner.scanner().type) != Types.TypeIdent && t != Types.Typemain) {
            throw new DiagramsException("Ожидалось идентификатор или main", scanner);
        }


        /*
         * занести  в дерево идентификатор с типом
         * */
        Tree vertex = root.include(lexeme.lexeme.toString(), DataType.TFunction, currentType, null);

        if (scanner.scanner().type != Types.TypeOpenParenthesis) {
            throw new DiagramsException("Ожидалось (", scanner);
        }

        FormalParameterList();

        if (scanner.scanner().type != Types.TypeCloseParenthesis) {
            throw new DiagramsException("Ожидалось )", scanner);
        }

        Block();


        /*
         * восстановить значение указателя
         * */
        root.setCurrent(vertex);
    }

    public void ReturnType() throws DiagramsException {
        Types t;
        if ((t = scanner.scanner().type) != Types.TypeVoid &&
                t != Types.TypeIdent && t != Types.TypeBoolean &&
                t != Types.TypeInt) {
            throw new DiagramsException("Ожидался тип или идентификатор", scanner);
        }
    }

    public void FormalParameterList() throws DiagramsException, SemanticsException {
        Types t;
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
        while ((t = scanner.scanner().type) == Types.TypeComma);
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
        currentType = root.getType(lexeme.lexeme.toString());
        if (currentType == DataType.TClass) {
            currentType = DataType.TUserType;
            currentVertex = root.getClass(lexeme.lexeme.toString());
        }


        if ((t = scanner.scanner().type) != Types.TypeIdent) {
            throw new DiagramsException("Ожидался идентификатор", scanner);
        }

        /*
         * занести идентификатор с типом в дерево
         * определить ссылку на класс
         * */
        currentVertex = root.include(lexeme.lexeme.toString(), currentType, currentType == DataType.TUserType ?
                currentVertex.node.lexemeName : null);

        /*
         * нужно ли делать возврат
         * */


    }

    public void Block() throws DiagramsException, SemanticsException {
        Types t;

        if ((t = scanner.scanner().type) != Types.TypeOpenBrace) {
            throw new DiagramsException("Ожидалось {", scanner);
        }

        Operators();

        if ((t = scanner.scanner().type) != Types.TypeCloseBrace) {
            throw new DiagramsException("Ожидалось }", scanner);
        }

    }

    public void Operators() throws DiagramsException, SemanticsException {
        Types t;

        while (true) {
            getPositionAndLine();
            t = scanner.scanner().type;
            if (t == Types.TypeIdent && ((t = scanner.scanner().type) == Types.TypePlusPlus || t == Types.TypeMinusMinus)) {
                setPositionAndLine(position, row);
                typeOfV.clear();
                V();
                continue;
            }
            setPositionAndLine(position, row);
            if ((t = scanner.scanner().type) == Types.TypePlusPlus || t == Types.TypeMinusMinus || t == Types.TypeNegation) {
                setPositionAndLine(position, row);
                typeOfV.clear();
                V();
                continue;
            }
            setPositionAndLine(position, row);
            if (((t = scanner.scanner().type) == Types.TypeIdent || t == Types.TypeConstInt) && ((t = scanner.scanner().type) == Types.TypePlus || t == Types.TypeMinus)) {
                setPositionAndLine(position, row);
                typeOfV.clear();
                V();
                continue;
            }


            setPositionAndLine(position, row);
            if (((((t = scanner.scanner().type)) == Types.TypeIdent || t == Types.TypeBoolean || t == Types.TypeInt)) &&
                    (t = scanner.scanner().type) != Types.TypeDot && t != Types.TypeOpenParenthesis) {
                setPositionAndLine(position, row);
                LOGGER.info("Operators: " + t);
                LOGGER.info(scanner.getCurrentItem());
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
                continue;
            } else if (t == Types.TypeIdent) {
                if ((t = scanner.scanner().type) == Types.TypeDot) {
                    setPositionAndLine(position, row);
                    Operator();
                    continue;
                } else if (t == Types.TypeOpenParenthesis) {
                    setPositionAndLine(position, row);
                    Operator();
                    continue;
                }
            } else if (t == Types.TypeOpenBrace) {
                setPositionAndLine(position, row);
                Block();
                continue;
            } else if (t == Types.TypeReturn) {
                getPositionAndLine();
                if ((t = scanner.scanner().type) == Types.TypeSemicolon) {

                    Tree vertex = root.getFunction();
                    if (vertex.node.returnType != DataType.TVoid) {
                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                    }

                    continue;
                }

                setPositionAndLine(position, row);
                if (((t = scanner.scanner().type) == Types.TypeIdent || t == Types.TypeConstInt || t == Types.TypeFalse || t == Types.TypeFalse)
                        && (scanner.scanner().type) == Types.TypeSemicolon) {

                    Tree vertex = root.getFunction();


                    if (t == Types.TypeConstInt || t == Types.TypeInt) {
                        if (vertex.node.returnType != DataType.TConstant && vertex.node.returnType != DataType.TInt) {
                            throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                        }
                    }

                    if (t == Types.TypeFalse || t == Types.TypeTrue) {
                        if (vertex.node.returnType != DataType.TBoolean) {
                            throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                        }
                    }

                    if (t == Types.TypeIdent) {
                        setPositionAndLine(position, row);
                        scanner.scanner();
                        Tree returnValue = root.getTypeOfLexeme(lexeme.lexeme.toString());
                        scanner.scanner();
                        if (returnValue.node.type != vertex.node.returnType) {
                            throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                        }
                    }
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
                        if (currentType != currentVertex.node.returnType) {
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


                if ((t = scanner.scanner().type) == Types.TypeCloseBrace) {
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
                continue;
            } else if (t == Types.TypeSemicolon) {
                continue;
            } else if (t == Types.TypeCloseBrace && brace.size() != 0) {
                brace.remove(brace.size() - 1);
                continue;
            } else {
                setPositionAndLine(position, row);
                return;
            }
        }
    }

    public void VariableDescription() throws DiagramsException, SemanticsException {
        Types t;
        int currentPos = -1;

        getPositionAndLine();
        if ((t = scanner.scanner().type) != Types.TypeIdent && t != Types.TypeBoolean && t != Types.TypeInt) {
            throw new DiagramsException("Ожидался тип переменной", scanner);
        }


        /*
         * определить тип переменной
         * */
        currentType = root.getType(lexeme.lexeme.toString());
        if (currentType == DataType.TClass) {
            currentVertex = root.getClass(lexeme.lexeme.toString());
            currentType = DataType.TUserType;
        }

        currentPos = scanner.getPtr();
        if ((t = scanner.scanner().type) == Types.TypeAssign) {
            setPositionAndLine(position, row);
        } else {
            setPositionAndLine(currentPos, row);
        }
        ListOfIdentifiers();
    }

    public void ListOfIdentifiers() throws DiagramsException, SemanticsException {
        Types t = null;
        String typeIdent;
        Tree vertex = null;
        int count = 0;
        do {

            if ((t = scanner.scanner().type) != Types.TypeIdent) {
                throw new DiagramsException("Ожидался идентификатор", scanner);
            }

            /*
             * заносим идентификатор с типом currentType в дерево
             * */
            vertex = root.include(lexeme.lexeme.toString(), currentType, currentType == DataType.TUserType ?
                    currentVertex.node.lexemeName : null);
            typeIdent = currentType.toString();

            getPositionAndLine();
            if ((t = scanner.scanner().type) == Types.TypeAssign) {
                typeOfV.clear();
                V();



             //   vertex.node.dataValue.value. =
                /*
                 * в V() вычисляется новый currentType (тип выражения),
                 * затем сравнивается с типом объявленной переменной(идентификатора)
                 * */

                if (typeIdent != currentType.toString()) {
                    if (currentVertex.node != null && currentVertex.node.type == DataType.TFunction) {
                        if (currentVertex.node.returnType.toString() != typeIdent) {
                            throw new SemanticsException("Тип формальной и фактической переменной не совпадают: ",
                                    currentVertex.node.returnType + " " + typeIdent);
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

    public void Operator() throws DiagramsException, SemanticsException {
        Types t = null, nextT = null;
        getPositionAndLine();
        if ((t = scanner.scanner().type) == Types.TypeIf) {
            setPositionAndLine(position, row);
            OperatorIF();
            return;
        }


        setPositionAndLine(position, row);
        if ((t = scanner.scanner().type) == Types.TypeIdent && (t = scanner.scanner().type) == Types.TypeOpenParenthesis) {
            setPositionAndLine(position, row);
            FunctionCall();
            return;
        }

        setPositionAndLine(position, row);
        if ((t = scanner.scanner().type) == Types.TypeIdent) {
            if ((t = scanner.scanner().type) == Types.TypeDot) {
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
            V();

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
            if ((t = scanner.scanner().type) == Types.TypeSemicolon) {
                Tree vertex = root.getFunction();
                if (vertex.node.returnType != DataType.TVoid) {
                    throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                }
                return;
            }

            setPositionAndLine(position, row);
            if (((t = scanner.scanner().type) == Types.TypeIdent || t == Types.TypeConstInt || t == Types.TypeFalse || t == Types.TypeTrue)
                    && (nextT = scanner.scanner().type) == Types.TypeSemicolon) {

                Tree vertex = root.getFunction();

                if (t == Types.TypeConstInt || t == Types.TypeInt) {
                    if (vertex.node.returnType != DataType.TConstant && vertex.node.returnType != DataType.TInt) {
                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                    }
                }

                if (t == Types.TypeFalse || t == Types.TypeTrue) {
                    if (vertex.node.returnType != DataType.TBoolean) {
                        throw new SemanticsException("Тип возвращаемого значения не совпадает: функция " + vertex.node.lexemeName);
                    }
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
                    V();

                    /*
                     * проверить тип возвращаемого значения (выражения)
                     * */
                    if (currentType != currentVertex.node.returnType) {
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
            return;
        }

    }

    public DataType ObjectName() throws DiagramsException, SemanticsException {
        Types t;
        DataType returnType;
        getPositionAndLine();
        int startPosition = scanner.getPtr();
        if ((t = scanner.scanner().type) != Types.TypeIdent) {
            throw new DiagramsException("Ожидался идентификатор", scanner);
        }


        /*
         * нужно проверить, есть ли такой путь до этого объекта
         * для этого проверяем каждый идентификатор:определен ли он как тип класс
         * последний идентификатор проверяем как идентификатор (он только int или boolean)
         * */

        currentVertex = root.getObjectName(lexeme.lexeme.toString(), root.getCurrent());

        getPositionAndLine();
        while ((t = scanner.scanner().type) == Types.TypeDot) {
            getPositionAndLine();
            t = scanner.scanner().type;
            if (t != Types.TypeIdent) {
                throw new DiagramsException("Ожидался идентификатор", scanner);
            }

            /*
             * проверяем, что такой класс объявлен
             * */

            if (currentVertex.node.classLink == null) {
                throw new SemanticsException("Неверный вызов класса");
            }
            currentVertex = root.getObjectNameForClass(lexeme.lexeme.toString(), currentVertex.node.classLink);

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
            returnType = FunctionCall();
            typeOfV.clear();
            functionCall = true;
            return returnType;
        }


        if (t != Types.TypeAssign) {
            setPositionAndLine(position, row);
        }
        if (t == Types.TypeError) {
            throw new DiagramsException("Ожидался идентификатор", scanner);
        }
        return null;
    }

    public DataType FunctionCall() throws DiagramsException, SemanticsException {
        Types t;
        DataType returnType;
        if ((t = scanner.scanner().type) != Types.TypeIdent) {
            throw new DiagramsException("Ожидался идентификатор", scanner);
        }

        nameCurrentFunction.delete(0, nameCurrentFunction.length());
        nameCurrentFunction.append(lexeme.lexeme);
        /*
         * получить имя функции (получим всю вершину)
         * */
        if (!functionCall) {
            currentVertex = root.getFunction(lexeme.lexeme.toString());
        } else {
            functionCall = false;
        }
        returnType = currentVertex.node.returnType;

        if ((t = scanner.scanner().type) != Types.TypeOpenParenthesis) {
            throw new DiagramsException("Ожидалось '('", scanner);
        }


        ActualParameterList();

        if ((t = scanner.scanner().type) != Types.TypeCloseParenthesis) {
            throw new DiagramsException("Ожидалось ')'", scanner);
        }
        getPositionAndLine();
        if ((t = scanner.scanner().type) != Types.TypeSemicolon) {
            throw new DiagramsException("Ожидалось ';'", scanner);
        }

        return returnType;
    }

    public void ActualParameterList() throws DiagramsException, SemanticsException {
        Types t;
        int numberOfActualParameter = 0, numberOfParameter = currentVertex.node.numberOfParameters;
        String functionName = currentVertex.node.lexemeName;
        Tree functionVertex = currentVertex;
        do {
            typeOfV.clear();
            V();
            numberOfActualParameter++;
            if (currentType != DataType.TUserType) {
                functionVertex = root.findParameter(functionVertex, currentType.toString());
            } else {
                functionVertex = root.findParameter(functionVertex, typeName);
            }

            /*
             * проверить, что тип фактической и формальной переменных совпадает
             * */
            getPositionAndLine();
        }
        while ((t = scanner.scanner().type) == Types.TypeComma);

        if (numberOfActualParameter != numberOfParameter) {
            throw new SemanticsException("Количество формальных и фактических переменных не совпадает");
        }

        setPositionAndLine(position, row);
    }

    public void OperatorIF() throws DiagramsException, SemanticsException {
        Types t;
        func.add(true);
        if ((t = scanner.scanner().type) != Types.TypeIf) {
            throw new DiagramsException("Ожидалось if", scanner);
        }

        if ((t = scanner.scanner().type) != Types.TypeOpenParenthesis) {
            throw new DiagramsException("Ожидалось (", scanner);
        }
        typeOfV.clear();
        V();


        /*
         * проверить, что выражение в скобках
         * имеет тип boolean
         * */
        if (currentType != DataType.TBoolean) {
            throw new SemanticsException("Выржение в скобках должно иметь тип boolean");
        }

        if ((t = scanner.scanner().type) != Types.TypeCloseParenthesis) {
            throw new DiagramsException("Ожидалось )", scanner);
        }

        if ((t = scanner.scanner().type) != Types.TypeOpenBrace) {
            throw new DiagramsException("Ожидалось {",  scanner);
        }

        Operators();

        if (func.size() != 0) {
            func.remove(func.size() - 1);
        }

        if ((t = scanner.scanner().type) != Types.TypeCloseBrace) {
            throw new DiagramsException("Ожидалось }",  scanner);
        }

        getPositionAndLine();
        if ((t = scanner.scanner().type) != Types.TypeElse && t == Types.TypeSemicolon) {
            setPositionAndLine(position, row);
            return;
        }
        if (t == Types.TypeElse) {

            if ((t = scanner.scanner().type) != Types.TypeOpenBrace) {
                throw new DiagramsException("Ожидалось {",  scanner);
            }

            func.add(true);

            Operators();

            if (func.size() != 0) {
                func.remove(func.size() - 1);
            }
            if ((t = scanner.scanner().type) != Types.TypeCloseBrace) {
                throw new DiagramsException("Ожидалось }", scanner);
            }
            return;
        } else {
            setPositionAndLine(position, row);
        }
    }

    public Types V() throws DiagramsException, SemanticsException {
        Types t;
        getPositionAndLine();
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

    public Types A2() throws DiagramsException, SemanticsException {
        A3();
        Types t;
        while ((t = scanner.scanner().type) == Types.TypeLe || t == Types.TypeGe || t == Types.TypeGt || t == Types.TypeLt || t == Types.TypeComparison || t == Types.TypeNegation) {
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




        dataValue.type = currentType;
        dataValue.value = value;

        return Types.TypeForReturn;
    }

    public Types A3() throws DiagramsException, SemanticsException {
        A4();
        Types t;
        getPositionAndLine();

        while ((t = scanner.scanner().type) == Types.TypePlus || t == Types.TypeMinus) {
            if (A4() == Types.TypeError) {
                throw new DiagramsException("Ожидалось выражение",  scanner);
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

    public Types A4() throws DiagramsException, SemanticsException {
        A5();
        Types t;
        getPositionAndLine();

        while ((t = scanner.scanner().type) == Types.TypeMultiply ||
                t == Types.TypeDivision || t == Types.TypeMod) {
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
        A7();
        getPositionAndLine();

        while ((t = scanner.scanner().type) == Types.TypePlusPlus || t == Types.TypeMinusMinus) {
            getPositionAndLine();

            /*
             * проверять, должен быть только тип TInt
             * */

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
        DataType returnType;
        getPositionAndLine();
        if ((t = scanner.scanner().type) == Types.TypeIdent) {
            setPositionAndLine(position, row);
            returnType = ObjectName();
            functionCall = false;

            if (lexeme.lexeme.toString().compareTo(";") == 0) {
                setPositionAndLine(position, row);
            }

            /*
             * найдем тип идентификатора
             * добавим его в список типов для выражения
             * */

            if (returnType != null) {
                typeOfV.add(returnType);
                typeOfV.add(DataType.TFunction);
                /*
                * обработать возвращаемое значение функции
                *
                * */
            } else if (currentVertex.node.type == DataType.TBoolean || currentVertex.node.type == DataType.TInt || currentVertex.node.type == DataType.TUserType) {
                typeOfV.add(currentVertex.node.type);
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
                dataOfV.add(scanner.getLexeme().lexeme.toString());

                return t;
            } else if (t == Types.TypeOpenParenthesis) {
                setPositionAndLine(position, row);
                dataOfV.add(scanner.getLexeme().lexeme.toString());
                V();
            } else {
                throw new DiagramsException("Ожидалось (/числовая константа/true/false",  scanner);
            }
        }
        dataOfV.add(scanner.getLexeme().lexeme.toString());
        if ((t = scanner.scanner().type) != Types.TypeCloseParenthesis) {
            throw new DiagramsException("Ожидалось )",  scanner);
        }
        return t;
    }


    public Tree getRoot() {
        return root;
    }

    public Tree getReset() {
        return reset;
    }
}
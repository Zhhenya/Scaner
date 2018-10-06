import java.io.*;

public class Scaner {
    private final int MAX_LENGTH_LEXEMES = 100;
    private final int MAX_NUMBER_KEYWORDS = 11;
    private String text;
    private int currentIndexPosition = 0;
    private int numberOfRow = 1;//номер строки
    private String filePath;
    private BufferedReader reader;

    public void setNumberOfRow(int numberOfRow){
        this.numberOfRow = numberOfRow;
    }
    public int getNumberOfRow(){
        return numberOfRow;
    }

    public Types scaner(StringBuilder lexeme) {
        lexeme.delete(0, lexeme.length());
        while (true) {
            skipSymbols();

            //пропуск комментариев
            if (text.charAt(currentIndexPosition) == '/') {
                //однострочный
                if (text.charAt(currentIndexPosition + 1) == '/') {
                    currentIndexPosition += 2;
                    while (text.charAt(currentIndexPosition) != '\n')
                        currentIndexPosition++;
                    newLine();
                } else
                    //многострочный
                    if (text.charAt(currentIndexPosition + 1) == 42) {
                        currentIndexPosition += 2;
                        while (true) {
                            if (text.charAt(currentIndexPosition) == 42 && text.charAt(currentIndexPosition + 1) == 47) {
                                currentIndexPosition += 2;
                                break;
                            }
                            newLine();
                            if (text.charAt(currentIndexPosition) == '#')
                                return Types.TypeEnd;
                            else
                                currentIndexPosition++;
                        }
                    } else
                        break;

            } else
                break;

        }

        //если встретился символ конца
        if (text.charAt(currentIndexPosition) == '\0' || text.charAt(currentIndexPosition) == '#') {
            lexeme.append("#");
            return Types.TypeEnd;
        }


        //константа
        if (text.charAt(currentIndexPosition) >= '0' && text.charAt(currentIndexPosition) <= '9') {
            lexeme.append(text.charAt(currentIndexPosition));
            currentIndexPosition++;
            while (text.charAt(currentIndexPosition) >= '0' && text.charAt(currentIndexPosition) <= '9') {
                if (lexeme.length() < MAX_LENGTH_LEXEMES - 1)
                    lexeme.append(text.charAt(currentIndexPosition++));
                else
                    currentIndexPosition++;
            }
            return Types.TypeConstInt;
        } else if (text.charAt(currentIndexPosition) >= 'a' && text.charAt(currentIndexPosition) <= 'z' ||
                text.charAt(currentIndexPosition) >= 'A' && text.charAt(currentIndexPosition) <= 'Z') {
            lexeme.append(text.charAt(currentIndexPosition++));

            //идентификатор
            while (text.charAt(currentIndexPosition) >= '0' && text.charAt(currentIndexPosition) <= '9' ||
                    text.charAt(currentIndexPosition) >= 'a' && text.charAt(currentIndexPosition) <= 'z' ||
                    text.charAt(currentIndexPosition) >= 'A' && text.charAt(currentIndexPosition) <= 'Z')
                if (lexeme.length() < MAX_LENGTH_LEXEMES - 1)
                    lexeme.append(text.charAt(currentIndexPosition++));
                else {
                    currentIndexPosition++;
                    return printError("Слишком длинный идентификатор", lexeme, new StringBuilder());
                }

            for (int j = 0; j < MAX_NUMBER_KEYWORDS; j++) {
                if (KeyWords.keyWords[j].equalsIgnoreCase(lexeme.toString()))
                    return Types.valueOf("Type" + KeyWords.keyWords[j]);
            }
            return Types.TypeIdent;
        } else if (text.charAt(currentIndexPosition) == ',')
            return getTypes(lexeme, Types.TypeComma);
        else if (text.charAt(currentIndexPosition) == ';')
            return getTypes(lexeme, Types.TypeSemicolon);
        else if (text.charAt(currentIndexPosition) == '(')
            return getTypes(lexeme, Types.TypeOpenParenthesis);
        else if (text.charAt(currentIndexPosition) == ')')
            return getTypes(lexeme, Types.TypeCloseParenthesis);
        else if (text.charAt(currentIndexPosition) == '{')
            return getTypes(lexeme, Types.TypeOpenBrace);
        else if (text.charAt(currentIndexPosition) == '}')
            return getTypes(lexeme, Types.TypeCloseBrace);
        else if (text.charAt(currentIndexPosition) == '+') {
            if (text.charAt(currentIndexPosition + 1) == '+') {
                lexeme.append(text.charAt(currentIndexPosition++));
                return getTypes(lexeme, Types.TypePlusPlus);
            }
            return getTypes(lexeme, Types.TypePlus);
        } else if (text.charAt(currentIndexPosition) == '-') {
            if (text.charAt(currentIndexPosition + 1) == '-') {
                lexeme.append(text.charAt(currentIndexPosition++));
                return getTypes(lexeme, Types.TypeMinusMinus);
            }
            return getTypes(lexeme, Types.TypeMinus);
        } else if (text.charAt(currentIndexPosition) == '/')
            return getTypes(lexeme, Types.TypeDivision);
        else if(text.charAt(currentIndexPosition) == '.')
                return getTypes(lexeme, Types.TypeDot);
        else if (text.charAt(currentIndexPosition) == '%')
            return getTypes(lexeme, Types.TypeMod);
        else if (text.charAt(currentIndexPosition) == '*')
            return getTypes(lexeme, Types.TypeMultiply);
        else if (text.charAt(currentIndexPosition) == '<') {
            if (text.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(text.charAt(currentIndexPosition++));
                return getTypes(lexeme, Types.TypeLe);
            }
            return getTypes(lexeme, Types.TypeLt);
        } else if (text.charAt(currentIndexPosition) == '>') {
            if (text.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(text.charAt(currentIndexPosition++));
                return getTypes(lexeme, Types.TypeGe);
            }
            return getTypes(lexeme, Types.TypeGt);
        } else if (text.charAt(currentIndexPosition) == '!') {
            if (text.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(text.charAt(currentIndexPosition++));
                lexeme.append(text.charAt(currentIndexPosition++));
                return getTypes(lexeme, Types.TypeNegation);
            }
            // return printError("Неверный входной символ", lexeme, new StringBuilder().append(text.charAt(currentIndexPosition)));
            return getTypes(lexeme, Types.TypeNegation);
        }
        if (text.charAt(currentIndexPosition) == '=') {
            if (text.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(text.charAt(currentIndexPosition++));
                lexeme.append(text.charAt(currentIndexPosition++));
                return getTypes(lexeme, Types.TypeComparison);
            }
            return getTypes(lexeme, Types.TypeAssign);
        } else if (text.charAt(currentIndexPosition) == '&') {
            if (text.charAt(currentIndexPosition + 1) == '&') {
                lexeme.append(text.charAt(currentIndexPosition++));
                lexeme.append(text.charAt(currentIndexPosition++));
                return getTypes(lexeme, Types.TypeAnd);
            } else
                return printError("Неверный входной символ", lexeme, new StringBuilder().append(text.charAt(currentIndexPosition)));

        } else if (text.charAt(currentIndexPosition) == '|') {
            if (text.charAt(currentIndexPosition) == '|') {
                lexeme.append(text.charAt(currentIndexPosition++));
                lexeme.append(text.charAt(currentIndexPosition++));
                return getTypes(lexeme, Types.TypeOr);
            }
            return printError("Неверный входной символ", lexeme, new StringBuilder().append(text.charAt(currentIndexPosition)));
        } else
            return printError("Неверный входной символ", lexeme, new StringBuilder().append(text.charAt(currentIndexPosition)));
    }



    public String reader(String filePath, BufferedReader reader){
            StringBuilder s = new StringBuilder();
            try {
                String line = "";
                while ((line = reader.readLine()) != null) {
                    s.append(line + '\n'); }
                if ((line = reader.readLine()) != null)
                    s.append(line + '\n');

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return s.toString();

    }

    private Types getTypes(StringBuilder lexeme, Types types) {
        lexeme.append(text.charAt(currentIndexPosition++));
        return types;
    }

    public StringBuilder getCurrentItem(){
     return new StringBuilder().append(text.charAt(currentIndexPosition));
    }

    public Types printError(String error, StringBuilder lexeme, StringBuilder errorItem) {
        System.out.println("строка" + numberOfRow + ": " + error + ": " + " позиция " + currentIndexPosition + ": " + errorItem.toString());
        currentIndexPosition++;
        return Types.TypeError;
    }

  /*  public void readNextLine(BufferedReader reader){
        setText(reader(filePath, reader));
        setNumberOfRow(getNumberOfRow() + 1);
        currentIndexPosition = 0;
        System.out.println("строка " + getNumberOfRow());
    }

    private boolean checkIdentName(char item){
        if(item >= 91 && item <= 94 || item == 95 || item >= 58 && item <= 64)
            return false;
        return true;
    }
*/


    public void setCurrentIndexPosition(int currentIndexPosition) {
        this.currentIndexPosition = currentIndexPosition;
    }

    public int getCurrentIndexPosition() {
        return currentIndexPosition;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setReader(BufferedReader reader){
        this.reader = reader;
    }

    public void skipSymbols(){
        //пропуск незначащих символов
        while (text.charAt(currentIndexPosition) == ' ' || text.charAt(currentIndexPosition) == '\n' || text.charAt(currentIndexPosition) == '\t') {
            if(!newLine())
                currentIndexPosition++;
        }
    }

    private boolean newLine(){
        boolean change = false;
        while (text.charAt(currentIndexPosition) == '\n') {
            numberOfRow++;
            currentIndexPosition++;
            change = true;
            System.out.println("строка " + numberOfRow);
        }
        return change;
    }
}
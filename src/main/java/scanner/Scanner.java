package scanner;

import service.Types;

import java.io.*;

public class Scanner {
    private final int MAX_LENGTH_LEXEMES = 100;
    private final int MAX_NUMBER_KEYWORDS = 11;
    private String text;
    private int currentIndexPosition = 0;
    private int numberOfRow = 1;//номер строки
    private String filePath;
    private BufferedReader reader;

    private Lexeme lexeme = new Lexeme();


    public static class Lexeme{
        public StringBuilder lexeme = new StringBuilder();
        public Types type;

        public Lexeme(StringBuilder lexeme, Types type){

            this.lexeme.append(lexeme);
            this.type = type;
        }

        public void delete(){
            this.lexeme.delete(0, lexeme.length());
        }

        public void delete(int start, int end){
            this.lexeme.delete(start, end);
        }

        public void append(String symbol){
            lexeme.append(symbol);
        }

        public void append(Character symbol){
            lexeme.append(symbol);
        }

        public int length(){
            return lexeme.length();
        }

        public void setType(Types type) {
            this.type = type;
        }

        public Types setGetType(Types type){
            this.type = type;
            return type;
        }

        public Lexeme(){}
    }

    public Types setGetType(Types type){
       return lexeme.setGetType(type);
    }

    public Lexeme getLexeme(){
        return lexeme;
    }


    public void setNumberOfRow(int numberOfRow){
        this.numberOfRow = numberOfRow;
    }
    public int getNumberOfRow(){
        return numberOfRow;
    }



    public Types scanner() {
      //  lexeme.lexeme.delete(0, lexeme.lexeme.length());
        lexeme.delete();
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
                                return setGetType(Types.TypeEnd);
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
            return setGetType(Types.TypeEnd);
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
                    return printError("Слишком длинный идентификатор", lexeme.lexeme, new StringBuilder());
                }

            for (int j = 0; j < MAX_NUMBER_KEYWORDS; j++) {
                if (KeyWords.keyWords[j].equalsIgnoreCase(lexeme.lexeme.toString()))
                    return setGetType(Types.valueOf("Type" + KeyWords.keyWords[j]));
            }

            return Types.TypeIdent;
        } else if (text.charAt(currentIndexPosition) == ',')
            return setGetType(getTypes(lexeme.lexeme, Types.TypeComma));
        else if (text.charAt(currentIndexPosition) == ';')
            return setGetType(getTypes(lexeme.lexeme, Types.TypeSemicolon));
        else if (text.charAt(currentIndexPosition) == '(')
            return setGetType(getTypes(lexeme.lexeme, Types.TypeOpenParenthesis));
        else if (text.charAt(currentIndexPosition) == ')')
            return setGetType(getTypes(lexeme.lexeme, Types.TypeCloseParenthesis));
        else if (text.charAt(currentIndexPosition) == '{')
            return setGetType(getTypes(lexeme.lexeme, Types.TypeOpenBrace));
        else if (text.charAt(currentIndexPosition) == '}')
            return setGetType(getTypes(lexeme.lexeme, Types.TypeCloseBrace));
        else if (text.charAt(currentIndexPosition) == '+') {
            if (text.charAt(currentIndexPosition + 1) == '+') {
                lexeme.append(text.charAt(currentIndexPosition++));
                return setGetType(getTypes(lexeme.lexeme, Types.TypePlusPlus));
            }
            return setGetType(getTypes(lexeme.lexeme, Types.TypePlus));
        } else if (text.charAt(currentIndexPosition) == '-') {
            if (text.charAt(currentIndexPosition + 1) == '-') {
                lexeme.append(text.charAt(currentIndexPosition++));
                return setGetType(getTypes(lexeme.lexeme, Types.TypeMinusMinus));
            }
            return setGetType(getTypes(lexeme.lexeme, Types.TypeMinus));
        } else if (text.charAt(currentIndexPosition) == '/')
            return setGetType(getTypes(lexeme.lexeme, Types.TypeDivision));
        else if(text.charAt(currentIndexPosition) == '.')
                return setGetType(getTypes(lexeme.lexeme, Types.TypeDot));
        else if (text.charAt(currentIndexPosition) == '%')
            return setGetType(getTypes(lexeme.lexeme, Types.TypeMod));
        else if (text.charAt(currentIndexPosition) == '*')
            return setGetType(getTypes(lexeme.lexeme, Types.TypeMultiply));
        else if (text.charAt(currentIndexPosition) == '<') {
            if (text.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(text.charAt(currentIndexPosition++));
                return setGetType(getTypes(lexeme.lexeme, Types.TypeLe));
            }
            return setGetType(getTypes(lexeme.lexeme, Types.TypeLt));
        } else if (text.charAt(currentIndexPosition) == '>') {
            if (text.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(text.charAt(currentIndexPosition++));
                return setGetType(getTypes(lexeme.lexeme, Types.TypeGe));
            }
            return setGetType(getTypes(lexeme.lexeme, Types.TypeGt));
        } else if (text.charAt(currentIndexPosition) == '!') {
            if (text.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(text.charAt(currentIndexPosition++));
                lexeme.append(text.charAt(currentIndexPosition++));
                return setGetType(getTypes(lexeme.lexeme, Types.TypeNegation));
            }
            // return printError("Неверный входной символ", lexeme, new StringBuilder().append(text.charAt(currentIndexPosition)));
            return setGetType(getTypes(lexeme.lexeme, Types.TypeNegation));
        }
        if (text.charAt(currentIndexPosition) == '=') {
            if (text.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(text.charAt(currentIndexPosition++));
                lexeme.append(text.charAt(currentIndexPosition++));
                return setGetType(getTypes(lexeme.lexeme, Types.TypeComparison));
            }
            return setGetType(getTypes(lexeme.lexeme, Types.TypeAssign));
        } else if (text.charAt(currentIndexPosition) == '&') {
            if (text.charAt(currentIndexPosition + 1) == '&') {
                lexeme.append(text.charAt(currentIndexPosition++));
                lexeme.append(text.charAt(currentIndexPosition++));
                return setGetType(getTypes(lexeme.lexeme, Types.TypeAnd));
            } else
                return printError("Неверный входной символ", lexeme.lexeme, new StringBuilder().append(text.charAt(currentIndexPosition)));

        } else if (text.charAt(currentIndexPosition) == '|') {
            if (text.charAt(currentIndexPosition) == '|') {
                lexeme.append(text.charAt(currentIndexPosition++));
                lexeme.append(text.charAt(currentIndexPosition++));
                return setGetType(getTypes(lexeme.lexeme, Types.TypeOr));
            }
            return printError("Неверный входной символ", lexeme.lexeme, new StringBuilder().append(text.charAt(currentIndexPosition)));
        } else
            return printError("Неверный входной символ", lexeme.lexeme, new StringBuilder().append(text.charAt(currentIndexPosition)));
    }


    public boolean isEndWord(int pos, int length){
        if (pos >= length - 1)
            return false;
        return true;

    }


    public Types next(String grammarWord) {
        //  lexeme.lexeme.delete(0, lexeme.lexeme.length());

    //    grammarWord += "\n";

        Lexeme lexeme = new Lexeme();
        int currentIndexPosition = 0;
        lexeme.delete();


        //если встретился символ конца
        if (grammarWord.charAt(currentIndexPosition) == '\0' || grammarWord.charAt(currentIndexPosition) == '#') {
            lexeme.append("#");
            return setGetType(Types.TypeEnd);
        }

       // if(!isEndWord(currentIndexPosition, grammarWord.length());


        //константа
        if (grammarWord.charAt(currentIndexPosition) >= '0' && grammarWord.charAt(currentIndexPosition) <= '9') {
            lexeme.append(grammarWord.charAt(currentIndexPosition));
            currentIndexPosition++;
            while (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition) >= '0' && grammarWord.charAt(currentIndexPosition) <= '9') {
                if (lexeme.length() < MAX_LENGTH_LEXEMES - 1)
                    lexeme.append(grammarWord.charAt(currentIndexPosition++));
                else
                    currentIndexPosition++;
            }
            return Types.TypeConstInt;
        } else if (grammarWord.charAt(currentIndexPosition) >= 'a' && grammarWord.charAt(currentIndexPosition) <= 'z' ||
                grammarWord.charAt(currentIndexPosition) >= 'A' && grammarWord.charAt(currentIndexPosition) <= 'Z') {
            lexeme.append(grammarWord.charAt(currentIndexPosition++));



            //идентификатор
            while (isEndWord(currentIndexPosition, grammarWord.length()) && (grammarWord.charAt(currentIndexPosition) >= '0' && grammarWord.charAt(currentIndexPosition) <= '9' ||
                    grammarWord.charAt(currentIndexPosition) >= 'a' && grammarWord.charAt(currentIndexPosition) <= 'z' ||
                    grammarWord.charAt(currentIndexPosition) >= 'A' && grammarWord.charAt(currentIndexPosition) <= 'Z'))
                if (lexeme.length() < MAX_LENGTH_LEXEMES - 1)
                    lexeme.append(grammarWord.charAt(currentIndexPosition++));
                else {
                    currentIndexPosition++;
                    return printError("Слишком длинный идентификатор", lexeme.lexeme, new StringBuilder());
                }

            for (int j = 0; j < MAX_NUMBER_KEYWORDS; j++) {
                if (KeyWords.keyWords[j].equalsIgnoreCase(grammarWord.toString()))
                    return setGetType(Types.valueOf("Type" + KeyWords.keyWords[j]));
            }

            return Types.TypeIdent;
        } else if (grammarWord.charAt(currentIndexPosition) == ',')
            return setGetType(Types.TypeComma);
        else if (grammarWord.charAt(currentIndexPosition) == ';')
            return setGetType( Types.TypeSemicolon);
        else if (grammarWord.charAt(currentIndexPosition) == '(')
            return setGetType(Types.TypeOpenParenthesis);
        else if (grammarWord.charAt(currentIndexPosition) == ')')
            return setGetType( Types.TypeCloseParenthesis);
        else if (grammarWord.charAt(currentIndexPosition) == '{')
            return setGetType(Types.TypeOpenBrace);
        else if (grammarWord.charAt(currentIndexPosition) == '}')
            return setGetType(Types.TypeCloseBrace);
        else if (grammarWord.charAt(currentIndexPosition) == '+') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '+') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType( Types.TypePlusPlus);
            }
            return setGetType(Types.TypePlus);
        } else if (grammarWord.charAt(currentIndexPosition) == '-') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '-') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType( Types.TypeMinusMinus);
            }
            return setGetType( Types.TypeMinus);
        } else if (grammarWord.charAt(currentIndexPosition) == '/')
            return setGetType( Types.TypeDivision);
        else if(grammarWord.charAt(currentIndexPosition) == '.')
            return setGetType( Types.TypeDot);
        else if (grammarWord.charAt(currentIndexPosition) == '%')
            return setGetType( Types.TypeMod);
        else if (grammarWord.charAt(currentIndexPosition) == '*')
            return setGetType( Types.TypeMultiply);
        else if (grammarWord.charAt(currentIndexPosition) == '<') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType( Types.TypeLe);
            }
            return setGetType( Types.TypeLt);
        } else if (grammarWord.charAt(currentIndexPosition) == '>') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType( Types.TypeGe);
            }
            return setGetType( Types.TypeGt);
        } else if (grammarWord.charAt(currentIndexPosition) == '!') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType( Types.TypeNegation);
            }
            // return printError("Неверный входной символ", lexeme, new StringBuilder().append(grammarWord.charAt(currentIndexPosition)));
            return setGetType( Types.TypeNegation);
        }
        if (grammarWord.charAt(currentIndexPosition) == '=') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType( Types.TypeComparison);
            }
            return setGetType(Types.TypeAssign);
        } else if (grammarWord.charAt(currentIndexPosition) == '&') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '&') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType( Types.TypeAnd);
            } else
                return printError("Неверный входной символ", lexeme.lexeme, new StringBuilder().append(grammarWord.charAt(currentIndexPosition)));

        } else if (grammarWord.charAt(currentIndexPosition) == '|') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition) == '|') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType(Types.TypeOr);
            }
            return printError("Неверный входной символ", lexeme.lexeme, new StringBuilder().append(grammarWord.charAt(currentIndexPosition)));
        } else
            return printError("Неверный входной символ", lexeme.lexeme, new StringBuilder().append(grammarWord.charAt(currentIndexPosition)));
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
        return setGetType(types);
    }

    public StringBuilder getCurrentItem(){
     return new StringBuilder().append(text.charAt(currentIndexPosition));
    }

    public Types printError(String error, StringBuilder lexeme, StringBuilder errorItem) {
       // System.out.println("строка" + numberOfRow + ": " + error + ": " + " позиция " + currentIndexPosition + ": " + errorItem.toString() + "лексемма " + lexeme);
        System.out.println(error + ": " + " позиция " + currentIndexPosition + ": " + errorItem.toString() + " лексемма " + lexeme);
        currentIndexPosition++;
        return setGetType(Types.TypeError);
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
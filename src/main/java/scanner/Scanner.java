package scanner;

import org.apache.log4j.Logger;
import service.Types;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scanner {
    private static Logger LOGGER = Logger.getLogger(Scanner.class);
    private final int MAX_LENGTH_LEXEMES = 100;
    private int ptr = 0, startPtr = 0;
    private int currentLine = 0;//номер строки
    private List<char[]> lines = new ArrayList<>();
    private char[] line;

    private Lexeme lexeme = new Lexeme();

    public void initLine(int index) {
        line = lines.get(index);
    }


    public void createLines(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));

        String readed;
        while ((readed = reader.readLine()) != null) {
            lines.add((readed + "\n").toCharArray());
        }
        if (lines.size() != 0) {
            line = lines.get(0);
        }
    }

    public Types setGetType(Types type) {
        return lexeme.setGetType(type);
    }

    public Lexeme getLexeme() {
        return lexeme;
    }

    public void setLexeme(StringBuilder lexeme) {
        this.lexeme.lexeme = lexeme;
    }

    public void setCurrentLine(int currentLine) {
        this.currentLine = currentLine;
        line = lines.get(currentLine);
    }

    public int getCurrentLine() {
        return currentLine;
    }

    public void restoreState(State s) {
        ptr = s.line; ptr = s.pointer;
        line = lines.get(ptr);
    }

    public State getState() {
        return new State(currentLine, ptr);
    }

    private void fillLexeme(Types types) {
        lexeme.type = types;
        lexeme.line = currentLine;
        lexeme.ptr = startPtr;
    }

    public Lexeme scanner() {
        lexeme = new Lexeme();
        startPtr = ptr;
        while (true) {
            skipSymbols();

            //пропуск комментариев
            if (line[ptr] == '/') {
                //однострочный
                if (line[ptr + 1] == '/') {
                    ptr += 2;
                    while (line[ptr] != '\n') {
                        ptr++;
                    }
                    newLine();
                } else
                    //многострочный
                    if (line[ptr + 1] == 42) {
                        ptr += 2;
                        while (true) {
                            if (line[ptr] == 42 && line[ptr + 1] == 47) {
                                ptr += 2;
                                break;
                            }
                            newLine();
                            if (line[ptr] == '#') {
                                lexeme.lexeme.append(line[ptr]);
                                fillLexeme(Types.TypeEnd);
                                return lexeme;
                            } else {
                                ptr++;
                            }
                        }
                    } else {
                        break;
                    }

            } else {
                break;
            }

        }

        //если встретился символ конца
        if (line[ptr] == '\0' || line[ptr] == '#') {
            lexeme.lexeme.append(line[ptr]);
            fillLexeme(Types.TypeEnd);
            return lexeme;
        }


        //константа
        if (line[ptr] >= '0' && line[ptr] <= '9') {
            lexeme.append(line[ptr]); ptr++;
            while (line[ptr] >= '0' && line[ptr] <= '9') {
                if (lexeme.length() < MAX_LENGTH_LEXEMES - 1) {
                    lexeme.append(line[ptr++]);
                } else {
                    ptr++;
                }
                fillLexeme(Types.TypeConstInt);
            }
            fillLexeme(Types.TypeConstInt);
            return lexeme;
        } else if (line[ptr] >= 'a' && line[ptr] <= 'z' || line[ptr] >= 'A' && line[ptr] <= 'Z') {
            lexeme.append(line[ptr++]);

            //идентификатор
            while (line[ptr] >= '0' && line[ptr] <= '9' || line[ptr] >= 'a' && line[ptr] <= 'z' || line[ptr] >=
                    'A' && line[ptr] <= 'Z') {
                if (lexeme.length() < MAX_LENGTH_LEXEMES - 1) {
                    lexeme.append(line[ptr++]);
                } else {
                    ptr++;
                    return printError("Слишком длинный идентификатор", lexeme.lexeme, new StringBuilder());
                }
            }

            for (int j = 0; j < KeyWords.keyWords.length; j++) {
                if (lexeme.lexeme.toString().equals("Main")) {
                    fillLexeme(Types.TypeMain);
                    return lexeme;
                }
                if (lexeme.lexeme.toString().equals("main")) {
                    fillLexeme(Types.Typemain);
                    return lexeme;
                }
                if (KeyWords.keyWords[j].equalsIgnoreCase(lexeme.lexeme.toString())) {
                    fillLexeme(Types.valueOf("Type" + KeyWords.keyWords[j]));
                    return lexeme;
                }
            }
            fillLexeme(Types.TypeIdent);
            return lexeme;
        } else if (line[ptr] == ',') {
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeComma);
            return lexeme;
        } else if (line[ptr] == ';') {
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeSemicolon);
            return lexeme;
        } else if (line[ptr] == '(') {
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeOpenParenthesis);
            return lexeme;
        } else if (line[ptr] == ')') {
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeCloseParenthesis);
            return lexeme;
        } else if (line[ptr] == '{') {
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeOpenBrace);
            return lexeme;
        } else if (line[ptr] == '}') {
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeCloseBrace);
            return lexeme;
        } else if (line[ptr] == '+') {
            lexeme.append(line[ptr++]);
            if (line[ptr + 1] == '+') {
                lexeme.append(line[ptr++]);
                fillLexeme(Types.TypePlusPlus);
                return lexeme;
            }
            fillLexeme(Types.TypePlus);
            return lexeme;
        } else if (line[ptr] == '-') {
            lexeme.append(line[ptr++]);
            if (line[ptr + 1] == '-') {
                lexeme.append(line[ptr++]);
                fillLexeme(Types.TypeMinusMinus);
                return lexeme;
            }
            fillLexeme(Types.TypeMinus);
            return lexeme;
        } else if (line[ptr] == '/') {
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeDivision);
            return lexeme;
        } else if (line[ptr] == '.') {
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeDot);
            return lexeme;
        } else if (line[ptr] == '%') {
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeMod);
            return lexeme;
        } else if (line[ptr] == '*') {
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeMultiply);
            return lexeme;
        } else if (line[ptr] == '<') {
            if (line[ptr + 1] == '=') {
                lexeme.append(line[ptr++]);
                fillLexeme(Types.TypeLe);
                return lexeme;
            }
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeLt);
            return lexeme;
        } else if (line[ptr] == '>') {
            if (line[ptr + 1] == '=') {
                lexeme.append(line[ptr++]);
                fillLexeme(Types.TypeGe);
                return lexeme;
            }
            lexeme.append(line[ptr++]);
            fillLexeme(Types.TypeGt);
            return lexeme;
        } else if (line[ptr] == '!') {
            lexeme.append(line[ptr++]);
            if (line[ptr] == '=') {
                lexeme.append(line[ptr++]);
                fillLexeme(Types.TypeNegation);
                return lexeme;
            }
            fillLexeme(Types.TypeNegation);
            return lexeme;
        } if (line[ptr] == '=') {
            lexeme.append(line[ptr++]);
            if (line[ptr] == '=') {
                lexeme.append(line[ptr++]);
                fillLexeme(Types.TypeComparison);
                return lexeme;
            }
            fillLexeme(Types.TypeAssign);
            return lexeme;
        } else if (line[ptr] == '&') {
            if (line[ptr + 1] == '&') {
                lexeme.append(line[ptr++]);
                lexeme.append(line[ptr++]);
                fillLexeme(Types.TypeAnd);
                return lexeme;
            } else {
                return printError("Неверный входной символ", lexeme.lexeme,
                                  new StringBuilder().append(line[ptr]));
            }

        } else if (line[ptr] == '|') {
            if (line[ptr] == '|') {
                lexeme.append(line[ptr++]);
                lexeme.append(line[ptr++]);
                fillLexeme(Types.TypeOr);
                return lexeme;
            }
            return printError("Неверный входной символ", lexeme.lexeme,
                              new StringBuilder().append(line[ptr]));
        } else {
            return printError("Неверный входной символ", lexeme.lexeme,
                              new StringBuilder().append(line[ptr]));
        }
    }


    public boolean isEndWord(int pos, int length) {
        if (pos >= length - 1) {
            return false;
        }
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
                if (lexeme.length() < MAX_LENGTH_LEXEMES - 1) {
                    lexeme.append(grammarWord.charAt(currentIndexPosition++));
                } else {
                    currentIndexPosition++;
                }
            }
            return Types.TypeConstInt;
        } else if (grammarWord.charAt(currentIndexPosition) >= 'a' && grammarWord.charAt(currentIndexPosition) <= 'z' ||
                grammarWord.charAt(currentIndexPosition) >= 'A' && grammarWord.charAt(currentIndexPosition) <= 'Z') {
            lexeme.append(grammarWord.charAt(currentIndexPosition++));


            //идентификатор
            while (isEndWord(currentIndexPosition, grammarWord.length()) && (grammarWord.charAt(currentIndexPosition) >= '0' && grammarWord.charAt(currentIndexPosition) <= '9' ||
                    grammarWord.charAt(currentIndexPosition) >= 'a' && grammarWord.charAt(currentIndexPosition) <= 'z' ||
                    grammarWord.charAt(currentIndexPosition) >= 'A' && grammarWord.charAt(currentIndexPosition) <= 'Z')) {
                if (lexeme.length() < MAX_LENGTH_LEXEMES - 1) {
                    lexeme.append(grammarWord.charAt(currentIndexPosition++));
                } else {
                    currentIndexPosition++;
                    return printErrorTypes("Слишком длинный идентификатор", lexeme.lexeme, new StringBuilder());
                }
            }

            for (int j = 0; j < KeyWords.keyWords.length; j++) {
                if (grammarWord.equals("Main")) {
                    return setGetType(Types.TypeMain);
                }
                if (grammarWord.equals("main")) {
                    return setGetType(Types.Typemain);
                }
                if (KeyWords.keyWords[j].equalsIgnoreCase(grammarWord)) {
                    return setGetType(Types.valueOf("Type" + KeyWords.keyWords[j]));
                }
            }

            return Types.TypeIdent;
        } else if (grammarWord.charAt(currentIndexPosition) == ',') {
            return setGetType(Types.TypeComma);
        } else if (grammarWord.charAt(currentIndexPosition) == ';') {
            return setGetType(Types.TypeSemicolon);
        } else if (grammarWord.charAt(currentIndexPosition) == '(') {
            return setGetType(Types.TypeOpenParenthesis);
        } else if (grammarWord.charAt(currentIndexPosition) == ')') {
            return setGetType(Types.TypeCloseParenthesis);
        } else if (grammarWord.charAt(currentIndexPosition) == '{') {
            return setGetType(Types.TypeOpenBrace);
        } else if (grammarWord.charAt(currentIndexPosition) == '}') {
            return setGetType(Types.TypeCloseBrace);
        } else if (grammarWord.charAt(currentIndexPosition) == '+') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '+') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType(Types.TypePlusPlus);
            }
            return setGetType(Types.TypePlus);
        } else if (grammarWord.charAt(currentIndexPosition) == '-') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '-') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType(Types.TypeMinusMinus);
            }
            return setGetType(Types.TypeMinus);
        } else if (grammarWord.charAt(currentIndexPosition) == '/') {
            return setGetType(Types.TypeDivision);
        } else if (grammarWord.charAt(currentIndexPosition) == '.') {
            return setGetType(Types.TypeDot);
        } else if (grammarWord.charAt(currentIndexPosition) == '%') {
            return setGetType(Types.TypeMod);
        } else if (grammarWord.charAt(currentIndexPosition) == '*') {
            return setGetType(Types.TypeMultiply);
        } else if (grammarWord.charAt(currentIndexPosition) == '<') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType(Types.TypeLe);
            }
            return setGetType(Types.TypeLt);
        } else if (grammarWord.charAt(currentIndexPosition) == '>') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType(Types.TypeGe);
            }
            return setGetType(Types.TypeGt);
        } else if (grammarWord.charAt(currentIndexPosition) == '!') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType(Types.TypeNegation);
            }
            // return printError("Неверный входной символ", lexeme, new StringBuilder().append(grammarWord.charAt
            // (currentIndexPosition)));
            return setGetType(Types.TypeNegation);
        }
        if (grammarWord.charAt(currentIndexPosition) == '=') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '=') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType(Types.TypeComparison);
            }
            return setGetType(Types.TypeAssign);
        } else if (grammarWord.charAt(currentIndexPosition) == '&') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition + 1) == '&') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType(Types.TypeAnd);
            } else {
                return printErrorTypes("Неверный входной символ", lexeme.lexeme,
                                  new StringBuilder().append(grammarWord.charAt(currentIndexPosition)));
            }

        } else if (grammarWord.charAt(currentIndexPosition) == '|') {
            if (isEndWord(currentIndexPosition, grammarWord.length()) && grammarWord.charAt(currentIndexPosition) == '|') {
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                lexeme.append(grammarWord.charAt(currentIndexPosition++));
                return setGetType(Types.TypeOr);
            }
            return printErrorTypes("Неверный входной символ", lexeme.lexeme,
                              new StringBuilder().append(grammarWord.charAt(currentIndexPosition)));
        } else {
            return printErrorTypes("Неверный входной символ", lexeme.lexeme,
                              new StringBuilder().append(grammarWord.charAt(currentIndexPosition)));
        }
    }


   /* public Lexeme next(String grammarWord) {
        //  lexeme.lexeme.delete(0, lexeme.lexeme.length());

        //    grammarWord += "\n";

        Lexeme lexeme = new Lexeme(); int ptr = 0; lexeme.delete();


        //если встретился символ конца
        if (grammarWord[ptr) =='\0' || grammarWord[ptr) =='#'){
            lexeme.append("#");
            return setGetType(Types.TypeEnd);
        }

        // if(!isEndWord(ptr, grammarWord.length());


        //константа
        if (grammarWord[ptr) >='0' && grammarWord[ptr) <='9'){
            lexeme.append(grammarWord[ptr)); ptr++;
            while (isEndWord(ptr, grammarWord.length()) && grammarWord[ptr)
            >='0' && grammarWord[ptr) <='9'){
                if (lexeme.length() < MAX_LENGTH_LEXEMES - 1) {
                    lexeme.append(grammarWord[ptr++)
                });
                else ptr++;
            } return Types.TypeConstInt;
        } else if (grammarWord[ptr) >='a' && grammarWord[ptr) <='z'
                || grammarWord[ptr) >='A' && grammarWord[ptr) <='Z'){
            lexeme.append(grammarWord[ptr++));


            //идентификатор
            while (isEndWord(ptr, grammarWord.length()) && (grammarWord[ptr)
                    >= '0' && grammarWord[ptr) <='9' || grammarWord[ptr) >=
            'a' && grammarWord[ptr) <='z' || grammarWord[ptr) >=
            'A' && grammarWord[ptr) <='Z')){
                if (lexeme.length() < MAX_LENGTH_LEXEMES - 1) {
                    lexeme.append(grammarWord[ptr++)
                });
                else{
                    ptr++;
                    return printError("Слишком длинный идентификатор", lexeme.lexeme, new StringBuilder());
                }
            }

            for (int j = 0; j < KeyWords.keyWords.length; j++) {
                if (grammarWord.equals("Main")) {
                    return setGetType(Types.TypeMain);
                }
                if (grammarWord.equals("main")) {
                    return setGetType(Types.Typemain);
                }
                if (KeyWords.keyWords[j].equalsIgnoreCase(grammarWord)) {
                    return setGetType(Types.valueOf("Type" + KeyWords.keyWords[j]));
                }
            }

            return Types.TypeIdent;
        } else if (grammarWord[ptr) ==',')return setGetType(Types.TypeComma);
        else if (grammarWord[ptr) ==';')return setGetType(Types.TypeSemicolon);
        else if (grammarWord[ptr) =='(')return setGetType(Types.TypeOpenParenthesis);
        else if (grammarWord[ptr) ==')')return setGetType(Types.TypeCloseParenthesis);
        else if (grammarWord[ptr) =='{')return setGetType(Types.TypeOpenBrace);
        else if (grammarWord[ptr) =='}')return setGetType(Types.TypeCloseBrace);
        else if (grammarWord[ptr) =='+'){
            if (isEndWord(ptr, grammarWord.length()) && grammarWord[ptr + 1)
             =='+'){
                lexeme.append(grammarWord[ptr++)); return setGetType(Types.TypePlusPlus);
            } return setGetType(Types.TypePlus);
        } else if (grammarWord[ptr) =='-'){
            if (isEndWord(ptr, grammarWord.length()) && grammarWord[ptr + 1)
             =='-'){
                lexeme.append(grammarWord[ptr++)); return setGetType(Types.TypeMinusMinus);
            } return setGetType(Types.TypeMinus);
        } else if (grammarWord[ptr) =='/')return setGetType(Types.TypeDivision);
        else if (grammarWord[ptr) =='.')return setGetType(Types.TypeDot);
        else if (grammarWord[ptr) =='%')return setGetType(Types.TypeMod);
        else if (grammarWord[ptr) =='*')return setGetType(Types.TypeMultiply);
        else if (grammarWord[ptr) =='<'){
            if (isEndWord(ptr, grammarWord.length()) && grammarWord[ptr + 1)
             =='='){
                lexeme.append(grammarWord[ptr++)); return setGetType(Types.TypeLe);
            } return setGetType(Types.TypeLt);
        } else if (grammarWord[ptr) =='>'){
            if (isEndWord(ptr, grammarWord.length()) && grammarWord[ptr + 1)
             =='='){
                lexeme.append(grammarWord[ptr++)); return setGetType(Types.TypeGe);
            } return setGetType(Types.TypeGt);
        } else if (grammarWord[ptr) =='!'){
            if (isEndWord(ptr, grammarWord.length()) && grammarWord[ptr + 1)
             =='='){
                lexeme.append(grammarWord[ptr++));
                lexeme.append(grammarWord[ptr++)); return setGetType(Types.TypeNegation);
            }
            // return printError("Неверный входной символ", lexeme, new StringBuilder().append(grammarWord.charAt
            (ptr)));
            return setGetType(Types.TypeNegation);
        } if (grammarWord[ptr) =='='){
            if (isEndWord(ptr, grammarWord.length()) && grammarWord[ptr + 1)
             =='='){
                lexeme.append(grammarWord[ptr++));
                lexeme.append(grammarWord[ptr++)); return setGetType(Types.TypeComparison);
            } return setGetType(Types.TypeAssign);
        } else if (grammarWord[ptr) =='&'){
            if (isEndWord(ptr, grammarWord.length()) && grammarWord[ptr + 1)
             =='&'){
                lexeme.append(grammarWord[ptr++));
                lexeme.append(grammarWord[ptr++)); return setGetType(Types.TypeAnd);
            } else
            return printError("Неверный входной символ", lexeme.lexeme, new StringBuilder().append(grammarWord
                                                                                                           [ptr)));

        } else if (grammarWord[ptr) =='|'){
            if (isEndWord(ptr, grammarWord.length()) && grammarWord[ptr) ==
            '|'){
                lexeme.append(grammarWord[ptr++));
                lexeme.append(grammarWord[ptr++)); return setGetType(Types.TypeOr);
            }
            return printError("Неверный входной символ", lexeme.lexeme, new StringBuilder().append(grammarWord.charAt
                    (ptr)));
        } else
        return printError("Неверный входной символ", lexeme.lexeme, new StringBuilder().append(grammarWord.charAt
                (ptr)));
    }
*/

    public String reader(String filePath, BufferedReader reader) {
        StringBuilder s = new StringBuilder(); try {
            String line = ""; while ((line = reader.readLine()) != null) {
                s.append(line + '\n');
            }
            if ((line = reader.readLine()) != null) {
                s.append(line + '\n');
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } return s.toString();

    }

    private Types getTypes(StringBuilder lexeme, Types types) {
        lexeme.append(line[ptr++]);
        return setGetType(types);
    }

    public StringBuilder getCurrentItem() {
        return new StringBuilder().append(line[ptr]);
    }

    public Lexeme printError(String error, StringBuilder lexeme, StringBuilder errorItem) {
        System.out.println(error + ": " + " позиция " + ptr + ": " + errorItem.toString() + " " +
                                   "лексемма " + lexeme);
        ptr++;
        fillLexeme(Types.TypeError);
        return this.lexeme;
    }

    public Types printErrorTypes(String error, StringBuilder lexeme, StringBuilder errorItem) {
        System.out.println(error + ": " + " позиция " + ptr + ": " + errorItem.toString() + " " +
                                   "лексемма " + lexeme);
        ptr++;
        return Types.TypeError;

    }

    public void setPtr(int ptr) {
        this.ptr = ptr;
    }

    public int getPtr() {
        return ptr;
    }

    public void setLine(int lineIndex) {
        this.line = lines.get(lineIndex);
    }

    public void skipSymbols() {
        //пропуск незначащих символов
        if (line == null) {
            return;
        }
        LOGGER.info("skipSymbols");
        LOGGER.info(Arrays.toString(line));
        LOGGER.info(currentLine);
        for (; ; ) {
            if (ptr >= line.length || line[ptr] == '\n') {
                newLine();
            } else if (line[ptr] == ' ' || line[ptr] == '\t') {
                ptr++;
            } else {
                break;
            }

        }
    }

    private void newLine() {
        currentLine++;
        ptr = 0;
        line = (currentLine != lines.size()) ? lines.get(currentLine) : null;
    }

    public void readForEndFunctionCall() {
        Lexeme tmp = new Lexeme();
        while (scanner().type != Types.TypeCloseParenthesis) {
            ;
        }
//        ptr = tmp.ptr;
//        currentLine = tmp.line;
    }
}
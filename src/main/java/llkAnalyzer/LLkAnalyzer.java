package llkAnalyzer;


import org.apache.commons.lang3.SerializationUtils;
import scanner.Lexeme;
import scanner.Scanner;
import service.Types;
import service.Types.*;

import java.io.File;
import java.io.FileInputStream;

import java.time.temporal.Temporal;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.exit;
import static service.Types.*;


public class LLkAnalyzer {

    private static final boolean DEBUG = false;
    private static final int DEPTH = 4 ;

    private final Table controlTable;
    private final FirstFollowTable firstFollowTable;
    private final Scanner scanner;
    private Lexeme prevLexeme = new Lexeme();
    Lexeme currentLexeme = new Lexeme();
    Stack<Table.Element> stack = new Stack<>();
    Stack<Types> stackClasses = new Stack<>();
    Types errorPrevType = null;
    private Set<Types> safeSymbols;
    Table.NonTerminal classes = new Table.NonTerminal("C", 0);
    public int countOfOpenBrace = 0;
    public int countOfCloseBrace = 0;

    public LLkAnalyzer(File table, Scanner source, File firstFollow) throws Exception {
        controlTable = SerializationUtils.deserialize(new FileInputStream(table));
        firstFollowTable = SerializationUtils.deserialize(new FileInputStream(firstFollow));
        scanner = source;
    }


    public boolean program() {
        constructSafeSymbolSet();
        stack.add(controlTable.getEndTerminal());
        stack.add(controlTable.getAxiom());

        scanner.scanner();
        currentLexeme = scanner.getLexeme();
        for (; ; ) {
            if (stack.isEmpty()) {
                System.out.println("Конец файла не достигнут. Проверьте правильность расстановки скобок");
                exit(-1);
            }
            Table.Element current = stack.pop();
            String tempTerminal = "";
            if (current instanceof Table.Terminal) {
                Table.Terminal terminal = (Table.Terminal) current;

                if (scanner.getLexeme().type != terminal.type) {
                    String line1 = "ожидался " + terminal.type, line2 = "но найден " + scanner.getLexeme().type;
                    errorPrevType = terminal.type;
                    System.out.println(new AnalyzeError(
                            scanner, scanner.getLexeme(), line1, line2).getDisplayMessage());

                    Types fg = stackClasses.pop();
                    stackClasses.push(fg);
                    if((terminal.type == TypeClass || terminal.type == TypePublic) ) {
                     /*   Table.Terminal t = null;
                        Table.NonTerminal non = null;
                        Table.Element e = stack.pop();
                        stack.push(e);
                        if(e instanceof Table.Terminal)
                            t = (Table.Terminal) e;
                        if(t != null) {
                            do {
                                scanner.scanner();
                            } while (scanner.getLexeme().type.toString().compareTo(stack.peek().value) != 0 && scanner.getLexeme().type != TypeEnd);
                        }
                        stackClasses.pop();*/


                        if(stack.peek() instanceof Table.NonTerminal)
                            stack.pop();

                     while(scanner.getLexeme().type != TypeOpenBrace
                             && scanner.getLexeme().type != TypeCloseBrace) {
                         scanner.scanner();


                     }

                        if(stack.peek() instanceof Table.NonTerminal) {
                            Table.Cell cell;
                            cell = controlTable.get((Table.NonTerminal)stack.pop(), scanner.getLexeme().type);
                            stack.addAll(cell.get(0));
                        }
                    }

                } else {
                    if (scanner.getLexeme().type == Types.TypeEnd) {
                        if (!stack.isEmpty())
                            return false;
                        return true;
                    }
                  //  if(stackClasses.peek() == TypeClass)
                        stackClasses.push(scanner.getLexeme().type);
                    scanner.scanner();
                    if(scanner.getLexeme().lexeme.toString().equals("{"))
                        countOfOpenBrace++;
                    if(scanner.getLexeme().lexeme.toString().equals("}"))
                        countOfCloseBrace++;
                    currentLexeme = scanner.getLexeme();
                }
                if (scanner.getLexeme().type == Types.TypeEnd) break;
            } else {
                Table.NonTerminal nonTerminal = (Table.NonTerminal) current;
                Table.Cell cell;
                cell = controlTable.get(nonTerminal, scanner.getLexeme().type);

                if (cell.isEmpty()) {
                    String line1 = "ошибочная комбинация " + scanner.getLexeme().type, line2 = "при анализе " + nonTerminal;
                  //  if(scanner.getLexeme().lexeme.toString().equals(";"))
                    System.out.println(new AnalyzeError(
                            scanner, scanner.getLexeme(), line1, line2).getDisplayMessage());

                  /*  if(stack.peek().value.compareTo(";") == 0 )
                        stack.pop();*/

                    if(stack.peek() instanceof Table.Terminal) {
                        Table.Element e = stack.pop();
                        stack.push(e);
                        Table.Terminal t = (Table.Terminal) e;
                        if(scanner.getLexeme().lexeme.toString().equals("}"))
                            countOfCloseBrace++;
                        if(scanner.getLexeme().lexeme.toString().equals("{"))
                            countOfOpenBrace++;
                        if (scanner.getLexeme().type.toString().compareTo(t.type.toString()) == 0)
                            continue;
                    }


                 /*   Types fg = stackClasses.pop();
                    stackClasses.push(fg);
                    if(fg != TypePublic && fg != TypeClass
                            && errorPrevType == TypeClass || errorPrevType == TypePublic) {
                        Table.Terminal t = null;
                        Table.NonTerminal non = null;
                        Table.Element e = stack.pop();
                        stack.push(e);
                        if(e instanceof Table.Terminal)
                            t = (Table.Terminal) e;
                        if(t != null) {
                            do {
                                scanner.scanner();
                            } while (scanner.getLexeme().type.toString().compareTo(stack.peek().value) != 0 && scanner.getLexeme().type != TypeEnd);
                        }
                        stackClasses.pop();

                    }*/

                    Pair pair = null;

                    tempTerminal = findNonTerminal(nonTerminal, scanner.getLexeme().lexeme.toString(), pair);

                    if (tempTerminal == "")
                        continue;

                    nonTerminal = controlTable.getNonTerminals(tempTerminal);

                    cell = controlTable.get(nonTerminal, scanner.getLexeme().type);


                }
                	if(!cell.isEmpty())
                stack.addAll(cell.get(0));

            }
        }

        return true;
    }



    private void neutralizeError() {
        //System.out.format("%-17s %-2d %s\n", currentLexeme.getType().name(), currentLexeme.getLine() + 1, stack);
        boolean ok = false;
        do {
            findNextSaveSymbol();
            for (int i = 0; i < DEPTH && !stack.isEmpty(); i++) {
                Table.Element currentElement = stack.pop();
                if (currentElement instanceof Table.Terminal) {
                    Table.Terminal terminal = (Table.Terminal) currentElement;
                    while (currentLexeme.type != terminal.type && currentLexeme.type != Types.TypeEnd) {
                        scanner.scanner();
                        currentLexeme = scanner.getLexeme();
                    }
                    scanner.scanner();
                    currentLexeme = scanner.getLexeme();
                    ok = true;
                    break;
                }
                if(stack.peek().value.compareTo(scanner.getLexeme().lexeme.toString()) == 0 )
                    continue;
                Table.NonTerminal nonTerminal = (Table.NonTerminal) currentElement;
                if(nonTerminal.getLast().contains(currentLexeme.type)) {
                    ok = true;
                    scanner.scanner();
                    currentLexeme = scanner.getLexeme();
                    if (stack.size() == 1)
                        stack.add(controlTable.getAxiom());
                    break;
                }
                if (!controlTable.get(nonTerminal, currentLexeme.type).isEmpty()) {
                    stack.add(currentElement);
                    ok = true;
                    break;
                }
            }
        } while (!ok && !currentLexeme.type.equals(Types.TypeEnd));
        //System.out.format("%-17s %-2d %s\n", currentLexeme.getType().name(), currentLexeme.getLine() + 1, stack);
    }

    private void findNextSaveSymbol() {
        while (!safeSymbols.contains(currentLexeme.type) && !currentLexeme.type.equals(Types.TypeEnd)) {
            scanner.scanner();
            currentLexeme = scanner.getLexeme();
        }
    }


    private void constructSafeSymbolSet() {
        safeSymbols = Stream.of(Types.TypeIdent,
                TypeConstInt,
                TypeSemicolon,
                TypeOpenParenthesis,
                TypeEnd,
                TypeComma,
                TypeAssign,
                TypeFalse,
                TypeTrue,
                TypeNegation,
                TypeCloseBrace,
                TypeOpenBrace,
                TypePlus,
                TypeMinus,
                TypeCloseParenthesis,
                TypeError,
                TypeDot,
                TypePublic,
                TypeBoolean,
                TypeClass,
                Typemain,
                TypeMain,
                TypeMultiply,
                TypeMod,
                TypeAnd,
                TypeOr,
                TypeGe,
                TypeLe,
                TypeLt,
                TypeGt,
                TypeIf,
                TypeElse,
                TypeReturn,
                TypeComparison,
                TypeFinal)
                .collect(Collectors.toSet());

    }

    public String findNonTerminal(Table.NonTerminal nonTerminal, String tempTerminal, Pair pair) {
        int minSize = 10000, countOfIteration = 0;


        do {
            countOfIteration = 0;
            tempTerminal = "";
            boolean br = false;
            while (true) {
                if((scanner.getLexeme().lexeme.toString().compareTo(";") == 0
                        || scanner.getLexeme().lexeme.toString().compareTo("}") == 0
                        || scanner.getLexeme().lexeme.toString().compareTo(")") == 0)) {

                    if(scanner.getLexeme().lexeme.toString().equals("}"))
                        countOfCloseBrace++;
                    if(scanner.getLexeme().lexeme.toString().equals("{"))
                        countOfOpenBrace++;
                  /*  if(nonTerminal.getLast().contains(scanner.getLexeme().type)) {
                        scanner.scanner();
                        currentLexeme = scanner.getLexeme();
                        if(stack.peek().value.compareTo(";") == 0 || stack.peek().value.compareTo("}") == 0
                                || stack.peek().value.compareTo(")") == 0)
                            stack.pop();
                        if (stack.size() == 1)
                            stack.add(controlTable.getAxiom());
                        return nonTerminal.value;
                    }*/
                   /* if (!controlTable.get(nonTerminal, scanner.getLexeme().type).isEmpty()) {
                        stack.add(nonTerminal);
                        break;
                    }*/

                    br = true;
                    break;
                }
                 if(scanner.getLexeme().lexeme.toString().compareTo("{") == 0
                 || scanner.getLexeme().lexeme.toString().compareTo("(") == 0) {
                     if(scanner.getLexeme().lexeme.toString().equals("}"))
                         countOfCloseBrace++;
                     if(scanner.getLexeme().lexeme.toString().equals("{"))
                         countOfOpenBrace++;
                     br = true;
                     break;
                 }

                 scanner.scanner();
            }
          /*  if(!br){
                System.out.println("Нет завершающей ; или } или )");
                exit(-1);
            }*/
          //  scanner.scanner();
            for (Map.Entry<String, Pair> p : firstFollowTable.firstFollow.entrySet()) {
                countOfIteration++;
                pair = p.getValue();
                if (pair.first.size() <= minSize) {
                    for (String s : pair.first)
                        if (s.compareTo(scanner.getLexeme().type.toString()) == 0) {
                            tempTerminal = p.getKey();
                            minSize = pair.first.size();
                            break;
                        }
                    if(tempTerminal != "")
                        break;

                }
                if (countOfIteration >= firstFollowTable.firstFollow.size())
                    return "";
            }


        } while (tempTerminal == "");

        //	Pair p = firstFollowTable.firstFollow.get(tempTerminal);


        return tempTerminal;
    }

}

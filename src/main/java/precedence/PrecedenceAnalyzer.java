package precedence;

import llkAnalyzer.AnalyzeError;
import llkAnalyzer.Grammar;
import llkAnalyzer.Grammar.*;
import org.apache.commons.lang3.SerializationUtils;
import scanner.Scanner;
import scanner.Scanner.*;
import service.Types;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;


public class PrecedenceAnalyzer {

    private static final boolean DEBUG = false;

    private final Table data;
    private final Scanner scanner;

    public PrecedenceAnalyzer(File file, Scanner source) throws Exception {
        data = SerializationUtils.deserialize(new FileInputStream(file));
        scanner = source;
    }

    //	@Override
    public void program() {
        Stack<String> stack = new Stack<>();
        stack.push("#");

        scanner.scanner();
        Lexeme lexeme = scanner.getLexeme();
        String lexemeStr = data.convertLexeme(lexeme.type);

        if (lexeme.type == Types.TypeEnd) return;

        for (; ; ) {
            char relation = getRelation(lexeme, stack.peek(), lexemeStr);
            if (relation == '<' || relation == '=') {
                stack.push(lexemeStr);

                scanner.scanner();
                lexeme = scanner.getLexeme();
                lexemeStr = data.convertLexeme(lexeme.type);
            } else {
                Stack<String> reduce = new Stack<>();
                while (reduce.isEmpty() || getRelation(lexeme, stack.peek(), reduce.peek()) == '=')
                    reduce.push(stack.pop());
                Collections.reverse(reduce);

                //     if (DEBUG) System.out.println((scanner.getNumberOfRow() + 1) + " " + stack + " " + reduce);

                if (stack.size() == 1 && reduce.size() == 1 && reduce.get(0).equals("S")) {
                    break; // finish
                } else if (Grammar.isRulesEquals(reduce, "a")) {
                    int cnt = 0;
                    cycle:
                    for (int i = stack.size() - 1; i >= 0; i--) {
                        String s = stack.get(i);
                        switch (s) {
                            case "return":
                                stack.push("$885");
                                break cycle;
                            case "=":
                                if (lexeme.lexeme.toString().equals("(")) {
                                    stack.push("$888");
                                    break cycle;
                                }
                                if (i > 0 && (stack.get(i - 1).equals("N7")
                                        || stack.get(i - 1).equals("N5")
                                        || stack.get(i - 1).equals("N2")))
                                    stack.push("$885");
//                                else if (stack.get(i - 1).equals("$89"))
//                                    stack.push("$89");
                                else
                                    stack.push("$34");
                                break cycle;
                            case ")":
                                cnt--;
                                break;
                            case "(":
                                if(stack.get(i - 1).equals("$76")){
                                    stack.push("$35");
                                    break cycle;
                                }
                                if (stack.get(i - 1).equals("if")) {
                                    stack.push("$885");
                                    break cycle;
                                } else if (stack.get(i - 1).equals("$888")) {
                                    stack.push("$886");
                                    break cycle;
                                }
                                //  else
                                //      if(stack.get(i - 1).equals("T"))
//                                else {
//                                    cnt++;
//                                    if (cnt == 1) {
//                                        stack.push("M");
//                                        break cycle;
//                                    }
//                                }
                                break;
                            case "class":
                                stack.push("N");
                                break cycle;
                            case "#":
                                stack.push("N");
                                break cycle;
                           /* case "if":
                                stack.push("N2");
                                break cycle;*/
                            case ",":
                                if (stack.get(i - 1).equals("F1"))
                                    stack.push("$35");
                                else
                                    stack.push("$89");
                                break cycle;
//                            case "" :
//                                stack.push("N1");
//                                break cycle;
                            case "T":
                                if (lexeme.lexeme.toString().equals("("))
                                    stack.push("$76");
                                else if (i > 0 && stack.get(i - 1).equals("("))
                                    stack.push("N2");
                                else if (i > 0 && stack.get(i - 1).equals(",") && stack.get(i - 2).equals("F"))
                                    stack.push("N2");
                                else
                                    stack.push("$89");
                                break cycle;
                            case "public":
                                if (lexemeStr.equals(""))
                                    stack.push("N1");
                                else
                                    stack.push("$89");
                                break cycle;
                            case "P1":
                                stack.push("I");
                                break cycle;
                            case "O1":
                                if (lexeme.lexeme.toString().equals("("))
                                    stack.push("N2");
                                else
                                    stack.push("$885");
                                break cycle;
                            case ";":
                                if (stack.get(stack.size() - 2).equals("A"))
                                    stack.push("O");
                                break cycle;
                        }
                    }
                } else if (Grammar.isRulesEquals(reduce, "A5")) {
                    String prev1 = stack.get(stack.size() - 1);
                    String prev2 = stack.get(stack.size() - 2);

                    if (!prev1.equals("A3"))
                        stack.push("A4");
                    //    else if (prev2.equals("A3"))
                    //      stack.push("A4");
                    //    else
                    //      stack.push("$21");
                } else if (Grammar.isRulesEquals(reduce, "N", "N1")) {
                    if (stack.get(stack.size() - 1).equals("a"))
                        stack.push("N");
                    else if (stack.get(stack.size() - 1).equals("main"))
                        stack.push("N1");
                    else if (stack.get(stack.size() - 1).equals("Main"))
                        stack.push("N");
                    else if (stack.get(stack.size() - 1).equals("."))
                        stack.push("N2");
                } else if (Grammar.isRulesEquals(reduce, "int")) {
                    stack.push("T");

                }else if (Grammar.isRulesEquals(reduce, "$89", "=", "H")) {
                    if(stack.peek().equals("T"))
                        stack.pop();
                    else
                        System.out.println("Ошибка T a = H");
                    stack.push("O1");
                  //  stack.push(";");

               } else if (Grammar.isRulesEquals(reduce, "N2")) {
                    String s = stack.get(stack.size() - 1);
                    String s1 = stack.get(stack.size() - 2);
                    boolean f = false;
                    if ((s.equals("O1") || s1.equals("O1")) && lexeme.lexeme.toString().equals("("))
                        stack.push("$888");
                    else if (stack.get(stack.size() - 3).equals("$378")) {
                        stack.push("N7");
                    } else if (stack.get(stack.size() - 1).equals("=") && (stack.get(stack.size() - 2).equals("N7")
                            || stack.get(stack.size() - 2).equals("$89")))
                        stack.push("N89");
                    else {
                        for (int i = stack.size() - 1; i > 0; i--) {
                           /* if (stack.get(i).equals("return")) {
                                stack.push("N7");
                                f = true;
                                break;
                            }*/
                            if (stack.get(i).equals("if")) {
                                stack.push("N89");
                                f = true;
                                break;
                            }
                        }
                        if (!f)
                            stack.push("N7");
                    }
                } else if (Grammar.isRulesEquals(reduce, "P1")) {
                    stack.push("I");

                } /*else if (Grammar.isRulesEquals(reduce, "O1", "O")) {
                    if(stack.get(stack.size() - 1).equals("{")
                    && stack.get(stack.size() - 2).equals(")")
                    && stack.get(stack.size() - 3).equals("F"))
                    stack.push("$O");
                }*/ else if (Grammar.isRulesEquals(reduce, "V")) {
                    if (stack.get(stack.size() - 2).equals("$89"))
                        stack.push("P1");
                    else if (stack.get(stack.size() - 1).equals("return")
                            || stack.get(stack.size() - 2).equals("if"))
                        stack.push("$378");
                    else if (stack.get(stack.size() - 1).equals("=") && stack.get(stack.size() - 2).equals("N7"))
                        stack.push("$780");
                    else if (stack.get(stack.size() - 1).equals("(") && stack.get(stack.size() - 2).equals("$888")
                            || stack.get(stack.size() - 1).equals(",") && stack.get(stack.size() - 2).equals("F1"))
                        stack.push("$35");
                } else if (Grammar.isRulesEquals(reduce, "S")) {
                    String prev = stack.get(stack.size() - 2);
                    String prev2 = stack.get(stack.size() - 3);
                    boolean f = false;
                    if (!prev.equals("public") && !prev2.equals("class")) {
                        for (int i = stack.size() - 1; i > 3; i--)
                            if (stack.get(i).equals("{")
                                    && stack.get(i - 1).equals(")") && stack.get(i - 2).equals("F")) {
                                stack.push("O1");
                                f = true;
                            }
                        if (!f)
                            stack.push("$92");
                    } else
                        stack.push("C");
                } else if (Grammar.isRulesEquals(reduce, "A")) {
                    String prev = stack.get(stack.size() - 1);
                    String prev2 = stack.get(stack.size() - 2);
                    String prev3 = stack.get(stack.size() - 3);
                    if (prev.equals("{") && prev2.equals("N") && prev3.equals("class"))
                        stack.push("C");
                    else
                        stack.push("O1");
                } else if (Grammar.isRulesEquals(reduce, "O")) {
                    String prev = stack.get(stack.size() - 1);
                    String prev2 = stack.get(stack.size() - 2);
                    String prev3 = stack.get(stack.size() - 3);
                    if (prev.equals("{") && prev2.equals(")") && prev3.equals("F"))
                        stack.push("$92");
                    else if (prev3.equals("$378")) {
                        stack.push("$556");
                        scanner.scanner();
                        lexeme = scanner.getLexeme();
                        lexemeStr = data.convertLexeme(lexeme.type);
                    } else if (prev.equals("{") && prev2.equals("else")) {
                        stack.push("$555");

                        scanner.scanner();
                        lexeme = scanner.getLexeme();
                        lexemeStr = data.convertLexeme(lexeme.type);
                    }
                } else if (Grammar.isRulesEquals(reduce, "O1")) {
                    String prev = stack.get(stack.size() - 1);
                    String prev2 = stack.get(stack.size() - 2);
                    // String prev3 = stack.get(stack.size() - 3);
                    if (prev.equals(")") && prev2.equals("$378"))
                        stack.push("$555");
                    else
                        // if(prev.equals(""))
                        stack.push("O");
                } else if (Grammar.isRulesEquals(reduce, "RR")) {
                    if (stack.get(stack.size() - 1).equals("O1") && lexemeStr.equals("}"))
                        stack.push("O");
                    else
                        stack.push("O1");
                } else {
                    boolean flag = false;
                    for (Map.Entry<String, ArrayList<Rule>> e : data.getGrammar().map.entrySet()) {
                        for (Rule rule : e.getValue()) {
                            if (Grammar.isRulesEquals(rule, reduce)) {
                                if (flag) throw new AnalyzeError(scanner, lexeme, reduce, "Already reduced");

                                stack.push(e.getKey());
                                flag = true;

                                if (DEBUG) System.out.println(">>> " + stack);
                            }
                        }
                    }

                    if (!flag) throw new AnalyzeError(scanner, lexeme, reduce, "Not reduced");
                }
            }
            if (DEBUG) System.out.println(stack);
        }
    }

    private char getRelation(Scanner.Lexeme lexeme, String a, String b) {
        if (a.equals("#")) return '<';
        if (b.equals("#")) return '>';
        if (a.equals(")") && b.equals(";"))
            return '=';
        if (a.equals("O1") && b.equals("T"))
            return '<';
        if ((a.equals("N6") || a.equals("N5") || a.equals("O1")) && b.equals("}"))
            return '>';
        if ((a.equals("I") && b.equals("return")))
            return '>';
            //return '>';
       /* if(a.equals("N89") && b.equals("="))
            return '>';*/
        Table.Cell cell = data.getCell(data.getIndex(a), data.getIndex(b));
        if (cell.relations.size() != 1) {
            System.out.println(cell.relations.size());
            throw new AnalyzeError(scanner, lexeme, "Requested wrong relation between " + a + " and " + b);
        }
        return cell.relations.iterator().next();
    }
}

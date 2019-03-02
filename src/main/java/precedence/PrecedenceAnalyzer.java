/*
package precedence;

import llkAnalyzer.AnalyzeError;
import llkAnalyzer.Grammar;
import llkAnalyzer.Grammar.*;
import org.apache.commons.lang3.SerializationUtils;
import scanner.Lexeme;
import scanner.Scanner;
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

           //     if (DEBUG) System.out.println((scanner.getCurrentLine() + 1) + " " + stack + " " + reduce);

                if (stack.size() == 1 && reduce.size() == 1 && reduce.get(0).equals("S")) {
                    break; // finish
                } else if (Grammar.isRulesEquals(reduce, "a")) {
                    int cnt = 0;
                    cycle:
                    for (int i = stack.size() - 1; i >= 0; i--) {
                        String s = stack.get(i);
                        switch (s) {
                            case "return":
                            case "=":
                                stack.push("$67");
                                break cycle;
                            case ")":
                                cnt--;
                                break;
                            case "(":
                                cnt++;
                                if (cnt == 1) {
                                    stack.push("$67");
                                    break cycle;
                                }
                                break;
                            case "#":
                                stack.push("$67");
                                break cycle;
                        }
                    }
                } */
/*else if (Grammar.isRulesEquals(reduce, "A5")) {
                    String prev1 = stack.get(stack.size() - 1);
                    String prev2 = stack.get(stack.size() - 2);

                    if (!prev1.equals("+") && !prev1.equals("-"))
                        stack.push("A4");
                    else if (prev2.equals("A3"))
                        stack.push("A4");
                    else
                        stack.push("$21");
                }*//*
 else if (Grammar.isRulesEquals(reduce, "O")) {
                    if (stack.get(stack.size() - 1).equals("do"))
                        stack.push("$0");
                    else
                        stack.push("H");
                } else if (Grammar.isRulesEquals(reduce, "$7", ";")) {
                    String prev = stack.get(stack.size() - 1);
                    if (prev.equals("T") || prev.equals(","))
                        stack.push("V");
                    else
                        stack.push("O");
                } else if (Grammar.isRulesEquals(reduce, "$40")) {
                    if (stack.get(stack.size() - 1).equals("(") && !stack.get(stack.size() - 2).equals("I"))
                        stack.push("$41");
                    else
                        stack.push("J");
                } else if (Grammar.isRulesEquals(reduce, "D")) {
                    if (stack.contains("{"))
                        stack.push("H");
                    else
                        stack.push("P");
                } else if (Grammar.isRulesEquals(reduce, "C")) {
                    String prev = stack.get(stack.size() - 1);
                    if (prev.equals(";") || prev.equals("{") || prev.equals("}") || prev.equals("W"))
                        stack.push("$4");
                    else
                        stack.push("A6");
                } else {
                    boolean flag = false;
                    for (Map.Entry<String, ArrayList<Rule>> e : data.getGrammar().map.entrySet()) {
                        for (Rule rule : e.getValue()) {
                            if (Grammar.isRulesEquals(rule, reduce)) {
                                if (flag) throw new AnalyzeError(scanner, lexeme, "Already reduced");

                                stack.push(e.getKey());
                                flag = true;

                                if (DEBUG) System.out.println(">>> " + stack);
                            }
                        }
                    }

                    if (!flag) throw new AnalyzeError(scanner, lexeme, "Not reduced");
                }
            }
            if (DEBUG) System.out.println(stack);
        }
    }

    private char getRelation(Lexeme lexeme, String a, String b) {
        if (a.equals("#")) return '<';
        if (b.equals("#")) return '>';
        Table.Cell cell = data.getCell(data.getIndex(a), data.getIndex(b));
        if (cell.relations.size() != 1)
            throw new AnalyzeError(scanner, lexeme, "Requested wrong relation between " + a + " and " + b);
        return cell.relations.iterator().next();
    }
}
*/

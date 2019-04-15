package llkAnalyzer;

import llkAnalyzer.Grammar.Rule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import scanner.Scanner;

import java.io.File;
import java.util.*;


public class Generator {

    private static Grammar grammar, grammar2;
    private static HashMap<String, HashSet<String>> follow = new HashMap<>();

    public static void main(String[] args) throws Exception {
        boolean silent = args.length > 0 && args[0].equalsIgnoreCase("silent");
        grammar = new Grammar(new File("src/main/resources/grammar/grammar3.txt"));

        // Вычисление first1 для всех правил грамматики
        HashMap<String, HashSet<String>> used = new HashMap<>();
        for (String a : grammar.nonTerminals) {
            first1(a, used);
        }

//        Map<String, Set<String>> used1 = new HashMap<>();
//        grammar.getNonTerminals().forEach(s -> last(s, used1));

        // Приведение грамматики к неукорачивающей форме
        grammar2 = grammar.withoutEps();

        // Вычисление follow1 для всех правил грамматики
        for (String a : grammar.nonTerminals) {
            follow.put(a, new HashSet<>());
        }
        follow1(grammar.nonTerminals.get(0), "#", new HashSet<>());

        // Вывод полученных first1 и follow1 для всех нетерминалов
        for (String a : grammar.nonTerminals) {
            HashSet<String> first = new HashSet<>();
            for (Grammar.Rule rule : grammar.map.get(a)) {
                first.addAll(rule.first);
            }

            if (!silent) {
                System.out.format("%-3s %-38s %s\n", a, first, follow.get(a));
            }

        }
        if (!silent) {
            System.out.println();
        }

        // Построение управляющей таблицы
        Table table = new Table(grammar.nonTerminals, grammar.terminals, grammar.deltas, grammar.nonTerminals.get(0));
        for (Map.Entry<String, ArrayList<Grammar.Rule>> e : grammar.map.entrySet()) {
            for (Grammar.Rule rule : e.getValue()) {
                HashSet<String> set = rule.first;
                if (set.size() == 1 && set.iterator().next().equals("#")) {
                    set = follow.get(e.getKey());
                }

                for (String terminal : set) {
                    //System.out.println("  T[" + e.getKey() + "][" + terminal + "] = " + rule);
                    table.add(e.getKey(), terminal, rule.full);
                }
            }
        }

//        grammar.getGrammar()
//               .forEach((s, rules) -> table.setLastToNonTerminal(s, rules.stream()
//                                                                         .flatMap(rule -> rule.getLast().stream())
//                                                                         .collect(Collectors.toSet())));

        table.exportToExcel(new File("table.xls"), silent);
        if (!silent) {
            System.out.println();
        }

        // Сериализация полученной таблицы
        File file = new File("table.llk");
        FileUtils.writeByteArrayToFile(file, SerializationUtils.serialize(table));
        if (!silent) {
            System.out.println("Control table serialized and saved to " + file);
            System.out.println();
        }
    }

    private static HashSet<String> first1(String a, HashMap<String, HashSet<String>> used) {
        if (used.containsKey(a)) {
            return used.get(a);
        }

        HashSet<String> result = new HashSet<>();
        if (!grammar.map.containsKey(a)) {
            result.add(a);
        } else {
            for (Rule rule : grammar.map.get(a)) {
                for (String s : first1(rule.get(0), used)) {
                    if (s.equals("#") && rule.size() > 1) {
                        rule.first.addAll(first1(rule.get(1), used));
                    } else {
                        rule.first.add(s);
                    }
                }
                result.addAll(rule.first);
            }
        }
        return result;
    }

    private static Set<String> last(String nonTerminal, Map<String, Set<String>> used) {
        System.out.println(nonTerminal);
        if (used.containsKey(nonTerminal)) {
            return used.get(nonTerminal);
        }
        Set<String> result = new HashSet<>();
        HashMap<String, ArrayList<Rule>> g = grammar.getGrammar();
        if (!g.containsKey(nonTerminal)) {
            result.add(nonTerminal);
        } else {
            for (Rule rule : g.get(nonTerminal)) {
                String nextNonTerminal = rule.get(rule.size() - 1);
                if (nextNonTerminal.equals(nonTerminal)) {
                    if (rule.size() > 1) {
                        nextNonTerminal = rule.get(rule.size() - 2);
                    } else {
                        continue;
                    }
                    if (!g.containsKey(nextNonTerminal)) {
                        continue;
                    }
                }
                for (String ruleString : last(nextNonTerminal, used)) {
                    if (ruleString.equals("#") && rule.size() > 1 && g.containsKey(rule.get(rule.size() - 2))) {
                        rule.getLast().addAll(last(rule.get(rule.size() - 2), used));
                    } else {
                        rule.getLast().add(ruleString);
                    }
                }
                result.addAll(rule.getLast());
            }
            used.put(nonTerminal, result);
        }
        return result;
    }

    private static void follow1(String a, String b, HashSet<String> used) {
        if (used.contains(a + "$" + b)) {
            return;
        }
        used.add(a + "$" + b);

        if (!grammar2.map.containsKey(b)) {
            follow.get(a).add(b);
        }

        for (ArrayList<String> l : grammar2.map.get(a)) {
            ArrayList<String> list = new ArrayList<>(l);
            list.add(b);
            for (int i = 0; i < list.size() - 1; i++) {
                if (grammar2.map.containsKey(list.get(i))) {
                    follow1(list.get(i), list.get(i + 1), used);
                }
            }
        }

        if (grammar2.map.containsKey(b)) {
            for (ArrayList<String> l : grammar2.map.get(b)) {
                ArrayList<String> list = new ArrayList<>();
                list.add(a);
                list.addAll(l);
                for (int i = 0; i < list.size() - 1; i++) {
                    if (grammar2.map.containsKey(list.get(i))) {
                        follow1(list.get(i), list.get(i + 1), used);
                    }
                }
            }
        }
    }

}

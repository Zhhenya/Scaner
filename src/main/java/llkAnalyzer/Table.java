package llkAnalyzer;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import service.DiagramsException;
import service.Types;


import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Table implements Serializable {

    private final HashMap<String, NonTerminal> nonTerminalsMapping = new HashMap<>();
    private final HashMap<String, Terminal> terminalsMapping = new HashMap<>();
    private final HashMap<Types, Integer> typesMapping = new HashMap<>();

    private final HashMap<String, Delta> deltasMapping = new HashMap<>();

    public NonTerminal getNonTerminals(String nonTerminal) {
        return nonTerminalsMapping.get(nonTerminal);
    }


    private final Cell[][] cells;

    private final Terminal end;
    private final NonTerminal axiom;

    public Table(ArrayList<String> nonTerminals, HashMap<String, Types> terminals, HashSet<String> deltas, String s) {
        cells = new Cell[nonTerminals.size()][terminals.size()];
        for (int i = 0; i < nonTerminals.size(); i++) {
            for (int j = 0; j < terminals.size(); j++) {
                cells[i][j] = new Cell();
            }
        }

        for (String nonTerminal : nonTerminals) {
            nonTerminalsMapping.put(nonTerminal, new NonTerminal(nonTerminal, nonTerminalsMapping.size()));
        }
        axiom = nonTerminalsMapping.get(s);

        Terminal tmpEnd = null;
        for (Map.Entry<String, Types> terminal : terminals.entrySet()) {
            String name = terminal.getKey();
            terminalsMapping.put(name, new Terminal(name, terminalsMapping.size(), terminal.getValue()));
            typesMapping.put(terminal.getValue(), typesMapping.size());

            if (terminal.getValue() == Types.TypeEnd) {
                tmpEnd = terminalsMapping.get(name);
            }
        }
        end = tmpEnd;

        for (String d : deltas) {
            deltasMapping.put(d, new Delta(d, deltas.size()));
        }
    }

    public void add(String nonTerminal, String terminal, ArrayList<String> rule) {
        int a = nonTerminalsMapping.get(nonTerminal).index;
        int b = terminalsMapping.get(terminal).index;

        ArrayList<Element> result = new ArrayList<>();
        for (int i = rule.size() - 1; i >= 0; i--) {
            String v = rule.get(i);
            if (v.equals("#")) {
                continue;
            }

            if (v.startsWith("[")) {
                result.add(deltasMapping.get(v));
            } else {
                result.add(nonTerminalsMapping.containsKey(v) ? nonTerminalsMapping.get(v) : terminalsMapping.get(v));
            }
        }

        cells[a][b].add(result);
    }

    public Cell get(NonTerminal nonTerminal, Types type) {
//        Integer u = typesMapping.get(type);
//        if (u == null) {
//            throw new DiagramsException("Символ такого типа не предусмотрен: " + type);
//        }
//        return cells[nonTerminal.index][typesMapping.get(type)];
     /*   System.out.println("NonTerminal " + nonTerminal);
        System.out.println("NonTerminal " + nonTerminal.index);
        System.out.println("Types"  + type);
        System.out.println("Types"  + typesMapping.get(type));*/
        return cells[nonTerminal.index][typesMapping.get(type)];
    }

    public void exportToExcel(File file, boolean silent) throws Exception {
        if (!silent) {
            System.out.println("Writing control table to " + file.getName() + "...");
        }

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Control table");

        for (int i = 0; i < cells.length + 1; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < cells[0].length + 1; j++) {
                row.createCell(j).getCellStyle().setWrapText(true);
            }
        }

        for (Map.Entry<String, Terminal> e : terminalsMapping.entrySet()) {
            sheet.getRow(0).getCell(e.getValue().index + 1).setCellValue(e.getKey());
        }

        for (Map.Entry<String, NonTerminal> e : nonTerminalsMapping.entrySet()) {
            sheet.getRow(e.getValue().index + 1).getCell(0).setCellValue(e.getKey());
        }

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j].isEmpty()) {
                    continue;
                }

                StringBuilder builder = new StringBuilder();
                for (ArrayList<Element> rule : cells[i][j]) {
                    if (builder.length() != 0) {
                        builder.append("\n");
                    }

                    for (int k = 0; k < rule.size(); k++) {
                        if (k != 0) {
                            builder.append(" ");
                        }
                        builder.append(rule.get(k));
                    }
                }

                sheet.getRow(i + 1).getCell(j + 1).setCellValue(builder.toString());
            }
        }

        for (int i = 0; i < cells[0].length + 1; i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        if (!silent) {
            System.out.println("Successfully finished!");
        }
    }

    public void setLastToNonTerminal(String nonTerminal, Set<String> last) {
        nonTerminalsMapping.get(nonTerminal).setLast(last.stream()
                                                         .map(s -> terminalsMapping.get(s).type)
                                                         .collect(Collectors.toSet()));
    }


    public NonTerminal getAxiom() {
        return axiom;
    }

    public Terminal getEndTerminal() {
        return end;
    }

    public static class Cell extends ArrayList<ArrayList<Element>> {
    }

    public static abstract class Element implements Serializable {

        public final String value;
        public final int index;

        public Element(String s, int i) {
            value = s; index = i;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    public static class Terminal extends Element {

        public final Types type;

        public Terminal(String s, int i, Types t) {
            super(s, i);
            type = t;
        }

    }

    public static class NonTerminal extends Element {

        private Set<Types> last = new HashSet<>();

        public NonTerminal(String s, int i) {
            super(s, i);
        }

        public void setLast(Set<Types> last) {
            this.last = last;
        }

        public Set<Types> getLast() {
            return last;
        }
    }

    public static class Delta extends Element {

        public Delta(String s, int i) {
            super(s.substring(1, s.length() - 1), i);
        }

        @Override
        public String toString() {
            return "Δ" + value;
        }

    }


}

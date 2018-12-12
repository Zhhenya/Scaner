import diagrams.Diagrams;
import llkAnalyzer.AnalyzeError;
import llkAnalyzer.LLkAnalyzer;
import precedence.PrecedenceAnalyzer;
import scanner.Scanner;
import service.DiagramsException;
import service.SemanticsException;
import service.Types;

import java.io.*;

public class Main {
    public static void main(String[] argv) throws Exception {

        Scanner scanner = new Scanner();
        String filePath = "src/main/resources/grammar/program1.java";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            scanner.setText(scanner.reader(filePath, reader));
            scanner.setReader(reader);
            PrecedenceAnalyzer precedenceAnalyzer = new PrecedenceAnalyzer(new File("data.prc"), scanner);
            precedenceAnalyzer.program();
            System.out.println("Анализ завершен без ошибок");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (AnalyzeError e) {
            System.out.println(e.getDisplayMessage());
        }


    }
}
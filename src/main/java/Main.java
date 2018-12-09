import diagrams.Diagrams;
import llkAnalyzer.AnalyzeError;
import llkAnalyzer.LLkAnalyzer;
import scanner.Scanner;
import service.DiagramsException;
import service.SemanticsException;
import service.Types;

import java.io.*;

public class Main {
    public static void main(String[] argv) throws Exception {
  /*      Scanner scanner = new Scanner();
        String filePath = "src/main/resources/program1.txt";
        Types types;
        StringBuilder lexeme = new StringBuilder();
        BufferedReader reader;
        scanner.setFilePath(filePath);
        Diagrams diagrams;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            scanner.setText(scanner.reader(filePath, reader));
            scanner.setReader(reader);
            diagrams = new Diagrams(scanner, lexeme);
            System.out.println("строка 1:" );
            diagrams.setRoot();
            types = diagrams.S();

            if(types != Types.TypeEnd) {
                System.out.println("Ошибка: конец файла не достигнут, проверьте правильность расстановки фигурных скобок");
                return;
            }

            if(types != Types.TypeError)
                System.out.println("Завершено без ошибок");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DiagramsException e) {
            e.printStackTrace();
        } catch (SemanticsException e) {
            e.printStackTrace();
        }*/



        Scanner scanner = new Scanner();
        String filePath = "src/main/resources/program1.txt";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            scanner.setText(scanner.reader(filePath, reader));
            scanner.setReader(reader);
            LLkAnalyzer lLkAnalyzer = new LLkAnalyzer(new File("table.llk"), scanner, new File("firstFollowTable.fft"));
            boolean t = lLkAnalyzer.program();
            if(!t)
                System.out.println("Конец программы не достигнут");
            else
                System.out.println("Анализ завершен");
        } catch (FileNotFoundException e) {
          //  e.printStackTrace();
        } /*catch (AnalyzeError e) {
            System.out.println(e.getDisplayMessage());
        }*/


    }
}
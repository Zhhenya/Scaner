import diagrams.Diagrams;
import scanner.Scanner;
import service.DiagramsException;
import service.SemanticsException;
import service.Types;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] argv){
        Scanner scanner = new Scanner();
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
//            diagrams.printTree();

            if(types != Types.TypeEnd) {
                System.out.println("Ошибка: конец файла не достигнут, проверьте правильность расстановки фигурных скобок");
                return;
            }

            if(types != Types.TypeError)
                System.out.println("Завершено без ошибок");
            /*do{
                types = scanner.scanner(lexeme);
                if(types != service.Types.TypeForReturn)
                    System.out.println("\t; тип " + types + " "  +lexeme.toString());
            }while (types != service.Types.TypeEnd );*/
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DiagramsException e) {
            e.printStackTrace();
        } catch (SemanticsException e) {
            e.printStackTrace();
        }


    }
}
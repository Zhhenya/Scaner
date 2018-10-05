import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] argv){
        Scaner scaner = new Scaner();
        String filePath = "src/main/resources/program1.txt";
        Types types;
        StringBuilder lexeme = new StringBuilder();
        BufferedReader reader;
        scaner.setFilePath(filePath);
        Diagram diagrams;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            scaner.setText(scaner.reader(filePath, reader));
            scaner.setReader(reader);
            diagrams = new Diagram(scaner, lexeme);
            System.out.println("строка 1:" );
            types = diagrams.S();

            if(types != Types.Type_error)
                System.out.println("Завершено без ошибок");
            /*do{
                types = scaner.scaner(lexeme);
                if(types != Types.Type_forReturn)
                    System.out.println("\t; тип " + types + " "  +lexeme.toString());
            }while (types != Types.Type_end );*/
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DiagramsException e) {
            e.printStackTrace();
        }


    }
}
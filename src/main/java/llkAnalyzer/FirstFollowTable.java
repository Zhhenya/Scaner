package llkAnalyzer;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FirstFollowTable implements Serializable {
    public HashMap<String, Pair> firstFollow = new HashMap<>();
    public int size = 0;

    public void exportToExcel(File file) throws Exception{
        System.out.println("Writing FirstFollow table to " + file.getName() + "...");

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("FirstFollow table");


        for(int i = 0; i < firstFollow.size(); i++) {
            Row row = sheet.createRow(i);
            for(int j = 0; j < size + 1; j++)
                row.createCell(j).getCellStyle().setWrapText(true);
        }

        for(Map.Entry<String, Pair> e : firstFollow.entrySet()) {
            sheet.getRow(e.getValue().index).getCell(0).setCellValue(e.getKey());
            Pair pair = e.getValue();
            int j = 1;
            for(String s : pair.first)
                sheet.getRow(e.getValue().index ).getCell(j++).setCellValue(s);

        }



        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();

        System.out.println("Successfully finished!");
    }




}
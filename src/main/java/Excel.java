import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Excel {
    public static void writeToFile(String filename, List<DrugInfo> drugInfoList) {
        Workbook workbook = new XSSFWorkbook(); // Using XSSF for xlsx format, for xls use HSSF
        Sheet resultSheet = workbook.createSheet("Generic list");

        int rowIndex = 0;
        Row row = resultSheet.createRow(rowIndex++);

        row.createCell(0).setCellValue("Generic name");
        row.createCell(1).setCellValue("ICD code");
        row.createCell(2).setCellValue("Therapeutic classification");
        row.createCell(3).setCellValue("Trade names");
        row.createCell(4).setCellValue("International name");
        row.createCell(5).setCellValue("Why it is prescribed");
        row.createCell(6).setCellValue("When it is not be taken");
        row.createCell(7).setCellValue("Pregnancy category");
        row.createCell(8).setCellValue("Category");
        row.createCell(9).setCellValue("Dosage and when it is to be taken");
        row.createCell(10).setCellValue("How it should be taken");
        row.createCell(11).setCellValue("Warning precaution");
        row.createCell(12).setCellValue("Side effects");
        row.createCell(13).setCellValue("Storage conditions");

        for (DrugInfo drugInfo : drugInfoList) {
            int cellIndex = 0;

            row = resultSheet.createRow(++rowIndex);
            row.createCell(cellIndex++).setCellValue(drugInfo.getGenericName().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getICDcode().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getTherapeuticClassification().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getTradeNames().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getInternationalName().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getWhyItIsPrescribed().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getWhenItIsNotBeTaken().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getPregnancyCategory().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getCategory().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getDosageAndWhenItIsToBeTaken().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getHowItShouldBeTaken().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getWarningPrecaution().trim());
            row.createCell(cellIndex++).setCellValue(drugInfo.getSideEffect().trim());
            row.createCell(cellIndex).setCellValue(drugInfo.getStorageConditions().trim());
        }

        try {
            new File("results").mkdirs();

            File file = new File("results/" + filename + ".xlsx");
            if (file.exists()) {
                file.delete();
            }

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.medindia.elance;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        char[] specifiedPages = new char[args.length];
        for (int i = 0; i<args.length; i++){
            specifiedPages[i] = args[i].charAt(0);
        }

        Medindia medindia = new Medindia(specifiedPages);
        //medindia.getGenericList();
        medindia.getBrandedList();
    }

    private static void calculatePricesQuantity() {
        int totalRows = 0;
        int totalPriceQuantity = 0;
        for (char page = 'a'; page <= 'z'; page++) {
            try {
                XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream("Finished/" + page + ".xlsx"));
                XSSFSheet sheet = wb.getSheetAt(0);

                int rows = sheet.getPhysicalNumberOfRows() + 1;
                int emptyRows = 0;
                int priceQuantity = 0;

                for (int i = 2; i < rows; i++) {
                    try {
                        XSSFRow row = sheet.getRow(i);
                        if (row != null) {
                            if (!sheet.getRow(i).getCell(6).toString().equals("")) {
                                //System.out.println(sheet.getRow(i).getCell(6).toString() + " " + sheet.getRow(i).getCell(7).toString());
                                priceQuantity++;
                            }
                        }
                    }catch (NullPointerException e){
                        emptyRows++;
                    }
                }
                System.out.println(String.format("Page: %s; Rows: %s; Price/quantity: %s; Empty rows: %s", page, rows - 2, priceQuantity, emptyRows));
                totalRows += rows - 2;
                totalPriceQuantity += priceQuantity;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(String.format("Total rows: %s; Available 'price/quaintity': %s", totalRows, totalPriceQuantity));
    }
}
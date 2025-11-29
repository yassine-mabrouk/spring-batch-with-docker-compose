package com.example.batch.config;

import com.example.batch.model.Customer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ExcelWriter implements ItemWriter<Customer> {

    private static final String OUTPUT_DIR = "app/output";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FILE_NAME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        if (chunk == null || chunk.isEmpty()) return;


        Files.createDirectories(Paths.get(OUTPUT_DIR));

        String timestamp = LocalDateTime.now().format(FILE_NAME_FORMAT);
        String fileName = OUTPUT_DIR + "/customers_" + timestamp + ".xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Customers");


            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


            Row headerRow = sheet.createRow(0);
            String[] headers = {"Customer ID", "Name", "Email", "Mobile Number",
                    "Created At", "Created By", "Updated At", "Updated By"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }


            int rowNum = 1;
            for (Customer customer : chunk.getItems()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(customer.getCustomerId());
                row.createCell(1).setCellValue(customer.getName());
                row.createCell(2).setCellValue(customer.getEmail());
                row.createCell(3).setCellValue(customer.getMobileNumber());
                row.createCell(4).setCellValue(customer.getCreatedAt() != null ?
                        customer.getCreatedAt().format(DATE_FORMATTER) : "");
                row.createCell(5).setCellValue(customer.getCreatedBy());
                row.createCell(6).setCellValue(customer.getUpdatedAt() != null ?
                        customer.getUpdatedAt().format(DATE_FORMATTER) : "");
                row.createCell(7).setCellValue(customer.getUpdatedBy() != null ?
                        customer.getUpdatedBy() : "");
            }


            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }


            try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
                workbook.write(fileOut);
            }

            System.out.println("Excel file created successfully: " + fileName);

        } catch (IOException e) {
            System.err.println("Error writing Excel file: " + e.getMessage());
            throw e;
        }
    }
}

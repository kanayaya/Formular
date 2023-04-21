package com.kanayaya.pharmnametoclass.DocumentParser;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;

public class ExcelModifier implements AutoCloseable {
    private final XSSFSheet pharmSheet;
    private XSSFWorkbook document;

    public ExcelModifier() throws IOException {
        document = new XSSFWorkbook();
        pharmSheet = document.createSheet("ФОРМУЛЯР");
    }

    @Override
    public void close() throws Exception {
        document.close();
    }
}

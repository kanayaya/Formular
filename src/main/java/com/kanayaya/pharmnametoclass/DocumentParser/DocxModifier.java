package com.kanayaya.pharmnametoclass.DocumentParser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.*;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DocxModifier {
    private final XWPFDocument document;

    public DocxModifier(File link) {
        try (InputStream stream = new FileInputStream(link)) {
            document = new XWPFDocument(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] setGroups(Function<String, Set<String>> map) {
        System.out.println("setting classes to " + document.getParagraphs().get(12).getText());
        XWPFTable table = document.getTables().get(0);
        table.addNewCol();
        int index = table.getRow(0).getTableCells().size()-1;

        for (XWPFTableRow row : table.getRows()) {
            try {
                String unprocessedName = row.getTableCells().get(1).getText().toLowerCase().trim();
                String name = unprocessedName
                        .split("[ \n]")[0]
                        .replace("+", "").replace(",", "")
                        .replace("%", "")
                        .replace(".", "")
                        .replace("0", "")
                        .replace("1", "")
                        .replace("2", "")
                        .replace("3", "")
                        .replace("4", "")
                        .replace("5", "")
                        .replace("6", "")
                        .replace("7", "")
                        .replace("8", "")
                        .replace("9", "").trim()
                        .toLowerCase();

                if (name.endsWith("ия") || name.endsWith("на") || name.endsWith("ая")) {
                    String secondWord = unprocessedName
                            .split("[ \n]")[1]
                            .replace("+", "").replace(",", "")
                            .toLowerCase();
                    name = name + secondWord;
                }
                row.getTableCells().get(index).setText(map.apply(name).toString());
                System.out.print('█');
            } catch (IndexOutOfBoundsException e) {
                System.out.println("\nErroring row:    " + row.getTableCells().stream().map((x -> x.getText())).collect(Collectors.joining()));
            }
        }
        System.out.println("\ndone");
        return turnDocumentToBytes();
    }

    private byte[] turnDocumentToBytes() {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            document.write(stream);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

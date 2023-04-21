package com.kanayaya.pharmnametoclass.DocumentParser;

import java.io.*;

public class DocumentWriter {
    public void write(File file, byte[] data) {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

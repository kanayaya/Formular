package com.kanayaya.pharmnametoclass.DocumentParser;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class DocxUniter {
    private Function<String, Set<String>> classNames;
    private String folder;
    public DocxUniter(Function<String, Set<String>> classNames, String folder) {
        this.classNames = classNames;
        this.folder = folder;
    }

    public void handleAll(List<File> files) {
        for (File f :
                files) {
            new DocumentWriter().write(new File(folder+f.getName()), new DocxModifier(f).setGroups(classNames));
        }
    }
}

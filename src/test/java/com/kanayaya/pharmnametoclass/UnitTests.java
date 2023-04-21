package com.kanayaya.pharmnametoclass;

import com.kanayaya.pharmnametoclass.DocumentParser.DocxUniter;
import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class UnitTests {

    private static final List<File> files = new ArrayList<>();
    static {
        files.add(new File("C:\\Users\\kanay\\Desktop\\Фарма\\Формуляр психоневр.docx"));
        files.add(new File("C:\\Users\\kanay\\Desktop\\Фарма\\Формуляр Онкология.docx"));
        files.add(new File("C:\\Users\\kanay\\Desktop\\Фарма\\Формуляр офтальмология.docx"));
        files.add(new File("C:\\Users\\kanay\\Desktop\\Фарма\\Формуляр эндокринология.docx"));
        files.add(new File("C:\\Users\\kanay\\Desktop\\Фарма\\Формуляр ОАР.docx"));
        files.add(new File("C:\\Users\\kanay\\Desktop\\Фарма\\Формуляр нефрология.docx"));
        files.add(new File("C:\\Users\\kanay\\Desktop\\Фарма\\Формуляр Кардиология 23.docx"));
        files.add(new File("C:\\Users\\kanay\\Desktop\\Фарма\\Формуляр АМП 23.docx"));
        files.add(new File("C:\\Users\\kanay\\Desktop\\Фарма\\Формуляр аллергология, пульмонология.docx"));
    }

    @Test
    public void testDocs() {
        DocxUniter uniter = new DocxUniter((x) -> new HashSet<>(), "C:\\Users\\kanay\\Desktop\\хуета\\");

        uniter.handleAll(files);
    }

    @Test
    public void testController() throws IOException, ExecutionException, InterruptedException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HelloController controller = new HelloController();

        Set<String> strings = new HashSet<>();
        strings.add("hehehe");

        Field nameTC = controller.getClass().getDeclaredField("nameToClass");
        nameTC.setAccessible(true);
        nameTC.set(controller, (Function<String, Set<String>>)(x) -> strings);

        Method wroteFiles = controller.getClass().getDeclaredMethod("wroteFiles", List.class);
        wroteFiles.setAccessible(true);
        wroteFiles.invoke(controller, files);

    }
}

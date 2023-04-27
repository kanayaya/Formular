package com.kanayaya.pharmnametoclass;

import com.kanayaya.pharmnametoclass.DocumentParser.DocxUniter;
import com.kanayaya.pharmnametoclass.Parser.IProxyList;
import com.kanayaya.pharmnametoclass.Parser.NameToClass;
import com.kanayaya.pharmnametoclass.Parser.ProxyList;
import com.kanayaya.pharmnametoclass.Parser.WholeClassificationDownloader;
import com.kanayaya.pharmnametoclass.SearchTrie.PartialLevenstein;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class HelloController {
    File folder;
    boolean folderGot;
     IProxyList proxyList = new ProxyList(
            "https://www.freeproxy.world/?type=socks4",
                    "https://www.freeproxy.world/?type=socks4&anonymity=&country=&speed=&port=&page=2",
                    "https://www.freeproxy.world/?type=socks4&anonymity=&country=&speed=&port=&page=3",
                    "https://www.freeproxy.world/?type=socks5&anonymity=&country=&speed=&port=&page=2",
                    "https://www.freeproxy.world/?type=socks4&anonymity=&country=&speed=&port=&page=4",
                    "https://www.freeproxy.world/?type=socks5&anonymity=&country=&speed=&port=&page=3",
                    "https://www.freeproxy.world/?type=socks5");
    private final WholeClassificationDownloader downloader = new WholeClassificationDownloader(proxyList);
    private final NameToClass classGetter = new NameToClass(proxyList);
    private final Function<String, Set<String>> nameToClass = (name) -> {
        if (name.trim().length() < 3) {
            Set<String> strings = new HashSet<>();
            strings.add("Нет в РЛС");
            return strings;
        }
        String keys = downloader.getTrie().fuzFindFirst(new PartialLevenstein(name.toLowerCase().trim()), name.length()/7+1);
        Set<String> result = downloader.getMap().get(keys==null? name.toLowerCase().trim() : keys);
        if (result == null) {
            result = new HashSet<>();
            try {
                result.add(classGetter.getClassByName(name.toLowerCase().trim().replace(' ', '+')));
            } catch (IOException | NoSuchElementException e) {
                Set<String> strings = new HashSet<>();
                strings.add("Нет в РЛС");
                return strings;
            }
        }
        System.out.println(downloader.getTrie().size());
        return result;
    };
    @FXML
    private Label welcomeText;
    @FXML
    private TextField name;


    public HelloController() throws IOException, ExecutionException, InterruptedException {
    }

    @FXML
    protected void onHelloButtonClick() {
        ExecutorService thread = Executors.newSingleThreadExecutor();
        thread.submit(() -> {
            try {
                downloader.download();
            } catch (IOException | ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            thread.shutdown();
        });
        AnimationTimer at = new AnimationTimer() {
            @Override
            public void handle(long l) {
                welcomeText.setText(downloader.getPercent() + "%");
                if (downloader.getPercent() == 100) {
                    welcomeText.setText(downloader.getPercent() + "%. Теперь перетащите в окно любую папку куда будет выдан результат");
                    this.stop();
                }
            }
        };
        at.start();
    }

    @FXML
    protected void dragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    @FXML
    protected void dragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        List<File> files = null;
        if (db.hasFiles()) {
            files = db.getFiles();
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
        if (folderGot) {
            if (files != null) {
                final List<File> constFiles = files;
                new Thread(() -> wroteFiles(constFiles)).start();
            } else welcomeText.setText("Повторите попытку");
        } else {
            if (files != null) {
                folder = files.get(0);
                if (folder.isFile()) welcomeText.setText("Нужна папка, не файл!");
                else {
                    welcomeText.setText("Папка получена, теперь Word-файлы");
                    System.out.println(folder);
                    folderGot = true;
                }
            } else welcomeText.setText("Повторите попытку");
        }

    }

    private void wroteFiles(List<File> files) {
        DocxUniter uniter = new DocxUniter(nameToClass, folder.getAbsolutePath() + '\\');
        uniter.handleAll(files);
    }
}
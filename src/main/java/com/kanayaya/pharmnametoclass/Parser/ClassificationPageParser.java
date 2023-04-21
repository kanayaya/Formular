package com.kanayaya.pharmnametoclass.Parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClassificationPageParser {
    private final Document page;

    public ClassificationPageParser(Document page) {
        this.page = page;
    }

    public Queue<String> getClassLinks() {
        Queue<String> result = new ConcurrentLinkedQueue<>();

        Element classesTree = page.selectFirst("div.b-tree__collapse");
        if (classesTree == null) {
            throw new NullPointerException("Page parsing error: classes tree is null");
        }

        Elements elements = classesTree.select("li.b-tree__parent-li");
        for (Element e :
                elements) {
            Element a = e.selectFirst("a");
            if (a != null) {
                result.add(a.attr("href"));
            }
        }
        return result;
    }
}

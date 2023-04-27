package com.kanayaya.pharmnametoclass.Parser;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

public class NameToClass {
    private final IProxyList proxyList;
    Proxy proxy;
    LocalDateTime timeFromError = LocalDateTime.MIN;
    public NameToClass(IProxyList proxyList) {
        this.proxyList = proxyList;
    }

    public String getClassByName(String name) throws IOException {
        if (name.equals("")) return "Нет в рлс";
        System.out.println(name);
        Document document = null;
        while (timeFromError.plusMinutes(2).compareTo(LocalDateTime.now()) < 0) {
            try {
                document = Jsoup.connect("https://www.rlsnet.ru/search_result.htm?word=" + name)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                        .get();
            } catch (HttpStatusException e) {
                System.out.println("\n" + e.getClass() + e.getLocalizedMessage());
                proxy = proxy==null? proxyList.get() : proxy;
                timeFromError = LocalDateTime.now();
                System.out.println("Using proxy now");
            }
        }
        while (proxy != null && document == null) {
            try {
                document = Jsoup.connect("https://www.rlsnet.ru/search_result.htm?word=" + name)
                        .proxy(proxy)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                        .get();
            } catch (IOException e) {
                System.out.println("\n" + e.getClass() + e.getLocalizedMessage());
                proxyList.freeProxy(proxy);
                proxy = proxyList.get();
            }
        }
        Element div = document.select("div.head")
                .stream().filter(x -> x.text().equals("В фармакологических группах"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Drug not found:  " + name));

        Elements str = div.nextElementSibling().select("li");
        proxyList.freeProxy(proxy);
        return str.get(0).text();
    }
}

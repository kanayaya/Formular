package com.kanayaya.pharmnametoclass.Parser;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WholeClassificationDownloader {
    IProxyList proxyList;

    private final ClassificationPageParser parser;
    private final Map<String, Set<String>> classification = new ConcurrentHashMap<>();
    private boolean downloaded = false;

    private final AtomicInteger counter = new AtomicInteger(0);
    private int size = 0;
    private ExecutorService executors;

    public WholeClassificationDownloader(IProxyList proxyList) {
        this.proxyList = proxyList;
        Proxy proxy = proxyList.get();
        Document classTree = null;
        while (classTree == null) {
            try {
                classTree = Jsoup.connect("https://www.rlsnet.ru/pharm-groups")
                        .proxy(proxy)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                        .get();
            } catch (IOException e) {
                proxy = proxyList.get();
            }
        }
        parser = new ClassificationPageParser(classTree);
    }

    public Map<String, Set<String>> getMap() {
        if (classification.isEmpty()) throw new IllegalStateException("Download first!");
        return classification;
    }

    public void download() throws IOException, ExecutionException, InterruptedException {
        if (downloaded) return;
        Queue<String> classLinks = parser.getClassLinks();
        size = classLinks.size();
        String[] th3Links = new String[] {
                "https://www.freeproxy.world/?type=socks4",
                "https://www.freeproxy.world/?type=socks4&anonymity=&country=&speed=&port=&page=2",
                "https://www.freeproxy.world/?type=socks4&anonymity=&country=&speed=&port=&page=3",
                "https://www.freeproxy.world/?type=socks5&anonymity=&country=&speed=&port=&page=2",
                "https://www.freeproxy.world/?type=socks4&anonymity=&country=&speed=&port=&page=4",
                "https://www.freeproxy.world/?type=socks5&anonymity=&country=&speed=&port=&page=3",
                "https://www.freeproxy.world/?type=socks5"
        };

        for (Future<?> f :
                startThreadPool(classLinks)) {
            f.get();
        }
        executors.shutdown();
        downloaded  = true;
    }

    private List<Future<?>> startThreadPool(Queue<String> classLinks) throws InterruptedException {
        Thread.sleep(9000);
        List<Future<?>> futures = new ArrayList<>();
        int nThreads = 16;
        executors = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            int n = i;
            futures.add(executors.submit(() -> downloadTillEnd(classLinks, proxyList, n)));
        }
        return futures;
    }

    private void downloadTillEnd(Queue<String> classLinks, IProxyList proxyList, int threadCounter ) {
        Proxy proxy = proxyList.get();
        while ( ! classLinks.isEmpty()) {
            String poll = classLinks.poll();
            while (true) {
                try {
                    downloadFromLink(proxy, threadCounter, poll);
                    break;
                } catch (HttpStatusException e) {
                    System.out.println(e.getClass() + e.getLocalizedMessage());
                    proxyList.alert429(proxy);
                    proxy = proxyList.get();
                } catch (IOException e) {
                    System.out.println(e.getClass() + e.getLocalizedMessage());
                    proxyList.freeProxy(proxy);
                    proxy = proxyList.get();
                }
            }

        }
        proxyList.freeProxy(proxy);
        System.out.println("Thread done:   " + threadCounter);
    }

    private void downloadFromLink(Proxy proxy, int threadCounter, String link) throws IOException {
        Document classPage = getDocument(proxy, link);
        Element drugNames = classPage.selectFirst("[name=aph]");
        Element classHeader = classPage.selectFirst("h1.b-tree-detail__heading");
        String drugClassName = classHeader.text().toLowerCase().trim();

        Elements merchantNames = classPage.selectFirst("table.b-drug-choice-table").select("tr");
        for (Element drugName: merchantNames) {
            putToClassification(drugClassName, drugName);
        }

        if (drugNames != null) {
            for (Element drugName :
                    drugNames.select("option")) {
                putToClassification(drugClassName, drugName);
            }
        } else System.out.println(link);
        int cnt = counter.incrementAndGet();
        System.out.println(cnt + " of " + size + " (" + threadCounter + ")  " + drugClassName);
    }

    private void putToClassification(String drugClassName, Element drugName) {
        String key = drugName.text().toLowerCase().replace("®", "")
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
                .replace("9", "").trim();
        classification.computeIfPresent(key, (k, set) -> {
            set.add(drugClassName);
            return set;
        });
        classification.computeIfAbsent(key, (x) -> {
            Set<String> set = new LinkedHashSet<>();
            set.add(drugClassName);
            return set;
        });
    }

    private Document getDocument(Proxy proxy, String link) throws IOException {
        Document classPage;
        classPage = Jsoup.connect(link)
                    .proxy(proxy)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                    .get();

        return classPage;
    }

    public int getPercent() {
        if (size == 0) return 0;
        return counter.get()*100/size;
    }
}

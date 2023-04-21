package com.kanayaya.pharmnametoclass.Parser;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyList implements IProxyList {
    private final LinkedHashMap<Proxy, LocalDateTime> proxiesMap = new LinkedHashMap<>();
    private final LocalDateTime created = LocalDateTime.now();
    private int shift = 0;

    public ProxyList(String... links) throws IOException {
        List<Proxy> proxies = new ArrayList<>();
        for (String link :
                links) {
            proxies.addAll(downloadFromLink(link));
        }
        verifyProxies(proxies);
        System.out.println("proxies downloaded");
    }

    private List<Proxy> downloadFromLink(String sourceLink) throws IOException {
        Document table = getDocument(Jsoup.connect(sourceLink));
        List<Proxy> proxies = new ArrayList<>();

        Elements rows = table.select("td.show-ip-div");
        for (Element row :
                rows) {
            String port = row.nextElementSibling().text();
            String host = row.text();
            proxies.add(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, Integer.parseInt(port.trim()))));
        }
        return proxies;
    }

    private Document getDocument(Connection sourceLink) throws IOException {
        return sourceLink
                .userAgent("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                .header("Accept-Language", "en-US")
                .header("Accept-Encoding", "gzip,deflate,sdch")
                .get();
    }

    private void verifyProxies(List<Proxy> unverifiedProxies) {
        ExecutorService executorService = Executors.newFixedThreadPool(unverifiedProxies.size());
        for (Proxy proxy: unverifiedProxies) {
            executorService.submit(() -> checkProxy(proxy));
        }
        executorService.submit(() -> {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            executorService.shutdown();
        });
    }

    private void checkProxy(Proxy p) {
        try {
            LocalDateTime begin = LocalDateTime.now();
            getDocument(Jsoup.connect("https://www.rlsnet.ru/")
                    .proxy(p));
            LocalDateTime end = LocalDateTime.now();
            if (begin.compareTo(end.minusSeconds(15)) > 0 || proxiesMap.size()<20) {
                proxiesMap.put(p, LocalDateTime.MIN);
                System.out.println("Proxies got:  " + proxiesMap.keySet().size());
            }
        } catch (SocketTimeoutException | HttpStatusException | SocketException |
                 SSLException ignored) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Proxy get() {
        while ((proxiesMap.isEmpty() || proxiesMap.size() < 16) && created.plusMinutes(1).compareTo(LocalDateTime.now()) > 0) {
            waitHalfASecond();
        }
        while (LocalDateTime.now().compareTo(proxiesMap.get((new ArrayList<>(proxiesMap.keySet())).get(shift))
        ) < 0) {
            waitHalfASecond();
            System.out.println("Seconds before using:   " + ChronoUnit.SECONDS.between(
                    LocalDateTime.now(),
                    proxiesMap.get((new ArrayList<>(proxiesMap.keySet())).get(shift))));
            shift();
        }

        proxiesMap.put(((List<Proxy>) new ArrayList<>(proxiesMap.keySet())).get(shift), LocalDateTime.now().plusHours(2));
        return ((List<Proxy>) new ArrayList<>(proxiesMap.keySet())).get(shift);
    }

    private void waitHalfASecond() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void shift() {
        if (proxiesMap.size() - 1 > shift) {
            shift++;
        } else shift = 0;
        System.out.println("Changing proxy to: " + new ArrayList<>(proxiesMap.keySet()).get(shift) + "  (" + shift + "/" + proxiesMap.size() + ")");
    }
    @Override
    public synchronized void freeProxy(Proxy p) {
        proxiesMap.put(p, LocalDateTime.now().plusNanos(10000000));
    }
    @Override
    public synchronized void alert429(Proxy p) {
        proxiesMap.put(p, LocalDateTime.now().plusMinutes(1));
    }
}

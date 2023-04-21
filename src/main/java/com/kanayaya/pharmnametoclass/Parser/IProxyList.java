package com.kanayaya.pharmnametoclass.Parser;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;

public interface IProxyList {


    Proxy get();

    void shift();

    void freeProxy(Proxy p);

    void alert429(Proxy p);
}

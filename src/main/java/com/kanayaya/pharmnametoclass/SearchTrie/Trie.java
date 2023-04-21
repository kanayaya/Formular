package com.kanayaya.pharmnametoclass.SearchTrie;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Trie extends AbstractSet<String> {
    private List<Node> roots = new ArrayList<>();

    public Trie() {

    }
    @Override
    public Iterator<String> iterator() {
        return new TrieIterator(this);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean add(String s) {
        return false;
    }


    private interface Noode {
        void add(String extension);

        List<String> getAll(String prefix);

        int size();
    }

    private class RootNode implements Noode {

        protected List<Node> children;
        public RootNode() {
            children = new ArrayList<>();
        }

        @Override
        public void add(String extension) {

        }

        @Override
        public List<String> getAll(String prefix) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private class Node implements Comparable<Node>, Noode {
        private String value;
        private List<Node> children;
        private boolean isWord;
        public Node(String value) {
            this.value = value;
            isWord = true;
        }

        @Override
        public void add(String extension) {
            if (children == null) children = new ArrayList<>();
            int i = 1;
            for (; i < value.length(); i++) {
                if (this.value.charAt(i) == extension.charAt(i)) continue;
                children.add(new Node(value.substring(i)));
                children.add(new Node(extension.substring(i)));
                value = value.substring(0,i);
                isWord = false;
                return;
            }
            children.add(new Node(extension.substring(i)));

        }

        @Override
        public List<String> getAll(String prefix) {
            List<String> result = new ArrayList<>();
            prefix = prefix + value;
            if (isWord) {
                result.add(prefix);
            }
            if (children != null) {
                for (Node child :
                        children) {
                    result.addAll(child.getAll(prefix));
                }
            }
            return result;
        }
        @Override
        public int size() {
            int size = 0;
            if (isWord) size++;
            if (children != null) {
                for (Node child :
                        children) {
                    size+=child.size();
                }
            }
            return size;
        }

        @Override
        public int compareTo(Node o) {
            return Character.compare(this.value.charAt(0), o.value.charAt(0));
        }
    }
    private class TrieIterator implements Iterator<String> {

        public TrieIterator(Trie strings) {

        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public String next() {
            return null;
        }
    }
}

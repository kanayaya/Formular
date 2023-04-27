package com.kanayaya.pharmnametoclass.SearchTrie;


import java.util.*;

public class FuzzyTrie extends AbstractSet<String> {
    private final RootNode root = new RootNode();
    private int size;

    public FuzzyTrie() {

    }
    public FuzzyTrie(Collection<String> donor) {
        for (String s : donor) {
            root.add(s);
        }
    }
    @Override
    public Iterator<String> iterator() {
        return new TrieIterator();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public synchronized boolean add(String s) {
        boolean added = root.add(s);
        if (added) size++;
        return added;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        for (String s : c) {
            add(s);
        }
        return true;
    }

    public void addAll(String... c) {
        for (String s : c) {
            add(s);
        }
    }

    public String fuzFindFirst(PartialLevenstein differencer, int maxDistance) {
        return root.fuzFindFirst(differencer, maxDistance);
    }
    public List<String> fuzFindAll(PartialLevenstein differencer, int maxDistance) {
        return root.fuzFindAll(differencer, maxDistance);
    }
    public String fuzFindClosest(PartialDifferencer differencer, int maxDistance) {
        return root.fuzFindClosest(differencer, maxDistance);
    }


    private interface Node {
        boolean add(String extension);
        List<String> getAll(String prefix);
        int size();
        String get(int index);
        static boolean addChecking(String extension, List<ChildNode> children) {
            int index = binarySearch(children, extension);
            if (index >= 0) {
                return children.get(index).add(extension);
            } else {
                children.add(-(index+1), new ChildNode(extension));
            }
            return true;
        }
    }

    private static class RootNode implements Node {

        protected List<ChildNode> children;
        public RootNode() {
            children = new ArrayList<>();
        }

        @Override
        public boolean add(String extension) {
            return Node.addChecking(extension, children);
        }

        @Override
        public List<String> getAll(String prefix) {
            List<String> result = new ArrayList<>();
            for (ChildNode child :
                    children) {
                result.addAll(child.getAll(""));
            }
            return result;
        }

        @Override
        public int size() {
            int size = 0;
            for (Node c : children) {
                size+=c.size();
            }
            return size;
        }

        @Override
        public String get(int index) {
            for (Node n :
                    children) {
                if (index < n.size()) return n.get(index);
                else index-=n.size();
            }
            return null;
        }

        @Override
        public String toString() {
            return "RootNode{\n" +
                    "children=" + children +
                    '}';
        }
        public String fuzFindFirst(PartialLevenstein differencer, int maxDistance) {
            for (ChildNode node : children) {
                String result = node.fuzFindFirst(differencer, maxDistance);
                if (result != null) return result;
            }
            return null;
        }
        public List<String> fuzFindAll(PartialLevenstein differencer, int maxDistance) {
            List<String> results = new ArrayList<>();
            for (ChildNode node : children) {
                List<String> result = node.fuzFindAll(differencer, maxDistance);
                if (result != null) results.addAll(result);
            }
            return results.size()==0? null : results;
        }

        public String fuzFindClosest(PartialDifferencer differencer, int maxDistance) {
            String result = null;
            int distance = maxDistance;
            for (ChildNode node :
                    children) {
                String s = node.fuzFindClosest(differencer, distance);
                result = s == null? result : s;
                if (result != null) {
                    distance = differencer.increaseDifference(result);
                    differencer.decreaseDifference(result);
                }
            }
            return result;
        }
    }

    private static class ChildNode implements Comparable<ChildNode>, Node {
        private String value;
        private List<ChildNode> children;
        private boolean isWord;
        public ChildNode(String value) {
            this.value = value;
            isWord = true;
        }

        private ChildNode(String value, List<ChildNode> children, boolean isWord) {
            this.value = value;
            this.children = children;
            this.isWord = isWord;
        }


        @Override
        public boolean add(String extension) {
            if (extension.equals(value)) return false;
            String smaller;
            String bigger;

            bigger = extension.length()>value.length()? extension: value;
            smaller = extension.length()<value.length()? extension: value;

            List<ChildNode> old = children;

            int i = 1;
            for (; i < smaller.length(); i++) {
                if (this.value.charAt(i) == extension.charAt(i)) continue;
                children = new ArrayList<>();
                children.add(new ChildNode(value.substring(i), old, isWord));
                Node.addChecking(extension.substring(i), children);
                value = value.substring(0,i);
                isWord = false;
                return true;
            }

            if (value.equals(bigger)) {
                value = smaller;
                children = new ArrayList<>();
                children.add(new ChildNode(bigger.substring(i), old, isWord));
                isWord = true;
            } else {
                if (children == null) {
                    children = new ArrayList<>();
                    children.add(new ChildNode(bigger.substring(i)));
                }
                Node.addChecking(bigger.substring(i), children);
            }
            return true;
        }

        @Override
        public List<String> getAll(String prefix) {
            List<String> result = new ArrayList<>();
            prefix = prefix + value;
            if (isWord) {
                result.add(prefix);
            }
            if (children != null) {
                for (ChildNode child :
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
                for (ChildNode child :
                        children) {
                    size+=child.size();
                }
            }
            return size;
        }

        @Override
        public String get(int index) {
            if (isWord) {
                if (index == 0) {
                    return value;
                }
                index--;
            }
            for (Node n :
                    children) {
                if (index < n.size()) return value + n.get(index);
                else index-=n.size();
            }
            return null;
        }
        @Override
        public int compareTo(ChildNode o) {
            return compareTo(o.value);
        }
        public int compareTo(String s) {
            return Character.compare(this.value.charAt(0), s.charAt(0));
        }

        @Override
        public String toString() {
            return ("ChildNode{" +
                    "isWord=" + isWord +
                    ",\nvalue='" + value + "':" +
                    ",\nchildren=" + children +
                    "}\n").replace("\n", "\n    ");
        }
        public String fuzFindFirst(PartialDifferencer differencer, int maxDistance) {
            int distance = differencer.increaseDifference(value);
            if (alreadyFailed(differencer, maxDistance, distance)) return null;
            if (selfSuccess(differencer, maxDistance)) return value;
            if (children != null) {
                for (ChildNode node :
                        children) {
                    String result = node.fuzFindFirst(differencer, maxDistance);
                    if (result != null) return value + result;
                }
            }
            differencer.decreaseDifference(value);
            return null;
        }
        public List<String> fuzFindAll(PartialDifferencer differencer, int maxDistance) {
            List<String> results = new ArrayList<>();
            int distance = differencer.increaseDifference(value);
            if (alreadyFailed(differencer, maxDistance, distance)) return null;
            if (selfSuccess(differencer, maxDistance)) results.add(value);
            if (children != null) {
                for (ChildNode node :
                        children) {
                    List<String> result = node.fuzFindAll(differencer, maxDistance);
                    if (result != null) result.forEach(x-> results.add(value+x));
                }
            }
            differencer.decreaseDifference(value);
            return results.size() == 0? null : results;
        }

        private boolean selfSuccess(PartialDifferencer differencer, int maxDistance) {
            return isWord && differencer.getMaxDistance() <= maxDistance;
        }

        private boolean alreadyFailed(PartialDifferencer differencer, int maxDistance, int distance) {
            if (distance > maxDistance) {
                differencer.decreaseDifference(value);
                return true;
            }
            return false;
        }

        public String fuzFindClosest(PartialDifferencer differencer, int maxDistance) {
            String result = null;
            int distance = differencer.increaseDifference(value);
            if (distance > maxDistance) {
                differencer.decreaseDifference(value);
                return null;
            }
            if (isWord && differencer.getMaxDistance() <= maxDistance) {
                result = value;
                distance = differencer.getMaxDistance();
            }
            if (children != null) {
                for (ChildNode node :
                        children) {
                    String promresult = node.fuzFindClosest(differencer, distance);
                    if (promresult != null) result = value + promresult;
                }
            }
            differencer.decreaseDifference(value);
            return result;
        }
    }
    private class TrieIterator implements Iterator<String> {
        int counter = 0;

        @Override
        public void remove() {
            Iterator.super.remove();
        }

        @Override
        public boolean hasNext() {
            return counter<root.size();
        }
        @Override
        public String next() {
            return root.get(counter++);
        }
    }


    public static int binarySearch(List<ChildNode> list, String target) {
        if (list.size() == 0) return -1;
        int left = 0;
        int right = list.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = list.get(mid).compareTo(target);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -(left+1);
    }

    @Override
    public String toString() {
        return root.getAll(null).toString();
    }
    public String toTreeString() {
        return root.toString();
    }
}


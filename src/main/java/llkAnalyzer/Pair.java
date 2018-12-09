package llkAnalyzer;

import java.io.Serializable;
import java.util.HashSet;

public class Pair implements Serializable {
    public HashSet<String> first;
    public int index = 0;

    public Pair(HashSet<String> first, int index) {
        this.first = first;
        this.index = index;
    }



}
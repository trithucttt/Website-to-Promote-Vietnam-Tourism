package com.trithuc.dataStructures;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {

    public Map<Character, TrieNode> children = new HashMap<>();
    public boolean isEndOfWord = false;
}

package com.trithuc.dataStructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Trie {
    private final TrieNode root;

    public Trie(){
        root = new TrieNode();
    }
    public  void  insert(String word){
        TrieNode node = root;
        for (char c : word.toLowerCase().toCharArray()){
            node = node.children.computeIfAbsent(c, k-> new TrieNode());
        }
        node.isEndOfWord = true;
    }

    public List<String> autocomplete(String prefix){
        List<String> results = new ArrayList<>();
        TrieNode node = root;

        for (char c : prefix.toLowerCase().toCharArray()){
            node = node.children.get(c);
            if (node == null){
                return  results;
            }
        }
        findAllWord(node, results, prefix.toLowerCase());
        return results;
    }
    private void findAllWord(TrieNode node, List<String> results, String current){
        if (node.isEndOfWord){
            results.add(current);
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()){
            findAllWord(entry.getValue(), results, current + entry.getKey());
        }
    }

}

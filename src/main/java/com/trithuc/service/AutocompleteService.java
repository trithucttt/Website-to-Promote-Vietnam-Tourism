package com.trithuc.service;

import java.util.List;

public interface AutocompleteService {
    void addWord(String word);

    List<String> getSuggestions(String input);
}

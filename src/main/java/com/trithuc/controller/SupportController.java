package com.trithuc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trithuc.service.AutocompleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class SupportController {

    @Autowired
    private AutocompleteService autocompleteService;

    @Value("${openai.api.key}")
    private String apiKey;

    @PostMapping("/call/autocomplete")
    public ResponseEntity<String> getAutocomplete(@RequestParam String prompt) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        System.out.println("API Key: " + apiKey);
      String url =  "https://api.openai.com/v1/chat/completions";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> requestBodyMap = new HashMap<>();
//        requestBodyMap.put("prompt", prompt);
//        requestBodyMap.put("max_tokens", 50);
//
//        String requestBody = objectMapper.writeValueAsString(requestBodyMap);
        String requestBody = String.format("{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": 50}", prompt);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody,headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            return response;
        } catch (HttpClientErrorException e) {
            System.out.println("Error Response: " + e.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        }

    }

    @PostMapping("/addWord")
    public ResponseEntity<String> addWord(@RequestParam String word) {
        autocompleteService.addWord(word);
        return ResponseEntity.ok("Word added successfully!");
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(@RequestParam String input) {
        List<String> suggestions = autocompleteService.getSuggestions(input);
        return ResponseEntity.ok(suggestions);
    }
}

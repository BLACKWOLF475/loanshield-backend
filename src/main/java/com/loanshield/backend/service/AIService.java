package com.loanshield.backend.service;

import com.loanshield.backend.model.ChatRequest;
import com.loanshield.backend.model.UserProfile;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final FinancialService financialService;
    private final ProfileService profileService;
    public String processMessage(ChatRequest request) {

    String message = request.getMessage();
    String userId = request.getUserId();

    var profile = profileService.getProfile(userId);

    // 🔹 Extract data
    Map<String, Double> data = extractData(message);

    // 🔹 Update memory
    data.forEach((key, value) -> {
        if (value > 0) {
            profileService.updateProfile(profile, key, value);
        }
    });

    // 🔹 Check if enough data exists
    if (profile.getIncome() != null && profile.getIncome() > 0) {

        if (profile.getIncome() != null && profile.getIncome() > 0 &&
            profile.getExpenses() != null &&
            profile.getExistingEmi() != null) {

            double emi = financialService.calculateEMI(300000, 10, 60);

            String verdict = financialService.getLoanSafety(
                    profile.getIncome(),
                    profile.getExpenses(),
                    profile.getExistingEmi(),
                    emi
            );

            return formatResponse(emi, verdict);
        }

        return askMissing(profile);
    }

    return callGroq(message);
}
private String askMissing(UserProfile profile) {

    if (profile.getIncome() == null) {
        return "What is your monthly income?";
    }

    if (profile.getExpenses() == null) {
        return "What are your monthly expenses?";
    }

    if (profile.getExistingEmi() == null) {
        return "Do you have any existing EMIs?";
    }

    return "Tell me about your loan amount.";
}

    private String callGroq(String userMessage) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://api.groq.com/openai/v1/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.1-8b-instant");
        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content", buildSystemPrompt()
        ));

        messages.add(Map.of(
                "role", "user",
                "content", userMessage
        ));

        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map choice = (Map) ((List) response.getBody().get("choices")).get(0);
        Map msg = (Map) choice.get("message");

        return msg.get("content").toString();
    }

    // 🔥 Clean system prompt (VERY IMPORTANT)
    private String buildSystemPrompt() {
        return """
        You are LoanShield AI, a financial advisor for Indian users.

        Your job:
        - Help users understand loans
        - Ask for income, expenses, existing EMIs
        - Keep answers simple and clear

        Rules:
        - Do not use complex banking terms
        - Ask one question at a time
        - Be short and helpful
        """;
    }

    private String formatResponse(double emi, String verdict) {
        return "📊 EMI: ₹" + (int) emi + "\n" +
               "📌 Status: " + verdict;
    }

    // Basic extractor (we'll improve later)
    private Map<String, Double> extractData(String msg) {

        Map<String, Double> data = new HashMap<>();
        msg = msg.toLowerCase();

        if (msg.contains("salary") || msg.contains("income")) {
            data.put("income", extractNumber(msg));
        }
        if (msg.contains("loan")) {
            data.put("loan", extractNumber(msg));
        }
        if (msg.contains("expense")) {
            data.put("expenses", extractNumber(msg));
        }

        return data;
    }

    private double extractNumber(String text) {
        String num = text.replaceAll("[^0-9]", "");
        if (num.isEmpty()) return 0;
        return Double.parseDouble(num);
    }
}
package com.loanshield.backend.controller;

import com.loanshield.backend.model.ChatRequest;
import com.loanshield.backend.model.ChatResponse;
import com.loanshield.backend.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin
public class ChatController {

    private final AIService aiService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = aiService.processMessage(request);
        return new ChatResponse(reply);
    }
}
package com.loanshield.backend.model;

//package com.loanshield.backend.model;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String userId;
}
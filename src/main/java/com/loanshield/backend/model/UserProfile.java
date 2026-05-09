package com.loanshield.backend.model;

import lombok.Data;

@Data
public class UserProfile {
    private String userId;
    private Double income;
    private Double expenses;
    private Double existingEmi;
}
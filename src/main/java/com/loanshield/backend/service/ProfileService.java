package com.loanshield.backend.service;

import com.loanshield.backend.model.UserProfile;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProfileService {

    private final ConcurrentHashMap<String, UserProfile> store = new ConcurrentHashMap<>();

    public UserProfile getProfile(String userId) {
        return store.computeIfAbsent(userId, id -> {
            UserProfile p = new UserProfile();
            p.setUserId(id);
            return p;
        });
    }

    public void updateProfile(UserProfile profile, String key, Double value) {
        switch (key) {
            case "income" -> profile.setIncome(value);
            case "expenses" -> profile.setExpenses(value);
            case "existing" -> profile.setExistingEmi(value);
        }
    }
}
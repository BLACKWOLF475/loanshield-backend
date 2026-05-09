package com.loanshield.backend.service;

import org.springframework.stereotype.Service;

@Service
public class FinancialService {

    public double calculateEMI(double principal, double rate, int months) {
        double r = rate / (12 * 100);
        return (principal * r * Math.pow(1 + r, months)) /
               (Math.pow(1 + r, months) - 1);
    }

    public String getLoanSafety(double income, double expenses, double existingEmi, double newEmi) {

        double survival = income * 0.35;
        double disposable = income - expenses - existingEmi - survival;
        double maxSafeEmi = disposable * 0.40;

        if (newEmi <= maxSafeEmi) return "SAFE";
        if (newEmi <= maxSafeEmi * 1.25) return "CAUTION";
        return "UNSAFE";
    }
}
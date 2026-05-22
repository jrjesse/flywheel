package com.antigravity.sales.core.service;

import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RevenueParserService {

    // Regex para achar RRMx12 = 30.000,00 ou RRMx12=30.000,00
    private static final Pattern ANNUAL_PATTERN = Pattern.compile("RRMx12\\s*=\\s*R?\\$?([\\d\\.,]+)", Pattern.CASE_INSENSITIVE);
    
    // Regex para achar R$ 2.500,00 ou R$2.500,00
    private static final Pattern MONTHLY_PATTERN = Pattern.compile("R\\$\\s*([\\d\\.,]+)", Pattern.CASE_INSENSITIVE);

    public Double extractRevenue(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        // 1. Prioridade: Tentar encontrar valor anualizado primeiro (ex: RRMx12 = 30.000,00)
        Matcher annualMatcher = ANNUAL_PATTERN.matcher(text);
        if (annualMatcher.find()) {
            return parseMonetaryValue(annualMatcher.group(1));
        }

        // 2. Fallback: Tentar encontrar valor mensal (ex: R$ 2.500,00)
        Matcher monthlyMatcher = MONTHLY_PATTERN.matcher(text);
        if (monthlyMatcher.find()) {
            return parseMonetaryValue(monthlyMatcher.group(1));
        }

        return null;
    }

    private Double parseMonetaryValue(String valueStr) {
        try {
            // Remove pontos de milhares
            String cleanStr = valueStr.replaceAll("\\.", "");
            // Troca virgula decimal por ponto
            cleanStr = cleanStr.replace(",", ".");
            return Double.parseDouble(cleanStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

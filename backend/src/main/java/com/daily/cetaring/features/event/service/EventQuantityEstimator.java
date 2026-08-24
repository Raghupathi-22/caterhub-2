package com.daily.cetaring.features.event.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class EventQuantityEstimator {

    public int quantity(String rule, int guests) {
        int safeGuests = Math.max(guests, 1);
        return switch (rule == null ? "one" : rule) {
            case "guests" -> safeGuests;
            case "half_guests" -> Math.max(1, safeGuests / 2);
            case "servers" -> Math.max(2, (int) Math.ceil(safeGuests / 40.0));
            case "helpers" -> Math.max(2, (int) Math.ceil(safeGuests / 80.0));
            case "cleaners" -> Math.max(2, (int) Math.ceil(safeGuests / 120.0));
            case "chefs" -> Math.max(1, (int) Math.ceil(safeGuests / 150.0));
            case "tables" -> Math.max(4, (int) Math.ceil(safeGuests / 8.0));
            case "cars" -> Math.max(1, (int) Math.ceil(safeGuests / 100.0));
            case "rooms" -> Math.max(1, (int) Math.ceil(safeGuests / 25.0));
            default -> 1;
        };
    }

    public BigDecimal estimate(String budgetRule, int guests, BigDecimal totalBudget) {
        int safeGuests = Math.max(guests, 1);
        BigDecimal budget = totalBudget == null ? BigDecimal.ZERO : totalBudget;
        return switch (budgetRule == null ? "small" : budgetRule) {
            case "venue" -> percentOrMin(budget, new BigDecimal("0.20"), rupees(safeGuests * 250));
            case "catering" -> rupees(safeGuests * 350L);
            case "meal" -> rupees(safeGuests * 180L);
            case "photo" -> percentOrMin(budget, new BigDecimal("0.08"), rupees(25000));
            case "medium" -> percentOrMin(budget, new BigDecimal("0.08"), rupees(15000));
            case "staff" -> rupees(Math.max(1, quantity("servers", safeGuests)) * 800L);
            case "transport" -> rupees(8000);
            case "stay" -> rupees(Math.max(1, quantity("rooms", safeGuests)) * 2500L);
            case "tiny" -> rupees(1500);
            default -> rupees(8000);
        };
    }

    private static BigDecimal percentOrMin(BigDecimal budget, BigDecimal percent, BigDecimal minimum) {
        if (budget.compareTo(BigDecimal.ZERO) <= 0) {
            return minimum;
        }
        BigDecimal share = budget.multiply(percent).setScale(0, RoundingMode.HALF_UP);
        return share.max(minimum);
    }

    private static BigDecimal rupees(long amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }
}

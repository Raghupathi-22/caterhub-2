package com.daily.cetaring.features.event.service;

import com.daily.cetaring.features.event.RequirementUnit;

public record ChecklistItemSpec(
        String category,
        String serviceKey,
        String serviceName,
        RequirementUnit unit,
        boolean required,
        String quantityRule,
        String budgetRule
) {
}

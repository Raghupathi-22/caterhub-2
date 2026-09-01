package com.daily.cetaring.features.catalog;

import lombok.Builder;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    @Value
    @Builder
    public static class CatalogResponse {
        List<ServiceCatalog.CategoryDefinition> categories;
    }

    @GetMapping
    public CatalogResponse getCatalog() {
        return CatalogResponse.builder()
            .categories(ServiceCatalog.categories())
            .build();
    }

    @GetMapping("/categories")
    public List<ServiceCatalog.CategoryDefinition> getCategories() {
        return ServiceCatalog.categories();
    }

    @GetMapping("/categories/{categoryId}")
    public ServiceCatalog.CategoryDefinition getCategory(@PathVariable String categoryId) {
        ServiceCatalog.CategoryDefinition category = ServiceCatalog.categoryById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("Category not found");
        }
        return category;
    }
}

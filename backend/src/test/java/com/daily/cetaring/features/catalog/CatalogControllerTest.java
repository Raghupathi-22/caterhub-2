package com.daily.cetaring.features.catalog;

import com.daily.cetaring.features.admin.repository.CouponRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/** Guards the /catalog JSON contract consumed by the web Services/Categories pages and booking flow. */
class CatalogControllerTest {

    private final CatalogController controller = new CatalogController(mock(CouponRepository.class));

    @Test
    void catalogReturnsAllCodeDefinedCategoriesWithServices() {
        CatalogController.CatalogResponse response = controller.getCatalog();

        assertEquals(ServiceCatalog.categories(), response.getCategories());
        assertFalse(response.getCategories().isEmpty());
        Set<String> ids = new HashSet<>();
        response.getCategories().forEach(category -> {
            assertTrue(ids.add(category.getId()), "duplicate category id " + category.getId());
            assertFalse(category.getServices().isEmpty(), category.getId() + " has no services");
        });
    }

    @Test
    void catalogJsonHasTheShapeTheWebClientValidates() throws Exception {
        JsonNode json = new ObjectMapper().valueToTree(controller.getCatalog());

        JsonNode categories = json.get("categories");
        assertTrue(categories.isArray());
        assertTrue(categories.size() > 0);
        for (JsonNode category : categories) {
            for (String field : new String[]{"id", "name", "description", "serviceType", "icon", "accent"}) {
                assertTrue(category.get(field).isTextual(), field + " must be a string");
            }
            assertTrue(category.get("services").isArray());
            assertTrue(category.get("roles").isArray());
            for (JsonNode role : category.get("roles")) {
                assertTrue(role.get("id").isTextual());
                assertTrue(role.get("title").isTextual());
                assertTrue(role.get("workerType").isTextual());
                assertTrue(role.get("skills").isArray());
            }
        }
    }
}

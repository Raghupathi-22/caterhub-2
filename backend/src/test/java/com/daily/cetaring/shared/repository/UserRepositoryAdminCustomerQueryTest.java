package com.daily.cetaring.shared.repository;

import com.daily.cetaring.features.admin.dto.AdminCustomerDtos;
import com.daily.cetaring.features.admin.service.AdminCustomerService;
import com.daily.cetaring.shared.entity.Role;
import com.daily.cetaring.shared.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Executes the real admin customer JPQL (select + count) through a Spring Data repository proxy
 * against an in-memory H2 database (MySQL mode). Creating the proxy also validates every @Query
 * declared on {@link UserRepository}, so a broken query fails here rather than at startup.
 */
class UserRepositoryAdminCustomerQueryTest {

    private static LocalContainerEntityManagerFactoryBean factoryBean;
    private static EntityManager entityManager;
    private static AdminCustomerService service;

    @BeforeAll
    static void setUp() {
        factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPackagesToScan("com.daily.cetaring");
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factoryBean.setJpaPropertyMap(Map.of(
            "jakarta.persistence.jdbc.url",
            "jdbc:h2:mem:admin_customers;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
            "jakarta.persistence.jdbc.driver", "org.h2.Driver",
            "jakarta.persistence.jdbc.user", "sa",
            "jakarta.persistence.jdbc.password", "",
            "hibernate.hbm2ddl.auto", "create-drop",
            "hibernate.physical_naming_strategy",
            "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
        ));
        factoryBean.afterPropertiesSet();
        entityManager = factoryBean.getObject().createEntityManager();
        UserRepository repository = new JpaRepositoryFactory(entityManager).getRepository(UserRepository.class);
        service = new AdminCustomerService(repository);
        seed();
    }

    @AfterAll
    static void tearDown() {
        if (entityManager != null) entityManager.close();
        if (factoryBean != null) factoryBean.destroy();
    }

    private static void seed() {
        entityManager.getTransaction().begin();
        Role customer = role("ROLE_CUSTOMER");
        Role worker = role("ROLE_WORKER");
        Role admin = role("ROLE_ADMIN");
        Role superAdmin = role("ROLE_SUPER_ADMIN");

        user("asha", "+919800000001", "Asha", "Rao", null, customer);
        user("ravi", "+919800000002", "Ravi", "Kumar", null, customer);
        user("noname", "+919800000003", null, null, null, customer);
        // Worker that registered as a customer first: must be excluded.
        user("wcust", "+919800000004", "Worker", "AlsoCustomer", null, customer, worker);
        user("worker", "+919800000005", "Pure", "Worker", null, worker);
        user("admin", "+919800000006", "Site", "Admin", null, admin);
        user("sadmin", "+919800000007", "Super", "Admin", null, superAdmin, customer);
        // Soft-deleted customer: must be excluded.
        user("deleted", "+919800000008", "Gone", "Customer", LocalDateTime.now(), customer);
        entityManager.getTransaction().commit();
        entityManager.clear();
    }

    private static Role role(String name) {
        Role role = Role.builder().name(name).description(name).build();
        entityManager.persist(role);
        return role;
    }

    private static void user(String username, String phone, String first, String last, LocalDateTime deletedAt, Role... roles) {
        User user = User.builder()
            .username(username).phoneNumber(phone).firstName(first).lastName(last)
            .isActive(true).isVerified(false).deletedAt(deletedAt)
            .roles(new HashSet<>(List.of(roles)))
            .build();
        entityManager.persist(user);
    }

    private static Set<String> phones(AdminCustomerDtos.CustomerPage page) {
        return Set.copyOf(page.getContent().stream().map(AdminCustomerDtos.CustomerSummary::getPhoneNumber).toList());
    }

    @Test
    void listsOnlyRealCustomersOnceExcludingWorkersAdminsAndDeletedAccounts() {
        AdminCustomerDtos.CustomerPage page = service.listCustomers(0, 50, null);

        assertEquals(Set.of("+919800000001", "+919800000002", "+919800000003"), phones(page));
        assertEquals(3, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertTrue(page.getContent().stream().anyMatch(c -> "Asha Rao".equals(c.getFullName())));
    }

    @Test
    void searchesByNameCaseInsensitively() {
        assertEquals(Set.of("+919800000002"), phones(service.listCustomers(0, 20, "ravi KU")));
        // Workers/admins never match even when the search targets them.
        assertTrue(service.listCustomers(0, 20, "Worker").getContent().isEmpty());
        assertTrue(service.listCustomers(0, 20, "Admin").getContent().isEmpty());
    }

    @Test
    void searchesByMobileDigitsIgnoringFormatting() {
        assertEquals(Set.of("+919800000001"), phones(service.listCustomers(0, 20, "98000 00001")));
        assertTrue(service.listCustomers(0, 20, "9800000004").getContent().isEmpty());
    }

    @Test
    void paginatesWithCorrectTotals() {
        AdminCustomerDtos.CustomerPage first = service.listCustomers(0, 2, null);
        AdminCustomerDtos.CustomerPage second = service.listCustomers(1, 2, null);

        assertEquals(2, first.getContent().size());
        assertEquals(1, second.getContent().size());
        assertEquals(3, first.getTotalElements());
        assertEquals(2, first.getTotalPages());
    }

    @Test
    void emptySearchResultReturnsEmptyPage() {
        AdminCustomerDtos.CustomerPage page = service.listCustomers(0, 20, "nobody-matches-this");
        assertTrue(page.getContent().isEmpty());
        assertEquals(0, page.getTotalElements());
    }
}

package com.daily.cetaring.features.rice.repository;

import com.daily.cetaring.features.rice.entity.RiceOrder;
import com.daily.cetaring.features.rice.entity.RiceProduct;
import com.daily.cetaring.features.rice.entity.RiceSampleRequest;
import com.daily.cetaring.shared.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Runs the rice admin search JPQL and the idempotency unique constraint against H2 (MySQL mode). */
class RiceRepositoryQueryTest {
    private static LocalContainerEntityManagerFactoryBean factoryBean;
    private static EntityManager entityManager;
    private static RiceOrderRepository orders;
    private static RiceSampleRequestRepository samples;
    private static User asha;
    private static RiceProduct product;

    @BeforeAll
    static void setUp() {
        factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPackagesToScan("com.daily.cetaring");
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factoryBean.setJpaPropertyMap(Map.of(
            "jakarta.persistence.jdbc.url",
            "jdbc:h2:mem:rice_orders;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
            "jakarta.persistence.jdbc.driver", "org.h2.Driver",
            "jakarta.persistence.jdbc.user", "sa",
            "jakarta.persistence.jdbc.password", "",
            "hibernate.hbm2ddl.auto", "create-drop",
            "hibernate.physical_naming_strategy",
            "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
        ));
        factoryBean.afterPropertiesSet();
        entityManager = factoryBean.getObject().createEntityManager();
        JpaRepositoryFactory factory = new JpaRepositoryFactory(entityManager);
        orders = factory.getRepository(RiceOrderRepository.class);
        samples = factory.getRepository(RiceSampleRequestRepository.class);
        seed();
    }

    @AfterAll
    static void tearDown() {
        if (entityManager != null) entityManager.close();
        if (factoryBean != null) factoryBean.destroy();
    }

    private static void seed() {
        entityManager.getTransaction().begin();
        asha = User.builder().username("asha").phoneNumber("+919800000001").isActive(true).isVerified(true).build();
        User ravi = User.builder().username("ravi").phoneNumber("+919800000002").isActive(true).isVerified(true).build();
        entityManager.persist(asha);
        entityManager.persist(ravi);
        product = RiceProduct.builder().code(RiceProduct.VILLAGE_FARM_RICE).name("Village Farm Rice").active(true)
            .basePackKg(50).basePackPrice(new BigDecimal("5299.00")).minOrderKg(50).orderStepKg(1).maxOrderKg(5000)
            .sampleEnabled(true).sampleWeightGrams(500).build();
        entityManager.persist(product);
        entityManager.persist(order(asha, "RO-1", "Asha Rao", "+919800000001", "key-asha-1", RiceOrder.Status.PENDING, RiceOrder.PaymentStatus.PENDING));
        entityManager.persist(order(ravi, "RO-2", "Ravi Kumar", "+919800000002", "key-ravi-1", RiceOrder.Status.CONFIRMED, RiceOrder.PaymentStatus.PAID));
        entityManager.persist(RiceSampleRequest.builder().customer(asha).product(product).requestNumber("RS-1")
            .customerName("Asha Rao").mobileNumber("+919800000001").sampleWeightGrams(500)
            .price(new BigDecimal("25")).fulfillmentType("DELIVERY")
            .idempotencyKey("sample-asha-1").build());
        entityManager.getTransaction().commit();
        entityManager.clear();
    }

    private static RiceOrder order(User user, String number, String name, String mobile, String key,
                                   RiceOrder.Status status, RiceOrder.PaymentStatus payment) {
        return RiceOrder.builder().customer(user).product(product).orderNumber(number)
            .customerName(name).mobileNumber(mobile).quantityKg(50).basePackKg(50)
            .basePackPrice(new BigDecimal("5299.00")).subtotal(new BigDecimal("5299.00")).totalAmount(new BigDecimal("5299.00"))
            .deliveryAddress("1 Street").area("Anna Nagar").pincode("600040")
            .status(status).paymentStatus(payment).idempotencyKey(key).build();
    }

    @Test
    void filtersOrdersByStatusPaymentAndSearch() {
        assertEquals(2, orders.search(null, null, null, null, null).size());
        assertEquals(List.of("RO-2"), orders.search(RiceOrder.Status.CONFIRMED, null, null, null, null).stream().map(RiceOrder::getOrderNumber).toList());
        assertEquals(List.of("RO-1"), orders.search(null, RiceOrder.PaymentStatus.PENDING, null, null, null).stream().map(RiceOrder::getOrderNumber).toList());
        assertEquals(List.of("RO-1"), orders.search(null, null, null, null, "%asha%").stream().map(RiceOrder::getOrderNumber).toList());
        assertEquals(List.of("RO-2"), orders.search(null, null, null, null, "%9800000002%").stream().map(RiceOrder::getOrderNumber).toList());
        assertTrue(orders.search(null, null, LocalDateTime.now().plusDays(1), null, null).isEmpty());
        assertEquals(2, orders.search(null, null, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), null).size());
    }

    @Test
    void customerHistoryIsScopedToOwner() {
        assertEquals(1, orders.findByCustomerIdOrderByCreatedAtDesc(asha.getId()).size());
        assertTrue(orders.findByCustomerIdAndIdempotencyKey(asha.getId(), "key-asha-1").isPresent());
        assertTrue(orders.findByCustomerIdAndIdempotencyKey(asha.getId(), "key-ravi-1").isEmpty());
    }

    @Test
    void sampleSearchAndStoredValuesAreFreePickup() {
        List<RiceSampleRequest> found = samples.search(RiceSampleRequest.Status.REQUESTED, null, null, "%asha%");
        assertEquals(1, found.size());
        assertEquals(0, BigDecimal.ZERO.compareTo(found.get(0).getPrice()));
        assertEquals("PICKUP", found.get(0).getFulfillmentType());
        assertTrue(samples.search(RiceSampleRequest.Status.COLLECTED, null, null, null).isEmpty());
    }

    @Test
    void duplicateIdempotencyKeyIsRejectedByDatabase() {
        entityManager.getTransaction().begin();
        try {
            User owner = entityManager.find(User.class, asha.getId());
            RiceProduct p = entityManager.find(RiceProduct.class, product.getId());
            RiceOrder dup = order(owner, "RO-3", "Asha Rao", "+919800000001", "key-asha-1", RiceOrder.Status.PENDING, RiceOrder.PaymentStatus.PENDING);
            dup.setProduct(p);
            // IDENTITY ids insert immediately on persist, so the violation can surface on persist or flush.
            assertThrows(PersistenceException.class, () -> {
                entityManager.persist(dup);
                entityManager.flush();
            });
        } finally {
            entityManager.getTransaction().rollback();
            entityManager.clear();
        }
    }
}

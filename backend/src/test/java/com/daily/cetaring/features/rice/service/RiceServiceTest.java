package com.daily.cetaring.features.rice.service;

import com.daily.cetaring.features.rice.dto.RiceDtos;
import com.daily.cetaring.features.rice.entity.RiceOrder;
import com.daily.cetaring.features.rice.entity.RiceProduct;
import com.daily.cetaring.features.rice.entity.RiceSampleRequest;
import com.daily.cetaring.features.rice.repository.RiceOrderRepository;
import com.daily.cetaring.features.rice.repository.RiceProductRepository;
import com.daily.cetaring.features.rice.repository.RiceSampleRequestRepository;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RiceServiceTest {
    private RiceOrderRepository orderRepository;
    private RiceSampleRequestRepository sampleRepository;
    private RiceService service;
    private RiceProduct product;
    private final User user = User.builder().id(10L).username("cust").phoneNumber("+919800000001").build();

    @BeforeEach
    void setUp() {
        RiceProductRepository productRepository = mock(RiceProductRepository.class);
        orderRepository = mock(RiceOrderRepository.class);
        sampleRepository = mock(RiceSampleRequestRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        service = new RiceService(productRepository, orderRepository, sampleRepository, userRepository,
            mock(PlatformTransactionManager.class));
        product = RicePricingTest.product();
        product.setSamplePickupInstructions("Bring your request number.");
        when(productRepository.findByCode(RiceProduct.VILLAGE_FARM_RICE)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("cust")).thenReturn(Optional.of(user));
        when(orderRepository.saveAndFlush(any())).thenAnswer(inv -> {
            RiceOrder o = inv.getArgument(0);
            o.setId(7L);
            return o;
        });
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sampleRepository.saveAndFlush(any())).thenAnswer(inv -> {
            RiceSampleRequest s = inv.getArgument(0);
            s.setId(3L);
            return s;
        });
        when(sampleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private RiceDtos.CreateOrderRequest order(int qty, String expectedTotal) {
        return RiceDtos.CreateOrderRequest.builder()
            .customerName(" Asha ").mobileNumber("98000 00001").quantityKg(qty)
            .deliveryAddress("12 Main Road").area("Anna Nagar").pincode("600040")
            .preferredDeliverySlot("morning")
            .expectedTotalAmount(expectedTotal == null ? null : new BigDecimal(expectedTotal))
            .idempotencyKey("order-key-0001")
            .build();
    }

    @Test
    void createsOrderWithServerCalculatedPrice() {
        RiceDtos.OrderResponse response = service.createOrder("cust", order(75, "7948.50"));

        ArgumentCaptor<RiceOrder> saved = ArgumentCaptor.forClass(RiceOrder.class);
        verify(orderRepository).saveAndFlush(saved.capture());
        assertEquals(new BigDecimal("7948.50"), saved.getValue().getTotalAmount());
        assertEquals("+919800000001", saved.getValue().getMobileNumber());
        assertEquals("Asha", saved.getValue().getCustomerName());
        assertEquals("MORNING", saved.getValue().getPreferredDeliverySlot());
        assertEquals(RiceOrder.PaymentStatus.PENDING, saved.getValue().getPaymentStatus());
        assertTrue(response.orderNumber.startsWith("RO-"));
        assertNull(response.adminNotes);
    }

    @Test
    void rejectsQuantityBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> service.createOrder("cust", order(49, null)));
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsUnknownDeliverySlot() {
        RiceDtos.CreateOrderRequest r = order(50, null);
        r.preferredDeliverySlot = "midnight";
        assertThrows(IllegalArgumentException.class, () -> service.createOrder("cust", r));
    }

    @Test
    void rejectsWhenDisplayedTotalNoLongerMatchesServerPrice() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> service.createOrder("cust", order(50, "4999.00")));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void duplicateSubmissionWithSameKeyReturnsExistingOrder() {
        RiceOrder existing = RiceOrder.builder().id(5L).orderNumber("RO-1").product(product).customer(user)
            .quantityKg(50).totalAmount(new BigDecimal("5299.00")).status(RiceOrder.Status.PENDING).build();
        when(orderRepository.findByCustomerIdAndIdempotencyKey(10L, "order-key-0001")).thenReturn(Optional.of(existing));

        assertEquals(5L, service.createOrder("cust", order(50, null)).id);
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void concurrentDuplicateInsertFallsBackToExistingOrder() {
        RiceOrder existing = RiceOrder.builder().id(9L).orderNumber("RO-9").product(product).customer(user).build();
        when(orderRepository.findByCustomerIdAndIdempotencyKey(10L, "order-key-0001"))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existing));
        doThrow(new DataIntegrityViolationException("duplicate")).when(orderRepository).saveAndFlush(any());

        assertEquals(9L, service.createOrder("cust", order(50, null)).id);
    }

    @Test
    void inactiveProductCannotBeOrdered() {
        product.setActive(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createOrder("cust", order(50, null)));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertThrows(ResponseStatusException.class, () -> service.publicProduct());
    }

    private RiceDtos.CreateSampleRequest sample() {
        return RiceDtos.CreateSampleRequest.builder()
            .customerName("Asha").mobileNumber("9800000001").idempotencyKey("sample-key-0001").build();
    }

    @Test
    void sampleIsAlwaysFreeAndPickupOnly() {
        when(sampleRepository.findFirstByCustomerIdAndStatusInOrderByCreatedAtDesc(anyLong(), anyCollection())).thenReturn(Optional.empty());
        when(sampleRepository.findByCustomerIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());

        RiceDtos.SampleResponse response = service.requestSample("cust", sample());

        ArgumentCaptor<RiceSampleRequest> saved = ArgumentCaptor.forClass(RiceSampleRequest.class);
        verify(sampleRepository).saveAndFlush(saved.capture());
        assertEquals(0, BigDecimal.ZERO.compareTo(saved.getValue().getPrice()));
        assertEquals("PICKUP", saved.getValue().getFulfillmentType());
        assertEquals(500, saved.getValue().getSampleWeightGrams());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.price));
        assertEquals("PICKUP", response.fulfillmentType);
        assertEquals(RiceSampleRequest.Status.REQUESTED, response.status);
        assertEquals("Bring your request number.", response.pickupInstructions);
        assertTrue(response.requestNumber.startsWith("RS-"));
    }

    @Test
    void productAlwaysAdvertisesFreePickupSample() {
        RiceDtos.ProductResponse p = service.publicProduct();
        assertEquals(0, BigDecimal.ZERO.compareTo(p.samplePrice));
        assertEquals("PICKUP", p.sampleFulfillmentType);
        assertEquals(new BigDecimal("5299.00"), p.basePackPrice);
        assertEquals(50, p.minOrderKg);
    }

    @Test
    void openSampleRequestIsReturnedInsteadOfCreatingDuplicate() {
        RiceSampleRequest open = RiceSampleRequest.builder().id(2L).requestNumber("RS-2").product(product).customer(user)
            .status(RiceSampleRequest.Status.REQUESTED).build();
        when(sampleRepository.findFirstByCustomerIdAndStatusInOrderByCreatedAtDesc(eq(10L), anyCollection())).thenReturn(Optional.of(open));

        assertEquals(2L, service.requestSample("cust", sample()).id);
        verify(sampleRepository, never()).saveAndFlush(any());
    }

    @Test
    void customerWhoCollectedSampleCannotRequestAnother() {
        when(sampleRepository.findFirstByCustomerIdAndStatusInOrderByCreatedAtDesc(anyLong(), anyCollection())).thenReturn(Optional.empty());
        when(sampleRepository.findByCustomerIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(
            RiceSampleRequest.builder().id(1L).status(RiceSampleRequest.Status.COLLECTED).build()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.requestSample("cust", sample()));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void disabledSamplesAreRejected() {
        product.setSampleEnabled(false);
        when(sampleRepository.findFirstByCustomerIdAndStatusInOrderByCreatedAtDesc(anyLong(), anyCollection())).thenReturn(Optional.empty());
        when(sampleRepository.findByCustomerIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());

        assertThrows(ResponseStatusException.class, () -> service.requestSample("cust", sample()));
        verify(sampleRepository, never()).saveAndFlush(any());
    }

    @Test
    void schedulingPickupRequiresLocationAndTime() {
        RiceSampleRequest s = RiceSampleRequest.builder().id(4L).status(RiceSampleRequest.Status.REQUESTED).build();
        when(sampleRepository.findById(4L)).thenReturn(Optional.of(s));

        assertThrows(IllegalArgumentException.class, () -> service.updateSample(4L,
            RiceDtos.UpdateSampleRequest.builder().status(RiceSampleRequest.Status.PICKUP_SCHEDULED).pickupLocation("Farm office").build()));

        LocalDateTime time = LocalDateTime.of(2030, 1, 5, 10, 30);
        RiceDtos.SampleResponse updated = service.updateSample(4L, RiceDtos.UpdateSampleRequest.builder()
            .status(RiceSampleRequest.Status.PICKUP_SCHEDULED).pickupLocation("Farm office").pickupTime(time).build());
        assertEquals(RiceSampleRequest.Status.PICKUP_SCHEDULED, updated.status);
        assertEquals(time, updated.pickupTime);
        assertEquals("Farm office", updated.pickupLocation);
    }

    @Test
    void collectedSampleCannotBeChanged() {
        when(sampleRepository.findById(4L)).thenReturn(Optional.of(
            RiceSampleRequest.builder().id(4L).status(RiceSampleRequest.Status.COLLECTED).build()));
        assertThrows(IllegalArgumentException.class, () -> service.updateSample(4L,
            RiceDtos.UpdateSampleRequest.builder().status(RiceSampleRequest.Status.CANCELLED).build()));
    }

    @Test
    void orderStatusTransitions() {
        assertDoesNotThrow(() -> RiceService.ensureOrderTransition(RiceOrder.Status.PENDING, RiceOrder.Status.CONFIRMED));
        assertDoesNotThrow(() -> RiceService.ensureOrderTransition(RiceOrder.Status.PROCESSING, RiceOrder.Status.CANCELLED));
        assertThrows(IllegalArgumentException.class, () -> RiceService.ensureOrderTransition(RiceOrder.Status.DELIVERED, RiceOrder.Status.CANCELLED));
        assertThrows(IllegalArgumentException.class, () -> RiceService.ensureOrderTransition(RiceOrder.Status.CANCELLED, RiceOrder.Status.PENDING));
        assertThrows(IllegalArgumentException.class, () -> RiceService.ensureOrderTransition(RiceOrder.Status.OUT_FOR_DELIVERY, RiceOrder.Status.CONFIRMED));
    }

    @Test
    void adminUpdatesOrderStatusAndPayment() {
        RiceOrder o = RiceOrder.builder().id(7L).status(RiceOrder.Status.PENDING).paymentStatus(RiceOrder.PaymentStatus.PENDING).build();
        when(orderRepository.findById(7L)).thenReturn(Optional.of(o));

        assertThrows(IllegalArgumentException.class, () -> service.updateOrder(7L, RiceDtos.UpdateOrderStatusRequest.builder()
            .paymentStatus(RiceOrder.PaymentStatus.REFUNDED).build()));

        RiceDtos.OrderResponse updated = service.updateOrder(7L, RiceDtos.UpdateOrderStatusRequest.builder()
            .status(RiceOrder.Status.CONFIRMED).paymentStatus(RiceOrder.PaymentStatus.PAID).adminNotes("Called customer").build());
        assertEquals(RiceOrder.Status.CONFIRMED, updated.status);
        assertEquals(RiceOrder.PaymentStatus.PAID, updated.paymentStatus);
        assertEquals("Called customer", updated.adminNotes);
    }

    @Test
    void productUpdateRejectsMinAboveMax() {
        RiceDtos.UpdateProductRequest r = RiceDtos.UpdateProductRequest.builder()
            .name("Rice").active(true).basePackKg(50).basePackPrice(new BigDecimal("5299"))
            .minOrderKg(100).orderStepKg(1).maxOrderKg(50).sampleEnabled(true).sampleWeightGrams(500).build();
        assertThrows(IllegalArgumentException.class, () -> service.updateProduct(r));
    }

    @Test
    void likePatternStripsWildcards() {
        assertNull(RiceService.likePattern("  "));
        assertNull(RiceService.likePattern("%_"));
        assertEquals("%asha%", RiceService.likePattern(" ASHA "));
    }
}

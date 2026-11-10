package com.daily.cetaring.features.rice.service;

import com.daily.cetaring.features.auth.service.MobileNumberNormalizer;
import com.daily.cetaring.features.rice.dto.RiceDtos;
import com.daily.cetaring.features.rice.entity.RiceOrder;
import com.daily.cetaring.features.rice.entity.RiceProduct;
import com.daily.cetaring.features.rice.entity.RiceSampleRequest;
import com.daily.cetaring.features.rice.repository.RiceOrderRepository;
import com.daily.cetaring.features.rice.repository.RiceProductRepository;
import com.daily.cetaring.features.rice.repository.RiceSampleRequestRepository;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RiceService {
    public static final Set<String> DELIVERY_SLOTS = Set.of("MORNING", "AFTERNOON", "EVENING", "ANYTIME");
    private static final Set<RiceSampleRequest.Status> OPEN_SAMPLE_STATUSES =
        EnumSet.of(RiceSampleRequest.Status.REQUESTED, RiceSampleRequest.Status.PICKUP_SCHEDULED);
    private static final DateTimeFormatter NUMBER_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final RiceProductRepository productRepository;
    private final RiceOrderRepository orderRepository;
    private final RiceSampleRequestRepository sampleRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate tx;

    public RiceService(
        RiceProductRepository productRepository,
        RiceOrderRepository orderRepository,
        RiceSampleRequestRepository sampleRepository,
        UserRepository userRepository,
        PlatformTransactionManager transactionManager
    ) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
        this.userRepository = userRepository;
        this.tx = new TransactionTemplate(transactionManager);
    }

    // ---------------------------------------------------------------- product

    @Transactional(readOnly = true)
    public RiceDtos.ProductResponse publicProduct() {
        RiceProduct product = loadProduct();
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Village Farm Rice is currently unavailable.");
        }
        return mapProduct(product);
    }

    @Transactional(readOnly = true)
    public RiceDtos.ProductResponse adminProduct() {
        return mapProduct(loadProduct());
    }

    @Transactional
    public RiceDtos.ProductResponse updateProduct(RiceDtos.UpdateProductRequest r) {
        if (r.minOrderKg > r.maxOrderKg) {
            throw new IllegalArgumentException("Minimum order quantity cannot exceed the maximum order quantity.");
        }
        RiceProduct product = loadProduct();
        product.setName(r.name.trim());
        product.setTagline(trimToNull(r.tagline));
        product.setDescription(trimToNull(r.description));
        product.setHighlights(r.highlights == null ? null : trimToNull(
            r.highlights.stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.joining("\n"))));
        product.setImageUrl(trimToNull(r.imageUrl));
        product.setActive(r.active);
        product.setBasePackKg(r.basePackKg);
        product.setBasePackPrice(r.basePackPrice);
        product.setMinOrderKg(r.minOrderKg);
        product.setOrderStepKg(r.orderStepKg);
        product.setMaxOrderKg(r.maxOrderKg);
        product.setDeliveryCharge(r.deliveryCharge);
        product.setPaymentNote(trimToNull(r.paymentNote));
        product.setSampleEnabled(r.sampleEnabled);
        product.setSampleWeightGrams(r.sampleWeightGrams);
        product.setSamplePickupLocation(trimToNull(r.samplePickupLocation));
        product.setSamplePickupInstructions(trimToNull(r.samplePickupInstructions));
        return mapProduct(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public RiceDtos.QuoteResponse quote(Integer quantityKg) {
        RiceProduct product = loadActiveProduct();
        RicePricing.validateQuantity(product, quantityKg);
        return mapQuote(product, RicePricing.quote(product, quantityKg));
    }

    // ---------------------------------------------------------------- orders

    public RiceDtos.OrderResponse createOrder(String username, RiceDtos.CreateOrderRequest r) {
        String key = r.idempotencyKey.trim();
        try {
            return tx.execute(status -> {
                User user = loadUser(username);
                var existing = orderRepository.findByCustomerIdAndIdempotencyKey(user.getId(), key);
                if (existing.isPresent()) {
                    return mapOrder(existing.get(), false);
                }
                RiceProduct product = loadActiveProduct();
                RicePricing.Quote quote = RicePricing.quote(product, r.quantityKg);
                if (r.expectedTotalAmount != null && r.expectedTotalAmount.compareTo(quote.total()) != 0) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The rice price has changed. Please review the updated total of â‚¹" + quote.total().toPlainString() + " and confirm again.");
                }
                String slot = normalizeSlot(r.preferredDeliverySlot);
                if (r.preferredDeliveryDate != null && r.preferredDeliveryDate.isAfter(LocalDate.now().plusDays(90))) {
                    throw new IllegalArgumentException("Preferred delivery date must be within the next 90 days.");
                }
                RiceOrder order = orderRepository.saveAndFlush(RiceOrder.builder()
                    .customer(user)
                    .product(product)
                    .customerName(r.customerName.trim())
                    .mobileNumber(MobileNumberNormalizer.normalize(r.mobileNumber))
                    .quantityKg(quote.quantityKg())
                    .basePackKg(product.getBasePackKg())
                    .basePackPrice(product.getBasePackPrice())
                    .subtotal(quote.subtotal())
                    .deliveryCharge(quote.deliveryCharge())
                    .totalAmount(quote.total())
                    .deliveryAddress(r.deliveryAddress.trim())
                    .area(r.area.trim())
                    .pincode(r.pincode.trim())
                    .preferredDeliveryDate(r.preferredDeliveryDate)
                    .preferredDeliverySlot(slot)
                    .deliveryNotes(trimToNull(r.deliveryNotes))
                    .status(RiceOrder.Status.PENDING)
                    .paymentStatus(RiceOrder.PaymentStatus.PENDING)
                    .idempotencyKey(key)
                    .build());
                order.setOrderNumber(number("RO", order.getId()));
                return mapOrder(orderRepository.save(order), false);
            });
        } catch (DataIntegrityViolationException race) {
            // A concurrent request with the same idempotency key won the insert; return that order.
            return tx.execute(status -> {
                User user = loadUser(username);
                return orderRepository.findByCustomerIdAndIdempotencyKey(user.getId(), key)
                    .map(order -> mapOrder(order, false))
                    .orElseThrow(() -> race);
            });
        }
    }

    @Transactional(readOnly = true)
    public List<RiceDtos.OrderResponse> myOrders(String username) {
        User user = loadUser(username);
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(order -> mapOrder(order, false)).toList();
    }

    @Transactional(readOnly = true)
    public RiceDtos.OrderResponse myOrder(String username, Long id) {
        User user = loadUser(username);
        return orderRepository.findByIdAndCustomerId(id, user.getId())
            .map(order -> mapOrder(order, false))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rice order not found."));
    }

    @Transactional(readOnly = true)
    public List<RiceDtos.OrderResponse> adminOrders(
        RiceOrder.Status status, RiceOrder.PaymentStatus paymentStatus, LocalDate from, LocalDate to, String search) {
        return orderRepository.search(status, paymentStatus, startOf(from), endOf(to), likePattern(search)).stream()
            .map(order -> mapOrder(order, true)).toList();
    }

    @Transactional
    public RiceDtos.OrderResponse updateOrder(Long id, RiceDtos.UpdateOrderStatusRequest r) {
        if (r.status == null && r.paymentStatus == null && r.adminNotes == null) {
            throw new IllegalArgumentException("Nothing to update.");
        }
        RiceOrder order = orderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rice order not found."));
        if (r.status != null && r.status != order.getStatus()) {
            ensureOrderTransition(order.getStatus(), r.status);
            order.setStatus(r.status);
        }
        if (r.paymentStatus != null && r.paymentStatus != order.getPaymentStatus()) {
            if (r.paymentStatus == RiceOrder.PaymentStatus.REFUNDED && order.getPaymentStatus() != RiceOrder.PaymentStatus.PAID) {
                throw new IllegalArgumentException("Only paid orders can be marked as refunded.");
            }
            order.setPaymentStatus(r.paymentStatus);
        }
        if (r.adminNotes != null) {
            order.setAdminNotes(trimToNull(r.adminNotes));
        }
        return mapOrder(orderRepository.save(order), true);
    }

    static void ensureOrderTransition(RiceOrder.Status from, RiceOrder.Status to) {
        if (from == RiceOrder.Status.DELIVERED || from == RiceOrder.Status.CANCELLED) {
            throw new IllegalArgumentException("This order is already " + from.name().toLowerCase(Locale.ROOT).replace('_', ' ') + " and cannot be changed.");
        }
        if (to != RiceOrder.Status.CANCELLED && to.ordinal() < from.ordinal()) {
            throw new IllegalArgumentException("Order status cannot move back from " + from + " to " + to + ".");
        }
    }

    // ---------------------------------------------------------------- samples

    public RiceDtos.SampleResponse requestSample(String username, RiceDtos.CreateSampleRequest r) {
        String key = r.idempotencyKey.trim();
        try {
            return tx.execute(status -> {
                User user = loadUser(username);
                var sameKey = sampleRepository.findByCustomerIdAndIdempotencyKey(user.getId(), key);
                if (sameKey.isPresent()) {
                    return mapSample(sameKey.get(), false);
                }
                var open = sampleRepository.findFirstByCustomerIdAndStatusInOrderByCreatedAtDesc(user.getId(), OPEN_SAMPLE_STATUSES);
                if (open.isPresent()) {
                    return mapSample(open.get(), false);
                }
                boolean alreadyCollected = sampleRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId()).stream()
                    .anyMatch(s -> s.getStatus() == RiceSampleRequest.Status.COLLECTED);
                if (alreadyCollected) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "You have already collected your free rice sample. You can place a bulk order any time.");
                }
                RiceProduct product = loadActiveProduct();
                if (!Boolean.TRUE.equals(product.getSampleEnabled())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Free rice samples are not available right now.");
                }
                RiceSampleRequest sample = sampleRepository.saveAndFlush(RiceSampleRequest.builder()
                    .customer(user)
                    .product(product)
                    .customerName(r.customerName.trim())
                    .mobileNumber(MobileNumberNormalizer.normalize(r.mobileNumber))
                    .sampleWeightGrams(product.getSampleWeightGrams())
                    .price(BigDecimal.ZERO)
                    .fulfillmentType(RiceSampleRequest.FULFILLMENT_PICKUP)
                    .status(RiceSampleRequest.Status.REQUESTED)
                    .pickupInstructions(product.getSamplePickupInstructions())
                    .idempotencyKey(key)
                    .build());
                sample.setRequestNumber(number("RS", sample.getId()));
                return mapSample(sampleRepository.save(sample), false);
            });
        } catch (DataIntegrityViolationException race) {
            return tx.execute(status -> {
                User user = loadUser(username);
                return sampleRepository.findByCustomerIdAndIdempotencyKey(user.getId(), key)
                    .map(s -> mapSample(s, false))
                    .orElseThrow(() -> race);
            });
        }
    }

    @Transactional(readOnly = true)
    public List<RiceDtos.SampleResponse> mySamples(String username) {
        User user = loadUser(username);
        return sampleRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(s -> mapSample(s, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<RiceDtos.SampleResponse> adminSamples(RiceSampleRequest.Status status, LocalDate from, LocalDate to, String search) {
        return sampleRepository.search(status, startOf(from), endOf(to), likePattern(search)).stream()
                    .map(s -> mapSample(s, true)).toList();
    }

    @Transactional
    public RiceDtos.SampleResponse updateSample(Long id, RiceDtos.UpdateSampleRequest r) {
        RiceSampleRequest sample = sampleRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sample request not found."));
        RiceSampleRequest.Status current = sample.getStatus();
        if (current == RiceSampleRequest.Status.COLLECTED || current == RiceSampleRequest.Status.CANCELLED) {
            throw new IllegalArgumentException("This sample request is already " + current.name().toLowerCase(Locale.ROOT) + " and cannot be changed.");
        }
        if (r.pickupLocation != null) sample.setPickupLocation(trimToNull(r.pickupLocation));
        if (r.pickupTime != null) sample.setPickupTime(r.pickupTime);
        if (r.pickupInstructions != null) sample.setPickupInstructions(trimToNull(r.pickupInstructions));
        if (r.adminNotes != null) sample.setAdminNotes(trimToNull(r.adminNotes));
        if (r.status != null && r.status != current) {
            if (r.status == RiceSampleRequest.Status.REQUESTED) {
                throw new IllegalArgumentException("A scheduled pickup cannot be moved back to requested.");
            }
            sample.setStatus(r.status);
        }
        if (sample.getStatus() == RiceSampleRequest.Status.PICKUP_SCHEDULED
            && (sample.getPickupLocation() == null || sample.getPickupTime() == null)) {
            throw new IllegalArgumentException("Pickup location and collection time are required to schedule a sample pickup.");
        }
        return mapSample(sampleRepository.save(sample), true);
    }

    // ---------------------------------------------------------------- helpers

    private RiceProduct loadProduct() {
        return productRepository.findByCode(RiceProduct.VILLAGE_FARM_RICE)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Village Farm Rice is not configured."));
    }

    private RiceProduct loadActiveProduct() {
        RiceProduct product = loadProduct();
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Village Farm Rice is currently unavailable.");
        }
        return product;
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    private static String number(String prefix, Long id) {
        return prefix + "-" + LocalDate.now().format(NUMBER_DATE) + "-" + String.format("%05d", id);
    }

    private static String normalizeSlot(String slot) {
        String value = trimToNull(slot);
        if (value == null) return null;
        String normalized = value.toUpperCase(Locale.ROOT);
        if (!DELIVERY_SLOTS.contains(normalized)) {
            throw new IllegalArgumentException("Preferred delivery slot must be one of Morning, Afternoon, Evening or Anytime.");
        }
        return normalized;
    }

    private static LocalDateTime startOf(LocalDate date) { return date == null ? null : date.atStartOfDay(); }

    private static LocalDateTime endOf(LocalDate date) { return date == null ? null : date.plusDays(1).atStartOfDay(); }

    static String likePattern(String search) {
        String value = trimToNull(search);
        if (value == null) return null;
        String escaped = value.toLowerCase(Locale.ROOT).replace("%", "").replace("_", "");
        return escaped.isEmpty() ? null : "%" + escaped + "%";
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private RiceDtos.ProductResponse mapProduct(RiceProduct p) {
        List<String> highlights = p.getHighlights() == null ? List.of()
            : Arrays.stream(p.getHighlights().split("\\R")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return RiceDtos.ProductResponse.builder()
            .id(p.getId()).code(p.getCode()).name(p.getName()).tagline(p.getTagline())
            .description(p.getDescription()).highlights(highlights).imageUrl(p.getImageUrl())
            .active(p.getActive())
            .basePackKg(p.getBasePackKg()).basePackPrice(p.getBasePackPrice())
            .minOrderKg(p.getMinOrderKg()).orderStepKg(p.getOrderStepKg()).maxOrderKg(p.getMaxOrderKg())
            .deliveryCharge(p.getDeliveryCharge()).paymentNote(p.getPaymentNote())
            .sampleEnabled(p.getSampleEnabled()).sampleWeightGrams(p.getSampleWeightGrams())
            .samplePrice(BigDecimal.ZERO.setScale(2))
            .sampleFulfillmentType(RiceSampleRequest.FULFILLMENT_PICKUP)
            .samplePickupLocation(p.getSamplePickupLocation())
            .samplePickupInstructions(p.getSamplePickupInstructions())
            .updatedAt(p.getUpdatedAt())
            .build();
    }

    private RiceDtos.QuoteResponse mapQuote(RiceProduct p, RicePricing.Quote q) {
        return RiceDtos.QuoteResponse.builder()
            .quantityKg(q.quantityKg()).basePackKg(p.getBasePackKg()).basePackPrice(p.getBasePackPrice())
            .subtotal(q.subtotal()).deliveryCharge(q.deliveryCharge()).totalAmount(q.total())
            .deliveryChargeToBeConfirmed(q.deliveryCharge() == null)
            .build();
    }

    private RiceDtos.OrderResponse mapOrder(RiceOrder o, boolean includeAdminFields) {
        return RiceDtos.OrderResponse.builder()
            .id(o.getId()).orderNumber(o.getOrderNumber())
            .customerUserId(includeAdminFields && o.getCustomer() != null ? o.getCustomer().getId() : null)
            .productName(o.getProduct() == null ? null : o.getProduct().getName())
            .customerName(o.getCustomerName()).mobileNumber(o.getMobileNumber())
            .quantityKg(o.getQuantityKg()).basePackKg(o.getBasePackKg()).basePackPrice(o.getBasePackPrice())
            .subtotal(o.getSubtotal()).deliveryCharge(o.getDeliveryCharge()).totalAmount(o.getTotalAmount())
            .deliveryAddress(o.getDeliveryAddress()).area(o.getArea()).pincode(o.getPincode())
            .preferredDeliveryDate(o.getPreferredDeliveryDate()).preferredDeliverySlot(o.getPreferredDeliverySlot())
            .deliveryNotes(o.getDeliveryNotes())
            .status(o.getStatus()).paymentStatus(o.getPaymentStatus())
            .adminNotes(includeAdminFields ? o.getAdminNotes() : null)
            .createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt())
            .build();
    }

    private RiceDtos.SampleResponse mapSample(RiceSampleRequest s, boolean includeAdminFields) {
        return RiceDtos.SampleResponse.builder()
            .id(s.getId()).requestNumber(s.getRequestNumber())
            .customerUserId(includeAdminFields && s.getCustomer() != null ? s.getCustomer().getId() : null)
            .productName(s.getProduct() == null ? null : s.getProduct().getName())
            .customerName(s.getCustomerName()).mobileNumber(s.getMobileNumber())
            .sampleWeightGrams(s.getSampleWeightGrams())
            .price(BigDecimal.ZERO.setScale(2))
            .fulfillmentType(RiceSampleRequest.FULFILLMENT_PICKUP)
            .status(s.getStatus())
            .pickupLocation(s.getPickupLocation()).pickupTime(s.getPickupTime())
            .pickupInstructions(s.getPickupInstructions())
            .adminNotes(includeAdminFields ? s.getAdminNotes() : null)
            .createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt())
            .build();
    }
}

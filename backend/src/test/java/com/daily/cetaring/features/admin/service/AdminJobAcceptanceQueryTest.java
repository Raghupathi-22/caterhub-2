package com.daily.cetaring.features.admin.service;

import com.daily.cetaring.features.admin.dto.AdminOrderSummaryDTO;
import com.daily.cetaring.features.admin.dto.AdminServiceRequestDetailsDTO;
import com.daily.cetaring.features.admin.dto.AdminStaffingJobDetailsDTO;
import com.daily.cetaring.features.admin.repository.CouponRepository;
import com.daily.cetaring.features.admin.repository.PromotionCampaignRepository;
import com.daily.cetaring.features.booking.repository.BookingRepository;
import com.daily.cetaring.features.service.entity.ServiceRequest;
import com.daily.cetaring.features.service.repository.ServiceRequestRepository;
import com.daily.cetaring.features.worker.entity.StaffingJobAcceptance;
import com.daily.cetaring.features.worker.entity.StaffingRequest;
import com.daily.cetaring.features.worker.entity.WorkerProfile;
import com.daily.cetaring.features.worker.repository.JobAssignmentRepository;
import com.daily.cetaring.features.worker.repository.StaffingJobAcceptanceRepository;
import com.daily.cetaring.features.worker.repository.StaffingRequestRepository;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the admin job/acceptance queries (fetch joins, grouped counts, date-bounded job queries) against real
 * entities in H2 (MySQL mode) and checks the counts/worker lists an administrator sees.
 */
class AdminJobAcceptanceQueryTest {

    private static final LocalDate PAST_EVENT = LocalDate.of(2026, 9, 27);
    private static final LocalDate FUTURE_EVENT = LocalDate.of(2026, 12, 5);

    private static LocalContainerEntityManagerFactoryBean factoryBean;
    private static EntityManager em;
    private static AdminOperationsService service;
    private static StaffingRequestRepository staffingRequestRepository;

    private static Long sr1Id;
    private static Long sr2Id;
    private static Long boysJobId;
    private static Long orphanJobId;

    @BeforeAll
    static void setUp() {
        factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPackagesToScan("com.daily.cetaring");
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factoryBean.setJpaPropertyMap(Map.of(
            "jakarta.persistence.jdbc.url",
            "jdbc:h2:mem:admin_jobs;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
            "jakarta.persistence.jdbc.driver", "org.h2.Driver",
            "jakarta.persistence.jdbc.user", "sa",
            "jakarta.persistence.jdbc.password", "",
            "hibernate.hbm2ddl.auto", "create-drop",
            "hibernate.physical_naming_strategy",
            "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
        ));
        factoryBean.afterPropertiesSet();
        em = factoryBean.getObject().createEntityManager();
        JpaRepositoryFactory repositories = new JpaRepositoryFactory(em);
        staffingRequestRepository = repositories.getRepository(StaffingRequestRepository.class);
        service = new AdminOperationsService(
            repositories.getRepository(BookingRepository.class),
            repositories.getRepository(CouponRepository.class),
            repositories.getRepository(PromotionCampaignRepository.class),
            repositories.getRepository(UserRepository.class),
            repositories.getRepository(JobAssignmentRepository.class),
            repositories.getRepository(ServiceRequestRepository.class),
            staffingRequestRepository,
            repositories.getRepository(StaffingJobAcceptanceRepository.class)
        );
        seed();
    }

    @AfterAll
    static void tearDown() {
        if (em != null) em.close();
        if (factoryBean != null) factoryBean.destroy();
    }

    private static void seed() {
        em.getTransaction().begin();
        User asha = user("asha", "+919800000101", "Asha", "Customer");
        User dev = user("dev", "+919800000102", "Dev", "Customer");
        WorkerProfile[] workers = new WorkerProfile[9];
        for (int i = 0; i < workers.length; i++) {
            User u = user("worker" + i, "+91970000000" + i, "Worker", String.valueOf(i));
            workers[i] = WorkerProfile.builder().user(u).workerType(WorkerProfile.WorkerType.CATERING_BOY)
                .status(i == 8 ? WorkerProfile.WorkerStatus.PENDING_VERIFICATION : WorkerProfile.WorkerStatus.ACTIVE)
                .experienceYears(2).rating(BigDecimal.ZERO).totalRatings(0).build();
            em.persist(workers[i]);
        }

        // Past catering-staff request (historical) explicitly linked to two worker jobs: 10 boys + 2 chefs.
        ServiceRequest sr1 = request(asha, "CATERING_STAFF", PAST_EVENT, "Kondapur Hall", "Kondapur");
        StaffingRequest boys = job(asha, sr1.getId(), WorkerProfile.WorkerType.CATERING_BOY, PAST_EVENT, "Kondapur Hall", "Kondapur", 10, 7);
        StaffingRequest chefs = job(asha, sr1.getId(), WorkerProfile.WorkerType.CHEF, PAST_EVENT, "Kondapur Hall", "Kondapur", 2, 1);
        for (int i = 0; i < 7; i++) accept(boys, workers[i], StaffingJobAcceptance.AcceptanceStatus.ACCEPTED);
        accept(boys, workers[7], StaffingJobAcceptance.AcceptanceStatus.CANCELLED);
        accept(chefs, workers[0], StaffingJobAcceptance.AcceptanceStatus.ACCEPTED);

        // Future decoration request whose job was posted separately (no explicit link, legacy Android flow).
        ServiceRequest sr2 = request(dev, "DECORATION", FUTURE_EVENT, "Gachibowli Lawns", "Gachibowli");
        StaffingRequest decor = job(dev, null, WorkerProfile.WorkerType.EVENT_DECORATOR, FUTURE_EVENT, "Gachibowli Lawns", "Gachibowli", 3, 2);
        accept(decor, workers[1], StaffingJobAcceptance.AcceptanceStatus.ACCEPTED);
        accept(decor, workers[8], StaffingJobAcceptance.AcceptanceStatus.ACCEPTED);

        // Job with no matching service request (different venue) must still be visible to admins.
        StaffingRequest orphan = job(dev, null, WorkerProfile.WorkerType.CATERING_BOY, FUTURE_EVENT, "Other Venue", "Madhapur", 4, 1);
        accept(orphan, workers[2], StaffingJobAcceptance.AcceptanceStatus.ACCEPTED);
        em.getTransaction().commit();
        em.clear();

        sr1Id = sr1.getId();
        sr2Id = sr2.getId();
        boysJobId = boys.getId();
        orphanJobId = orphan.getId();
    }

    private static User user(String username, String phone, String first, String last) {
        User user = User.builder().username(username).phoneNumber(phone).firstName(first).lastName(last)
            .isActive(true).isVerified(true).roles(new HashSet<>()).build();
        em.persist(user);
        return user;
    }

    private static ServiceRequest request(User customer, String type, LocalDate date, String location, String area) {
        ServiceRequest request = ServiceRequest.builder().createdBy(customer).serviceType(type).eventType("Wedding")
            .eventDate(date).startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(23, 0)).location(location).area(area)
            .quoteBased(false).totalAmount(new BigDecimal("1000")).status(ServiceRequest.Status.PENDING).build();
        em.persist(request);
        return request;
    }

    private static StaffingRequest job(User creator, Long serviceRequestId, WorkerProfile.WorkerType type, LocalDate date,
                                       String location, String area, int required, int acceptedCounter) {
        StaffingRequest job = StaffingRequest.builder().createdBy(creator).serviceRequestId(serviceRequestId).eventType("Wedding")
            .workerType(type).eventDate(date).startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(23, 0))
            .location(location).area(area).requiredWorkers(required).acceptedWorkers(acceptedCounter)
            .payment(new BigDecimal("850")).status(StaffingRequest.StaffingStatus.OPEN).build();
        em.persist(job);
        return job;
    }

    private static void accept(StaffingRequest job, WorkerProfile worker, StaffingJobAcceptance.AcceptanceStatus status) {
        em.persist(StaffingJobAcceptance.builder().staffingRequest(job).workerProfile(worker).status(status).build());
    }

    private static AdminOrderSummaryDTO row(List<AdminOrderSummaryDTO> rows, AdminOrderSummaryDTO.OrderType type, Long id) {
        return rows.stream().filter(r -> r.getOrderType() == type && r.getId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void feedShowsAccurateAcceptedAndRequestedCountsForPastAndFutureRequests() {
        List<AdminOrderSummaryDTO> rows = service.getAllOrders();

        AdminOrderSummaryDTO past = row(rows, AdminOrderSummaryDTO.OrderType.SERVICE_REQUEST, sr1Id);
        assertEquals(8, past.getAcceptedWorkerCount(), "7 boys + 1 chef; the cancelled acceptance is not counted");
        assertEquals(12, past.getRequestedWorkerCount());
        assertEquals(Set.of("CATERING_BOY", "CHEF"), Set.copyOf(past.getRequestedRoles()));
        assertEquals(PAST_EVENT, past.getEventDate());
        assertEquals(LocalTime.of(18, 0), past.getStartTime());
        assertNotNull(past.getBookedAt());
        assertEquals("+919800000101", past.getCustomer().getMobileNumber());

        AdminOrderSummaryDTO future = row(rows, AdminOrderSummaryDTO.OrderType.SERVICE_REQUEST, sr2Id);
        assertEquals(2, future.getAcceptedWorkerCount());
        assertEquals(3, future.getRequestedWorkerCount());

        AdminOrderSummaryDTO orphan = row(rows, AdminOrderSummaryDTO.OrderType.STAFFING_JOB, orphanJobId);
        assertEquals(1, orphan.getAcceptedWorkerCount());
        assertEquals(4, orphan.getRequestedWorkerCount());
        assertEquals(3, rows.size(), "each job is attributed once; linked jobs are not repeated as standalone rows");
    }

    @Test
    void serviceRequestDetailsListEveryActualAcceptanceWithContactDetailsAndNoDuplicates() {
        AdminServiceRequestDetailsDTO details = service.getServiceRequestDetails(sr1Id);

        assertEquals(8, details.getAcceptedWorkerCount());
        assertEquals(12, details.getRequestedWorkerCount());
        assertEquals(8, details.getAcceptedWorkers().size());
        assertTrue(details.getAcceptedWorkers().stream().noneMatch(w -> w.getAcceptanceStatus() == StaffingJobAcceptance.AcceptanceStatus.CANCELLED));
        assertEquals(8, details.getAcceptedWorkers().stream().map(AdminServiceRequestDetailsDTO.AcceptedWorkerDTO::getAcceptanceId).distinct().count());
        assertTrue(details.getAcceptedWorkers().stream().allMatch(w -> w.getMobileNumber() != null && w.getMobileNumber().startsWith("+9197")));
        assertTrue(details.getAcceptedWorkers().stream().allMatch(w -> w.getAcceptedAt() != null));
        assertFalse(details.getAcceptedWorkers().stream().anyMatch(w -> "+919700000007".equals(w.getMobileNumber())),
            "worker whose acceptance was cancelled is not listed as accepted");

        AdminServiceRequestDetailsDTO.StaffingJobDTO boys = details.getStaffingJobs().stream()
            .filter(j -> j.getId().equals(boysJobId)).findFirst().orElseThrow();
        assertEquals(7, boys.getAcceptedWorkers());
        assertEquals(3, boys.getRemainingPositions());
    }

    @Test
    void unlinkedJobIsAttributedToItsMatchingRequestIncludingUnverifiedWorkerStatus() {
        AdminServiceRequestDetailsDTO details = service.getServiceRequestDetails(sr2Id);

        assertEquals(2, details.getAcceptedWorkerCount());
        assertEquals(Set.of(WorkerProfile.WorkerStatus.ACTIVE, WorkerProfile.WorkerStatus.PENDING_VERIFICATION),
            Set.copyOf(details.getAcceptedWorkers().stream().map(AdminServiceRequestDetailsDTO.AcceptedWorkerDTO::getProfileStatus).toList()));
        assertTrue(details.getAcceptedWorkers().stream().allMatch(w -> w.getRequestedWorkerType() == WorkerProfile.WorkerType.EVENT_DECORATOR));
    }

    @Test
    void standaloneJobDetailsListAcceptedWorkers() {
        AdminStaffingJobDetailsDTO details = service.getStaffingJobDetails(orphanJobId);

        assertEquals(1, details.getAcceptedWorkerCount());
        assertEquals("+919700000002", details.getAcceptedWorkers().get(0).getMobileNumber());
        assertEquals("+919800000102", details.getCustomer().getMobileNumber());
    }

    @Test
    void offerableJobQueryExcludesPastEventDates() {
        List<StaffingRequest> open = staffingRequestRepository.findByStatusAndEventDateGreaterThanEqualOrderByEventDateAscStartTimeAsc(
            StaffingRequest.StaffingStatus.OPEN, LocalDate.of(2026, 10, 22));

        assertEquals(2, open.size());
        assertTrue(open.stream().allMatch(job -> !job.getEventDate().isBefore(LocalDate.of(2026, 10, 22))));
    }
}

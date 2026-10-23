package com.daily.cetaring.features.admin.service;

import com.daily.cetaring.features.service.entity.ServiceRequest;
import com.daily.cetaring.features.worker.entity.StaffingRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Attributes staffing jobs (the rows workers actually accept) to customer service requests.
 *
 * <p>Jobs created by the catering-staff booking flow carry an explicit {@code service_request_id}. Older jobs, and
 * jobs created by the Android booking flow for other categories (which posts the staffing jobs and the service
 * request separately), have no link. For those, the existing admin fallback rule is applied: same creator, event
 * type, date, start/end time, location and area. Each unlinked job is attributed to at most one request (the one
 * created closest in time) so acceptances are never double-counted across requests.
 */
public final class StaffingJobLinker {

    private StaffingJobLinker() {
    }

    public record LinkResult(Map<Long, List<StaffingRequest>> jobsByServiceRequestId, List<StaffingRequest> unmatchedJobs) {
        public List<StaffingRequest> jobsFor(Long serviceRequestId) {
            return jobsByServiceRequestId.getOrDefault(serviceRequestId, List.of());
        }
    }

    public static LinkResult link(Collection<ServiceRequest> requests, Collection<StaffingRequest> jobs) {
        Map<Long, ServiceRequest> requestsById = new LinkedHashMap<>();
        Map<Long, List<ServiceRequest>> requestsByCreator = new HashMap<>();
        for (ServiceRequest request : requests) {
            requestsById.put(request.getId(), request);
            Long creatorId = creatorId(request);
            if (creatorId != null) {
                requestsByCreator.computeIfAbsent(creatorId, ignored -> new ArrayList<>()).add(request);
            }
        }

        Map<Long, List<StaffingRequest>> linked = new LinkedHashMap<>();
        List<StaffingRequest> unmatched = new ArrayList<>();
        for (StaffingRequest job : jobs) {
            ServiceRequest owner = null;
            if (job.getServiceRequestId() != null) {
                owner = requestsById.get(job.getServiceRequestId());
                if (owner == null) {
                    // Explicitly linked to a request outside this result set; never re-attribute it heuristically.
                    continue;
                }
            } else {
                Long creatorId = job.getCreatedBy() == null ? null : job.getCreatedBy().getId();
                owner = requestsByCreator.getOrDefault(creatorId, List.of()).stream()
                    .filter(request -> matches(request, job))
                    .min(Comparator.<ServiceRequest>comparingLong(request -> distance(request.getCreatedAt(), job.getCreatedAt()))
                        .thenComparing(ServiceRequest::getId))
                    .orElse(null);
            }
            if (owner == null) {
                unmatched.add(job);
            } else {
                linked.computeIfAbsent(owner.getId(), ignored -> new ArrayList<>()).add(job);
            }
        }
        linked.values().forEach(list -> list.sort(Comparator.comparing(StaffingRequest::getId, Comparator.nullsLast(Comparator.naturalOrder()))));
        return new LinkResult(linked, unmatched);
    }

    static boolean matches(ServiceRequest request, StaffingRequest job) {
        return equalsTrimmedIgnoreCase(request.getEventType(), job.getEventType())
            && Objects.equals(request.getEventDate(), job.getEventDate())
            && Objects.equals(request.getStartTime(), job.getStartTime())
            && (request.getEndTime() == null || Objects.equals(request.getEndTime(), job.getEndTime()))
            && equalsTrimmedIgnoreCase(request.getLocation(), job.getLocation())
            && equalsTrimmedIgnoreCase(request.getArea(), job.getArea());
    }

    private static Long creatorId(ServiceRequest request) {
        return request.getCreatedBy() == null ? null : request.getCreatedBy().getId();
    }

    private static boolean equalsTrimmedIgnoreCase(String a, String b) {
        if (a == null || b == null) return a == b;
        return a.trim().equalsIgnoreCase(b.trim());
    }

    private static long distance(LocalDateTime a, LocalDateTime b) {
        if (a == null || b == null) return Long.MAX_VALUE;
        return Math.abs(Duration.between(a, b).toMillis());
    }
}

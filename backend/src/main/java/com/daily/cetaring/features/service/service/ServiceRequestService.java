package com.daily.cetaring.features.service.service;

import com.daily.cetaring.features.service.dto.ServiceRequestDtos;
import com.daily.cetaring.features.service.entity.ServiceRequest;
import com.daily.cetaring.features.service.repository.ServiceRequestRepository;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class ServiceRequestService {
    private final ServiceRequestRepository repository;
    private final UserRepository userRepository;

    @Transactional
    public ServiceRequestDtos.Response create(String username, ServiceRequestDtos.CreateRequest r) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        ServiceRequest saved = repository.save(ServiceRequest.builder().createdBy(user)
            .serviceType(r.serviceType.trim()).eventType(r.eventType.trim()).eventDate(r.eventDate)
            .startTime(r.startTime).location(r.location.trim()).area(r.area.trim()).details(r.details)
            .totalAmount(r.totalAmount).status(ServiceRequest.Status.PENDING).build());
        return map(saved);
    }
    @Transactional(readOnly=true)
    public List<ServiceRequestDtos.Response> mine(String username) {
        return repository.findByCreatedByUsernameOrderByCreatedAtDesc(username).stream().map(this::map).toList();
    }

    @Transactional(readOnly=true)
    public List<ServiceRequestDtos.Response> all() { return repository.findAllByOrderByCreatedAtDesc().stream().map(this::map).toList(); }
    private ServiceRequestDtos.Response map(ServiceRequest s) { return ServiceRequestDtos.Response.builder().id(s.getId()).serviceType(s.getServiceType()).eventType(s.getEventType()).eventDate(s.getEventDate()).startTime(s.getStartTime()).location(s.getLocation()).area(s.getArea()).details(s.getDetails()).totalAmount(s.getTotalAmount()).status(s.getStatus()).createdAt(s.getCreatedAt()).build(); }
}

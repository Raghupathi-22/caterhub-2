package com.daily.cetaring.features.worker.controller;

import com.daily.cetaring.features.worker.dto.WorkerDtos;
import com.daily.cetaring.features.worker.service.WorkerService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class WorkerControllerProfileUpdateTest {

    @Test
    void updateMyProfileUsesAuthenticatedPrincipalOnly() {
        WorkerService service = mock(WorkerService.class);
        WorkerController controller = new WorkerController(service);
        WorkerDtos.UpdateMyWorkerProfileRequest request = WorkerDtos.UpdateMyWorkerProfileRequest.builder()
            .fullName("Ravi Kumar")
            .skills("Cooking")
            .preferredAreas("Madhapur")
            .languages("Telugu")
            .build();
        WorkerDtos.WorkerProfileResponse expected = WorkerDtos.WorkerProfileResponse.builder().id(20L).build();
        when(service.updateMyProfile("worker11", request)).thenReturn(expected);

        WorkerDtos.WorkerProfileResponse response = controller.updateMyProfile(
            new UsernamePasswordAuthenticationToken("worker11", null, List.of()),
            request
        );

        assertSame(expected, response);
        verify(service).updateMyProfile("worker11", request);
        verifyNoMoreInteractions(service);
    }

    @Test
    void updateEndpointIsWorkerOnlyAndHasNoTargetIdentifier() throws Exception {
        Method method = WorkerController.class.getMethod(
            "updateMyProfile",
            Authentication.class,
            WorkerDtos.UpdateMyWorkerProfileRequest.class
        );

        PutMapping mapping = method.getAnnotation(PutMapping.class);
        assertNotNull(mapping);
        assertArrayEquals(new String[]{"/profiles/me"}, mapping.value());
        assertEquals("hasAuthority('ROLE_WORKER')", method.getAnnotation(PreAuthorize.class).value());
        for (Parameter parameter : method.getParameters()) {
            assertFalse(parameter.isAnnotationPresent(PathVariable.class));
            assertFalse(parameter.isAnnotationPresent(RequestParam.class));
        }
    }

    @Test
    void updateRequestCannotCarryIdentityRoleStatusOrRatingFields() {
        Set<String> fields = Arrays.stream(WorkerDtos.UpdateMyWorkerProfileRequest.class.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .map(Field::getName)
            .collect(Collectors.toSet());

        assertEquals(Set.of("fullName", "age", "skills", "preferredAreas", "languages", "bio"), fields);
    }
}

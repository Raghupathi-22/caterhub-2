package com.daily.cetaring.features.auth.mapper;

import com.daily.cetaring.shared.dto.UserDTO;
import com.daily.cetaring.shared.entity.User;
import com.daily.cetaring.shared.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "businessId", source = "business.id")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserDTO toDTO(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toEntity(UserDTO userDTO);

    default List<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream().map(Role::getName).sorted().toList();
    }
}

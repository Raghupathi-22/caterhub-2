package com.daily.cetaring.features.auth.mapper;

import com.daily.cetaring.shared.dto.UserDTO;
import com.daily.cetaring.shared.entity.Business;
import com.daily.cetaring.shared.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-18T18:48:02+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO.UserDTOBuilder userDTO = UserDTO.builder();

        userDTO.businessId( userBusinessId( user ) );
        userDTO.id( user.getId() );
        userDTO.username( user.getUsername() );
        userDTO.email( user.getEmail() );
        userDTO.phoneNumber( user.getPhoneNumber() );
        userDTO.firstName( user.getFirstName() );
        userDTO.lastName( user.getLastName() );
        userDTO.profileImageUrl( user.getProfileImageUrl() );
        userDTO.isActive( user.getIsActive() );
        userDTO.isVerified( user.getIsVerified() );
        userDTO.emailVerifiedAt( user.getEmailVerifiedAt() );
        userDTO.phoneVerifiedAt( user.getPhoneVerifiedAt() );
        userDTO.lastLoginAt( user.getLastLoginAt() );
        userDTO.createdAt( user.getCreatedAt() );
        userDTO.updatedAt( user.getUpdatedAt() );

        userDTO.roles( mapRoles(user.getRoles()) );

        return userDTO.build();
    }

    @Override
    public User toEntity(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( userDTO.getId() );
        user.username( userDTO.getUsername() );
        user.email( userDTO.getEmail() );
        user.phoneNumber( userDTO.getPhoneNumber() );
        user.firstName( userDTO.getFirstName() );
        user.lastName( userDTO.getLastName() );
        user.profileImageUrl( userDTO.getProfileImageUrl() );
        user.isActive( userDTO.getIsActive() );
        user.isVerified( userDTO.getIsVerified() );
        user.emailVerifiedAt( userDTO.getEmailVerifiedAt() );
        user.phoneVerifiedAt( userDTO.getPhoneVerifiedAt() );
        user.lastLoginAt( userDTO.getLastLoginAt() );
        user.createdAt( userDTO.getCreatedAt() );
        user.updatedAt( userDTO.getUpdatedAt() );

        return user.build();
    }

    private Long userBusinessId(User user) {
        if ( user == null ) {
            return null;
        }
        Business business = user.getBusiness();
        if ( business == null ) {
            return null;
        }
        Long id = business.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

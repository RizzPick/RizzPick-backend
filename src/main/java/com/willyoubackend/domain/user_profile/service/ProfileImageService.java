package com.willyoubackend.domain.user_profile.service;

import com.willyoubackend.domain.user.entity.UserEntity;
import com.willyoubackend.domain.user_profile.dto.ImageResponseDto;
import com.willyoubackend.domain.user_profile.dto.ProfileImageRequestDto;
import com.willyoubackend.domain.user_profile.entity.ProfileImageEntity;
import com.willyoubackend.domain.user_profile.entity.UserProfileEntity;
import com.willyoubackend.domain.user_profile.repository.ProfileImageRepository;
import com.willyoubackend.domain.user_profile.repository.UserProfileRepository;
import com.willyoubackend.global.dto.ApiResponse;
import com.willyoubackend.global.exception.CustomException;
import com.willyoubackend.global.exception.ErrorCode;
import com.willyoubackend.global.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final S3Uploader s3Uploader;
    private final ProfileImageRepository profileImageRepository;
    private final UserProfileRepository userProfileRepository;

    public ResponseEntity<ApiResponse<ImageResponseDto>> updateProfileImage(UserEntity userEntity, ProfileImageRequestDto profileImageRequestDto) throws IOException {
        ProfileImageEntity profileImageEntity = null;

        switch (profileImageRequestDto.getAction()) {
            case ADD -> profileImageEntity = addProfileImage(userEntity, profileImageRequestDto);
            case DELETE -> deleteProfileImage(profileImageRequestDto.getId());
            case MODIFY -> profileImageEntity = modifyProfileImage(userEntity, profileImageRequestDto);
        }

        updateUserActiveStatus(userEntity);

        ImageResponseDto imageResponseDto = (profileImageEntity != null) ? new ImageResponseDto(profileImageEntity) : null;
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(imageResponseDto));
    }

    private void updateUserActiveStatus(UserEntity userEntity) {
        UserProfileEntity userProfileEntity = userEntity.getUserProfileEntity();

        boolean isActive = userProfileEntity.getNickname() != null && !userProfileEntity.getNickname().isEmpty() &&
                userProfileEntity.getGender() != null;

        userProfileEntity.setUserActiveStatus(isActive);
        userProfileRepository.save(userProfileEntity);
    }

    private ProfileImageEntity addProfileImage(UserEntity userEntity, ProfileImageRequestDto profileImageRequestDto) throws IOException {
        List<ProfileImageEntity> profileImageEntities = profileImageRepository.findAllByUserEntity(userEntity);
        if (profileImageEntities.size() >= 6) {
            throw new CustomException(ErrorCode.INVALID_MAXIMA);
        }
        String fileName = s3Uploader.upload(profileImageRequestDto.getImage(), "profileImage/" + userEntity.getUsername());
        ProfileImageEntity profileImageEntity = new ProfileImageEntity(fileName);
        profileImageEntity.setUserEntity(userEntity);
        profileImageRepository.save(profileImageEntity);
        return profileImageEntity;
    }

    public void deleteProfileImage(Long imageId) {
        ProfileImageEntity profileImageEntity = findImageById(imageId);
        UserEntity userEntity = profileImageEntity.getUserEntity();
        List<ProfileImageEntity> profileImageEntities = profileImageRepository.findAllByUserEntity(userEntity);
        if (profileImageEntities.size() <= 1) {
            throw new CustomException(ErrorCode.INVALID_IMAGE);
        }
        s3Uploader.delete(profileImageEntity.getImage());
        profileImageRepository.deleteById(imageId);
    }

    private ProfileImageEntity modifyProfileImage(UserEntity userEntity, ProfileImageRequestDto profileImageRequestDto) throws IOException {
        ProfileImageEntity profileImageEntity = findImageById(profileImageRequestDto.getId());
        s3Uploader.delete(profileImageEntity.getImage());
        String fileName = s3Uploader.upload(profileImageRequestDto.getImage(), "profileImage/" + userEntity.getUsername());
        profileImageEntity.setImage(fileName);
        profileImageRepository.save(profileImageEntity);
        return profileImageEntity;
    }

    private ProfileImageEntity findImageById(Long imageId) {
        return profileImageRepository.findById(imageId).orElseThrow(() ->
                new CustomException(ErrorCode.NOT_FOUND_ENTITY));
    }
}
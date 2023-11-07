package com.willyoubackend.domain.user_profile.service;

import com.willyoubackend.domain.dating.dto.DatingResponseDto;
import com.willyoubackend.domain.dating.entity.Dating;
import com.willyoubackend.domain.dating.repository.DatingRepository;
import com.willyoubackend.domain.user.entity.UserEntity;
import com.willyoubackend.domain.user.repository.UserRepository;
import com.willyoubackend.domain.user_like_match.entity.UserMatchStatus;
import com.willyoubackend.domain.user_like_match.repository.UserLikeStatusRepository;
import com.willyoubackend.domain.user_like_match.repository.UserMatchStatusRepository;
import com.willyoubackend.domain.user_like_match.repository.UserNopeStatusRepository;
import com.willyoubackend.domain.user_profile.dto.*;
import com.willyoubackend.domain.user_profile.entity.GenderEnum;
import com.willyoubackend.domain.user_profile.entity.ProfileImageEntity;
import com.willyoubackend.domain.user_profile.entity.UserProfileEntity;
import com.willyoubackend.domain.user_profile.repository.ProfileImageRepository;
import com.willyoubackend.domain.user_profile.repository.UserProfileRepository;
import com.willyoubackend.global.dto.ApiResponse;
import com.willyoubackend.global.exception.CustomException;
import com.willyoubackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "UserProfile Service")
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DatingRepository datingRepository;
    private final UserLikeStatusRepository userLikeStatusRepository;
    private final UserNopeStatusRepository userNopeStatusRepository;
    private final ProfileImageRepository profileImageRepository;
    private final UserMatchStatusRepository userMatchStatusRepository;

    public ResponseEntity<ApiResponse<UserProfileResponseDto>> updateUserProfile(UserEntity userEntity, UserProfileRequestDto userProfileRequestDto) {

        if (userProfileRequestDto.getNickname() == null || userProfileRequestDto.getNickname().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME);
        }

        if (userProfileRequestDto.getGender() == null || userProfileRequestDto.getGender().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_GENDER);
        }

        UserEntity loggedInUser = findUserById(userEntity.getId());

        UserProfileEntity userProfileEntity = userEntity.getUserProfileEntity();

        userProfileEntity.setUserEntity(loggedInUser);
        userProfileEntity.updateProfile(userProfileRequestDto);

        List<ProfileImageEntity> profileImageEntities = profileImageRepository.findAllByUserEntity(userEntity);

        userProfileEntity.setUserActiveStatus(!profileImageEntities.isEmpty()); // 프로필 이미지가 있으면 true, 없으면 false

//        if (!profileImageEntities.isEmpty()) {
//            userProfileEntity.setUserActiveStatus(true);
//        } else {
//            userProfileEntity.setUserActiveStatus(false);
//        }

        userProfileRepository.save(userProfileEntity);

        UserProfileResponseDto userProfileResponseDto = new UserProfileResponseDto(loggedInUser);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(userProfileResponseDto));
    }

    public ResponseEntity<ApiResponse<List<UserProfileResponseDto>>> getRecommendations(UserEntity userEntity) {

        String location = userEntity.getUserProfileEntity().getLocation();
        GenderEnum gender = userEntity.getUserProfileEntity().getGender();

        List<UserProfileResponseDto> userProfileResponseDtoList = new ArrayList<>();
        List<UserEntity> filteredUsers;
        if (gender == GenderEnum.MALE || gender == GenderEnum.FEMALE) {
            filteredUsers = userRepository.findByUserProfileEntity_LocationAndUserProfileEntity_GenderNotAndIdNot(location, gender, userEntity.getId());
        } else {
            filteredUsers = userRepository.findByUserProfileEntity_LocationAndIdNot(location, userEntity.getId());
        }
        int maxLimit = 0;
        for (UserEntity filteredUser : filteredUsers) {
            if (maxLimit == 100) break;
            log.info(userNopeStatusRepository.existBySentUserAndReceivedUser(userEntity, filteredUser) + "");
            if (!userNopeStatusRepository.existBySentUserAndReceivedUser(userEntity, filteredUser) &&
                    !userLikeStatusRepository.existBySentUserAndReceivedUser(userEntity, filteredUser) &&
                    filteredUser.getUserProfileEntity().isUserActiveStatus()) {
                userProfileResponseDtoList.add(new UserProfileResponseDto(filteredUser));
            }
            maxLimit++;
        }

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(userProfileResponseDtoList));
    }

    public ResponseEntity<ApiResponse<UserOwnProfileResponseDto>> getMyProfile(UserEntity userEntity) {
        List<DatingResponseDto> datingList = datingRepository.findAllByUser(userEntity)
                .stream()
                .map(DatingResponseDto::new)
                .toList();
        UserOwnProfileResponseDto userProfileResponseDto = new UserOwnProfileResponseDto(findUserById(userEntity.getId()), datingList);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(userProfileResponseDto));
    }

    public ResponseEntity<ApiResponse<UserProfileMatchResponseDto>> getUserProfile(UserEntity user, Long userId) {
        UserMatchStatus matchStatus = (userMatchStatusRepository.findByUserMatchedOneAndUserMatchedTwo(user, findUserById(userId)) == null) ? userMatchStatusRepository.findByUserMatchedOneAndUserMatchedTwo(findUserById(userId), user) : userMatchStatusRepository.findByUserMatchedOneAndUserMatchedTwo(user, findUserById(userId));
        Long matchId =(matchStatus == null)?null:matchStatus.getId();
        UserProfileMatchResponseDto userProfileResponseDto = new UserProfileMatchResponseDto(findUserById(userId), matchId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(userProfileResponseDto));
    }

    public ResponseEntity<ApiResponse<UserProfileResponseDto>> setMainDating(UserEntity userEntity, SetMainDatingRequestDto setMainDatingRequestDto) {
        Dating dating = findByIdDateAuthCheck(setMainDatingRequestDto.getDatingId(), userEntity);

        UserEntity loggedInUser = findUserById(userEntity.getId());
        UserProfileEntity userProfileEntity = loggedInUser.getUserProfileEntity();

        userProfileEntity.setDating(dating);

        userProfileRepository.save(userProfileEntity);

        UserProfileResponseDto userProfileResponseDto = new UserProfileResponseDto(loggedInUser);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(userProfileResponseDto));
    }

    public ResponseEntity<ApiResponse<UserProfileResponseDto>> deleteMainDating(UserEntity userEntity, Long datingId) {
        Dating dating = findByIdDateAuthCheck(datingId, userEntity);
        UserEntity loggedInUser = findUserById(userEntity.getId());
        UserProfileEntity userProfileEntity = loggedInUser.getUserProfileEntity();

        userProfileEntity.setDating(null);

        userProfileRepository.save(userProfileEntity);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successMessage("삭제 되었습니다."));
    }

    private UserEntity findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new CustomException(ErrorCode.NOT_FOUND_ENTITY));
    }

    private Dating findByIdDateAuthCheck(Long id, UserEntity user) {
        Dating selectedDating = datingRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_ENTITY)
        );
        if (!selectedDating.getUser().getId().equals(user.getId())) throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        return selectedDating;
    }

    public void deactivateUser(Long userId) {
        UserProfileEntity userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userProfile.setUserActiveStatus(false);
        userProfileRepository.save(userProfile);
    }

    public void activateUserStatusByUsername(String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        UserProfileEntity userProfileEntity = userProfileRepository.findByUserEntity(userEntity);
        userProfileEntity.setUserActiveStatus(true);
        userProfileRepository.save(userProfileEntity);
    }
}
package com.willyoubackend.domain.user_profile.controller;

import com.willyoubackend.domain.user.entity.UserEntity;
import com.willyoubackend.domain.user.repository.UserRepository;
import com.willyoubackend.domain.user.security.UserDetailsImpl;
import com.willyoubackend.domain.user_profile.dto.ImageResponseDto;
import com.willyoubackend.domain.user_profile.dto.ProfileImageRequestDto;
import com.willyoubackend.domain.user_profile.service.ProfileImageService;
import com.willyoubackend.global.dto.ApiResponse;
import com.willyoubackend.global.exception.CustomException;
import com.willyoubackend.global.exception.ErrorCode;
import com.willyoubackend.global.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@Tag(name = "프로필 이미지", description = "프로필 이미지")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/profileImage")
public class ProfileImageController {

    private final ProfileImageService profileImageService;
    private final UserRepository userRepository;

    @Operation(summary = "프로필 이미지 설정")
    @PutMapping(value = "/updateImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageResponseDto>> updateProfileImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @ModelAttribute ProfileImageRequestDto profileImageRequestDto) throws IOException {
        log.info(profileImageRequestDto.toString());
        return profileImageService.updateProfileImage(userDetails.getUser(), profileImageRequestDto);
    }

    @Operation(summary = "관리자가 프로필 이미지 삭제")
    @DeleteMapping("/deleteImage")
    public ResponseEntity<ApiResponse<String>> deleteProfileImage(
            @AuthenticationPrincipal UserDetails userDetails, @RequestParam(required = false) Long imageId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        UserEntity currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 어드민 권한 확인
        if (AuthorizationUtils.isAdmin(currentUser) || currentUser.getId().equals(userDetails.getUsername())) {
            profileImageService.deleteProfileImage(imageId); // ProfileImageService 객체를 주입받아서 메서드 호출
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successMessage("프로필 이미지 삭제 완료"));
        } else {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }
    }
}
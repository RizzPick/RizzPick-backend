package com.willyoubackend.domain.user_profile.controller;

import com.willyoubackend.domain.user.security.UserDetailsImpl;
import com.willyoubackend.domain.user_profile.dto.SetMainDatingRequestDto;
import com.willyoubackend.domain.user_profile.dto.UserProfileRequestDto;
import com.willyoubackend.domain.user_profile.dto.UserProfileResponseDto;
import com.willyoubackend.domain.user_profile.service.UserProfileService;
import com.willyoubackend.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "회원 프로필", description = "회원 프로필")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserProfileController {

    private final UserProfileService userProfileService;

    // 회원 프로필 업데이트
    @Operation(summary = "회원 프로필 업데이트")
    @PutMapping("/updateProfile")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> updateUserProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody UserProfileRequestDto userProfileRequestDto) {
        return userProfileService.updateUserProfile(userDetails.getUser(), userProfileRequestDto);
    }

    // 프로필 전체 조회
    @Operation(summary = "프로필 전체 조회")
    @GetMapping("/userProfiles")
    public ResponseEntity<ApiResponse<List<UserProfileResponseDto>>> getUserProfiles(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userProfileService.getUserProfiles(userDetails.getUser());
    }

    // 마이 프로필 조회
    @Operation(summary = "마이 프로필 조회")
    @GetMapping("/myProfile")
    private ResponseEntity<ApiResponse<UserProfileResponseDto>> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userProfileService.getMyProfile(userDetails.getUser());
    }

    // 프로필 상세 조회
    @Operation(summary = "프로필 상세 조회")
    @GetMapping("/userProfile/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getUserProfile(@PathVariable Long userId) {
        return userProfileService.getUserProfile(userId);
    }

    // 대표 데이트 설정 및 수정
    @Operation(summary = "대표 데이트 설정 및 수정")
    @PutMapping("/setMainDating")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> setMainDating(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody SetMainDatingRequestDto setMainDatingRequestDto) {
        return userProfileService.setMainDating(userDetails.getUser(), setMainDatingRequestDto);
    }


    // 대표 데이트 삭제
    @Operation(summary = "대표 데이트 삭제")
    @DeleteMapping("/deleteMainDating/{datingId}")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> deleteMainDating(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long datingId) {
        return userProfileService.deleteMainDating(userDetails.getUser(), datingId);
    }

    // Wooyong Jeong
    // User Profile Recommendations
    @Operation(summary = "유저 추천 로직")
    @GetMapping("/userprofile/recommendations")
    public ResponseEntity<ApiResponse<List<UserProfileResponseDto>>> getRecommendations(
            @AuthenticationPrincipal UserDetailsImpl userDetails )
    {
        return userProfileService.getRecommendations(userDetails.getUser());
    }
}

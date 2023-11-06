package com.willyoubackend.domain.dating.controller;

import com.willyoubackend.domain.dating.dto.DatingDetailResponseDto;
import com.willyoubackend.domain.dating.dto.DatingRequestDto;
import com.willyoubackend.domain.dating.dto.DatingResponseDto;
import com.willyoubackend.domain.dating.service.DatingService;
import com.willyoubackend.domain.user.entity.UserEntity;
import com.willyoubackend.domain.user.repository.UserRepository;
import com.willyoubackend.domain.user.security.UserDetailsImpl;
import com.willyoubackend.global.dto.ApiResponse;
import com.willyoubackend.global.exception.CustomException;
import com.willyoubackend.global.exception.ErrorCode;
import com.willyoubackend.global.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "데이트 추가", description = "데이트 관련 CRUD API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j(topic = "Dating Controller")
public class DatingController {
    private final DatingService datingService;
    private final UserRepository userRepository;

    @Operation(summary = "데이트 생성", description = "더미 값이 들어 있는 데이트를 생성(및 저장)합니다. 추후 Update를 이용해 저장을 해야합니다.")
    @PostMapping("/dating")
    public ResponseEntity<ApiResponse<DatingResponseDto>> createDating(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return datingService.createDating(userDetails.getUser());
    }

    @Operation(summary = "데이트 전체 조회", description = "앱에 등록 돼있는 모든 데이트를 반환 합니다.")
    @GetMapping("/datings")
    public ResponseEntity<ApiResponse<List<DatingResponseDto>>> getDatingList() {
        return datingService.getDatingList();
    }

    @Operation(summary = "유저가 작성한 데이트 조회", description = "로그인한 유저의 데이트를 조회 할 수 있습니다.")
    @GetMapping("/datings/user/me")
    public ResponseEntity<ApiResponse<List<DatingResponseDto>>> getDatingListByUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return datingService.getDatingListByUser(userDetails.getUser());
    }

    @Operation(summary = "특정 지역의 데이트 조회", description = "지역별 현재 존재하는 데이트들을 반환합니다.")
    @GetMapping("/datings/location")
    public ResponseEntity<ApiResponse<List<DatingResponseDto>>> getDatingListByLocation(@RequestParam String location) {
        return datingService.getDatingListByLocation(location);
    }

    @Operation(summary = "선택한 유저 데이트 조회", description = "선택한 유저가 작성한 데이트들을 반환합니다.")
    @GetMapping("/dataings/user/{userId}")
    public ResponseEntity<ApiResponse<List<DatingResponseDto>>> getDatingListBySelectedUser(@PathVariable Long userId) {
        return datingService.getDatingListBySelectedUser(userId);
    }

    @Operation(summary = "특정 데이트 상세조회", description = "특정 데이트를 상세 조회 할 수 있습니다. 대표 데이트 조회 혹은 데이트 전체 조회에서의 선택에 사용될 수 있습니다.")
    @GetMapping("/dating/{id}")
    public ResponseEntity<ApiResponse<DatingDetailResponseDto>> getDatingDetail(@PathVariable Long id) {
        return datingService.getDatingDetail(id);
    }

    @Operation(summary = "특정 데이트 수정", description = "유저 본인이 작성한 데이트를 수정 할 수 있습니다.")
    @PutMapping("/dating/{id}")
    public ResponseEntity<ApiResponse<DatingResponseDto>> updateDating(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id,
            @RequestBody DatingRequestDto requestDto) {
        return datingService.updateDating(userDetails.getUser(), id, requestDto);
    }

    @Operation(summary = "특정 데이트를 삭제", description = "유저 본인이 작성한 데이트를 삭제할 수 있습니다.")
    @DeleteMapping("/dating/{id}")
    public ResponseEntity<ApiResponse<DatingResponseDto>> deleteDating(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        return datingService.deleteDating(userDetails.getUser(), id);
    }

    @Operation(summary = "관리자 데이트 삭제", description = "관리자가 불건전한 데이트를 삭제할 수 있습니다.")
    @DeleteMapping("/dating/admin/{id}")
    public ResponseEntity<ApiResponse<DatingResponseDto>> AdmindeleteDating(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        UserEntity currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 어드민 권한 확인
        if (AuthorizationUtils.isAdmin(currentUser) || currentUser.getId().equals(userDetails.getUsername())) {
            datingService.deleteDating(userDetails.getUser(), id);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successMessage("데이트 계획 삭제 완료"));
        } else {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }
    }
}
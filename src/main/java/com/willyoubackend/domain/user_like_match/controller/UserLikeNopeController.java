package com.willyoubackend.domain.user_like_match.controller;

import com.willyoubackend.domain.user.security.UserDetailsImpl;
import com.willyoubackend.domain.user_like_match.dto.LikeNopeResponseDto;
import com.willyoubackend.domain.user_like_match.service.UserLikeService;
import com.willyoubackend.domain.user_like_match.service.UserNopeService;
import com.willyoubackend.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "좋아용 싫어요 보내기", description = "좋아요와 싫어요를 보내는 URL입니다.")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j(topic = "User Like Controller")
public class UserLikeNopeController {
    private final UserLikeService userLikeService;
    private final UserNopeService userNopeService;

    @Operation(summary = "좋아요 누르기", description = "유저가 다른 유저에게 좋아요를 누르게 할 수 있습니다.")
    @PostMapping("/like/{userId}")
    public ResponseEntity<ApiResponse<LikeNopeResponseDto>> createLike(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long userId) {
        return userLikeService.createLike(userDetails.getUser(), userId);

    }

    @PostMapping("/nope/{userId}")
    @Operation(summary = "싫어요 누르기", description = "유저가 다른 유저에게 싫어요를 누르게 할 수 있습니다.")
    public ResponseEntity<ApiResponse<LikeNopeResponseDto>> createNope(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long userId) {
        return userNopeService.createNope(userDetails.getUser(), userId);
    }
}
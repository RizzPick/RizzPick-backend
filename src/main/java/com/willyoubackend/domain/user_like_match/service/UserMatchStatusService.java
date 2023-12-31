package com.willyoubackend.domain.user_like_match.service;

import com.willyoubackend.domain.user.entity.UserEntity;
import com.willyoubackend.domain.user_like_match.dto.MatchResponseDto;
import com.willyoubackend.domain.user_like_match.entity.UserMatchStatus;
import com.willyoubackend.domain.user_like_match.repository.UserMatchStatusRepository;
import com.willyoubackend.domain.websocket.entity.ChatRoom;
import com.willyoubackend.domain.websocket.repository.ChatMessageRepository;
import com.willyoubackend.domain.websocket.repository.ChatRoomRepository;
import com.willyoubackend.global.dto.ApiResponse;
import com.willyoubackend.global.exception.CustomException;
import com.willyoubackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "User Match Status Service")
public class UserMatchStatusService {
    private final UserMatchStatusRepository userMatchStatusRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ResponseEntity<ApiResponse<List<MatchResponseDto>>> getMatches(UserEntity user) {
        List<UserMatchStatus> userMatchStatusList = userMatchStatusRepository.findAllByUserMatchedOneOrUserMatchedTwo(user, user);
        List<MatchResponseDto> matchResponseDtoList = new ArrayList<>();
        for (UserMatchStatus userMatchStatus : userMatchStatusList) {
            UserEntity notMe = (userMatchStatus.getUserMatchedOne().equals(user)) ? userMatchStatus.getUserMatchedOne() : userMatchStatus.getUserMatchedTwo();
            matchResponseDtoList.add(new MatchResponseDto(user.getUsername(), notMe.getUsername()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(matchResponseDtoList));
    }

    @Transactional
    public ResponseEntity<ApiResponse<MatchResponseDto>> deleteMatch(UserEntity user, Long matchId) {
        UserMatchStatus userMatchStatus = userMatchStatusRepository.findById(matchId).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_CHATROOM)
        );

        if (!(userMatchStatus.getUserMatchedTwo().getId().equals(user.getId()) || userMatchStatus.getUserMatchedOne().getId().equals(user.getId()))) {
            throw new CustomException(ErrorCode.INVALID_ARGUMENT);
        }

        String username1 = userMatchStatus.getUserMatchedOne().getUsername();
        String username2 = userMatchStatus.getUserMatchedTwo().getUsername();

        ChatRoom chatRoom = chatRoomRepository.findByUser1_UsernameAndUser2_Username(username1, username2);
        if (chatRoom != null) {
            chatMessageRepository.deleteByChatRoomId(chatRoom.getId());
            chatRoomRepository.delete(chatRoom);
        }
        userMatchStatus.setDeleteStatus(true);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successMessage("아쉽지만 다음에 더 좋은 기회가 있을거에요!"));
    }

}
package com.willyoubackend.domain.dating.service;

import com.willyoubackend.domain.dating.dto.DatingRequestDto;
import com.willyoubackend.domain.dating.dto.DatingResponseDto;
import com.willyoubackend.domain.dating.entity.Dating;
import com.willyoubackend.domain.dating.repository.DatingRepository;
import com.willyoubackend.domain.user.entity.UserEntity;
import com.willyoubackend.global.dto.ApiResponse;
import com.willyoubackend.global.exception.CustomException;
import com.willyoubackend.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Dating Service")
public class DatingService {
    private final DatingRepository datingRepository;

    public ResponseEntity<ApiResponse<DatingResponseDto>> createDating(UserEntity user) {
        Dating dating = new Dating(
                "이목을 끄는 이름을 지어주세요!",
                "어디서 만나실건가요?",
                "어떤 컨셉의 데이트인가요?");
        dating.setUser(user);
        DatingResponseDto responseDto = new DatingResponseDto(datingRepository.save(dating));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.successData(responseDto));
    }

    public ResponseEntity<ApiResponse<List<DatingResponseDto>>> getDatingList() {
        List<DatingResponseDto> datingResponseDtoList = datingRepository.findAllByOrderByCreatedAt()
                .stream()
                .map(DatingResponseDto::new)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(datingResponseDtoList));
    }

    public ResponseEntity<ApiResponse<List<DatingResponseDto>>> getDatingListByUser(UserEntity user) {
        List<DatingResponseDto> datingResponseDtoListByUser = datingRepository.findAllByUserOrderByCreatedAt(user)
                .stream()
                .map(DatingResponseDto::new)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(datingResponseDtoListByUser));
    }

    public ResponseEntity<ApiResponse<List<DatingResponseDto>>> getDatingListByLocation(String location) {
        List<DatingResponseDto> datingResponseDtoListByLocation = datingRepository.findAllByLocationOrderByCreatedAt(location)
                .stream()
                .map(DatingResponseDto::new)
                .toList();;
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(datingResponseDtoListByLocation));
    }

    public ResponseEntity<ApiResponse<DatingResponseDto>> getDatingDetail(Long id) {
        DatingResponseDto responseDto = new DatingResponseDto(findByIdDating(id));
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(responseDto));
    }

    @Transactional
    public ResponseEntity<ApiResponse<DatingResponseDto>> updateDating(UserEntity user, Long id, DatingRequestDto requestDto) {
        Dating selectedDate = findByIdDating(id);
        if (!selectedDate.getUser().getId().equals(user.getId())) throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        selectedDate.update(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successData(new DatingResponseDto(selectedDate)));
    }

    public ResponseEntity<ApiResponse<DatingResponseDto>> deleteDating(UserEntity user, Long id) {
        Dating selectedDate = findByIdDating(id);
        if (!selectedDate.getUser().getId().equals(user.getId())) throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        datingRepository.delete(selectedDate);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successMessage("삭제 되었습니다."));
    }

    private Dating findByIdDating(Long id) {
        return datingRepository.findById(id).orElseThrow(
                () ->new CustomException(ErrorCode.NOT_FOUND_ENTITY)
        );
    }
}
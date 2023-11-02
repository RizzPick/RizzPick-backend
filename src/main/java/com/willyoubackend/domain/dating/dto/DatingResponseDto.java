package com.willyoubackend.domain.dating.dto;

import com.willyoubackend.domain.dating.entity.Dating;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DatingResponseDto {
    private final Long datingId;
    private final Long userId;
    private final String datingTitle;
    private final String datingLocation;
    private final String datingTheme;
    private final LocalDateTime createdAt;

    public DatingResponseDto(Dating dating) {
        this.datingId = dating.getId();
        this.userId = dating.getUser().getId();
        this.datingTitle = dating.getTitle();
        this.datingLocation = dating.getLocation();
        this.datingTheme = dating.getTheme();
        this.createdAt = dating.getCreatedAt();
    }
}
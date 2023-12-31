package com.willyoubackend.domain.dating.entity;

import com.willyoubackend.domain.dating.dto.DatingRequestDto;
import com.willyoubackend.domain.user.entity.UserEntity;
import com.willyoubackend.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Dating extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "theme", nullable = false)
    private String theme;

    @Column(name = "delete_status", nullable = false)
    private Boolean deleteStatus;

    @Column(name = "active_status", nullable = false)
    private Boolean activeStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(mappedBy = "dating")
    private DatingImage datingImage;

    public Dating(String title, String location, String theme) {
        this.title = title;
        this.location = location;
        this.theme = theme;
        this.deleteStatus = false;
        this.activeStatus = false;
    }

    public void update(DatingRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.theme = requestDto.getTheme();
        this.location = requestDto.getLocation();
        if (!this.activeStatus) this.activeStatus = true;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void setDatingImage(DatingImage datingImage) {
        this.datingImage = datingImage;
    }

    public void setDeleteStatus(Boolean status) {
        this.deleteStatus = status;
        this.activeStatus = false;
    }
}
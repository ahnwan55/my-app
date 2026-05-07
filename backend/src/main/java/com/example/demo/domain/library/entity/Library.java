package com.example.demo.domain.library.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "libraries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Library {

    // 정보나루가 부여하는 도서관 고유 코드 (PK 직접 사용)
    @Id
    @Column(name = "library_code", length = 10)
    private String libraryCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "tel", length = 20)
    private String tel;

    // 정보나루 API 마지막 동기화 시각
    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Builder
    public Library(String libraryCode, String name, String address, String tel) {
        this.libraryCode = libraryCode;
        this.name = name;
        this.address = address;
        this.tel = tel;
        this.syncedAt = LocalDateTime.now();
    }

    public void updateSyncedAt() {
        this.syncedAt = LocalDateTime.now();
    }
}

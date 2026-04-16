package com.example.demo.domain.book.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Book - 도서 엔티티
 *
 * 도서관 정보나루 API에서 가져온 도서 정보를 저장합니다.
 * isbn13을 고유 식별자로 사용합니다.
 *
 * 기존 Product 엔티티와의 변경사항:
 *   - finPrdtCd → isbn13 (도서 고유 식별자)
 *   - korCoNm(은행명) → publisher(출판사)
 *   - finPrdtNm(상품명) → title(도서명)
 *   - productType → genre (BookGenre enum)
 *   - ProductOption 관계 제거 (도서는 옵션 없음)
 *   - joinWay, spclCnd 등 금융 필드 제거
 *   - authors, bookImageURL, description 추가
 */
@Entity
@Table(name = "book")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ISBN 13자리 (고유값)
    @Column(nullable = false, unique = true)
    private String isbn13;

    // 도서명
    @Column(nullable = false)
    private String title;

    // 저자
    @Column(nullable = false)
    private String authors;

    // 출판사
    @Column(nullable = false)
    private String publisher;

    // 출판연도
    private String publicationYear;

    // 표지 이미지 URL
    private String bookImageUrl;

    // 도서 소개 (상세 조회 시 채워짐)
    @Column(columnDefinition = "TEXT")
    private String description;

    // 장르 분류
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookGenre genre;

    // KDC 분류번호 (도서관 정보나루 class_no)
    private String classNo;

    // 현재 활성 여부
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // API에서 마지막으로 가져온 시간
    private LocalDateTime fetchedAt;
}
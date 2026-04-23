package com.example.demo.infra.library.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LibraryApiResponse - 도서관 정보나루 API 응답 DTO
 *
 * 도서관 정보나루 API는 응답 형식이 API마다 조금씩 다르지만
 * 공통적으로 response.result 안에 데이터가 들어옵니다.
 *
 * 사용 API:
 *   - 인기대출도서 조회    : GET /api/loanItemSrch
 *   - 마니아 추천도서 조회  : GET /api/recommandList (maniaType=mania)
 *   - 다독자 추천도서 조회  : GET /api/recommandList (maniaType=reader)
 *   - 대출 급상승 도서     : GET /api/loanItemSrch (ascending=true)
 *   - 도서 상세 조회       : GET /api/srchDtlList
 */
@Getter
@NoArgsConstructor
public class LibraryApiResponse {

    // 최상위 응답 래퍼
    private Response response;

    @Getter
    @NoArgsConstructor
    public static class Response {
        private Result result;
    }

    @Getter
    @NoArgsConstructor
    public static class Result {

        // 전체 결과 수
        @JsonProperty("numFound")
        private Integer numFound;

        // 도서 목록 (인기대출 / 급상승 / 추천)
        @JsonProperty("docs")
        private List<DocItem> docs;

        // 도서 상세 단건 (도서 상세 조회 API)
        @JsonProperty("detail")
        private List<DetailItem> detail;
    }

    /**
     * 도서 목록 아이템 (인기대출 / 급상승 / 추천 공통)
     *
     * doc 안에 실제 book 정보가 들어있는 구조:
     * { "docs": [ { "doc": { "bookname": "...", ... } } ] }
     */
    @Getter
    @NoArgsConstructor
    public static class DocItem {
        private BookItem doc;
    }

    /**
     * 도서 기본 정보
     */
    @Getter
    @NoArgsConstructor
    public static class BookItem {

        // ISBN (13자리 우선, 공백으로 구분된 경우 있음)
        @JsonProperty("isbn13")
        private String isbn13;

        // 도서명
        @JsonProperty("bookname")
        private String bookname;

        // 저자
        @JsonProperty("authors")
        private String authors;

        // 출판사
        @JsonProperty("publisher")
        private String publisher;

        // 출판연도
        @JsonProperty("publication_year")
        private String publicationYear;

        // 표지 이미지 URL
        @JsonProperty("bookImageURL")
        private String bookImageURL;

        // 주제분류번호 (KDC)
        @JsonProperty("class_no")
        private String classNo;

        // 주제분류명
        @JsonProperty("class_nm")
        private String classNm;

        // 대출 횟수 (인기대출 / 급상승 API에서 제공)
        @JsonProperty("loan_count")
        private String loanCount;
    }

    /**
     * 도서 상세 정보 아이템
     * 도서 상세 조회 API 전용
     */
    @Getter
    @NoArgsConstructor
    public static class DetailItem {
        private BookDetail book;
    }

    @Getter
    @NoArgsConstructor
    public static class BookDetail {

        @JsonProperty("isbn13")
        private String isbn13;

        @JsonProperty("bookname")
        private String bookname;

        @JsonProperty("authors")
        private String authors;

        @JsonProperty("publisher")
        private String publisher;

        @JsonProperty("publication_year")
        private String publicationYear;

        @JsonProperty("bookImageURL")
        private String bookImageURL;

        // 도서 소개 (상세 조회에서만 제공)
        @JsonProperty("description")
        private String description;

        @JsonProperty("class_no")
        private String classNo;

        @JsonProperty("class_nm")
        private String classNm;
    }
}
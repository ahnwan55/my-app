package com.example.demo.infra.library.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
public class LibraryApiResponse {

    /**
     * 도서 목록 (loanItemSrch API 응답)
     * <docs><doc>...</doc></docs>
     */
    @JacksonXmlElementWrapper(localName = "docs")
    @JacksonXmlProperty(localName = "doc")
    private List<BookItem> docs;

    /**
     * 도서 상세 목록 (srchDtlList API 응답)
     * <detail><book>...</book></detail>
     */
    @JacksonXmlElementWrapper(localName = "detail")
    @JacksonXmlProperty(localName = "book")
    private List<BookDetail> detail;

    /**
     * 도서관 목록 (libSrch API 응답)
     * <libs><lib>...</lib></libs>
     */
    @JacksonXmlElementWrapper(localName = "libs")
    @JacksonXmlProperty(localName = "lib")
    private List<LibItem> libs;

    /**
     * 전체 결과 수 (페이징 처리용)
     */
    @JacksonXmlProperty(localName = "numFound")
    private Integer numFound;

    /**
     * 페이지당 결과 수
     */
    @JacksonXmlProperty(localName = "resultNum")
    private Integer resultNum;

    // ── 도서 기본 정보 ──────────────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookItem {
        private String bookname;
        private String authors;
        private String publisher;
        private String publicationYear;
        private String isbn13;
        private String bookImageURL;
        private String classNo;
        private String classNm;
        private String loanCount;
        private String ranking;
    }

    // ── 도서 상세 정보 ──────────────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookDetail {
        private String bookname;
        private String authors;
        private String publisher;
        private String publicationYear;
        private String isbn13;
        private String bookImageURL;
        private String classNo;
        private String description;
    }

    // ── 도서관 정보 ─────────────────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LibItem {
        private String libCode;       // 도서관 코드
        private String libName;       // 도서관명
        private String address;       // 주소
        private String tel;           // 전화번호
        private String latitude;      // 위도
        private String longitude;     // 경도
        private String homepage;      // 홈페이지
        private String closed;        // 휴관일
        private String operatingTime; // 운영시간
    }

    // ── 장서/대출 정보 ──────────────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HoldingItem {
        private String isbn13;        // ISBN-13
        private String bookname;      // 도서명
        private String loanAvailable; // 대출 가능 여부 (Y/N)
        private String libCode;       // 도서관 코드
    }
}
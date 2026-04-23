package com.example.demo.infra.library.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * LibraryApiResponse - 정보나루 API XML 응답 파싱 DTO
 *
 * 정보나루 API는 XML로 응답하므로 XmlMapper로 파싱한다.
 * @JsonIgnoreProperties(ignoreUnknown = true): 알 수 없는 필드는 무시한다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
public class LibraryApiResponse {

    /**
     * 도서 목록 (loanItemSrch API 응답)
     * <docs><doc>...</doc></docs> 구조를 List<BookItem>으로 파싱
     */
    @JacksonXmlElementWrapper(localName = "docs")
    @JacksonXmlProperty(localName = "doc")
    private List<BookItem> docs;

    /**
     * 도서 상세 목록 (srchDtlList API 응답)
     */
    @JacksonXmlElementWrapper(localName = "detail")
    @JacksonXmlProperty(localName = "book")
    private List<BookDetail> detail;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookItem {
        // 정보나루 API 응답 필드명과 일치해야 함
        private String bookname;           // 도서명
        private String authors;            // 저자
        private String publisher;          // 출판사
        private String publicationYear;    // 출판년도
        private String isbn13;             // ISBN-13
        private String bookImageURL;       // 표지 이미지 URL
        private String classNo;            // KDC 분류번호
        private String classNm;            // KDC 분류명
        private String loanCount;          // 대출 횟수 (문자열로 수신)
        private String ranking;            // 순위
    }

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
        private String description;        // 도서 소개 (SRoBERTa 임베딩 입력값)
    }
}

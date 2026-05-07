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
    	@JacksonXmlProperty(localName = "bookname")
    	private String bookname;
    	@JacksonXmlProperty(localName = "authors")
    	private String authors;
    	@JacksonXmlProperty(localName = "publisher")
    	private String publisher;
    	@JacksonXmlProperty(localName = "publication_year")
    	private String publicationYear;
    	@JacksonXmlProperty(localName = "isbn13")
    	private String isbn13;
    	@JacksonXmlProperty(localName = "bookImageURL")
    	private String bookImageURL;
    	@JacksonXmlProperty(localName = "class_no")
    	private String classNo;
    	@JacksonXmlProperty(localName = "class_nm")
    	private String classNm;
    	@JacksonXmlProperty(localName = "loan_count")
    	private String loanCount;
    	@JacksonXmlProperty(localName = "ranking")
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
        private String libCode;
        private String libName;
        private String address;
        private String tel;
        private String latitude;
        private String longitude;
        private String homepage;
        private String closed;
        private String operatingTime;
    }
}

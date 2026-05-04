package com.example.demo.domain.book.service;

import com.example.demo.domain.book.dto.BookDto;
import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.repository.BookRepository;
import com.example.demo.infra.kakao.KakaoBookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BookService - 도서 비즈니스 로직 서비스
 *
 * 변경 사항:
 *   - BookGenre 파라미터 제거 → kdc(String) 파라미터로 교체
 *   - BookRepository 조회 메서드도 kdc 기반으로 교체
 *   - keyword 파라미터 추가 → 제목/저자 키워드 검색 지원
 *
 * @Transactional(readOnly = true):
 *   - 조회 전용 트랜잭션으로 성능 최적화
 *   - Hibernate dirty checking(변경 감지) 비활성화
 */
@Service
@RequiredArgsConstructor
public class BookService {

    private final KakaoBookClient kakaoBookClient;
    private final BookRepository bookRepository; // 필요한 경우를 위해 남겨둠

    /**
     * 도서 목록 조회
     *
     * 우선순위:
     *  1. keyword가 있으면 제목/저자 키워드 검색
     *  2. kdc가 있으면 KDC 코드 필터링
     *  3. 둘 다 없으면 전체 조회
     *
     * @param keyword 검색 키워드 (제목 또는 저자 부분 일치)
     * @param kdc     KDC 코드 앞자리 (예: "813", "840", "320")
     */
    public List<BookDto.BookResponse> getBooks(String keyword, String kdc) {
        // 1. 키워드가 있으면 카카오 도서 검색 API 호출
        if (keyword != null && !keyword.isBlank()) {
            return kakaoBookClient.search(keyword, 50);
        } 
        
        // 2. KDC 코드만 넘어온 경우 키워드로 매핑하여 카카오 API 호출
        if (kdc != null && !kdc.isBlank()) {
            String categoryKeyword = mapKdcToKeyword(kdc);
            return kakaoBookClient.search(categoryKeyword, 50);
        }

        // 3. 아무 조건이 없으면 기본 추천 키워드로 검색 (빈 화면 방지)
        return kakaoBookClient.search("베스트셀러", 50);
    }
    
    private String mapKdcToKeyword(String kdc) {
        if (kdc.startsWith("813")) return "한국소설";
        if (kdc.startsWith("840")) return "영미소설";
        if (kdc.startsWith("320")) return "경제학";
        if (kdc.startsWith("100")) return "철학";
        if (kdc.startsWith("300")) return "사회과학";
        if (kdc.startsWith("400")) return "자연과학";
        if (kdc.startsWith("500")) return "기술과학";
        if (kdc.startsWith("600")) return "예술";
        if (kdc.startsWith("800")) return "문학";
        if (kdc.startsWith("900")) return "역사";
        return "교양";
    }

    /**
     * 도서 단건 조회
     *
     * @param bookId 도서 PK (ISBN-13)
     * @throws IllegalArgumentException 도서가 없을 때
     */
    public BookDto.BookResponse getBook(String bookId) {
        // ISBN-13 (bookId)을 키워드로 카카오 API 단건 검색
        List<BookDto.BookResponse> results = kakaoBookClient.search(bookId, 1);
        
        if (results.isEmpty()) {
            // 카카오 검색 실패 시 DB에서 폴백(Fallback) 조회
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다: " + bookId));
            return BookDto.BookResponse.of(book);
        }
        
        return results.get(0);
    }
}
package com.example.demo.domain.library.repository;

import com.example.demo.domain.library.entity.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryRepository extends JpaRepository<Library, String> {

    /**
     * 도서관 이름 키워드 검색 (대소문자 무시)
     * 마이페이지 도서관 등록 시 이름으로 검색하는 용도로 사용한다.
     *
     * 예) keyword="노원" → "노원정보도서관", "노원구립도서관" 등 반환
     */
    List<Library> findByNameContainingIgnoreCase(String keyword);
}

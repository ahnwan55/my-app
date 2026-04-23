package com.example.demo.domain.library.repository;

import com.example.demo.domain.library.entity.Library;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryRepository extends JpaRepository<Library, String> {
    // libraryCode(String)가 PK이므로 JpaRepository<Library, String>
}

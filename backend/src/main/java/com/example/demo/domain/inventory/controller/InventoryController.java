
package com.example.demo.domain.inventory.controller;

import com.example.demo.auth.repository.UserRepository;
import com.example.demo.domain.inventory.dto.response.InventoryResponseDto;
import com.example.demo.domain.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 도서관 도서 재고 조회 컨트롤러
 *
 * [엔드포인트]
 *   GET /api/inventory?isbn={isbn}&libCodes={code1}&libCodes={code2}
 *
 * [인증]
 *   - 로그인한 사용자만 접근 가능 (SecurityConfig에서 authenticated 처리)
 *   - 마이페이지 등록 도서관이 자동으로 조회 대상에 포함된다.
 *
 * [userId 획득 방식]
 *   JWT subject = kakaoId(String)
 *   → UserDetails.getUsername() = kakaoId
 *   → UserRepository로 kakaoId → userId(Long) 변환
 */
@Tag(name = "Inventory", description = "도서관 도서 재고 조회 API")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    /**
     * 도서 재고 조회
     *
     * @param isbn         ISBN-13 (필수)
     * @param libCodes     추가로 조회할 도서관 코드 목록 (선택, 없으면 빈 리스트)
     * @param userDetails  인증된 사용자 정보 (username = kakaoId)
     * @return             도서관별 재고 현황 리스트
     */
    @Operation(summary = "도서 재고 조회", description = "ISBN-13 기준으로 지정 도서관의 소장 여부 및 대출 가능 여부를 조회한다.")
    @GetMapping
    public ResponseEntity<List<InventoryResponseDto>> getInventory(
            @RequestParam String isbn,
            @RequestParam(required = false, defaultValue = "") List<String> libCodes,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // UserDetails.getUsername() = kakaoId(String)
        // UserRepository로 kakaoId → userId(Long)를 조회한다.
        Long userId = null;
        if (userDetails != null) {
            Long kakaoId = Long.parseLong(userDetails.getUsername());
            userId = userRepository.findByKakaoId(kakaoId)
                    .map(user -> user.getUserId())
                    .orElse(null);
        }

        List<InventoryResponseDto> result = inventoryService.getInventory(isbn, libCodes, userId);
        return ResponseEntity.ok(result);
    }
}
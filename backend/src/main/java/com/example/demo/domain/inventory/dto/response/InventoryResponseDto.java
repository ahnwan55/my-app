package com.example.demo.domain.inventory.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 도서관 단건 재고 조회 응답 DTO
 *
 * 프론트엔드는 이 객체의 리스트를 받아 도서관별 카드 UI로 렌더링한다.
 *
 * [상태 구분]
 * - hasBook=true,  loanAvail=true  → 대출 가능   (녹색 뱃지)
 * - hasBook=true,  loanAvail=false → 대출 중      (주황 뱃지)
 * - hasBook=false                  → 미소장        (회색 뱃지)
 * - error=true                     → 조회 실패    (빨간 뱃지)
 */
@Getter
@Builder
public class InventoryResponseDto {

    /** 도서관 코드 (정보나루 기준) */
    private String libCode;

    /** 도서관 이름 (DB 조회 결과. 없으면 libCode 그대로) */
    private String libName;

    /** 조회한 ISBN-13 */
    private String isbn;

    /** 소장 여부 */
    private boolean hasBook;

    /** 대출 가능 여부 (hasBook=false이면 항상 false) */
    private boolean loanAvail;

    /**
     * 프론트에서 바로 사용할 수 있도록 상태 문자열을 계산해서 반환한다.
     * AVAILABLE / ON_LOAN / NOT_HELD / ERROR
     */
    private String status;

    /** API 호출 자체가 실패한 경우 true */
    private boolean error;

    // ── 팩토리 메서드 ────────────────────────────────────────────────

    /** 정상 조회 결과로부터 DTO 생성 */
    public static InventoryResponseDto of(
            String libCode,
            String libName,
            String isbn,
            boolean hasBook,
            boolean loanAvail
    ) {
        String status;
        if (!hasBook) {
            status = "NOT_HELD";       // 미소장
        } else if (loanAvail) {
            status = "AVAILABLE";      // 대출 가능
        } else {
            status = "ON_LOAN";        // 대출 중
        }

        return InventoryResponseDto.builder()
                .libCode(libCode)
                .libName(libName)
                .isbn(isbn)
                .hasBook(hasBook)
                .loanAvail(loanAvail)
                .status(status)
                .error(false)
                .build();
    }

    /** API 호출 실패 시 오류 응답 DTO 생성 */
    public static InventoryResponseDto error(String libCode, String isbn) {
        return InventoryResponseDto.builder()
                .libCode(libCode)
                .libName(libCode)   // 이름을 모를 수 있으므로 코드로 대체
                .isbn(isbn)
                .hasBook(false)
                .loanAvail(false)
                .status("ERROR")
                .error(true)
                .build();
    }
}

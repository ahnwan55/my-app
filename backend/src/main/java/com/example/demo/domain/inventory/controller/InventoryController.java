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

@Tag(name = "Inventory", description = "도서관 도서 재고 조회 API")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    @Operation(summary = "도서 재고 조회")
    @GetMapping
    public ResponseEntity<List<InventoryResponseDto>> getInventory(
            @RequestParam String isbn,
            @RequestParam(required = false, defaultValue = "") List<String> libCodes,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = null;
        if (userDetails != null) {
            Long kakaoId = Long.parseLong(userDetails.getUsername());
            userId = userRepository.findByKakaoId(kakaoId)
                    .map(user -> user.getUserId())
                    .orElse(null);
        }

        return ResponseEntity.ok(inventoryService.getInventory(isbn, libCodes, userId));
    }
}

package com.example.demo.domain.product.controller;

import com.example.demo.domain.product.dto.ProductDto;
import com.example.demo.domain.product.entity.ProductType;
import com.example.demo.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController — 금융 상품 API 엔드포인트
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 판매 중인 상품 목록을 반환합니다.
     *
     * GET /api/products          → 전체 상품
     * GET /api/products?type=DEPOSIT → 예금만
     * GET /api/products?type=SAVING  → 적금만
     *
     * @RequestParam(required = false): 필수가 아닌 쿼리 파라미터.
     *   파라미터가 없으면 null이 주입됩니다.
     */
    @GetMapping
    public ResponseEntity<List<ProductDto.ProductResponse>> getProducts(
            @RequestParam(required = false) ProductType type) {
        return ResponseEntity.ok(productService.getProducts(type));
    }

    /**
     * 특정 상품의 상세 정보를 반환합니다.
     *
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }
}
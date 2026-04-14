package com.example.demo.domain.product.service;

import com.example.demo.domain.product.dto.ProductDto;
import com.example.demo.domain.product.entity.Product;
import com.example.demo.domain.product.entity.ProductType;
import com.example.demo.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ProductService — 금융 상품 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 판매 중인 상품 목록을 조회합니다.
     *
     * @param type null이면 전체, DEPOSIT/SAVING이면 해당 유형만 반환
     */
    public List<ProductDto.ProductResponse> getProducts(ProductType type) {
        List<Product> products;

        if (type == null) {
            // 유형 필터 없음 → 전체 조회
            products = productRepository.findAllActiveWithOptions();
        } else {
            // 유형 필터 적용
            products = productRepository.findActiveByProductTypeWithOptions(type);
        }

        // Entity 리스트 → DTO 리스트 변환
        // stream().map().toList(): 각 Product를 ProductResponse로 변환하는 Java Stream 파이프라인
        return products.stream()
                .map(ProductDto.ProductResponse::of)
                .toList();
    }

    /**
     * 특정 상품의 상세 정보를 조회합니다.
     *
     * @param id 상품 PK
     * @throws IllegalArgumentException 상품이 없거나 비활성 상태일 때
     */
    public ProductDto.ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::getIsActive)   // 판매 중인 상품만 반환
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));

        return ProductDto.ProductResponse.of(product);
    }
}
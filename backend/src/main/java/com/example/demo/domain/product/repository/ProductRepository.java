package com.example.demo.domain.product.repository;

import com.example.demo.domain.product.entity.Product;
import com.example.demo.domain.product.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ProductRepository — 금융 상품 조회 인터페이스
 *
 * 금융감독원 API에서 받아온 상품 데이터를 조회합니다.
 * Product 1개는 여러 ProductOption(기간별 금리)을 가집니다.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 금융감독원 상품 코드(finPrdtCd)로 상품을 조회합니다.
     *
     * 사용 시나리오:
     *   - API 수집 시 이미 저장된 상품인지 중복 체크 (upsert 로직에 사용)
     *   - finPrdtCd는 금감원이 부여한 고유 코드로, 업데이트 기준이 됩니다
     */
    Optional<Product> findByFinPrdtCd(String finPrdtCd);

    /**
     * 현재 판매 중(isActive=true)인 상품을 유형별로 조회합니다.
     * ProductOption을 FETCH JOIN해서 N+1 문제를 방지합니다.
     *
     * N+1 문제란:
     *   - 상품 10개를 조회한 뒤 options를 각각 조회하면 쿼리가 1+10=11번 나감
     *   - FETCH JOIN을 쓰면 JOIN 한 번으로 상품+옵션을 한꺼번에 가져옴
     *   - DISTINCT는 JOIN 결과의 중복 Product 행을 제거하기 위해 사용
     *
     * @param productType DEPOSIT(예금) 또는 SAVING(적금)
     */
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.options " +
            "WHERE p.isActive = true AND p.productType = :productType")
    List<Product> findActiveByProductTypeWithOptions(@Param("productType") ProductType productType);

    /**
     * 판매 중인 전체 상품을 옵션과 함께 조회합니다.
     *
     * 사용 시나리오: 추천 로직에서 전체 상품 풀을 가져올 때
     */
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.options WHERE p.isActive = true")
    List<Product> findAllActiveWithOptions();
}
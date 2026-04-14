package com.example.demo.domain.survey.repository;

import com.example.demo.domain.survey.entity.SurveySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * SurveySessionRepository — 설문 세션 조회 인터페이스
 *
 * SurveySession은 "한 사용자가 특정 설문을 푸는 단위"입니다.
 * 익명 사용자는 sessionUuid(UUID)로 식별합니다.
 */
public interface SurveySessionRepository extends JpaRepository<SurveySession, Long> {

    /**
     * UUID로 세션을 조회합니다.
     *
     * 사용 시나리오:
     *   - 설문 시작 시 UUID를 프론트에서 생성해서 넘겨주면, 기존 세션이 있는지 확인
     *   - 설문 제출 시 해당 세션에 답변을 저장
     */
    Optional<SurveySession> findBySessionUuid(String sessionUuid);

    /**
     * UUID로 완료된 세션만 조회합니다.
     *
     * 사용 시나리오:
     *   - 추천 API 호출 시, 설문이 완료된 세션인지 검증 후 페르소나 정보를 사용
     *   - 미완료 세션으로 추천을 요청하면 400 에러 반환
     *
     * @Query: 메서드 네이밍이 복잡해질 때 JPQL을 직접 작성하는 방법.
     *   "s"는 SurveySession의 별칭. :uuid는 파라미터 바인딩.
     */
    @Query("SELECT s FROM SurveySession s WHERE s.sessionUuid = :uuid AND s.isCompleted = true")
    Optional<SurveySession> findCompletedByUuid(@Param("uuid") String uuid);
}
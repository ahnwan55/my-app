-- financial_product 관련 INSERT 전부 삭제
-- (외부 금융 API 실시간 연동으로 변경됨)

-- 페르소나 타입 (Entity 설계에 맞게 컬럼명 수정)
--INSERT INTO persona_type (id, name, description, risk_level, min_score, max_score)
--VALUES
--  (1, '안정 추구형', '원금 보장을 최우선으로 하는 보수적 성향', 'LOW', 0, 10),
--  (2, '균형 추구형', '수익과 안정성의 균형을 원하는 성향', 'MEDIUM', 11, 20),
--  (3, '수익 추구형', '다소 리스크를 감수하더라도 높은 수익을 원하는 성향', 'HIGH', 21, 30);

-- 설문지
--INSERT INTO survey (id, title, is_active)
--VALUES (1, '나의 재무 성향 파악하기', true);

-- 설문 문항
--INSERT INTO survey_question (id, survey_id, content, order_num, options, scores)
--VALUES
--  (1, 1, '월 소득 중 저축하는 비율은?',
--   1, '["10% 미만","10~30%","30% 이상"]', '[1,2,3]'),
--  (2, 1, '목돈이 생기면 어떻게 하시나요?',
--   2, '["안전한 예금","분산 투자","고수익 투자"]', '[1,2,3]'),
--  (3, 1, '투자 손실이 발생하면?',
--   3, '["즉시 손절","추이 관망","추가 매수"]', '[1,2,3]');
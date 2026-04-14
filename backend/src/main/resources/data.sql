-- 페르소나 타입
INSERT INTO persona_type (code, name)
SELECT code, name FROM (VALUES
  ('SAFETY_GUARD',     '철벽 수비대'),
  ('GOAL_ACHIEVER',    '목표 달성러'),
  ('RATE_OPTIMIZER',   '알뜰 최적화러'),
  ('STEADY_WORKER',    '안정 추구 직장인'),
  ('BALANCED_SPENDER', '감성 소비 저축러'),
  ('FUTURE_PLANNER',   '미래 설계자')
) AS v(code, name)
WHERE NOT EXISTS (SELECT 1 FROM persona_type WHERE persona_type.code = v.code);

-- 설문지
INSERT INTO survey (title, is_active)
SELECT '나의 재무 성향 파악하기', true
WHERE NOT EXISTS (SELECT 1 FROM survey WHERE title = '나의 재무 성향 파악하기');

-- 설문 문항
DELETE FROM survey_question WHERE survey_id = 1;

INSERT INTO survey_question (survey_id, content, order_num, options, scores)
VALUES
  (1, '월 소득 중 저축하는 비율은?',          1, '["10% 미만","10~30%","30% 이상"]',          '[1,2,3]'),
  (1, '목돈이 생기면 어떻게 하시나요?',        2, '["안전한 예금","분산 투자","고수익 투자"]', '[1,2,3]'),
  (1, '투자 손실이 발생하면?',                 3, '["즉시 손절","추이 관망","추가 매수"]',      '[1,2,3]'),
  (1, '한 달 생활비 중 저축 계획을 세우나요?', 4, '["전혀 안 세움","가끔 세움","항상 세움"]', '[1,2,3]'),
  (1, '재테크 정보를 얼마나 자주 찾아보나요?', 5, '["거의 안 봄","가끔 봄","매일 확인함"]',   '[1,2,3]'),
  (1, '저축 목표 기간은 어느 정도인가요?',     6, '["1년 이내","1~3년","3년 이상"]',           '[1,2,3]');

-- 추천 규칙 (페르소나별)
-- STEADY_WORKER: 장기 적금, 복리, 12개월 이상, 금리 3% 이상
INSERT INTO recommend_rule (persona_type_id, product_type, rate_type, min_term_months, max_term_months, min_rate, priority)
SELECT id, 'SAVING', 'M', 12, 36, 3.00, 1 FROM persona_type WHERE code = 'STEADY_WORKER';

-- SAFETY_GUARD: 단기 예금, 단리, 6개월 이상
INSERT INTO recommend_rule (persona_type_id, product_type, rate_type, min_term_months, max_term_months, min_rate, priority)
SELECT id, 'DEPOSIT', 'S', 6, 24, 3.00, 1 FROM persona_type WHERE code = 'SAFETY_GUARD';

-- RATE_OPTIMIZER: 예금/적금 모두, 단리, 우대금리 높은 것
INSERT INTO recommend_rule (persona_type_id, product_type, rate_type, min_term_months, max_term_months, min_rate, priority)
SELECT id, 'DEPOSIT', 'S', 6, 36, 3.50, 1 FROM persona_type WHERE code = 'RATE_OPTIMIZER';
INSERT INTO recommend_rule (persona_type_id, product_type, rate_type, min_term_months, max_term_months, min_rate, priority)
SELECT id, 'SAVING', 'S', 6, 36, 3.50, 2 FROM persona_type WHERE code = 'RATE_OPTIMIZER';

-- GOAL_ACHIEVER: 단기 적금, 목표 달성용
INSERT INTO recommend_rule (persona_type_id, product_type, rate_type, min_term_months, max_term_months, min_rate, priority)
SELECT id, 'SAVING', 'S', 6, 12, 3.00, 1 FROM persona_type WHERE code = 'GOAL_ACHIEVER';

-- BALANCED_SPENDER: 중기 예금
INSERT INTO recommend_rule (persona_type_id, product_type, rate_type, min_term_months, max_term_months, min_rate, priority)
SELECT id, 'DEPOSIT', 'S', 6, 12, 3.00, 1 FROM persona_type WHERE code = 'BALANCED_SPENDER';

-- FUTURE_PLANNER: 장기 복리 예금
INSERT INTO recommend_rule (persona_type_id, product_type, rate_type, min_term_months, max_term_months, min_rate, priority)
SELECT id, 'DEPOSIT', 'M', 24, 36, 3.20, 1 FROM persona_type WHERE code = 'FUTURE_PLANNER';
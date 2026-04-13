-- 페르소나 타입 (6가지 유형)
INSERT INTO persona_type (code, name)
VALUES
  ('SAFETY_GUARD',     '철벽 수비대'),
  ('GOAL_ACHIEVER',    '목표 달성러'),
  ('RATE_OPTIMIZER',   '알뜰 최적화러'),
  ('STEADY_WORKER',    '안정 추구 직장인'),
  ('BALANCED_SPENDER', '감성 소비 저축러'),
  ('FUTURE_PLANNER',   '미래 설계자');

-- 설문지
INSERT INTO survey (title, is_active)
VALUES ('나의 재무 성향 파악하기', true);

-- 설문 문항
INSERT INTO survey_question (survey_id, content, order_num, options, scores)
VALUES
  (1, '월 소득 중 저축하는 비율은?',
   1, '["10% 미만","10~30%","30% 이상"]', '[1,2,3]'),
  (1, '목돈이 생기면 어떻게 하시나요?',
   2, '["안전한 예금","분산 투자","고수익 투자"]', '[1,2,3]'),
  (1, '투자 손실이 발생하면?',
   3, '["즉시 손절","추이 관망","추가 매수"]', '[1,2,3]');
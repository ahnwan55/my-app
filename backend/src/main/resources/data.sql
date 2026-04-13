-- 금융상품 샘플 데이터 (예금/적금)
INSERT INTO financial_product (id, product_name, product_type, bank_name, interest_rate, min_period, max_period, min_amount)
VALUES
  (1, '청년 우대 정기예금', 'SAVINGS_DEPOSIT', '국민은행', 4.5, 6, 24, 100000),
  (2, '사회초년생 자유적금', 'INSTALLMENT_SAVINGS', '신한은행', 5.0, 12, 36, 10000),
  (3, '직장인 정기적금', 'INSTALLMENT_SAVINGS', '우리은행', 4.8, 6, 24, 50000);

-- 페르소나 타입 정의
INSERT INTO persona_type (id, persona_name, description, risk_level)
VALUES
  (1, '안정 추구형', '원금 보장을 최우선으로 하는 보수적 성향', 'LOW'),
  (2, '균형 추구형', '수익과 안정성의 균형을 원하는 성향', 'MEDIUM'),
  (3, '수익 추구형', '다소 리스크를 감수하더라도 높은 수익을 원하는 성향', 'HIGH');
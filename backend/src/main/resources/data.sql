-- ================================================================
-- 페르소나 타입 (독서 페르소나 세부 설계서 v2 기준 서브 12종)
-- PersonaCode enum의 name()값과 code 컬럼값이 반드시 일치해야 함
-- ================================================================
INSERT INTO persona_type (code, name, description, image_url)
SELECT code, name, description, image_url FROM (VALUES
    ('TREND_SURFER',
     '트렌드 서퍼',
     '새로 나온 힙한 분야는 다 건드려봐야 직성이 풀리는 타입. 최신성 민감도가 높고 다양한 주제를 빠르게 탐색한다.',
     NULL),
    ('POLYMATH_SEEKER',
     '박학다식형',
     '전혀 다른 두 분야를 깊게 파서 연결하는 고지능 탐험가. 분석적 깊이와 지적 확장성이 모두 높다.',
     NULL),
    ('AESTHETIC_COLLECTOR',
     '미학적 수집가',
     '책의 내용만큼이나 표지, 굿즈, 서재 배치에 진심인 타입. 다양한 장르를 심미적 관점으로 선별한다.',
     NULL),
    ('KNOWLEDGE_EDITOR',
     '지식 편집자',
     '방대한 정보를 요약하고 정리해서 남들에게 공유하는 정리의 달인. 독서 후 노션 정리가 독서만큼 중요한 타입.',
     NULL),
    ('FAST_SOLVER',
     '해결사',
     '필요한 부분만 발췌독해서 당장의 문제를 해결하는 실전파. 목차 보고 필요한 챕터만 골라 읽는다.',
     NULL),
    ('CAREER_STRATEGIST',
     '커리어 전략가',
     '자기계발을 위해 로드맵을 짜고 벽돌책도 씹어먹는 전략파. 올해 읽을 책 리스트가 연초에 이미 완성되어 있다.',
     NULL),
    ('EMOTIONAL_SYNCHRO',
     '감성 동기화형',
     '주인공에 빙의해서 밤새도록 소설을 읽으며 눈물 콧물 짜는 타입. 책을 덮고 나서도 한동안 현실로 돌아오지 못한다.',
     NULL),
    ('CASUAL_RESTER',
     '가벼운 휴식자',
     '남들 다 읽는 베스트셀러 에세이로 퇴근길 가볍게 힐링하는 타입. 독서가 취미라기보다 일상의 작은 쉼표에 가깝다.',
     NULL),
    ('COLD_CRITIC',
     '냉철한 비평가',
     '채팅방에서 논리적 허점을 찾아내고 토론하며 지적 쾌감을 느끼는 타입. 독서 후 서평 작성이 독서의 완성이라고 생각한다.',
     NULL),
    ('SILENT_RESEARCHER',
     '은둔형 연구자',
     '남들과 대화하기보다 혼자 조용히 텍스트의 이면을 파고드는 타입. 주석과 참고문헌까지 챙겨 읽는 독자.',
     NULL),
    ('CONTEMPLATIVE_MONK',
     '사유하는 수행자',
     '고전 한 권을 몇 달 동안 붙잡고 인생의 본질을 고민하는 타입. 속도보다 깊이, 정답보다 질문을 더 중시한다.',
     NULL),
    ('OBSESSIVE_FANDOM',
     '지독한 덕후',
     '한 작가의 절판된 초판본까지 싹 다 읽으며 끝을 보는 타입. 좋아하는 시리즈의 다음 권 발매일을 달력에 표시해 둔다.',
     NULL)
) AS v(code, name, description, image_url)
WHERE NOT EXISTS (
    SELECT 1 FROM persona_type WHERE persona_type.code = v.code
);
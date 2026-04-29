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

-- ──────────────────────────────────────────────────────────────
-- 도서 더미 데이터 (검색 기능 시연용)
-- 정보나루 API 호출 없이 서버 시작 시 자동 적재
-- KDC 분류: 810(한국문학), 840(영미문학), 180(철학/심리), 320(경제), 400(자연과학), 810.98(에세이)
-- INSERT IGNORE: 이미 존재하는 bookId는 건너뜀 (중복 방지)
-- ──────────────────────────────────────────────────────────────

-- ── 한국 소설 (KDC 813) ────────────────────────────────────────
INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788936433598', '채식주의자', '한강', '창비', '2007', '813.6', NULL, '평범한 가정주부 영혜가 어느 날 모든 고기를 거부하면서 벌어지는 이야기. 폭력과 욕망, 인간의 본성을 탐구한다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788954651135', '82년생 김지영', '조남주', '민음사', '2016', '813.6', NULL, '1982년생 평범한 한국 여성 김지영의 삶을 통해 한국 사회의 성차별 구조를 조명한다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788954641987', '아몬드', '손원평', '창비', '2017', '813.6', NULL, '편도체가 작아 감정을 느끼지 못하는 소년 윤재의 성장 이야기. 공감과 감정의 의미를 묻는다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788936434267', '소년이 온다', '한강', '창비', '2014', '813.6', NULL, '1980년 5월 광주를 배경으로 한 역사 소설. 죽음과 생존자들의 트라우마를 섬세하게 그린다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788932920245', '불편한 편의점', '김호연', '나무옆의자', '2021', '813.6', NULL, '서울역 노숙자 독고씨가 편의점에서 일하며 사람들과 관계를 맺어가는 따뜻한 이야기.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788954650595', '파친코', '이민진', '인플루엔셜', '2018', '813.6', NULL, '재일 조선인 4대에 걸친 가족 서사. 정체성과 차별, 생존의 의미를 묵직하게 담아낸다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788936435721', '작별하지 않는다', '한강', '문학동네', '2021', '813.6', NULL, '제주 4.3 사건을 배경으로 한 소설. 기억과 애도, 역사의 상처를 다룬다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

-- ── 외국 소설 (KDC 843) ────────────────────────────────────────
INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788937460449', '어린 왕자', '생텍쥐페리', '열린책들', '2015', '843', NULL, '사막에 불시착한 조종사와 소행성에서 온 어린 왕자의 만남. 순수함과 삶의 본질을 이야기한다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788937460068', '미드나잇 라이브러리', '매트 헤이그', '인플루엔셜', '2021', '823', NULL, '삶을 포기하려는 노라가 자정의 도서관에서 다른 삶의 가능성을 탐험한다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788937462382', '1984', '조지 오웰', '민음사', '2003', '823', NULL, '전체주의 사회의 공포를 그린 디스토피아 소설. 빅브라더와 감시 사회를 날카롭게 비판한다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788937460135', '백년의 고독', '가브리엘 가르시아 마르케스', '민음사', '2000', '863', NULL, '마콘도 마을의 부엔디아 가문 100년의 역사를 마술적 사실주의로 그린 노벨문학상 수상작.', NOW())
ON CONFLICT (book_id) DO NOTHING;

-- ── 에세이 (KDC 814) ────────────────────────────────────────────
INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9791130620677', '죽고 싶지만 떡볶이는 먹고 싶어', '백세희', '흔', '2018', '814.6', NULL, '경계성 성격장애를 가진 저자와 정신과 의사의 대화록. 우울과 일상의 공존을 솔직하게 담는다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788901270456', '나는 나로 살기로 했다', '김수현', '마음의숲', '2016', '814.6', NULL, '타인의 시선에서 벗어나 자신답게 살아가는 법에 대한 에세이.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788901220086', '어떻게 살 것인가', '유시민', '생각의길', '2013', '814.6', NULL, '역사와 철학을 넘나들며 삶의 방향에 대해 묻는 인문 에세이.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788936435745', '채식주의자의 딸', '한강', '창비', '2023', '814.6', NULL, '작가 한강의 개인적인 성찰과 문학적 여정을 담은 에세이 모음집.', NOW())
ON CONFLICT (book_id) DO NOTHING;

-- ── 인문/철학 (KDC 100) ─────────────────────────────────────────
INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788934972464', '사피엔스', '유발 하라리', '김영사', '2015', '909', NULL, '인류의 탄생부터 현재까지 역사를 조망하는 대작. 인지혁명, 농업혁명, 과학혁명을 중심으로 서술한다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788901227474', '총균쇠', '재레드 다이아몬드', '문학사상', '2005', '909', NULL, '왜 어떤 문명은 번성하고 다른 문명은 쇠락했는가. 지리와 환경이 역사를 결정한다는 주장을 펼친다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788901265322', '미움받을 용기', '기시미 이치로', '인플루엔셜', '2014', '180', NULL, '아들러 심리학을 바탕으로 타인의 시선과 과거에서 자유로워지는 법을 철학적 대화로 풀어낸다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788971906736', '정의란 무엇인가', '마이클 샌델', '김영사', '2010', '340', NULL, '공리주의, 자유주의, 공동체주의를 넘나들며 정의의 본질을 탐구하는 하버드 강의록.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788934977469', '지금 이 순간을 살아라', '에크하르트 톨레', '양문', '2008', '180', NULL, '과거와 미래에 집착하는 마음을 내려놓고 현재 순간에 깨어있는 법을 안내하는 자기계발서.', NOW())
ON CONFLICT (book_id) DO NOTHING;

-- ── 경제/경영 (KDC 320) ─────────────────────────────────────────
INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9791168811126', '트렌드 코리아 2025', '김난도 외', '미래의창', '2024', '320', NULL, '2025년 한국 소비 트렌드를 분석하고 전망하는 서울대 소비트렌드분석센터 연간 보고서.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788901257372', '부의 추월차선', '엠제이 드마코', '토트출판사', '2013', '320', NULL, '느린 부의 축적 방식에서 벗어나 빠르게 경제적 자유를 얻는 사고방식과 전략을 제시한다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788901267609', '돈의 속성', '김승호', '스노우폭스북스', '2020', '320', NULL, '돈을 대하는 올바른 태도와 철학, 부를 쌓는 원칙에 대해 이야기하는 재테크 철학서.', NOW())
ON CONFLICT (book_id) DO NOTHING;

-- ── 자연과학/기술 (KDC 400) ─────────────────────────────────────
INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788983714916', '코스모스', '칼 세이건', '사이언스북스', '2006', '440', NULL, '우주의 역사와 과학의 발전을 웅장한 서사로 풀어낸 과학 교양서의 고전.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788965700500', '이기적 유전자', '리처드 도킨스', '을유문화사', '2010', '476', NULL, '유전자 중심의 진화론을 대중적으로 설명한 책. 밈(meme) 개념을 처음 제시한 진화생물학의 고전.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788983716880', '파인만의 물리학 강의', '리처드 파인만', '승산', '2009', '420', NULL, '노벨 물리학상 수상자 파인만이 칼텍에서 진행한 전설적인 물리학 강의를 정리한 책.', NOW())
ON CONFLICT (book_id) DO NOTHING;

-- ── 판타지/SF (KDC 808) ─────────────────────────────────────────
INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788932916484', '달러구트 꿈 백화점', '이미예', '팩토리나인', '2020', '813.6', NULL, '꿈을 사고파는 백화점을 배경으로 한 판타지 소설. 따뜻하고 몽환적인 분위기가 특징이다.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788937460760', '해리포터와 마법사의 돌', 'J.K. 롤링', '문학수첩', '1999', '823', NULL, '마법사 해리 포터가 호그와트 마법 학교에 입학하면서 시작되는 판타지 시리즈 1편.', NOW())
ON CONFLICT (book_id) DO NOTHING;

INSERT INTO books (book_id, title, author, publisher, pub_year, kdc, cover_url, description, cached_at)
VALUES ('9788936435608', '우리가 빛의 속도로 갈 수 없다면', '김초엽', '허블', '2019', '813.6', NULL, '인간의 감정과 과학기술을 결합한 SF 단편집. 독창적인 세계관과 섬세한 감성으로 주목받은 작품.', NOW())
ON CONFLICT (book_id) DO NOTHING;
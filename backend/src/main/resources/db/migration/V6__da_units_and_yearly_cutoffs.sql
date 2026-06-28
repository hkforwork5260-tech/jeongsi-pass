-- ============================================================
-- V6: 다군(DA) 모집단위 보강 + 다개년 배치컷(시계열)
-- ============================================================
-- 목적
--   (1) 다군(DA)이 3개뿐 → 전략 조합(가/나/다) 다양성 확보 위해 14개 추가.
--   (2) 배치컷이 2027 단일 연도 → 2025·2026을 추가해 "연도별 추이"(LAG) 분석 가능.
-- 데이터 출처: 전부 'dummy'(현실 근사). 실데이터 도착 시 이 행만 교체하면 됨.
-- 설계 의도: cutoffs 테이블은 처음부터 (unit, year) 복합 UNIQUE로 시계열을 받도록 설계됨.
--            V6는 그 빈 시계열을 채워 윈도우 함수(LAG/RANK) 쇼케이스의 토대를 만든다.

-- ------------------------------------------------------------
-- 1) 다군(DA) 모집단위 14개 (id 77~90) — track·band를 다양하게 분산
-- ------------------------------------------------------------
INSERT INTO recruitment_units (id, university_id, department_name, admission_type, recruit_group, track, field, quota, admission_year) VALUES
    (77,  7, '미디어커뮤니케이션학부', '일반전형', 'DA', 'humanities', '인문', 25, 2027),
    (78,  8, '소프트웨어융합학과',     '일반전형', 'DA', 'natural',    '공학', 30, 2027),
    (79,  9, '융합인재학과',           '일반전형', 'DA', 'humanities', '인문', 20, 2027),
    (80, 11, '융합전공학부',           '일반전형', 'DA', 'natural',    '공학', 25, 2027),
    (81, 13, '자유전공학부',           '일반전형', 'DA', 'humanities', '인문', 30, 2027),
    (82, 14, 'AI융합학부',             '일반전형', 'DA', 'natural',    '공학', 25, 2027),
    (83, 15, '인공지능학과',           '일반전형', 'DA', 'natural',    '공학', 30, 2027),
    (84, 16, '데이터사이언스학과',     '일반전형', 'DA', 'natural',    '공학', 25, 2027),
    (85, 17, '금융공학과',             '일반전형', 'DA', 'natural',    '공학', 20, 2027),
    (86, 18, '융합보안학과',           '일반전형', 'DA', 'natural',    '공학', 25, 2027),
    (87, 19, '간호학과',               '일반전형', 'DA', 'natural',    '간호', 30, 2027),
    (88, 21, '전자공학부',             '일반전형', 'DA', 'natural',    '공학', 35, 2027),
    (89, 22, '경영학부',               '일반전형', 'DA', 'humanities', '경영', 30, 2027),
    (90, 10, '정보컴퓨터공학부',       '일반전형', 'DA', 'natural',    '공학', 30, 2027);

-- ------------------------------------------------------------
-- 2) 신규 모집단위 환산규칙 (V4와 동일 패턴: track/field별 반영비율·지정과목)
-- ------------------------------------------------------------
INSERT INTO score_rules (recruitment_unit_id, weight_korean, weight_math, weight_english, weight_inquiry, math_required, inquiry_required, english_grade_score, base_score)
SELECT
    u.id,
    CASE WHEN u.track = 'humanities' THEN 30 ELSE 20 END,
    CASE WHEN u.track = 'humanities' THEN 25 ELSE 35 END,
    20, 25,
    CASE WHEN u.track = 'natural' AND u.field IN ('의학','공학') THEN 'calculus_geometry' ELSE 'any' END,
    CASE WHEN u.track = 'natural' AND u.field = '의학' THEN 'science' ELSE 'any' END,
    '{"1":100,"2":98,"3":94,"4":88,"5":80,"6":70,"7":58,"8":44,"9":28}'::jsonb,
    100
FROM recruitment_units u WHERE u.id BETWEEN 77 AND 90;

-- ------------------------------------------------------------
-- 3) 신규 모집단위 2027 배치컷 (band 다양화 → 안정~위험 라벨 분포)
-- ------------------------------------------------------------
INSERT INTO cutoffs (recruitment_unit_id, year, cut_percentile, competition_rate, applicant_avg, target_avg, source) VALUES
    (77, 2027, 90.5, 9.20, 91.2, 92.3, 'dummy'),
    (78, 2027, 89.0, 8.10, 89.8, 91.0, 'dummy'),
    (79, 2027, 87.5, 6.40, 88.2, 89.5, 'dummy'),
    (80, 2027, 88.5, 7.20, 89.1, 90.4, 'dummy'),
    (81, 2027, 88.0, 6.90, 88.7, 90.0, 'dummy'),
    (82, 2027, 85.5, 5.80, 86.3, 87.8, 'dummy'),
    (83, 2027, 84.5, 5.30, 85.2, 86.9, 'dummy'),
    (84, 2027, 83.5, 4.90, 84.3, 86.0, 'dummy'),
    (85, 2027, 82.0, 4.50, 82.9, 84.7, 'dummy'),
    (86, 2027, 78.5, 3.80, 79.4, 81.6, 'dummy'),
    (87, 2027, 85.0, 6.10, 85.8, 87.3, 'dummy'),
    (88, 2027, 84.0, 5.50, 84.8, 86.5, 'dummy'),
    (89, 2027, 80.5, 4.20, 81.3, 83.4, 'dummy'),
    (90, 2027, 86.5, 6.70, 87.2, 88.6, 'dummy');

-- ------------------------------------------------------------
-- 4) 다개년 배치컷 파생 — 2027 기준으로 과거연도(2026 → 2025)를 추세 변동으로 생성
--    단위별 추세를 (id 기반)으로 다르게: 상승/하락/소폭상승/보합.
--    INSERT...SELECT 자체가 "기준 연도에서 시계열을 파생"하는 SQL 예시.
--    delta 의미: 과거가 현재보다 얼마나 낮았나(+면 상승추세, -면 과거가 더 높음=하락추세).
-- ------------------------------------------------------------
INSERT INTO cutoffs (recruitment_unit_id, year, cut_percentile, competition_rate, applicant_avg, target_avg, source)
SELECT recruitment_unit_id, 2026,
       GREATEST(40, LEAST(99.9,
           cut_percentile - (CASE recruitment_unit_id % 4
                                 WHEN 0 THEN 0.7   -- 상승 추세
                                 WHEN 1 THEN -0.4  -- 하락 추세(과거가 더 높음)
                                 WHEN 2 THEN 0.3   -- 소폭 상승
                                 ELSE 0.0 END))),  -- 보합
       ROUND(competition_rate * 0.96, 2),
       applicant_avg - 0.3,
       target_avg - 0.3,
       'dummy'
FROM cutoffs WHERE year = 2027;

INSERT INTO cutoffs (recruitment_unit_id, year, cut_percentile, competition_rate, applicant_avg, target_avg, source)
SELECT recruitment_unit_id, 2025,
       GREATEST(40, LEAST(99.9,
           cut_percentile - (CASE recruitment_unit_id % 4
                                 WHEN 0 THEN 0.7
                                 WHEN 1 THEN -0.4
                                 WHEN 2 THEN 0.3
                                 ELSE 0.0 END))),
       ROUND(competition_rate * 0.96, 2),
       applicant_avg - 0.3,
       target_avg - 0.3,
       'dummy'
FROM cutoffs WHERE year = 2026;

-- ------------------------------------------------------------
-- 5) 시퀀스 동기화 (명시적 id INSERT 후 필수 — FK 안정성)
-- ------------------------------------------------------------
SELECT setval('recruitment_units_id_seq', (SELECT MAX(id) FROM recruitment_units));
SELECT setval('cutoffs_id_seq',           (SELECT MAX(id) FROM cutoffs));
SELECT setval('score_rules_id_seq',       (SELECT MAX(id) FROM score_rules));

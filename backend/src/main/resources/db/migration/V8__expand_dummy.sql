-- ============================================================
-- V8: 전체 더미 보강 — 대학별 공통 학과를 가/나/다에 추가
-- ============================================================
-- 목적: 모든 성적대에서 군별(가/나/다) 선택지가 충분하도록.
--   대학(28) × 공통 학과 템플릿(가2·나2·다2)을 추가(기존 중복 제외).
--   배치컷은 V7 티어 공식(대학base + 학과가산 + 미세분산)으로 매겨 일관성 유지. 전부 placeholder.

-- 1) 모집단위 추가 (id는 BIGSERIAL 자동, 기존 (대학,학과) 중복 제외)
INSERT INTO recruitment_units (university_id, department_name, admission_type, recruit_group, track, field, quota, admission_year)
SELECT u.id, t.dept, '일반전형', t.grp, t.track, t.field, t.quota, 2027
FROM universities u
CROSS JOIN (VALUES
    ('컴퓨터공학과',     'GA', 'natural',    '공학', 35),
    ('경영학과',         'GA', 'humanities', '경영', 40),
    ('행정학과',         'NA', 'humanities', '사회', 30),
    ('영어영문학과',     'NA', 'humanities', '인문', 25),
    ('데이터사이언스학과','DA', 'natural',    '공학', 25),
    ('생명과학과',       'DA', 'natural',    '자연', 20)
) AS t(dept, grp, track, field, quota)
WHERE NOT EXISTS (
    SELECT 1 FROM recruitment_units r WHERE r.university_id = u.id AND r.department_name = t.dept
);

-- 2) 환산규칙 (score_rule 없는 신규 모집단위만)
INSERT INTO score_rules (recruitment_unit_id, weight_korean, weight_math, weight_english, weight_inquiry, math_required, inquiry_required, english_grade_score, base_score)
SELECT u.id,
    CASE WHEN u.track = 'humanities' THEN 30 ELSE 20 END,
    CASE WHEN u.track = 'humanities' THEN 25 ELSE 35 END,
    20, 25,
    CASE WHEN u.track = 'natural' AND u.field IN ('의학','공학') THEN 'calculus_geometry' ELSE 'any' END,
    CASE WHEN u.track = 'natural' AND u.field = '의학' THEN 'science' ELSE 'any' END,
    '{"1":100,"2":98,"3":94,"4":88,"5":80,"6":70,"7":58,"8":44,"9":28}'::jsonb,
    100
FROM recruitment_units u
WHERE NOT EXISTS (SELECT 1 FROM score_rules sr WHERE sr.recruitment_unit_id = u.id);

-- 3) 2027 배치컷 (V7 티어 공식, cutoff 없는 신규만)
INSERT INTO cutoffs (recruitment_unit_id, year, cut_percentile, competition_rate, applicant_avg, target_avg, source)
SELECT ru.id, 2027,
    GREATEST(45, LEAST(99.5,
        (CASE ru.university_id
            WHEN 1 THEN 92 WHEN 2 THEN 89 WHEN 3 THEN 89 WHEN 4 THEN 87 WHEN 5 THEN 87
            WHEN 6 THEN 86 WHEN 7 THEN 86 WHEN 8 THEN 86 WHEN 9 THEN 83 WHEN 11 THEN 84
            WHEN 12 THEN 83 WHEN 13 THEN 83 WHEN 14 THEN 82 WHEN 15 THEN 81 WHEN 16 THEN 81
            WHEN 17 THEN 80 WHEN 10 THEN 80 WHEN 21 THEN 79 WHEN 22 THEN 78 WHEN 19 THEN 78
            WHEN 18 THEN 77 WHEN 23 THEN 76 WHEN 24 THEN 75 WHEN 25 THEN 76 WHEN 26 THEN 75
            WHEN 27 THEN 76 WHEN 28 THEN 74 WHEN 20 THEN 73 ELSE 78 END)
        + (CASE
            WHEN ru.department_name LIKE '%의예%' OR ru.department_name LIKE '%치의%'
              OR ru.department_name LIKE '%한의%' OR ru.department_name LIKE '%수의%' THEN 8
            WHEN ru.department_name LIKE '%약학%' THEN 6
            WHEN ru.department_name LIKE '%간호%' THEN 2
            WHEN ru.department_name LIKE '%컴퓨터%' OR ru.department_name LIKE '%인공지능%'
              OR ru.department_name LIKE '%AI%' OR ru.department_name LIKE '%반도체%'
              OR ru.department_name LIKE '%소프트%' OR ru.department_name LIKE '%데이터%' THEN 3
            WHEN ru.department_name LIKE '%경영%' OR ru.department_name LIKE '%경제%' THEN 2
            ELSE 0 END)
        + ((ru.id % 5) * 0.4 - 0.8)
    )),
    ROUND(4 + (ru.id % 60) / 10.0, 2),
    NULL, NULL, 'dummy'
FROM recruitment_units ru
WHERE NOT EXISTS (SELECT 1 FROM cutoffs c WHERE c.recruitment_unit_id = ru.id AND c.year = 2027);

-- 4) 지원자평균·목표등록자평균 정합(방금 추가분만 NULL 상태)
UPDATE cutoffs SET
    applicant_avg = LEAST(99.8, cut_percentile + 0.8),
    target_avg    = LEAST(99.9, cut_percentile + 2.0)
WHERE applicant_avg IS NULL OR target_avg IS NULL;

-- 5) 과거연도(2026·2025) 파생 — 없는 신규분만
INSERT INTO cutoffs (recruitment_unit_id, year, cut_percentile, competition_rate, applicant_avg, target_avg, source)
SELECT c27.recruitment_unit_id, 2026,
    GREATEST(45, LEAST(99.5, c27.cut_percentile - (CASE c27.recruitment_unit_id % 4 WHEN 0 THEN 0.7 WHEN 1 THEN -0.4 WHEN 2 THEN 0.3 ELSE 0.0 END))),
    ROUND(c27.competition_rate * 0.96, 2), c27.applicant_avg - 0.3, c27.target_avg - 0.3, 'dummy'
FROM cutoffs c27
WHERE c27.year = 2027 AND NOT EXISTS (SELECT 1 FROM cutoffs c WHERE c.recruitment_unit_id = c27.recruitment_unit_id AND c.year = 2026);

INSERT INTO cutoffs (recruitment_unit_id, year, cut_percentile, competition_rate, applicant_avg, target_avg, source)
SELECT c26.recruitment_unit_id, 2025,
    GREATEST(45, LEAST(99.5, c26.cut_percentile - (CASE c26.recruitment_unit_id % 4 WHEN 0 THEN 0.7 WHEN 1 THEN -0.4 WHEN 2 THEN 0.3 ELSE 0.0 END))),
    ROUND(c26.competition_rate * 0.96, 2), c26.applicant_avg - 0.3, c26.target_avg - 0.3, 'dummy'
FROM cutoffs c26
WHERE c26.year = 2026 AND NOT EXISTS (SELECT 1 FROM cutoffs c WHERE c.recruitment_unit_id = c26.recruitment_unit_id AND c.year = 2025);

-- 6) 시퀀스 동기화
SELECT setval('recruitment_units_id_seq', (SELECT MAX(id) FROM recruitment_units));
SELECT setval('cutoffs_id_seq',           (SELECT MAX(id) FROM cutoffs));
SELECT setval('score_rules_id_seq',       (SELECT MAX(id) FROM score_rules));

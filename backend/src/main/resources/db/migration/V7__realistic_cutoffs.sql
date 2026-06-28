-- ============================================================
-- V7: 더미 배치컷 현실 티어 보정 (여전히 placeholder, 실데이터 아님)
-- ============================================================
-- 목적: 상위권 학생에게 지방 하위대학이 "소신"으로 뜨는 등 비현실적 분포 해소.
--   대학 티어 base(백분위) + 학과 가산(의약/이공/경상) + 미세 분산.
--   실데이터 도착 시 이 값들을 메가 실배치컷으로 교체(source='dummy' 유지).

-- 1) 2027 배치컷 재설정 (대학 티어 + 학과 가산 + id 기반 미세 분산)
UPDATE cutoffs SET cut_percentile = GREATEST(45, LEAST(99.5,
    (CASE ru.university_id
        WHEN 1  THEN 92                                   -- 서울대
        WHEN 2  THEN 89 WHEN 3  THEN 89                   -- 연세·고려
        WHEN 4  THEN 87 WHEN 5  THEN 87 WHEN 6 THEN 86
        WHEN 7  THEN 86 WHEN 8  THEN 86                   -- 성균관·한양·서강·중앙·경희
        WHEN 9  THEN 83 WHEN 11 THEN 84 WHEN 12 THEN 83
        WHEN 13 THEN 83 WHEN 14 THEN 82                   -- 건국·시립·이화·외대·동국
        WHEN 15 THEN 81 WHEN 16 THEN 81 WHEN 17 THEN 80   -- 세종·인하·아주
        WHEN 10 THEN 80 WHEN 21 THEN 79 WHEN 22 THEN 78
        WHEN 19 THEN 78                                   -- 부산·경북·전남·충남(거점국립)
        WHEN 18 THEN 77                                   -- 경기
        WHEN 23 THEN 76 WHEN 24 THEN 75 WHEN 25 THEN 76
        WHEN 26 THEN 75 WHEN 27 THEN 76 WHEN 28 THEN 74   -- 강원·충북·전북·경상·울산·제주
        WHEN 20 THEN 73                                   -- 한밭
        ELSE 78 END)
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
    + ((cutoffs.recruitment_unit_id % 5) * 0.4 - 0.8)   -- −0.8 ~ +0.8 미세 분산
))
FROM recruitment_units ru
WHERE ru.id = cutoffs.recruitment_unit_id AND cutoffs.year = 2027;

-- 2) 과거연도(2026·2025) 새 2027 기준으로 재파생 (단위별 추세 유지)
UPDATE cutoffs c26 SET cut_percentile = GREATEST(45, LEAST(99.5,
    c27.cut_percentile - (CASE c26.recruitment_unit_id % 4 WHEN 0 THEN 0.7 WHEN 1 THEN -0.4 WHEN 2 THEN 0.3 ELSE 0.0 END)))
FROM cutoffs c27
WHERE c27.recruitment_unit_id = c26.recruitment_unit_id AND c27.year = 2027 AND c26.year = 2026;

UPDATE cutoffs c25 SET cut_percentile = GREATEST(45, LEAST(99.5,
    c26.cut_percentile - (CASE c25.recruitment_unit_id % 4 WHEN 0 THEN 0.7 WHEN 1 THEN -0.4 WHEN 2 THEN 0.3 ELSE 0.0 END)))
FROM cutoffs c26
WHERE c26.recruitment_unit_id = c25.recruitment_unit_id AND c26.year = 2026 AND c25.year = 2025;

-- 3) 지원자평균·목표등록자평균을 새 배치컷에 맞춰 정합화
UPDATE cutoffs SET
    applicant_avg = LEAST(99.8, cut_percentile + 0.8),
    target_avg    = LEAST(99.9, cut_percentile + 2.0);

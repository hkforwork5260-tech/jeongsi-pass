# 합격각 — API 계약 (FE ↔ BE 공유)

- Base: `http://localhost:8080/api/v1` (로컬). JSON, `snake_case`.
- 인증: 익명 기기. 헤더 `X-Device-Id: <앱이 만든 UUID>`.
  - 성적/모의지원/관심/리포트: **필수**. 검색/상세/discover/홈: **선택**(없으면 합격 라벨 미계산).

## 성적
| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/scores` | 성적 입력·수정(upsert). body=ScoreDto → ScoreDto |
| GET | `/scores` | 내 성적 조회 (없으면 204) |
| GET | `/scores/analysis` | 성적 분석(평균 백분위·유리계열·상위%) → AnalysisDto |

**ScoreDto**: `korean_subject`(speech_writing/language_media) `korean_std/pct/grade`,
`math_subject`(probability/calculus/geometry) `math_std/pct/grade`, `english_grade`, `history_grade`,
`inquiry1_subject/type(social|science)/std/pct/grade`, `inquiry2_*`.

## 모집단위 (검색·상세·찾아보기·추천)
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/units/search` | 필터: `group`(GA/NA/DA) `track`(humanities/natural) `region` `q` `min_cut` `max_cut` → [UnitCardDto] |
| GET | `/units/{id}` | 상세(반영비율·소개 포함) → UnitDetailDto |
| GET | `/units/recommend` | 합격 라벨별 묶음 `{SAFE:[],MODERATE:[],REACH:[],HARD:[]}` |
| GET | `/discover?limit=30` | ★ 인스타 피드(점수 기반 랭킹) → [UnitCardDto] |

**UnitCardDto** 핵심: `unit_id`, `university{name,region,logo_url}`, `department_name`, `admission_type`,
`recruit_group(+_name)`, `track(+_name)`, `quota`, `competition_rate`, `applicant_avg`,
`admission{converted_score, cut_percentile, diff, label_code(SAFE/MODERATE/REACH/HARD),
label_name(안정/적정/소신/상향), probability, eligible, eligible_reason}`, `is_favorited`, `is_mock_applied`.

## 모의지원 / 관심 / 리포트
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET/POST/DELETE | `/mock-applications` · `/mock-applications/{unitId}` | 목록 / 담기 / 빼기 |
| GET/POST/DELETE | `/favorites` · `/favorites/{unitId}` | 목록 / 추가 / 삭제 |
| GET | `/report` | 합격예측 리포트(군별 가/나/다 묶음 + 요약) → ReportDto |

## 집계 / 홈
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/stats/popular` | 성적대별 인기 TOP5(SQL 집계). `band`(high/upper/mid/low) `track` `group` `limit` |
| GET | `/home` | 홈 묶음: `admission_year`, `d_day`, `has_score`, `analysis`, `hero_units`, `group_recommend`, `popular_top5` |

## 합격 라벨 (의미색 고정)
`SAFE` 안정(초록) · `MODERATE` 적정(블루) · `REACH` 소신(주황) · `HARD` 상향(빨강).
판정 = 내 환산점수 − 배치컷 백분위. 확률 = 로지스틱(diff). **placeholder — 실모델 도착 시 ScoringService만 교체.**

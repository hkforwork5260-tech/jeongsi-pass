package com.jeongsi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeongsi.app.data.Repository
import com.jeongsi.app.data.ScoreModel
import com.jeongsi.app.ui.components.HiFiButton
import com.jeongsi.app.ui.components.HiFiChip
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import com.jeongsi.app.ui.theme.cardShadow
import kotlinx.coroutines.launch

class ScoreViewModel : ViewModel() {
    var saving by mutableStateOf(false); private set
    private val _initial = MutableStateFlow<ScoreModel?>(null)
    val initial: StateFlow<ScoreModel?> = _initial

    fun load() {
        viewModelScope.launch {
            // 기존 성적이 있으면 불러와 수정, 없으면 빈 폼
            _initial.value = runCatching { Repository.getScore() }.getOrNull() ?: ScoreModel()
        }
    }

    fun save(score: ScoreModel, onDone: () -> Unit) {
        saving = true
        viewModelScope.launch {
            runCatching { Repository.saveScore(score) }
            saving = false
            onDone()
        }
    }
}

@Composable
fun ScoreInputScreen(onSaved: () -> Unit, onBack: () -> Unit, vm: ScoreViewModel = viewModel()) {
    val initial by vm.initial.collectAsStateCompat()
    LaunchedEffect(Unit) { vm.load() }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("성적 입력 · 수정", onBack)
        val init = initial
        if (init == null) CenterText("불러오는 중…") else ScoreForm(init, vm, onSaved)
    }
}

@Composable
private fun ScoreForm(init: ScoreModel, vm: ScoreViewModel, onSaved: () -> Unit) {
    fun s(v: Int?) = v?.toString() ?: ""
    // 국어
    var korSub by remember { mutableStateOf(init.koreanSubject ?: "language_media") }
    var korCommon by remember { mutableStateOf(s(init.koreanCommon)) }
    var korSelect by remember { mutableStateOf(s(init.koreanSelect)) }
    var korStd by remember { mutableStateOf(s(init.koreanStd)) }
    var korPct by remember { mutableStateOf(s(init.koreanPct)) }
    var korGrade by remember { mutableStateOf(s(init.koreanGrade)) }
    // 수학
    var mathSub by remember { mutableStateOf(init.mathSubject ?: "calculus") }
    var mathCommon by remember { mutableStateOf(s(init.mathCommon)) }
    var mathSelect by remember { mutableStateOf(s(init.mathSelect)) }
    var mathStd by remember { mutableStateOf(s(init.mathStd)) }
    var mathPct by remember { mutableStateOf(s(init.mathPct)) }
    var mathGrade by remember { mutableStateOf(s(init.mathGrade)) }
    // 영어/한국사
    var engRaw by remember { mutableStateOf(s(init.englishRaw)) }
    var engGrade by remember { mutableStateOf(s(init.englishGrade)) }
    var hisRaw by remember { mutableStateOf(s(init.historyRaw)) }
    var hisGrade by remember { mutableStateOf(s(init.historyGrade)) }
    // 탐구1/2 (과목 선택 + 원점수·표준·백분위·등급)
    var inq1Sub by remember { mutableStateOf(init.inquiry1Subject ?: "") }
    var inq1Type by remember { mutableStateOf(init.inquiry1Type ?: "") }
    var inq1Raw by remember { mutableStateOf(s(init.inquiry1Raw)) }
    var inq1Std by remember { mutableStateOf(s(init.inquiry1Std)) }
    var inq1Pct by remember { mutableStateOf(s(init.inquiry1Pct)) }
    var inq1Grade by remember { mutableStateOf(s(init.inquiry1Grade)) }
    var inq2Sub by remember { mutableStateOf(init.inquiry2Subject ?: "") }
    var inq2Type by remember { mutableStateOf(init.inquiry2Type ?: "") }
    var inq2Raw by remember { mutableStateOf(s(init.inquiry2Raw)) }
    var inq2Std by remember { mutableStateOf(s(init.inquiry2Std)) }
    var inq2Pct by remember { mutableStateOf(s(init.inquiry2Pct)) }
    var inq2Grade by remember { mutableStateOf(s(init.inquiry2Grade)) }

    var showWarn by remember { mutableStateOf(false) }

    ScrollColumn {
        Text("성적 입력", style = HiFiType.display, color = HiFiColors.Text)
        Text("표준점수·백분위·등급을 입력하면 합격 분석이 계산됩니다.", style = HiFiType.caption,
            color = HiFiColors.Text2, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
        Box(
            Modifier.fillMaxWidth().background(HiFiColors.BrandSoft, RoundedCornerShape(12.dp)).padding(12.dp),
        ) {
            Text(
                "📌 합격각·지원가능 학과는 백분위 기준이에요. 국·수 백분위 + 영어 등급 + 탐구 백분위를 입력하세요. 표준점수만으론 합격 판정이 안 됩니다.",
                style = HiFiType.caption, color = HiFiColors.BrandDark,
            )
        }

        AreaCard("국어") {
            ChipRow(listOf("speech_writing" to "화법과작문", "language_media" to "언어와매체"), korSub) { korSub = it }
            RawRow(korCommon, { korCommon = it }, korSelect, { korSelect = it })
            ScoreRow(korStd, { korStd = it }, korPct, { korPct = it }, korGrade, { korGrade = it })
        }
        AreaCard("수학") {
            ChipRow(listOf("probability" to "확률과통계", "calculus" to "미적분", "geometry" to "기하"), mathSub) { mathSub = it }
            RawRow(mathCommon, { mathCommon = it }, mathSelect, { mathSelect = it })
            ScoreRow(mathStd, { mathStd = it }, mathPct, { mathPct = it }, mathGrade, { mathGrade = it })
        }
        AreaCard("영어 (절대평가)") {
            RawAutoGradeRow(engRaw, onRaw = { engRaw = it; engGrade = engGradeFromRaw(it) }, grade = engGrade)
        }
        AreaCard("한국사 (절대평가)") {
            RawAutoGradeRow(hisRaw, onRaw = { hisRaw = it; hisGrade = historyGradeFromRaw(it) }, grade = hisGrade)
        }
        AreaCard("탐구 1") {
            SubjectPicker(inq1Sub) { sub, type -> inq1Sub = sub; inq1Type = type }
            RawStdRow(inq1Raw, { inq1Raw = it }, inq1Std, { inq1Std = it }, inq1Pct, { inq1Pct = it }, inq1Grade, { inq1Grade = it })
        }
        AreaCard("탐구 2") {
            SubjectPicker(inq2Sub) { sub, type -> inq2Sub = sub; inq2Type = type }
            RawStdRow(inq2Raw, { inq2Raw = it }, inq2Std, { inq2Std = it }, inq2Pct, { inq2Pct = it }, inq2Grade, { inq2Grade = it })
        }

        val submit = {
            vm.save(
                ScoreModel(
                    koreanSubject = korSub, koreanCommon = korCommon.toIntOrNull(), koreanSelect = korSelect.toIntOrNull(),
                    koreanStd = korStd.toIntOrNull(), koreanPct = korPct.toIntOrNull(), koreanGrade = korGrade.toIntOrNull(),
                    mathSubject = mathSub, mathCommon = mathCommon.toIntOrNull(), mathSelect = mathSelect.toIntOrNull(),
                    mathStd = mathStd.toIntOrNull(), mathPct = mathPct.toIntOrNull(), mathGrade = mathGrade.toIntOrNull(),
                    englishRaw = engRaw.toIntOrNull(), englishGrade = engGrade.toIntOrNull(),
                    historyRaw = hisRaw.toIntOrNull(), historyGrade = hisGrade.toIntOrNull(),
                    inquiry1Subject = inq1Sub.ifBlank { null }, inquiry1Type = inq1Type.ifBlank { null },
                    inquiry1Raw = inq1Raw.toIntOrNull(), inquiry1Std = inq1Std.toIntOrNull(), inquiry1Pct = inq1Pct.toIntOrNull(), inquiry1Grade = inq1Grade.toIntOrNull(),
                    inquiry2Subject = inq2Sub.ifBlank { null }, inquiry2Type = inq2Type.ifBlank { null },
                    inquiry2Raw = inq2Raw.toIntOrNull(), inquiry2Std = inq2Std.toIntOrNull(), inquiry2Pct = inq2Pct.toIntOrNull(), inquiry2Grade = inq2Grade.toIntOrNull(),
                ),
                onDone = onSaved,
            )
        }

        Box(Modifier.padding(top = 18.dp, bottom = 24.dp)) {
            HiFiButton(
                if (vm.saving) "저장 중…" else "저장하고 합격각 보기",
                onClick = {
                    // 합격 판정에 필요한 최소값: 백분위(국·수·탐) 또는 영어 등급
                    val hasAnalyzable = listOf(korPct, mathPct, inq1Pct, inq2Pct).any { it.toIntOrNull() != null } ||
                        engGrade.toIntOrNull() != null
                    if (hasAnalyzable) submit() else showWarn = true
                },
                fullWidth = true,
                enabled = !vm.saving,
            )
        }

        if (showWarn) {
            AlertDialog(
                onDismissRequest = { showWarn = false },
                confirmButton = { TextButton(onClick = { showWarn = false }) { Text("입력하러 가기") } },
                dismissButton = { TextButton(onClick = { showWarn = false; submit() }) { Text("그래도 저장") } },
                title = { Text("백분위가 필요해요") },
                text = {
                    Text("합격 분석·지원가능 학과는 백분위 기준이라, 표준점수만으론 합격각이 안 나와요.\n\n국·수 백분위 / 영어 등급 / 탐구 백분위 중 하나 이상을 입력해 주세요.")
                },
            )
        }
    }
}

@Composable
private fun AreaCard(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 12.dp)
            .cardShadow(RoundedCornerShape(16.dp), elevation = 5.dp)
            .background(HiFiColors.Bg2, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Text(title, style = HiFiType.title, color = HiFiColors.Text, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
private fun ChipRow(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { (code, label) ->
            HiFiChip(text = label, selected = selected == code, small = true, onClick = { onSelect(code) })
        }
    }
}

@Composable
private fun RawRow(common: String, onCommon: (String) -> Unit, select: String, onSelect: (String) -> Unit) {
    Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumBox("원점수(공통)", common, onCommon, Modifier.weight(1f))
        NumBox("원점수(선택)", select, onSelect, Modifier.weight(1f))
    }
}

@Composable
private fun ScoreRow(
    std: String, onStd: (String) -> Unit,
    pct: String, onPct: (String) -> Unit,
    grade: String, onGrade: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumBox("표준", std, onStd, Modifier.weight(1f))
        NumBox("백분위", pct, onPct, Modifier.weight(1f))
        NumBox("등급", grade, onGrade, Modifier.weight(1f))
    }
}

/** 원점수·표준·백분위·등급 4칸 (탐구용). */
@Composable
private fun RawStdRow(
    raw: String, onRaw: (String) -> Unit, std: String, onStd: (String) -> Unit,
    pct: String, onPct: (String) -> Unit, grade: String, onGrade: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        NumBox("원점수", raw, onRaw, Modifier.weight(1f))
        NumBox("표준", std, onStd, Modifier.weight(1f))
        NumBox("백분위", pct, onPct, Modifier.weight(1f))
        NumBox("등급", grade, onGrade, Modifier.weight(1f))
    }
}

/** 원점수·등급 2칸 (영어·한국사 절대평가용). */
@Composable
private fun RawGradeRow(raw: String, onRaw: (String) -> Unit, grade: String, onGrade: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumBox("원점수", raw, onRaw, Modifier.weight(1f))
        NumBox("등급", grade, onGrade, Modifier.weight(1f))
    }
}

/** 절대평가(영어·한국사) — 원점수 입력 시 등급 자동 산출(읽기전용 표시). */
@Composable
private fun RawAutoGradeRow(raw: String, onRaw: (String) -> Unit, grade: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumBox("원점수", raw, onRaw, Modifier.weight(1f))
        Column(Modifier.weight(1f)) {
            Text("등급 (자동)", style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(bottom = 2.dp))
            Box(
                Modifier.fillMaxWidth().background(HiFiColors.Bg3, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            ) {
                Text(grade.ifBlank { "-" }, style = HiFiType.body, color = if (grade.isBlank()) HiFiColors.Text3 else HiFiColors.Brand)
            }
        }
    }
}

/** 영어 절대평가 등급 (100점 만점, 10점 간격). */
private fun engGradeFromRaw(raw: String): String {
    val r = raw.toIntOrNull() ?: return ""
    return when {
        r >= 90 -> "1"; r >= 80 -> "2"; r >= 70 -> "3"; r >= 60 -> "4"; r >= 50 -> "5"
        r >= 40 -> "6"; r >= 30 -> "7"; r >= 20 -> "8"; else -> "9"
    }
}

/** 한국사 절대평가 등급 (50점 만점, 5점 간격). */
private fun historyGradeFromRaw(raw: String): String {
    val r = raw.toIntOrNull() ?: return ""
    return when {
        r >= 40 -> "1"; r >= 35 -> "2"; r >= 30 -> "3"; r >= 25 -> "4"; r >= 20 -> "5"
        r >= 15 -> "6"; r >= 10 -> "7"; r >= 5 -> "8"; else -> "9"
    }
}

private val SOCIAL_SUBJECTS = listOf("생활과윤리", "윤리와사상", "한국지리", "세계지리", "동아시아사", "세계사", "정치와법", "경제", "사회문화")
private val SCIENCE_SUBJECTS = listOf("물리학Ⅰ", "화학Ⅰ", "생명과학Ⅰ", "지구과학Ⅰ", "물리학Ⅱ", "화학Ⅱ", "생명과학Ⅱ", "지구과학Ⅱ")

/** 탐구 선택과목 선택 — 사탐/과탐 칩(가로 스크롤). 선택 시 과목+유형 동시 설정. */
@Composable
private fun SubjectPicker(selected: String, onSelect: (subject: String, type: String) -> Unit) {
    Column(Modifier.padding(bottom = 8.dp)) {
        Text("사회탐구", style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(bottom = 4.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SOCIAL_SUBJECTS.forEach { s ->
                HiFiChip(text = s, selected = selected == s, small = true, onClick = { onSelect(s, "social") })
            }
        }
        Text("과학탐구", style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SCIENCE_SUBJECTS.forEach { s ->
                HiFiChip(text = s, selected = selected == s, small = true, onClick = { onSelect(s, "science") })
            }
        }
    }
}

@Composable
private fun NumBox(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = HiFiType.caption, color = HiFiColors.Text2, modifier = Modifier.padding(bottom = 2.dp))
        Box(
            Modifier.fillMaxWidth().background(HiFiColors.Bg, RoundedCornerShape(10.dp))
                .border(1.dp, HiFiColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 10.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = { s -> onChange(s.filter { it.isDigit() }) },
                singleLine = true,
                textStyle = HiFiType.body.copy(color = HiFiColors.Text),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
    }
}

@Composable
private fun NameField(value: String, onChange: (String) -> Unit, hint: String) {
    Box(
        Modifier.fillMaxWidth().padding(bottom = 8.dp)
            .background(HiFiColors.Bg, RoundedCornerShape(10.dp))
            .border(1.dp, HiFiColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        BasicTextField(
            value = value, onValueChange = onChange, singleLine = true,
            textStyle = HiFiType.body.copy(color = HiFiColors.Text),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(hint, style = HiFiType.body, color = HiFiColors.Text3)
                inner()
            },
        )
    }
}

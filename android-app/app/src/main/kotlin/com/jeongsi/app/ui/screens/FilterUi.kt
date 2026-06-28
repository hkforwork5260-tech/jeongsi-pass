package com.jeongsi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType

/** 검색·전략·지원가능 공용 — 지역 목록. */
val REGIONS = listOf("서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주")

/** 필터 칩 — 탭 시 N칸 그리드 + 세로 스크롤 선택 다이얼로그. 선택값 없으면 label, 있으면 선택값 표시. */
@Composable
fun FilterDropdown(label: String, selected: String?, options: List<Pair<String, String>>, columns: Int = 3, onPick: (String?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val display = options.firstOrNull { it.first == selected }?.second
    val on = selected != null
    Row(
        Modifier.background(if (on) HiFiColors.Brand else HiFiColors.Bg2, RoundedCornerShape(999.dp))
            .border(1.dp, if (on) HiFiColors.Brand else HiFiColors.Border, RoundedCornerShape(999.dp))
            .clickableNoRipple { open = true }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(display ?: label, style = HiFiType.caption.copy(fontWeight = FontWeight.Bold),
            color = if (on) Color.White else HiFiColors.Text)
        Text(" ▾", style = HiFiType.caption, color = if (on) Color.White else HiFiColors.Text2)
    }
    if (open) {
        FilterPickerDialog(label, selected, options, columns, onDismiss = { open = false }) {
            onPick(it); open = false
        }
    }
}

@Composable
private fun FilterPickerDialog(
    label: String,
    selected: String?,
    options: List<Pair<String, String>>,
    columns: Int,
    onDismiss: () -> Unit,
    onPick: (String?) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth()
                .background(HiFiColors.Bg, RoundedCornerShape(18.dp))
                .padding(18.dp),
        ) {
            Text("$label 선택", style = HiFiType.title, color = HiFiColors.Text)
            Spacer(Modifier.height(14.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.heightIn(max = 420.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { PickerCell("전체", selected == null) { onPick(null) } }
                gridItems(options) { (v, d) -> PickerCell(d, selected == v) { onPick(v) } }
            }
        }
    }
}

@Composable
private fun PickerCell(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) HiFiColors.Brand else HiFiColors.Bg2)
            .border(1.dp, if (selected) HiFiColors.Brand else HiFiColors.Border, RoundedCornerShape(12.dp))
            .clickableNoRipple { onClick() }
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = HiFiType.caption.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.sp),
            color = if (selected) Color.White else HiFiColors.Text,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

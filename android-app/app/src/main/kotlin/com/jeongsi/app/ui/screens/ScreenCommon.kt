package com.jeongsi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<T>.collectAsStateCompat(): State<T> = collectAsState()

/** 뒤로가기 상단바 (푸시 화면용). */
@Composable
fun AppTopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clickableNoRipple(onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text("‹", style = HiFiType.display, color = HiFiColors.Text)
        }
        Text(title, style = HiFiType.title, color = HiFiColors.Text)
    }
}

/** 화면 전체 가운데 메시지(로딩/에러용). */
@Composable
fun CenterText(text: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, style = HiFiType.body, color = HiFiColors.Text2, textAlign = TextAlign.Center)
    }
}

/** 세로 스크롤 + 표준 패딩 컬럼. */
@Composable
fun ScrollColumn(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        content = content,
    )
}

/** 섹션 제목(+ 선택적 우측 액션). */
@Composable
fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(4.dp).height(18.dp).background(HiFiColors.Brand, RoundedCornerShape(2.dp)))
        Text(title, style = HiFiType.title.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = HiFiColors.Text, modifier = Modifier.padding(start = 8.dp))
        Box(Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(action, style = HiFiType.caption, color = HiFiColors.Brand,
                modifier = Modifier.clickableNoRipple(onAction))
        }
    }
}

/** 리플 없는 클릭(카드·텍스트 버튼용). */
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    clickable(interactionSource = interaction, indication = null) { onClick() }
}

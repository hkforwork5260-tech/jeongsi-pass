package com.jeongsi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeongsi.app.ui.theme.HiFiColors
import com.jeongsi.app.ui.theme.HiFiType

enum class HomeTab(val label: String, val icon: ImageVector, val route: String) {
    Home("홈", Icons.Outlined.Home, "home"),
    Discover("찾아보기", Icons.Outlined.Explore, "discover"),
    Report("모의지원", Icons.Outlined.Assessment, "report"),
}

/**
 * 하단 5탭. 활성 탭은 코랄 컬러, 비활성은 #AFAFAF.
 */
@Composable
fun HiFiTabBar(
    active: HomeTab,
    onTabClick: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().background(HiFiColors.Bg)) {
        // 1dp 윗선 (탭바와 본문 구분)
        Box(Modifier.fillMaxWidth().height(1.dp).background(HiFiColors.Border))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(63.dp)
                .padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
        HomeTab.values().forEach { t ->
            val on = t == active
            val color = if (on) HiFiColors.Brand else HiFiColors.Text3
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabClick(t) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(t.icon, contentDescription = t.label, tint = color, modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(2.dp))
                Text(
                    t.label,
                    style = HiFiType.caption.copy(fontSize = 10.sp, letterSpacing = 0.sp),
                    color = color,
                )
            }
        }
        }
    }
}

package com.jeongsi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jeongsi.app.data.DeviceId
import com.jeongsi.app.ui.components.HiFiTabBar
import com.jeongsi.app.ui.components.HomeTab
import com.jeongsi.app.ui.screens.AnalysisScreen
import com.jeongsi.app.ui.screens.ComboDetailScreen
import com.jeongsi.app.ui.screens.DiscoverScreen
import com.jeongsi.app.ui.screens.HomeScreen
import com.jeongsi.app.ui.screens.MockScreen
import com.jeongsi.app.ui.screens.RecommendScreen
import com.jeongsi.app.ui.screens.ScoreInputScreen
import com.jeongsi.app.ui.screens.SearchScreen
import com.jeongsi.app.ui.screens.StrategyScreen
import com.jeongsi.app.ui.screens.UnitDetailScreen
import com.jeongsi.app.ui.theme.AppTheme
import com.jeongsi.app.ui.theme.HiFiColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        DeviceId.init(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { AppTheme { AppRoot() } }
    }
}

@Composable
private fun AppRoot() {
    var showSplash by remember { mutableStateOf(true) }
    if (showSplash) {
        SplashScreen(onDone = { showSplash = false })
        return
    }
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val activeTab = HomeTab.entries.firstOrNull { it.route == route }

    Scaffold(
        containerColor = HiFiColors.Bg,
        bottomBar = {
            // 탭 화면에서만 하단바 노출 (성적 입력 등 푸시 화면에선 숨김)
            if (activeTab != null) {
                HiFiTabBar(
                    active = activeTab,
                    onTabClick = { tab -> navigateTab(nav, tab.route) },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).windowInsetsPadding(WindowInsets.systemBars)) {
            NavHost(navController = nav, startDestination = HomeTab.Home.route) {
                composable(HomeTab.Home.route) {
                    HomeScreen(
                        onOpenScoreInput = { nav.navigate("score") },
                        onOpenDiscover = { navigateTab(nav, HomeTab.Discover.route) },
                        onOpenAnalysis = { nav.navigate("analysis") },
                        onOpenDetail = { id -> nav.navigate("detail/$id") },
                        onOpenSearch = { nav.navigate("search") },
                        onOpenRecommend = { nav.navigate("recommend") },
                        onOpenStrategy = { nav.navigate("strategy") },
                    )
                }
                composable(HomeTab.Discover.route) {
                    DiscoverScreen(onOpenDetail = { id -> nav.navigate("detail/$id") })
                }
                composable(HomeTab.Report.route) {
                    MockScreen(onOpenDetail = { id -> nav.navigate("detail/$id") })
                }
                composable("search") {
                    SearchScreen(onOpenDetail = { id -> nav.navigate("detail/$id") }, onBack = { nav.popBackStack() })
                }
                composable("recommend") {
                    RecommendScreen(onBack = { nav.popBackStack() }, onOpenDetail = { id -> nav.navigate("detail/$id") })
                }
                composable("strategy") {
                    StrategyScreen(
                        onBack = { nav.popBackStack() },
                        onOpenDetail = { id -> nav.navigate("detail/$id") },
                        onOpenCombo = { ids -> nav.navigate("combo/${ids.joinToString(",")}") },
                    )
                }
                composable("combo/{ids}") { entry ->
                    val ids = entry.arguments?.getString("ids")?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
                    ComboDetailScreen(ids = ids, onBack = { nav.popBackStack() }, onOpenDetail = { id -> nav.navigate("detail/$id") })
                }
                composable("score") {
                    ScoreInputScreen(onSaved = { nav.popBackStack() }, onBack = { nav.popBackStack() })
                }
                composable("analysis") { AnalysisScreen(onBack = { nav.popBackStack() }) }
                composable("detail/{unitId}") { entry ->
                    UnitDetailScreen(
                        unitId = entry.arguments?.getString("unitId")?.toLongOrNull() ?: 0L,
                        onBack = { nav.popBackStack() },
                    )
                }
            }
        }
    }
}

/** 시작 스플래시 — 메가스터디 로고를 잠깐 보여주고 로딩 후 진입. */
@Composable
private fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) { delay(1600); onDone() }
    Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.megastudy_lockup),
                contentDescription = "megastudy",
                modifier = Modifier.width(200.dp),
            )
            CircularProgressIndicator(
                color = HiFiColors.Brand,
                strokeWidth = 3.dp,
                modifier = Modifier.padding(top = 28.dp).size(26.dp),
            )
        }
    }
}

private fun navigateTab(nav: NavController, route: String) {
    nav.navigate(route) {
        popUpTo(HomeTab.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

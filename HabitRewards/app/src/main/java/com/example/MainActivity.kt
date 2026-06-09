package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TrackerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge transparent system bars support
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppEntry()
            }
        }
    }
}

@Composable
fun MainAppEntry(
    viewModel: TrackerViewModel = viewModel()
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(0) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // Top notch/camera inset protection
            .navigationBarsPadding(), // Bottom gesture pill inset protection
        color = MaterialTheme.colorScheme.background
    ) {
        if (profile == null) {
            // Frictionless Onboarding UX Flow
            OnboardingScreen(
                onComplete = { username, picBase64, age, email ->
                    viewModel.createProfile(username, picBase64, age, email)
                }
            )
        } else {
            // Primary Application Layout with 4 M3 Bottom Navigation items
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.testTag("app_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = "Challenges"
                                )
                            },
                            label = { Text(stringResource(R.string.tab_challenges)) },
                            modifier = Modifier.testTag("nav_challenges_tab")
                        )

                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Vision"
                                )
                            },
                            label = { Text(stringResource(R.string.tab_vision)) },
                            modifier = Modifier.testTag("nav_vision_tab")
                        )

                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Shop"
                                )
                            },
                            label = { Text(stringResource(R.string.tab_shop)) },
                            modifier = Modifier.testTag("nav_shop_tab")
                        )

                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = { currentTab = 3 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            label = { Text(stringResource(R.string.tab_settings)) },
                            modifier = Modifier.testTag("nav_settings_tab")
                        )
                    }
                }
            ) { innerPadding ->
                val contentModifier = Modifier.padding(innerPadding)
                
                when (currentTab) {
                    0 -> DashboardScreen(viewModel = viewModel, modifier = contentModifier)
                    1 -> VisionBoardScreen(viewModel = viewModel, modifier = contentModifier)
                    2 -> ShopScreen(viewModel = viewModel, modifier = contentModifier)
                    3 -> SettingsScreen(viewModel = viewModel, modifier = contentModifier)
                }
            }
        }
    }
}

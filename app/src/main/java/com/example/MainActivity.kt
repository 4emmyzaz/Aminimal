package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.NotificationHelper
import com.example.ui.MainScreen
import com.example.ui.TaskNoteViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.widget.StreakGlanceWidget

class MainActivity : ComponentActivity() {
    private val viewModel: TaskNoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        val quickAction = intent?.getStringExtra(StreakGlanceWidget.EXTRA_QUICK_ACTION)

        setContent {
            val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
            MyApplicationTheme(themeConfig = themeConfig) {
                MainScreen(quickAction = quickAction, viewModel = viewModel)
            }
        }
    }
}

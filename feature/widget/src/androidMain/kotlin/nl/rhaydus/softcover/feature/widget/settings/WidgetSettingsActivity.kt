package nl.rhaydus.softcover.feature.widget.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator

internal abstract class WidgetSettingsActivity(
    private val receiverClass: Class<*>,
) : ComponentActivity() {
    protected abstract fun screen(appWidgetId: Int): Screen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )

        val provider = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)?.provider

        val isOwnWidget = provider == ComponentName(
            this,
            receiverClass,
        )

        if (isOwnWidget.not()) {
            setResult(RESULT_CANCELED)

            finish()

            return
        }

        setResult(
            RESULT_OK,
            Intent().putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId,
            ),
        )

        setContent {
            Navigator(screen(appWidgetId = appWidgetId))
        }
    }
}

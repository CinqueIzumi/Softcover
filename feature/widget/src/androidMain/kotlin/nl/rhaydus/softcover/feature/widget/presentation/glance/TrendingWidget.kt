package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.state.PreferencesGlanceStateDefinition
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.feature.widget.R
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class TrendingWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideSharedContent(
            source = get<TrendingWidgetModelSource>(),
            appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id),
            liveSessions = get(),
        )
    }
}

internal fun TrendingFeed.eyebrowRes(): Int = when (this) {
    is TrendingFeed.Trending -> period.eyebrowRes()
    is TrendingFeed.Upcoming -> window.eyebrowRes()
}

private fun UpcomingWindow.eyebrowRes(): Int = when (this) {
    UpcomingWindow.Month -> R.string.upcoming_eyebrow_month
    UpcomingWindow.ThreeMonths -> R.string.upcoming_eyebrow_three_months
    UpcomingWindow.SixMonths -> R.string.upcoming_eyebrow_six_months
    UpcomingWindow.Year -> R.string.upcoming_eyebrow_year
}

private fun TrendingPeriod.eyebrowRes(): Int = when (this) {
    TrendingPeriod.Week -> R.string.trending_eyebrow_week
    TrendingPeriod.Month -> R.string.trending_eyebrow_month
    TrendingPeriod.ThreeMonths -> R.string.trending_eyebrow_three_months
    TrendingPeriod.Year -> R.string.trending_eyebrow_year
    TrendingPeriod.AllTime -> R.string.trending_eyebrow_all_time
}

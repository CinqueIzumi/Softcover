package nl.rhaydus.softcover.core.notification

import androidx.core.app.NotificationManagerCompat
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class SoftcoverNotificationChannelTest {
    @Test
    fun `Session channel id is softcover session v2`() {
        SoftcoverNotificationChannel.Session.id shouldBe "softcover.session.v2"
    }

    @Test
    fun `Session channel importance is IMPORTANCE_DEFAULT not IMPORTANCE_LOW`() {
        SoftcoverNotificationChannel.Session.importance shouldNotBe NotificationManagerCompat.IMPORTANCE_LOW
        SoftcoverNotificationChannel.Session.importance shouldBe NotificationManagerCompat.IMPORTANCE_DEFAULT
    }

    @Test
    fun `Reading and Milestones channels importance is IMPORTANCE_DEFAULT`() {
        SoftcoverNotificationChannel.Reading.importance shouldBe NotificationManagerCompat.IMPORTANCE_DEFAULT
        SoftcoverNotificationChannel.Milestones.importance shouldBe NotificationManagerCompat.IMPORTANCE_DEFAULT
    }
}

/*
 * TEMPORARY diagnostic, to be deleted once the mark is confirmed to sit still and grow as the
 * hunter walks up to it. It reports, once a second, where augmented reality believes the camera is
 * in the world it tracks, whether it is tracking at all, and how far the mark ended up from the
 * camera once everything has been added together - which is the number that decides how big it
 * looks on the screen.
 */
package pl.marianjureczko.mysteryhunters.screen.camera

import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Session
import kotlin.math.sqrt

private const val TAG = "ArScene"
private const val REPORTING_PERIOD_IN_NANOS = 1_000_000_000L

class ArSceneLog {

    private var lastReportedAt = 0L

    @Volatile
    var markInWorldX: Float = Float.NaN

    @Volatile
    var markInWorldY: Float = Float.NaN

    @Volatile
    var markInWorldZ: Float = Float.NaN

    @Volatile
    var sight: String = "?"

    @Volatile
    var markAwayInMeters: Float = Float.NaN

    @Volatile
    var realWorldAwayInMeters: Float = Float.NaN

    private var configReported = false

    fun onFrame(session: Session, frame: Frame, cameraX: Float, cameraY: Float, cameraZ: Float) {
        if (!configReported) {
            configReported = true
            Log.i(TAG, "depthMode=${session.config.depthMode} (AUTOMATIC means occlusion can work)")
        }
        val now = System.nanoTime()
        if (now - lastReportedAt < REPORTING_PERIOD_IN_NANOS) {
            return
        }
        lastReportedAt = now
        val camera = frame.camera
        val forward = camera.pose.zAxis
        val apparentDistance = if (markInWorldX.isNaN()) {
            Float.NaN
        } else {
            val dx = markInWorldX - cameraX
            val dz = markInWorldZ - cameraZ
            sqrt(dx * dx + dz * dz)
        }
        Log.i(
            TAG,
            ("sight=%s tracking=%s reason=%s cameraInWorld=(%.2f, %.2f, %.2f) " +
                    "facing=(%.2f, %.2f) apartInMeters=%.2f markAway=%.2f realWorldAt=%.2f").format(
                sight,
                camera.trackingState,
                camera.trackingFailureReason,
                cameraX, cameraY, cameraZ,
                -forward[0], -forward[2],
                apparentDistance,
                markAwayInMeters,
                realWorldAwayInMeters
            )
        )
    }
}

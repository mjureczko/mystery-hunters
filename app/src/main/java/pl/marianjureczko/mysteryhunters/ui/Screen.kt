package pl.marianjureczko.mysteryhunters.ui

import android.app.Activity
import androidx.compose.ui.unit.Dp
import androidx.window.layout.WindowMetricsCalculator

/**
 * Screen relative sizing, so that the layout scales with the device instead of using fixed dp.
 */
object Screen {
    var WIDTH = 1080f
        private set
    var HEIGHT = 2280f
        private set
    var DENSITY = 2.75f
        private set

    fun init(activity: Activity) {
        WindowMetricsCalculator
            .getOrCreate()
            .computeCurrentWindowMetrics(activity)
            .bounds.let {
                WIDTH = it.width().toFloat()
                HEIGHT = it.height().toFloat()
            }
        DENSITY = activity.resources.displayMetrics.density
    }

    inline val Number.PxToDp get() = this.toFloat() / DENSITY
    inline val Number.dw: Dp get() = Dp(value = (this.toFloat() * WIDTH).PxToDp)
    inline val Number.dh: Dp get() = Dp(value = (this.toFloat() * HEIGHT).PxToDp)
}

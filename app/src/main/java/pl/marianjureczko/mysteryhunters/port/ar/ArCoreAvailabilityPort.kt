package pl.marianjureczko.mysteryhunters.port.ar

import android.content.Context
import android.util.Log
import com.google.ar.core.ArCoreApk
import pl.marianjureczko.mysteryhunters.port.ArAvailabilityPort

class ArCoreAvailabilityPort(private val context: Context) : ArAvailabilityPort {

    private val TAG = javaClass.simpleName

    override fun isArAvailable(): Boolean =
        try {
            ArCoreApk.getInstance().checkAvailability(context).isSupported
        } catch (e: Exception) {
            Log.w(TAG, "ARCore availability check failed", e)
            false
        }
}

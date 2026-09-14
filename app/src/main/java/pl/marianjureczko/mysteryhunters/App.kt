package pl.marianjureczko.mysteryhunters

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class App : Application() {

    companion object {
        private const val OSMDROID_PREFERENCES = "osmdroid"
    }

    override fun onCreate() {
        super.onCreate()
        // osmdroid needs a user agent and a cache location before the first map is shown.
        Configuration.getInstance().apply {
            load(applicationContext, applicationContext.getSharedPreferences(OSMDROID_PREFERENCES, Context.MODE_PRIVATE))
            userAgentValue = packageName
            osmdroidBasePath = cacheDir
            osmdroidTileCache = cacheDir.resolve("tiles")
        }
    }
}

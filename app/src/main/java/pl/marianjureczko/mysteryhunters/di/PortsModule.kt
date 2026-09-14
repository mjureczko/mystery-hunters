package pl.marianjureczko.mysteryhunters.di

import android.content.Context
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import pl.marianjureczko.mysteryhunters.port.ArAvailabilityPort
import pl.marianjureczko.mysteryhunters.port.DeviceOrientationPort
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort
import pl.marianjureczko.mysteryhunters.port.SpeechToTextPort
import pl.marianjureczko.mysteryhunters.port.ar.ArCoreAvailabilityPort
import pl.marianjureczko.mysteryhunters.port.orientation.SensorDeviceOrientationPort
import pl.marianjureczko.mysteryhunters.port.speech.VoskSpeechToTextPort
import pl.marianjureczko.mysteryhunters.port.storage.MysteryHuntersDatabase
import pl.marianjureczko.mysteryhunters.port.storage.RouteDao
import pl.marianjureczko.mysteryhunters.port.storage.RoomRouteStoragePort
import pl.marianjureczko.poszukiwacz.compass.api.CompassIoDispatcher
import pl.marianjureczko.poszukiwacz.compass.api.CompassMainDispatcher
import pl.marianjureczko.poszukiwacz.compass.api.LocationPort
import javax.inject.Singleton

/**
 * Module providing beans that shall be overridden in espresso tests.
 */
@Module
@InstallIn(SingletonComponent::class)
object PortsModule {

    @Singleton
    @Provides
    fun database(@ApplicationContext appContext: Context): MysteryHuntersDatabase =
        Room.databaseBuilder(appContext, MysteryHuntersDatabase::class.java, MysteryHuntersDatabase.NAME)
            .build()

    @Singleton
    @Provides
    fun routeDao(database: MysteryHuntersDatabase): RouteDao = database.routeDao()

    @Singleton
    @Provides
    fun routeStoragePort(routeDao: RouteDao): RouteStoragePort = RoomRouteStoragePort(routeDao)

    @Singleton
    @Provides
    fun speechToTextPort(
        @ApplicationContext appContext: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): SpeechToTextPort = VoskSpeechToTextPort(appContext, ioDispatcher)

    @Singleton
    @Provides
    fun arAvailabilityPort(@ApplicationContext appContext: Context): ArAvailabilityPort =
        ArCoreAvailabilityPort(appContext)

    @Provides
    fun deviceOrientationPort(@ApplicationContext appContext: Context): DeviceOrientationPort =
        SensorDeviceOrientationPort(appContext)

    @Singleton
    @Provides
    fun fusedLocationClient(@ApplicationContext appContext: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    /** Required by the compass module, see its README. */
    @Singleton
    @Provides
    fun locationPort(
        @ApplicationContext appContext: Context,
        locationClient: FusedLocationProviderClient,
        @CompassIoDispatcher ioDispatcher: CoroutineDispatcher,
        @CompassMainDispatcher mainDispatcher: CoroutineDispatcher
    ): LocationPort = LocationPort.create(appContext, locationClient, ioDispatcher, mainDispatcher)
}

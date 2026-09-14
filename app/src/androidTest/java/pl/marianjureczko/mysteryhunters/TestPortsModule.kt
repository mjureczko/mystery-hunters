package pl.marianjureczko.mysteryhunters

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import pl.marianjureczko.mysteryhunters.di.PortsModule
import pl.marianjureczko.mysteryhunters.port.ArAvailabilityPort
import pl.marianjureczko.mysteryhunters.port.DeviceOrientationPort
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort
import pl.marianjureczko.mysteryhunters.port.SpeechToTextPort
import pl.marianjureczko.mysteryhunters.port.TestArAvailabilityPort
import pl.marianjureczko.mysteryhunters.port.TestDeviceOrientationPort
import pl.marianjureczko.mysteryhunters.port.TestLocationPort
import pl.marianjureczko.mysteryhunters.port.TestRouteStoragePort
import pl.marianjureczko.mysteryhunters.port.TestSpeechToTextPort
import pl.marianjureczko.poszukiwacz.compass.api.LocationPort
import javax.inject.Singleton

/**
 * Replaces every external dependency with a test double, so the happy paths can be driven from a
 * test without a database, a GPS receiver, a microphone or ARCore.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [PortsModule::class])
object TestPortsModule {

    @Singleton
    @Provides
    fun testRouteStoragePort(): TestRouteStoragePort = TestRouteStoragePort()

    @Singleton
    @Provides
    fun routeStoragePort(testPort: TestRouteStoragePort): RouteStoragePort = testPort

    @Singleton
    @Provides
    fun testSpeechToTextPort(): TestSpeechToTextPort = TestSpeechToTextPort()

    @Singleton
    @Provides
    fun speechToTextPort(testPort: TestSpeechToTextPort): SpeechToTextPort = testPort

    @Singleton
    @Provides
    fun testArAvailabilityPort(): TestArAvailabilityPort = TestArAvailabilityPort()

    @Singleton
    @Provides
    fun arAvailabilityPort(testPort: TestArAvailabilityPort): ArAvailabilityPort = testPort

    @Singleton
    @Provides
    fun testDeviceOrientationPort(): TestDeviceOrientationPort = TestDeviceOrientationPort()

    @Singleton
    @Provides
    fun deviceOrientationPort(testPort: TestDeviceOrientationPort): DeviceOrientationPort = testPort

    @Singleton
    @Provides
    fun testLocationPort(): TestLocationPort = TestLocationPort()

    @Singleton
    @Provides
    fun locationPort(testPort: TestLocationPort): LocationPort = testPort
}

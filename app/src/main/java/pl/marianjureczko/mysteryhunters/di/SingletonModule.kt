/*
 * Copyright (C) 2026 Marian Jureczko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package pl.marianjureczko.mysteryhunters.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import pl.marianjureczko.mysteryhunters.port.RouteStoragePort
import pl.marianjureczko.mysteryhunters.usecase.AddPointOfInterestUC
import pl.marianjureczko.mysteryhunters.usecase.CalculateBearingUC
import pl.marianjureczko.mysteryhunters.usecase.CalculateMarkerPositionUC
import pl.marianjureczko.mysteryhunters.usecase.CatchPointUC
import pl.marianjureczko.mysteryhunters.usecase.CreateRouteUC
import pl.marianjureczko.mysteryhunters.usecase.DeletePointOfInterestUC
import pl.marianjureczko.mysteryhunters.usecase.DeleteRouteUC
import pl.marianjureczko.mysteryhunters.usecase.IsPointInCatchRangeUC
import pl.marianjureczko.mysteryhunters.usecase.LoadRouteUC
import pl.marianjureczko.mysteryhunters.usecase.ObserveRoutesUC
import pl.marianjureczko.mysteryhunters.usecase.RenameRouteUC
import pl.marianjureczko.mysteryhunters.usecase.SelectNextPointUC
import pl.marianjureczko.mysteryhunters.usecase.SelectPointToSearchUC
import pl.marianjureczko.mysteryhunters.usecase.SelectPointUC
import pl.marianjureczko.mysteryhunters.usecase.UpdatePointOfInterestUC
import pl.marianjureczko.poszukiwacz.compass.api.CompassIoDispatcher
import pl.marianjureczko.poszukiwacz.compass.api.CompassMainDispatcher
import pl.marianjureczko.poszukiwacz.compass.api.LocationCalculator
import javax.inject.Singleton

/**
 * Module providing beans that shall be used both in production and in espresso tests.
 */
@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    @Provides
    @IoDispatcher
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    fun mainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @CompassIoDispatcher
    fun compassIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @CompassMainDispatcher
    fun compassMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Singleton
    @Provides
    fun observeRoutesUC(storage: RouteStoragePort) = ObserveRoutesUC(storage)

    @Singleton
    @Provides
    fun loadRouteUC(storage: RouteStoragePort) = LoadRouteUC(storage)

    @Singleton
    @Provides
    fun createRouteUC(storage: RouteStoragePort) = CreateRouteUC(storage)

    @Singleton
    @Provides
    fun renameRouteUC(storage: RouteStoragePort) = RenameRouteUC(storage)

    @Singleton
    @Provides
    fun deleteRouteUC(storage: RouteStoragePort) = DeleteRouteUC(storage)

    @Singleton
    @Provides
    fun addPointOfInterestUC(storage: RouteStoragePort) = AddPointOfInterestUC(storage)

    @Singleton
    @Provides
    fun updatePointOfInterestUC(storage: RouteStoragePort) = UpdatePointOfInterestUC(storage)

    @Singleton
    @Provides
    fun deletePointOfInterestUC(storage: RouteStoragePort) = DeletePointOfInterestUC(storage)

    @Singleton
    @Provides
    fun selectPointToSearchUC(storage: RouteStoragePort) = SelectPointToSearchUC(storage)

    @Singleton
    @Provides
    fun selectNextPointUC(storage: RouteStoragePort) = SelectNextPointUC(storage)

    @Singleton
    @Provides
    fun selectPointUC(storage: RouteStoragePort) = SelectPointUC(storage)

    @Singleton
    @Provides
    fun catchPointUC(storage: RouteStoragePort) = CatchPointUC(storage)

    @Singleton
    @Provides
    fun locationCalculator() = LocationCalculator()

    @Singleton
    @Provides
    fun isPointInCatchRangeUC(locationCalculator: LocationCalculator) =
        IsPointInCatchRangeUC(locationCalculator)

    @Singleton
    @Provides
    fun calculateBearingUC() = CalculateBearingUC()

    @Singleton
    @Provides
    fun calculateMarkerPositionUC() = CalculateMarkerPositionUC()
}

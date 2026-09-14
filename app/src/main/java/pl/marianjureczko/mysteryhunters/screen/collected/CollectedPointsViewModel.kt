package pl.marianjureczko.mysteryhunters.screen.collected

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.marianjureczko.mysteryhunters.model.PointOfInterest
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.screen.Screens
import pl.marianjureczko.mysteryhunters.usecase.LoadRouteUC
import javax.inject.Inject

data class CollectedPointsState(
    val points: List<PointOfInterest> = emptyList(),
    val notCaughtInfoShown: Boolean = false
)

@HiltViewModel
class CollectedPointsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadRouteUC: LoadRouteUC
) : ViewModel() {

    val routeId: Long = savedStateHandle[Screens.CollectedPoints.PARAMETER_ROUTE_ID] ?: Route.NOT_PERSISTED

    private val _state = mutableStateOf(CollectedPointsState())
    val state: State<CollectedPointsState> = _state

    init {
        viewModelScope.launch {
            val route = loadRouteUC(routeId)
            _state.value = _state.value.copy(points = route?.pointsOfInterest ?: emptyList())
        }
    }

    fun showNotCaughtInfo() {
        _state.value = _state.value.copy(notCaughtInfoShown = true)
    }

    fun hideNotCaughtInfo() {
        _state.value = _state.value.copy(notCaughtInfoShown = false)
    }
}

package pl.marianjureczko.mysteryhunters.screen.pointdetail

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

data class PointDetailState(val point: PointOfInterest? = null)

@HiltViewModel
class PointDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadRouteUC: LoadRouteUC
) : ViewModel() {

    private val routeId: Long = savedStateHandle[Screens.PointDetail.PARAMETER_ROUTE_ID] ?: Route.NOT_PERSISTED
    private val pointId: Int? = savedStateHandle[Screens.PointDetail.PARAMETER_POINT_ID]

    private val _state = mutableStateOf(PointDetailState())
    val state: State<PointDetailState> = _state

    init {
        viewModelScope.launch {
            val route = loadRouteUC(routeId)
            _state.value = PointDetailState(point = route?.pointById(pointId))
        }
    }
}

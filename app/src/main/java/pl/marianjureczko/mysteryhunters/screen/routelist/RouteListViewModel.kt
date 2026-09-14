package pl.marianjureczko.mysteryhunters.screen.routelist

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.marianjureczko.mysteryhunters.model.Route
import pl.marianjureczko.mysteryhunters.usecase.DeleteRouteUC
import pl.marianjureczko.mysteryhunters.usecase.ObserveRoutesUC
import javax.inject.Inject

@HiltViewModel
class RouteListViewModel @Inject constructor(
    private val observeRoutesUC: ObserveRoutesUC,
    private val deleteRouteUC: DeleteRouteUC
) : ViewModel() {

    private val _state = mutableStateOf(RouteListState())
    val state: State<RouteListState> = _state

    init {
        viewModelScope.launch {
            observeRoutesUC().collectLatest { routes ->
                _state.value = _state.value.copy(routes = routes)
            }
        }
    }

    fun askToDelete(route: Route) {
        _state.value = _state.value.copy(routeToDelete = route)
    }

    fun cancelDeletion() {
        _state.value = _state.value.copy(routeToDelete = null)
    }

    fun confirmDeletion() {
        val route = _state.value.routeToDelete ?: return
        viewModelScope.launch {
            deleteRouteUC(route.id)
            _state.value = _state.value.copy(routeToDelete = null)
        }
    }
}

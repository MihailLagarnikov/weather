package cheysoff.weather.presention

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cheysoff.weather.data.RepositoriyImpl.ERROR_SIMPLE
import cheysoff.weather.domain.RequestState
import cheysoff.weather.domain.UseCase
import cheysoff.weather.domain.data.City
import cheysoff.weather.domain.data.Temperature
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val useCase = UseCase()
    private val _screenState = MutableStateFlow<State>(State.Start)
    val screenState = _screenState.asStateFlow()
    val errorHandler = CoroutineExceptionHandler { _, exeption ->
        viewModelScope.launch {
            _screenState.emit(State.Error(exeption.message ?: ERROR_SIMPLE))
        }
    }

    fun getCoordinatesByCityName(cityName: String) {
        viewModelScope.launch(Dispatchers.IO + errorHandler) {
            val state = useCase.getCoordinatesByCityName(cityName)
            when (state) {
                is RequestState.Success -> {
                    val response = state.response as? City
                    if (response != null) {
                        _screenState.emit(State.HasCityData(response))
                    } else {
                        _screenState.emit(State.Error(ERROR_SIMPLE))
                    }
                }

                is RequestState.Error -> {
                    _screenState.emit(State.Error(state.errorMessage))
                }

            }
        }
    }

    fun getWeatherByCoordinates(city: City, days: Int) {
        viewModelScope.launch(Dispatchers.IO + errorHandler) {
            val stateMin = useCase.getWeatherByCoordinates(city, days, MIN)
            val stateMax = useCase.getWeatherByCoordinates(city, days, MAX)
            if (stateMin is RequestState.Success
                && stateMax is RequestState.Success
            ) {
                val tempMin = stateMin.response as? Temperature
                val tempMax = stateMax.response as? Temperature
                if (tempMin?.temperaturesList?.isEmpty() == true
                    && tempMax?.temperaturesList?.isEmpty() == true
                ) {
                    _screenState.emit(State.Error(ERROR_SIMPLE))
                } else {
                    _screenState.emit(
                        State.HasAllData(
                            tempMin?.temperaturesList?.zip(
                                tempMax?.temperaturesList ?: emptyList()
                            ) ?: emptyList()
                        )
                    )
                }
            }
        }

    }

    companion object {
        private const val MIN = "min"
        private const val MAX = "max"
    }
}
package cheysoff.weather.presention

import cheysoff.weather.domain.data.City

sealed class State {
    object Start: State()
    class HasCityData(val city: City): State()
    class HasAllData(val weatherList: List<Pair<Double, Double>>): State()
    class Error(val errorText: String): State()
}
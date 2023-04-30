package cheysoff.weather.domain

import cheysoff.weather.domain.data.City

interface Repositoriy {

    suspend fun getCoordinatesByCityName(cityName: String):RequestState

    suspend fun getWeatherByCoordinates(city: City, days: Int, param: String):RequestState
}
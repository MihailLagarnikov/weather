package cheysoff.weather.domain

import cheysoff.weather.data.RepositoriyImpl
import cheysoff.weather.domain.data.City

class UseCase {
    val repositoriy: Repositoriy

    init {
        repositoriy = RepositoriyImpl
    }

    suspend fun getCoordinatesByCityName(cityName: String) = repositoriy.getCoordinatesByCityName(cityName)

    suspend fun getWeatherByCoordinates(city: City, days: Int, param: String) = repositoriy.getWeatherByCoordinates(city, days, param)
}
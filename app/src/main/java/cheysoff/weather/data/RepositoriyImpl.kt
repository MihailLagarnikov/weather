package cheysoff.weather.data

import android.util.Log
import cheysoff.weather.domain.data.City
import cheysoff.weather.domain.Repositoriy
import cheysoff.weather.domain.RequestState
import cheysoff.weather.domain.data.Temperature
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Scanner

object RepositoriyImpl : Repositoriy {
    val client = OkHttpClient()
    private const val EMPTY_LAT = 0.0
    const val ERROR_SIMPLE = "Произошла ошибка интернет соединения, попробуйте позже"

    override suspend fun getCoordinatesByCityName(cityName: String): RequestState {

        val url =
            "https://nominatim.openstreetmap.org/search?city=$cityName&countrycodes=\$countryCode&limit=9&format=json"
        val request = Request.Builder()
            .url(url)
            .build()
        Log.d("CityName", cityName)
        var latitude = EMPTY_LAT
        var longitude = EMPTY_LAT
        val response = client.newCall(request).execute()
        if (response.code in 200..299) {
            val responseBodyString = response.body?.string()
            val latitudeString = getNextWordAfterTriggerWord(responseBodyString, "\"lat\"")
            latitude = latitudeString?.substring(1, latitudeString.length - 1)?.toDoubleOrNull()
                ?: EMPTY_LAT

            val longitudeString = getNextWordAfterTriggerWord(responseBodyString, "\"lon\"")
            longitude =
                longitudeString?.substring(1, longitudeString.length - 1)?.toDoubleOrNull()
                    ?: EMPTY_LAT
            if (latitude == EMPTY_LAT || longitude == EMPTY_LAT) {
                return RequestState.Error(ERROR_SIMPLE)
            } else {
                return RequestState.Success(City(cityName, latitude, longitude))
            }

        } else {
            return RequestState.Error(ERROR_SIMPLE)
        }
    }

    override suspend fun getWeatherByCoordinates(city: City, days: Int, param: String): RequestState {
        val url =
            "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=${city.latitude}" +
                    "&longitude=${city.longitude}" +
                    "&timezone=auto" +
                    "&daily=temperature_2m_" +
                    "${param}&forecast_days=$days"
        val request = Request.Builder()
            .url(url)
            .build()
        var temperaturesList: List<Double>? = emptyList()
        val response = client.newCall(request).execute()
        if (response.code in 200..299) {
            val responseBodyString = response.body?.string()
            temperaturesList = responseBodyString?.let {
                temperaturesFromText(
                    it,
                    "\"temperature_2m_$param\":[",
                    days
                )
            }
            if (temperaturesList?.isNotEmpty() == true) {
                return RequestState.Success(Temperature(temperaturesList))
            } else {
                return RequestState.Error(ERROR_SIMPLE)
            }

        } else {
            return RequestState.Error(ERROR_SIMPLE)
        }
    }

    private fun getNextWordAfterTriggerWord(inputString: String?, triggerWord: String): String? {
        val scanner = Scanner(inputString).useDelimiter("[,:]")
        var shouldReturnNextWord = false
        var nextWord: String? = null

        while (scanner.hasNext()) {
            val word = scanner.next()
            if (shouldReturnNextWord) {
                nextWord = word
                break
            }

            if (word == triggerWord) {
                shouldReturnNextWord = true
            }
        }

        return nextWord
    }

    fun temperaturesFromText(text: String, trigger: String, days: Int): List<Double> {
        val startIndex = text.indexOf(trigger) + trigger.length

        val temperatures = mutableListOf<Double>()
        val textStream = text.substring(startIndex).split("[,\\]]".toRegex()).toTypedArray()
        var daysPassed = 0
        for (token in textStream) {
            if(daysPassed >= days) {
                break
            }
            Log.d("weat", token)
            temperatures.add(token.toDouble())
            daysPassed++
        }

        return temperatures
    }
}
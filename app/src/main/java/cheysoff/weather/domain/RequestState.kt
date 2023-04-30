package cheysoff.weather.domain

sealed class RequestState {
    class Success(val response: Response): RequestState()
    class Error(val errorMessage: String): RequestState()
}
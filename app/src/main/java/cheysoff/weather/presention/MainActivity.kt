package cheysoff.weather.presention

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import cheysoff.weather.R
import cheysoff.weather.data.RepositoriyImpl.ERROR_SIMPLE
import cheysoff.weather.ui.theme.LightPurple
import cheysoff.weather.ui.theme.MyTransparent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.UnknownHostException

class MainActivity : ComponentActivity() {
    private val viewModel: ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.viewModelScope.launch {
            viewModel.screenState
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .buffer()
                .collect { state ->
                    when (state) {
                        is State.Start -> {
                            viewModel.getCoordinatesByCityName(CITY_NAME)
                        }

                        is State.HasCityData -> {
                            viewModel.getWeatherByCoordinates(state.city, DAYS)
                        }

                        is State.HasAllData -> {
                            withContext(Dispatchers.Main) {
                                setContent {
                                    showWeatherList(state.weatherList)
                                }
                            }
                        }
                        is State.Error -> {
                            setContent {
                                showError(state.errorText)
                            }
                        }
                    }
                }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            if (!isInternetAvailable()){
                withContext(Dispatchers.Main){
                    setContent {
                        showError(ERROR_SIMPLE)
                    }
                }
            }
        }
    }


    @Composable
    fun greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Composable
    fun showError(errorText: String) {
        Text(
            text = errorText
        )
    }

    fun isInternetAvailable(): Boolean {
        try {
            val address = InetAddress.getByName("www.google.com")
            return !address.equals("")
        } catch (e: UnknownHostException) {
            Log.d("asqs", "e = ${e.message}")
        }
        return false
    }

    @Composable
    fun showWeatherList(weatherList: List<Pair<Double, Double>>) {
        Image(
            painter = painterResource(id = R.drawable.main_background),
            contentDescription = "background image",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f),
            contentScale = ContentScale.FillBounds
        )
        Card(

            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(LightPurple),
            shape = RoundedCornerShape(15.dp),


            ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MyTransparent),

                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(MyTransparent),
                ) {
                    for (i in weatherList.indices) {
                        val pair = weatherList.get(i)
                        Row(
                            modifier = Modifier
                                .background(MyTransparent)
                        ) {
                            Text(text = "(${pair.first}, ${pair.second})")
                        }

                    }
                }

            }
        }

//    Column {
//        for (i in weatherList.indices) {
//            val pair = weatherList.get(i)
//            Text(text = "(${pair.first}, ${pair.second})")
//        }
////        weatherList.forEach { pair ->
////            Text(text = "(${pair.first}, ${pair.second})")
////        }
//    }
    }

    companion object {
        private const val CITY_NAME = "Moscow"
        private const val DAYS = 16
    }
}
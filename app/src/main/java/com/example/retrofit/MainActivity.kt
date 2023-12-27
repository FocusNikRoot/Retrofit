package com.example.retrofit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import retrofit2.Retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.converter.gson.GsonConverterFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import kotlin.collections.forEach
import androidx.recyclerview.widget.LinearLayoutManager

interface WeatherService {
    @GET("weather")
    fun getWeatherForCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>
}

data class WeatherResponse(
    val main: WeatherData,
    val weather: List<Weather>
)

data class WeatherData(
    val temperature: Double,
    val dateTime: Date
)

data class Weather(
    val temperature: Double,
    val main: String,
    val description: String
)

class MainActivity : AppCompatActivity() {
    private val apiKey = "c4e181c57e83b56e80198542ca2889b1"

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(WeatherService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //---------------------------------------------------------------
        val call = service.getWeatherForCity("Shklov", apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    val temperatures = mutableListOf<Double>()
                    val weatherList = mutableListOf<Weather>()

                    weatherResponse?.weather?.forEach { weatherData ->
                        val weather = Weather(
                            temperature = weatherData.temperature,
                            main = weatherData.main,
                            description = weatherData.description
                        )
                        weatherList.add(weather)
                    }


                    val recyclerView = findViewById<RecyclerView>(R.id.r_view)
                    val adapter = WeatherAdapter(weatherList)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

                    adapter.notifyDataSetChanged()
                } else {
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
            }
        })
        //-------------------------------------------------------------
    }
}

class ViewHolderHot(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val temperatureTextView: TextView = itemView.findViewById(R.id.hotTemperatureTextView)

    fun bind(temperature: Double) {
        temperatureTextView.text = "Hot: +$temperature°C"
    }
}

class ViewHolderCold(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val temperatureTextView: TextView = itemView.findViewById(R.id.coldTemperatureTextView)

    fun bind(temperature: Double) {
        temperatureTextView.text = "Cold: $temperature°C"
    }
}

class WeatherAdapter(private val weatherList: List<Weather>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HOT_VIEW_TYPE -> {
                val hotView = LayoutInflater.from(parent.context).inflate(R.layout.hot_item_layout, parent, false)
                ViewHolderHot(hotView)
            }
            COLD_VIEW_TYPE -> {
                val coldView = LayoutInflater.from(parent.context).inflate(R.layout.cold_item_layout, parent, false)
                ViewHolderCold(coldView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val weather = weatherList[position]
        when (holder) {
            is ViewHolderHot -> holder.bind(weather.temperature)
            is ViewHolderCold -> holder.bind(weather.temperature)
        }
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }

    override fun getItemViewType(position: Int): Int {
        val temperature = weatherList[position].temperature
        return if (temperature >= 0) {
            HOT_VIEW_TYPE
        } else {
            COLD_VIEW_TYPE
        }
    }

    companion object {
        private const val HOT_VIEW_TYPE = 0
        private const val COLD_VIEW_TYPE = 1
    }
}
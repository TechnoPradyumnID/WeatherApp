package com.technopradyumn.weatherapp

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.ClientError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.technopradyumn.weatherapp.databinding.ActivityMainBinding
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TOKEN = "4a0721278942e9902d53a5806a0e5a14"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding.btnSearch.setOnClickListener {
            val location = binding.etLocation.text.toString().trim()
            if (location.isNotEmpty()) {
                showError(false)
                requestToServer(location)
            } else {
                binding.etLocation.error = "Location cannot be empty"
                binding.btnSearch.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_red))
                showToast("Error")
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun requestToServer(location: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$location&units=metric&appid=$TOKEN"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            try {
                val jsonResponse = JSONObject(response)
                val mainObject = jsonResponse.getJSONObject("main")
                val temperature = ceil(mainObject.getDouble("temp")).toInt()
                val humidity = mainObject.getInt("humidity")
                val windObject = jsonResponse.getJSONObject("wind")
                val windSpeed = windObject.getDouble("speed")
                val weatherArray = jsonResponse.getJSONArray("weather")
                val (description, main) =
                    if (weatherArray.length() > 0) {
                        val weatherObject = weatherArray.getJSONObject(0)
                        Pair(weatherObject.getString("description"), weatherObject.getString("main"))
                    } else null to null

                with(binding.layoutWeather) {
                    root.visibility = View.VISIBLE
                    locationTitle.text = location
                    tvTemperature.text = temperature.toString()
                    tvWindSpeed.text = "$windSpeed m/s"
                    tvHumidityValue.text = "$humidity%"
                    tvDescription.text = description.orEmpty()

                    ivStatus.setImageResource(
                        when (main) {
                            "Clear" -> R.drawable.clear
                            "Rain" -> R.drawable.rain
                            "Snow" -> R.drawable.snow
                            "Clouds" -> R.drawable.cloud
                            else -> 0
                        }
                    )

                    ivBack.setOnClickListener {
                        root.visibility = View.GONE
                    }
                }
            } catch (e: JSONException) {
                showToast(e.message.toString())
            }
        }, { error ->
            if (error is ClientError || error is ServerError) {
                val response: NetworkResponse = error.networkResponse
                val statusCode = response.statusCode
                if (statusCode == 404) showError(true, "Location NotFound")
                else showToast("Error in connecting to API")
            }
        })

        queue.add(stringRequest)
    }

    private fun showError(haveError: Boolean, message: String? = null) {
        binding.tvError.apply {
            visibility = if (haveError) {
                text = message
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    override fun onBackPressed() {
        super.onBackPressed()
        binding.layoutWeather.root.visibility = View.GONE
    }

}
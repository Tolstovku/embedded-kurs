package com.example.battery.ui.settings

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.battery.DTO.SetConfigRequest
import com.example.battery.MainActivity
import com.example.battery.R
import com.example.battery.models.HeaterConfig
import com.example.battery.models.HeaterStatus
import org.json.JSONObject
import com.google.gson.Gson;


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var editTemp: EditText
    private lateinit var editRad: EditText
    private lateinit var textView: TextView
    private lateinit var heaterStatusView: TextView
    private var urlBase = "http://fly.sytes.net:8080"
    private lateinit var heaterStatus: HeaterStatus

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val button = root.findViewById<Button>(R.id.button)

        heaterStatus = (context as MainActivity).heaterStatus

        textView = root.findViewById(R.id.textView)

        getConfig()

        button.setOnClickListener {
            changeConfig()
        }
        editTemp = root.findViewById(R.id.editTemp)
        editRad = root.findViewById(R.id.editRad)
        getConfig()
        return root
    }

    private fun getConfig() {
        val queue = Volley.newRequestQueue(context?.applicationContext)
        val urlAPI = "/get/config"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET,
            "$urlBase$urlAPI",
            null,
            { response ->
                textView.text = "Temperature is %s , Radius is %s ".format(response.get("temperature"), response.get("radius"))
                (context as MainActivity).circleRadius = response.get("radius") as Double
                editTemp.setText(response.get("temperature").toString())
                editRad.setText(response.get("radius").toString())
            },
            { error ->
                textView.text = "ERROR: %s".format(error.toString())
            }
        )
        queue.add(jsonObjectRequest)
    }


    private fun changeConfig() {
        val queue = Volley.newRequestQueue(context?.applicationContext)
        val urlAPI = "/set/config"
        val setConfigRequest = SetConfigRequest(
            heaterId = "313",
            config = HeaterConfig(
                temperature = editTemp.text.toString().toDouble(),
                radius = editRad.text.toString().toDouble()
            )
        )
        val jsonString = Gson().toJson(setConfigRequest)
        val jsonObject = JSONObject(jsonString)
        println(jsonObject)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST,
            "$urlBase$urlAPI",
            jsonObject,
            { _ ->
            },
            { _ ->
            }
        )
        queue.add(jsonObjectRequest)
        textView.text = "Temperature is %s, Radius is %s ".format(editTemp.text.toString().toDouble(), editRad.text.toString().toDouble())
    }
}
package com.example.battery.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import com.example.battery.R
import org.json.JSONObject
import org.w3c.dom.Text

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var editTemp: EditText
    private lateinit var editRad: EditText
    private lateinit var textView: TextView
    private lateinit var heaterStatus: TextView
    private lateinit var switchOnOff : Switch

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val button = root.findViewById<Button>(R.id.button)

        textView = root.findViewById(R.id.textView)
        heaterStatus = root.findViewById(R.id.textViewStatus)
        switchOnOff = root.findViewById(R.id.switchOnOff)

        onCreateRequest()

        button.setOnClickListener {
            changeConfig()
        }
        editTemp = root.findViewById(R.id.editTemp)
        editRad = root.findViewById(R.id.editRad)
        return root
    }

    private fun onCreateRequest() {
        val queue = Volley.newRequestQueue(context?.applicationContext)
        val url = "https://reqres.in/api/users?page=2"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET,
            url,
            null,
            { response ->
                textView.text = "Response: %s".format(response.get("page"))
            },
            { error ->
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun changeConfig() {
        val queue = Volley.newRequestQueue(context?.applicationContext)
        val url = "https://reqres.in/api/login"
        val jsonObject = JSONObject()
        jsonObject.put("email", "eve.holt@reqres.in")
        jsonObject.put("password", "cityslicka")

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST,
            url,
            jsonObject,
            { response ->
                textView.text = "Response: %s".format(response.toString())
            },
            { error ->
            }
        )
        queue.add(jsonObjectRequest)
    }
}
package com.example.battery.DTO

import com.example.battery.models.HeaterConfig
import com.example.battery.models.PointVo

data class ConnectHeaterResponse(
    val location: PointVo,
    val config: HeaterConfig
)
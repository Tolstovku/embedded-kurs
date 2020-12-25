package com.example.battery.DTO

import com.example.battery.models.HeaterConfig

data class SetConfigRequest(
    val heaterId: String,
    val config: HeaterConfig
)
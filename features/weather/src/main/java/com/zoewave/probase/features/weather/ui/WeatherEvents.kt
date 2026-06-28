package com.zoewave.probase.features.weather.ui

sealed class WeatherEvent {
    object Refresh : WeatherEvent()
    data class UpdateSetting(val settingKey: String, val settingValue: String) : WeatherEvent()
    object DeleteAllEntries : WeatherEvent()
    object FetchWeather : WeatherEvent()
}


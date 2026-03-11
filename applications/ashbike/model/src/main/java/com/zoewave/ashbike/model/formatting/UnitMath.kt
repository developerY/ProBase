package com.zoewave.ashbike.model.formatting

// Distance: Kilometers to Miles
fun Float.toMiles(): Float = this * 0.621371f

// Speed: km/h to mph
fun Float.toMph(): Float = this * 0.621371f

// Elevation: Meters to Feet (using Double for elevation precision if needed)
fun Double.toFeet(): Double = this * 3.28084
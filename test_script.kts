var counter = 0L
val clockMs: () -> Long = { 
    if (counter == 0L) {
        counter++
        0L // startMs
    } else {
        1800000L // 30 minutes later
    }
}
val startMs = clockMs()
val elapsedMs = clockMs() - startMs
val hours = elapsedMs / 3600000.0
val distanceKm = 10f
val result = if (hours > 0) distanceKm / hours else 0.0
println(result)

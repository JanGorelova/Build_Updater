package com.uploader.schedule

import java.time.Duration
import java.time.Duration.ZERO
import java.time.Duration.ofMinutes
import java.util.Timer
import java.util.TimerTask

class Job(
    task: TimerTask,
    name: String,
    delay: Duration = ZERO,
    period: Duration = ofMinutes(1)
) {
    private val timer: Timer = Timer(name)

    init {
        timer.scheduleAtFixedRate(task, delay.toMillis(), period.toMillis())
    }

    fun cancel() {
        timer.cancel()
    }
}

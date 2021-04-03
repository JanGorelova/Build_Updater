package com.uploader.schedule

import java.time.Duration
import java.time.Duration.ZERO
import java.time.Duration.ofMinutes
import java.util.*

class Job(
    task: TimerTask,
    name: String,
    delay: Duration = ZERO,
    period: Duration = ofMinutes(1)
) {
    init {
        val timer = Timer(name)

        timer.scheduleAtFixedRate(task, delay.toMillis(), period.toMillis())
    }
}
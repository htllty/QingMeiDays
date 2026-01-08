package com.qingmei.days.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun calculateDays(targetDateString: String): Long {
    val today = LocalDate.now()
    val target = LocalDate.parse(targetDateString)
    return ChronoUnit.DAYS.between(today, target)
}
package com.todonote.presentation.common.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Long.formatDate(): String {
    val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
    return sdf.format(Date(this))
}

fun Long.formatDateTime(): String {
    val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
    return sdf.format(Date(this))
}

fun Long.isOverdue(): Boolean {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return this < today.timeInMillis
}

fun Long.isToday(): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = this@isToday }
    val cal2 = Calendar.getInstance()
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

package com.andrewkingmarshall.pexels.util

fun getCurrentTimeInSec(): Long {
    return System.currentTimeMillis() / 1000
}

/**
 * Gets the number of seconds for a particular number of days.
 *
 * @param days The number of days
 * @return The number of seconds in "days"
 */
fun getSecondsInXDays(days: Int): Long {
    return (days * 60 *60 * 24).toLong()
}
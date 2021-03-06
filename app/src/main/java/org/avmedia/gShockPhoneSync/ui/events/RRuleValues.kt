/*
 * Created by Ivo Zivkov (izivkov@gmail.com) on 2022-04-07, 10:24 a.m.
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 2022-04-07, 10:24 a.m.
 */

package org.avmedia.gShockPhoneSync.ui.events

import com.philjay.Frequency
import com.philjay.RRule
import com.philjay.WeekdayNum
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object RRuleValues {
    data class Values(
        var localEndDate: LocalDate? = null,
        var incompatible: Boolean = false,
        var daysOfWeek: ArrayList<DayOfWeek>? = null,
        var repeatPeriod: EventsModel.RepeatPeriod = EventsModel.RepeatPeriod.NEVER
    )

    fun getValues(
        rrule: String?,
        startDate: EventsModel.EventDate,
        zone: ZoneId
    ): Values {
        val rruleValues = Values()

        if (rrule != null && rrule.isNotEmpty()) {

            val rruleObj = RRule(rrule)

            fun isCompatible(rruleObj: RRule): Boolean {
                val validNumberOnly = listOf<Int>(0)
                val numberArr = rruleObj.byDay.map { it.number }

                val validByMonth = rruleObj.byMonth.isEmpty()
                val validByDay = rruleObj.byDay.isEmpty() || validNumberOnly.containsAll(numberArr)
                val validInterval = rruleObj.interval != 1

                return validByMonth && validByDay && validInterval
            }

            if (!isCompatible(rruleObj)) {
                rruleValues.incompatible = true
                Timber.i("Event not compatible with Watch")
            }

            if (rrule.isNotEmpty()) {
                val rruleObjVal = RRule(rrule)
                if (rruleObjVal.until != null) {
                    val formatter: DateTimeFormatter =
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    val localDate =
                        LocalDateTime.parse(rruleObjVal.until.toString(), formatter).toLocalDate()
                    val instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()

                    rruleValues.localEndDate = instant.atZone(zone).toLocalDate()

                } else if (rruleObjVal.count != null) {
                    val numberOfPeriods: Long = (rruleObjVal.count - 1).toLong()
                    if (numberOfPeriods > 1) {
                        when (rruleObjVal.freq) {
                            Frequency.Daily -> {
                                rruleValues.localEndDate =
                                    LocalDate.of(
                                        startDate.year!!,
                                        startDate.month!!,
                                        startDate.day!!
                                    )
                                        .plusDays(numberOfPeriods)

                            }
                            Frequency.Weekly -> {
                                rruleValues.localEndDate =
                                    LocalDate.of(
                                        startDate.year!!,
                                        startDate.month!!,
                                        startDate.day!!
                                    )
                                        .plusWeeks(numberOfPeriods)
                            }
                            Frequency.Monthly -> {
                                rruleValues.localEndDate =
                                    LocalDate.of(
                                        startDate.year!!,
                                        startDate.month!!,
                                        startDate.day!!
                                    )
                                        .plusMonths(numberOfPeriods)
                            }
                            Frequency.Yearly -> {
                                rruleValues.localEndDate =
                                    LocalDate.of(
                                        startDate.year!!,
                                        startDate.month!!,
                                        startDate.day!!
                                    )
                                        .plusYears(numberOfPeriods)
                            }
                        }
                    }
                }

                rruleValues.repeatPeriod = toEventRepeatPeriod(rruleObjVal.freq)
                if (rruleValues.repeatPeriod == EventsModel.RepeatPeriod.WEEKLY) {
                    val weekDays = rruleObjVal.byDay
                    rruleValues.daysOfWeek = createDaysOfWeek(weekDays)
                }
            }
        }

        return rruleValues
    }

    private fun toEventRepeatPeriod(freq: Frequency): EventsModel.RepeatPeriod {
        return when (freq) {
            Frequency.Monthly -> EventsModel.RepeatPeriod.MONTHLY
            Frequency.Weekly -> EventsModel.RepeatPeriod.WEEKLY
            Frequency.Yearly -> EventsModel.RepeatPeriod.YEARLY
            Frequency.Daily -> EventsModel.RepeatPeriod.DAILY
            else -> EventsModel.RepeatPeriod.NEVER
        }
    }

    private fun createDaysOfWeek(weekDays: ArrayList<WeekdayNum>): ArrayList<DayOfWeek>? {
        var days: ArrayList<DayOfWeek>? = ArrayList()
        weekDays.forEach {
            when (it.weekday.name) {
                "Monday" -> days?.add(DayOfWeek.MONDAY)
                "Tuesday" -> days?.add(DayOfWeek.TUESDAY)
                "Wednesday" -> days?.add(DayOfWeek.WEDNESDAY)
                "Thursday" -> days?.add(DayOfWeek.THURSDAY)
                "Friday" -> days?.add(DayOfWeek.FRIDAY)
                "Saturday" -> days?.add(DayOfWeek.SATURDAY)
                else -> days?.add(DayOfWeek.SUNDAY)
            }
        }

        return days
    }
}
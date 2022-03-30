/*
 * Created by Ivo Zivkov (izivkov@gmail.com) on 2022-03-30, 12:06 a.m.
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 2022-03-20, 9:42 p.m.
 */
package org.avmedia.gShockPhoneSync.customComponents

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.avmedia.gShockPhoneSync.ble.Connection
import org.avmedia.gShockPhoneSync.utils.WatchDataEvents
import org.jetbrains.anko.runOnUiThread
import org.json.JSONObject

class AlarmList @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    init {
        adapter = AlarmAdapter(AlarmsData.alarms)
        layoutManager = LinearLayoutManager(context)

        if (AlarmsData.isEmpty()) {
            subscribe("ALARMS", ::onDataReceived)
            Connection.sendMessage(JSONObject("{ action: 'GET_ALARMS'}").toString())
        }
    }

    private fun onDataReceived(data: String) {
        AlarmsData.fromJson(data)
        context.runOnUiThread {
            adapter?.notifyDataSetChanged()
        }
    }

    @SuppressLint("CheckResult")
    private fun subscribe(subject: String, onDataReceived: (String) -> Unit) {
        WatchDataEvents.addSubject(subject)
        WatchDataEvents.subscribe(this.javaClass.simpleName, subject, onNext = {
            onDataReceived(it as String)
        })
    }
}

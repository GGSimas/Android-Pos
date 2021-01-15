package com.aula.agendapos

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.media.MediaPlayer
import androidx.annotation.RequiresApi
import com.aula.db.ContatoRepository

const val SMS_BUNDLE = "pdus"

class SMSReceiver : BroadcastReceiver() {

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {

        val intentExtras = intent.extras
        val subId = intentExtras?.getInt("subscription", -1)
        val sms = intentExtras?.get(SMS_BUNDLE) as Array<Any>
        var smsMessage : SmsMessage? = null

        (0 until sms.size).forEach { i ->
            val format = intentExtras.getString("format")
            smsMessage = SmsMessage.createFromPdu( sms[i] as ByteArray, format )
        }
        if(ContatoRepository(context).isContato(smsMessage?.originatingAddress.toString())){
            val mp = MediaPlayer.create(context, R.raw.gol4)
            mp.start()
        }

    }
}
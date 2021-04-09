package com.jimz.mmqt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import com.jimz.mmqt.databinding.ActivityMainBinding
import org.eclipse.paho.android.service.MqttService

class MainActivity : AppCompatActivity() {
    private lateinit var dataBinding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        initClick()
    }

    private fun initClick(){
        dataBinding.btnStartServer.setOnClickListener {
            startService(Intent(this,MQTTService::class.java))
        }
        dataBinding.btnSendMsg.setOnClickListener {
            val msg = dataBinding.etMqttInput.text
            MQTTService.publish(msg.toString())
        }
        dataBinding.btnStartServerz.setOnClickListener {
            MQTTService.connect()
        }
    }
}
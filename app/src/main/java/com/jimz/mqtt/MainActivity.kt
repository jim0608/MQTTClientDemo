package com.jimz.mqtt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.jimz.mqtt.databinding.ActivityMainBinding

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
package com.jimz.mqtt.ui.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jimz.mqtt.MQTTManager
import org.eclipse.paho.client.mqttv3.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "TAG_HomeViewModel"
    private val mContext: Context = application
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    private val _mqttState = MutableLiveData<String>().apply {
        value = "this is mqtt first msg"
    }
    val text: LiveData<String> = _text
    val mqttState:LiveData<String> = _mqttState

    private var mqttManager: MQTTManager? = null
    private var publishTopic: String = ""
    private var clientId: String = ""

    fun initMqtt(clientId: String) {
        this.clientId = clientId
        mqttManager = MQTTManager(clientId, iMqttActionListener)
        mqttManager?.initMQTT(mContext, receiveCallBack)
    }

    fun connect(publishTopic: String) {
        this.publishTopic = publishTopic
        mqttManager?.connectMQTT()
        mqttManager?.setWill(publishTopic)
    }

    fun subscribe(subscribeTopic: String) {
        mqttManager?.subscribe(clientId)
    }

    fun publish(message: String, publishTopic: String? = this.publishTopic) {
        mqttManager?.publish(publishTopic ?: this.publishTopic, message)
    }



    private val iMqttActionListener = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.i(
                TAG,
                "onSuccess:  \nuserContext:${asyncActionToken?.userContext}  \n topic : ${asyncActionToken?.topics}"
            )
            _mqttState.postValue(asyncActionToken?.topics.toString())
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.i(TAG, "onFailure: exception:${exception?.cause}")
            exception?.printStackTrace()
            _mqttState.postValue(exception?.message.toString())
        }
    }

    private val receiveCallBack = object : MqttCallback {
        override fun connectionLost(cause: Throwable?) {
            Log.i(TAG, "connectionLost: cause :${cause?.cause}")
            cause?.printStackTrace()
            _mqttState.postValue(cause?.message)
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            Log.i(TAG, "messageArrived: $message")
            _text.postValue(message.toString())
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            Log.i(TAG, "deliveryComplete: ${token?.message}")
//            _mqttState.postValue(token?.message.toString())
        }
    }
}
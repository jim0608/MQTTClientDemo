package com.jimz.mqtt

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*


/**
 * @author Create By 张晋铭
 * @Date on 2021/4/7
 * @Describe: mqtt
 */
class MQTTService : Service() {
    private val TAG = "TAG_MQTTServer"

    private val mHOST = "tcp://192.168.0.100:61613"
    private val mUserName = "admin"
    private val mPassword = "password"
    private val mClientId = "clientTestId"
    private var mPublishTopic = "tourist_enter" //发布主题

    private var mResponseTopic = "home" //响应主题

    companion object{
        private var mMQTTAndroidClient: MqttAndroidClient? = null
        private var mMQTTConnectOptions: MqttConnectOptions? = null
        private var mPublishTopic = "gallery" //发布主题

        fun connect(){
            mMQTTAndroidClient?.connect(mMQTTConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i("TAG", "onSuccess: $asyncActionToken")
                    mMQTTAndroidClient?.subscribe("home",2);
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.i("TAG", "onFailure: asy $asyncActionToken  exception ${exception?.message}")
                }
            })
        }
        /**
         * 发布 （模拟其他客户端发布消息）
         *
         * @param message 消息
         */
        fun publish(message: String) {
            val topic: String = mPublishTopic
            val qos = 2
            val retained = false
            try {
                val actionArgs = arrayOfNulls<String>(2)
                actionArgs[0] = message
                actionArgs[1] = topic

                //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
                mMQTTAndroidClient?.publish(
                    topic,
                    message.toByteArray(),
                    qos,
                    retained,
                    null,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.i("TAG", "onSuccess: $asyncActionToken")
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.i("TAG", "onFailure: asy $asyncActionToken  exception ${exception?.message}")
                        }
                    }
                )
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }


    private val mqttCallback = object : MqttCallback {
        override fun connectionLost(cause: Throwable?) {
            // 失去连接，重连
            Log.i(TAG, "connectionLost: cause ${cause?.message}")
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            Log.i(TAG, "messageArrived: topic $topic message $message")
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            Log.i(TAG, "deliveryComplete: token$token")
        }
    }

    //MQTT是否连接成功的监听
    private val iMqttActionListener = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.i(TAG, "onSuccess: $asyncActionToken")
            mMQTTAndroidClient?.subscribe(mResponseTopic,2)
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.i(TAG, "onFailure: asy $asyncActionToken  exception ${exception?.message}")
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initAndroidClient()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        try {
            mMQTTAndroidClient?.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    private fun initAndroidClient() {
        val uri = mHOST
        mMQTTAndroidClient = MqttAndroidClient(this, uri, mClientId)
        mMQTTAndroidClient?.setCallback(mqttCallback)

        mMQTTConnectOptions = MqttConnectOptions()
        mMQTTConnectOptions?.let {
            // 清除缓存
            it.isCleanSession = true
            // 设置超时时间，单位：秒
            it.connectionTimeout = 10
//            it.sslProperties = ssl
            // 心跳包发送间隔，单位：秒
            it.keepAliveInterval = 20
            it.userName = mUserName
            it.password = mPassword.toCharArray()
        }

        // last will message
        var doConnect = true
        val message = "{\"terminal_uid\":\"" + mClientId + "\"}"
        val topic: String = mPublishTopic
        val qos = 2
        val retained = false
        if (message != "" || topic != "") {
            // 最后的遗嘱
            try {
                mMQTTConnectOptions?.setWill(topic, message.toByteArray(), qos, retained)
            } catch (e: Exception) {
                Log.i(TAG, "Exception Occured", e)
                doConnect = false
                iMqttActionListener.onFailure(null, e)
            }
        }
        if (doConnect) {
            doClientConnection()
        }
    }

    /**
     * 连接MQTT服务器
     */
    private fun doClientConnection() {
        if (!mMQTTAndroidClient?.isConnected!! ?: false && isConnectIsNormal()) {
            try {
                mMQTTAndroidClient?.connect(mMQTTConnectOptions, null, iMqttActionListener)
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 判断网络是否连接
     */
    private fun isConnectIsNormal(): Boolean {
        val connectivityManager =
            this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return if (info != null && info.isAvailable) {
            val name = info.typeName
            Log.i(TAG, "当前网络名称：$name")
            true
        } else {
            Log.i(TAG, "没有可用网络")
            /*没有可用网络的时候，延迟3秒再尝试重连*/
            Handler().postDelayed({ doClientConnection() }, 3000)
            false
        }
    }

    /**
     * 开启服务
     */
    fun startService(mContext: Context) {
        mContext.startService(Intent(mContext, MQTTService::class.java))
    }

    /**
     * 发布 （模拟其他客户端发布消息）
     *
     * @param message 消息
     */
    fun publish(message: String) {
        val topic: String = mPublishTopic
        val qos = 2
        val retained = false
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mMQTTAndroidClient?.publish(topic, message.toByteArray(), qos, retained)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    /**
     * 响应 （收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等）
     *
     * @param message 消息
     */
    fun response(message: String) {
        val topic: String = mResponseTopic
        val qos = 2
        val retained = false
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mMQTTAndroidClient?.publish(topic, message.toByteArray(), qos, retained)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}
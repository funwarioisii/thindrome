package me.funwarioisii.thindrome

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Switch
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.swithes.*


class MainActivity : Activity() {

    companion object {
        private const val GREEN_PIN_NAME = "BCM6"
        private const val RED_PIN_NAME = "BCM5"
        private const val YELLOW_PIN_NAME = "BCM12"
        private const val SKELTON_PIN_NAME = "BCM13"
    }

    private val greenSwitch: Switch by lazy { green_switch }
    private val redSwitch :Switch by lazy { red_switch }
    private val yellowSwitch :Switch by lazy { yellow_switch }
    private val skeletonSwitch :Switch by lazy { skeleton_switch }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var greenGpio: Gpio
    private lateinit var redGpio: Gpio
    private lateinit var yellowGpio: Gpio
    private lateinit var skeletonGpio: Gpio

    private val nameList = listOf(GREEN_PIN_NAME, RED_PIN_NAME, YELLOW_PIN_NAME, SKELTON_PIN_NAME)
    private val gpioList by lazy { listOf(greenGpio, redGpio, yellowGpio, skeletonGpio) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(applicationContext)

        val manager: PeripheralManager = PeripheralManager.getInstance()

        // Gpioに最初は流れないようにする
        greenGpio = manager.openGpio(GREEN_PIN_NAME).apply {
            setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        }

        redGpio = manager.openGpio(RED_PIN_NAME).apply {
            setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        }
        yellowGpio = manager.openGpio(YELLOW_PIN_NAME).apply {
            setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        }
        skeletonGpio = manager.openGpio(SKELTON_PIN_NAME).apply {
            setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        }

        // 押すと反転するようにする
        greenSwitch.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
            switch(greenGpio)
        }

        redSwitch.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
            switch(redGpio)
        }

        yellowSwitch.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
            switch(yellowGpio)
        }

        skeletonSwitch.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
            switch(skeletonGpio)
        }

        firestore.collection("led").addSnapshotListener{
            value: QuerySnapshot?, e: FirebaseFirestoreException? ->
            if (e != null) {
                Log.e("FireStore", e.toString())
            }

            for (doc in value!!) {
                for(name in nameList) {
                    // 名前があればStatus変更
                    if (doc.get(name) != null) {
                        turn(name, status = doc.getBoolean(name)!!)
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        greenGpio.close()
        arrayOf(greenGpio, redGpio, yellowGpio, skeletonGpio).forEach { it.close() }
    }


    /**
     * パラメータを反転させる
     * @param [gpio]
     */
    private fun switch(gpio: Gpio) {
        gpio.value = !gpio.value
    }


    /**
     * オン・オフを指定して切り替える
     * @param [name]
     * @param [status]
     */
    private fun turn(name: String, status: Boolean) {
        gpioList.map { gpio ->
            if (gpio.name == name) {
                gpio.value = status
            }
        }
    }
}

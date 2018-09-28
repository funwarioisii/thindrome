package me.funwarioisii.thindrome

import android.app.Activity
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Switch
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
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

    private lateinit var greenGpio: Gpio
    private lateinit var redGpio: Gpio
    private lateinit var yellowGpio: Gpio
    private lateinit var skeletonGpio: Gpio


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val manager: PeripheralManager = PeripheralManager.getInstance()

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
    }


    override fun onDestroy() {
        super.onDestroy()
        greenGpio.close()
        arrayOf(greenGpio, redGpio, yellowGpio, skeletonGpio).forEach { it.close() }
    }


    private fun switch(gpio: Gpio) {
        gpio.value = !gpio.value
    }
}

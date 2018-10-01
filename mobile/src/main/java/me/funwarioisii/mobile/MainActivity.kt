package me.funwarioisii.mobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Switch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.switches.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val GREEN_PIN_NAME = "BCM6"
        private const val RED_PIN_NAME = "BCM5"
        private const val YELLOW_PIN_NAME = "BCM12"
        private const val SKELETON_PIN_NAME = "BCM13"
    }

    private val redSwitch by lazy { red_switch }
    private val greenSwitch by lazy { green_switch }
    private val yellowSwitch by lazy { yellow_switch }
    private val skeletonSwitch by lazy { skeleton_switch }

    private val nameList = listOf(GREEN_PIN_NAME, RED_PIN_NAME, YELLOW_PIN_NAME, SKELETON_PIN_NAME)
    private val statusMap = hashMapOf<String, Boolean?>(
            GREEN_PIN_NAME to null,
            RED_PIN_NAME to null,
            YELLOW_PIN_NAME to null,
            SKELETON_PIN_NAME to null
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val firestore = FirebaseFirestore.getInstance()

        firestore
                .collection("led")
                .addSnapshotListener {
                    value: QuerySnapshot?, e: FirebaseFirestoreException? ->
                    if (e != null) {
                        Log.e("FireStore", e.toString())
                    }

                    value?.forEach { doc ->
                        nameList.forEach {
                            val status = doc.getBoolean(it)
                            if (status != null) {
                                turn(it, status)
                                statusMap[it] = status
                            }
                        }
                    }
                }

        redSwitch.turn(firestore)
        greenSwitch.turn(firestore)
        skeletonSwitch.turn(firestore)
        yellowSwitch.turn(firestore)
    }


    /**
     * gpio名→Switch
     * @param [name]
     */
    private fun gpioToSwitch(name: String) :Switch {
        return when(name) {
            GREEN_PIN_NAME -> greenSwitch
            RED_PIN_NAME -> redSwitch
            YELLOW_PIN_NAME -> yellowSwitch
            SKELETON_PIN_NAME -> skeletonSwitch
            else -> throw Error("No such key: $name")
        }
    }


    /**
     * switch -> GPIO名
     * @param [switch]
     */
    private fun switchToName(switch: Switch) : String {
        return when(switch) {
            greenSwitch -> GREEN_PIN_NAME
            redSwitch -> RED_PIN_NAME
            yellowSwitch -> YELLOW_PIN_NAME
            skeletonSwitch -> SKELETON_PIN_NAME
            else -> ""
        }
    }


    /**
     * 指定したSwitchの変更
     * @param [name]
     * @param [status]
     */
    private fun turn(name: String, status: Boolean) {
        val switch = gpioToSwitch(name)
        switch.isChecked = status
    }


    /**
     * Switch押下時にFireStoreへの変更通知など
     */
    private fun Switch.turn(firestore: FirebaseFirestore) {
        setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->

            Log.d("name", "${this}")

            val name = switchToName(this)
            val before = statusMap.clone() as HashMap<String, Boolean>
            statusMap[name] = b
            if (before == statusMap) return@setOnCheckedChangeListener  // 何故かバグが減る


            firestore
                    .batch()
                    .update(
                            firestore.collection("led").document("light"),
                            statusMap as Map<String, Boolean>)
                    .commit()
                    .addOnCompleteListener { task ->
                        if (task.exception != null) {
                            Log.e("batch update", task.exception.toString())
                        }
                    }
        }
    }
}

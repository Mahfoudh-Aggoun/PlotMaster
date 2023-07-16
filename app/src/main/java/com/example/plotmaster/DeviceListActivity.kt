package com.example.plotmaster

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.plotmaster.events.UiToastEvent
import org.greenrobot.eventbus.EventBus

class DeviceListActivity : AppCompatActivity() {
    object Constants {
        const val EXTRA_DEVICE_ADDRESS = "device_address"
    }

    private var mBtAdapter: BluetoothAdapter? = null
    private var mNewDevicesArrayAdapter: ArrayAdapter<String>? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBtAdapter == null) {
            EventBus.getDefault()
                .post(UiToastEvent(getString(R.string.text_bluetooth_adapter_error), true, true))
            finish()
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_device_list)
        setResult(RESULT_CANCELED)

        val pairedDevicesArrayAdapter = ArrayAdapter<String>(this, R.layout.device_name)
        mNewDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)

        val pairedListView = findViewById<ListView>(R.id.paired_devices)
        pairedListView.adapter = pairedDevicesArrayAdapter
        pairedListView.onItemClickListener = mDeviceClickListener

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(mReceiver, filter)

        var pairedDevices: Set<BluetoothDevice?>? = emptySet<BluetoothDevice>()
        try {
            pairedDevices = mBtAdapter?.getBondedDevices()
        } catch (e: Exception) {
            EventBus.getDefault()
                .post(UiToastEvent(getString(R.string.text_bluetooth_adaptor_error), true, true))
            finish()
        }

        if (pairedDevices!!.size > 0) {
            for (device in pairedDevices) {
                pairedDevicesArrayAdapter.add(""" ${device!!.name}${device.address}""".trimIndent())
            }
        } else {
            pairedDevicesArrayAdapter.add(getString(R.string.text_no_paired_devices))
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        if (mBtAdapter != null) {
            mBtAdapter!!.cancelDiscovery()
        }
        unregisterReceiver(mReceiver)
    }

    @SuppressLint("MissingPermission")
    private val mDeviceClickListener = OnItemClickListener { av, v, arg2, arg3 ->
            mBtAdapter!!.cancelDiscovery()
            val info = (v as TextView).text.toString()
            if (info.length > 16) {
                val address = info.substring(info.length - 17)
                val intent = Intent()
                intent.putExtra(DeviceListActivity.Constants.EXTRA_DEVICE_ADDRESS, address)
                setResult(RESULT_OK, intent)
                finish()
            }
        }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device!!.bondState != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter!!.add(
                        """
                        ${device.name}
                        ${device.address}
                        """.trimIndent()
                    )
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setProgressBarIndeterminateVisibility(false)
                title = getString(R.string.text_select_paired_device)
                if (mNewDevicesArrayAdapter!!.count == 0) {
                    mNewDevicesArrayAdapter!!.add(getString(R.string.text_none_found))
                }
            }
        }
    }


}
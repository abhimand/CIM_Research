package com.example.abhimand.blujanky;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.abhimand.blujanky.*;

import java.util.ArrayList;
import java.util.HashMap;


public class DeviceMap {

    HashMap<String, String> bt_DeviceMap = new HashMap <String, String>();                          //key = address, value = name


    public void tryAddDevice(BluetoothDevice dev, ArrayList string_AL, ArrayAdapter <String> array_AdapterName, ArrayAdapter <String> array_AdapterAddress) {
        if (bt_DeviceMap.containsKey(dev.getAddress()) || bt_DeviceMap.containsValue(dev.getName())) {
            System.out.println("This device is already on the list");
        } else {
            if(dev.getName() == null) {
                System.out.println("There is currently no name for this device");
            }else {
                string_AL.add(dev.getName());
                array_AdapterName.notifyDataSetChanged();
            }if (dev.getAddress() != null) {
                    string_AL.add(dev.getAddress());
                    array_AdapterAddress.notifyDataSetChanged();
                    bt_DeviceMap.put(dev.getAddress(), dev.getName());
            }
        }
    }



 }

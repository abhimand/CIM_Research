/* In this section, I wanted to save the device's name and address so that a user can later on
 * revise the name and address locally by using a hashmap to store address and key as key and value,
 * respectively
 */


package edu.utexas.mpc.cimobserver;

import android.bluetooth.BluetoothDevice;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;


public class DeviceMap {

    HashMap<String, String> bt_DeviceMap = new HashMap <String, String>();                                       //key = address, value = name


    public void tryAddDevice(BluetoothDevice dev, ArrayList string_AL,
                             ArrayAdapter <String> array_AdapterName,
                             ArrayAdapter <String> array_AdapterAddress) {
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

    public void renameLocal(EditText address, EditText name) {
        bt_DeviceMap.put(address.getText().toString(), name.getText().toString());
    }



}

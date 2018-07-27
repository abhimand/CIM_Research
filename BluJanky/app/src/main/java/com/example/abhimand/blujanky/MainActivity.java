/*
 * BluJanky
 * Created by Abhishek E. Mandal
 * Summer 2018
 */
package com.example.abhimand.blujanky;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    Button button_On, button_Off, button_Discoverable, button_Scan, button_Get;
    ListView scan_ListName;
    ArrayList <String> string_AL = new ArrayList<String>();
    ArrayAdapter <String> array_AdapterName;
    ArrayAdapter <String> array_AdapterAddress;
    BluetoothAdapter bt_Adapter;



    Intent bt_Enable_Intent;
    int req_Enable;
    int i;                                                                                          //counter


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_On = (Button) findViewById(R.id.button_on);
        button_Off = (Button) findViewById(R.id.button_off);
        button_Discoverable = (Button) findViewById(R.id.button_discoverable);
        button_Scan = (Button) findViewById(R.id.button_scan);
        button_Get = (Button) findViewById(R.id.button_get);
        scan_ListName = (ListView) findViewById(R.id.scan_list);
        bt_Adapter = BluetoothAdapter.getDefaultAdapter();



        bt_Enable_Intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        req_Enable = 1;

        //methods
        bt_On_Method();
        bt_Off_Method();
        bt_Discoverable_Method();
        bt_Scan_Method();
        bt_getName_Method();

        /* *****************************************************************************************
         *                              DISCOVER DEVICES PORTION
         * ****************************************************************************************/
        IntentFilter intent_Filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver, intent_Filter);

        array_AdapterName = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1,string_AL);
        scan_ListName.setAdapter(array_AdapterName);

        array_AdapterAddress = new ArrayAdapter<String>(getApplicationContext(),                    //inclusion of bluetooth device address
                android.R.layout.simple_list_item_1, string_AL);
        scan_ListName.setAdapter(array_AdapterAddress);
    }

    /* *********************************************************************************************
     *
     *                                  ENABLE BLUETOOTH
     *                                  BLUETOOTH ON/OFF BUTTONS
     *
     * ********************************************************************************************/

    private void bt_On_Method () {
        button_On.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bt_Adapter == null) {                                                            // check if bluetooth is supported
                    Toast.makeText(getApplicationContext(),                                         // if it is not, print toast
                            "Bluetooth does not support on ths device",
                            Toast.LENGTH_LONG).show();
                } else if (!bt_Adapter.isEnabled())  {                                                                             // otherwise// check if adapter is turned on
                    startActivityForResult(bt_Enable_Intent, req_Enable);                       // if not, turn on
                    Toast.makeText(getApplicationContext(),"Bluetooth is now enabled", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
    private void bt_Off_Method() {
        button_Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if(bt_Adapter.isEnabled()) {                                                        // check if it is enabled
                    Toast.makeText(getApplicationContext(),
                            "Bluetooth is now disabled",
                            Toast.LENGTH_LONG).show();
                    bt_Adapter.disable();                                                           // if it is, disable
                }
            }
        });
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == req_Enable) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(),
                        "Bluetooth is now enabled",
                        Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        "Bluetooth enabling cancelled",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /* *********************************************************************************************
     *
     *                                  ENABLING DISCOVERABILITY
     *                                  BLUETOOTH DISCOVERABLE BUTTON
     *
     * ********************************************************************************************/
    private void bt_Discoverable_Method() {
        button_Discoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                                                           // this button makes the device discoverable
                Intent intent_Discoverable = new Intent                                             // compared to just simply turning on the device
                        (BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent_Discoverable.putExtra
                        (BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,30);                     // value in this case is the amount of seconds discoverabliity is on
                startActivity(intent_Discoverable);
            }
        });
    }

    /* *********************************************************************************************
     *
     *                                  DISCOVER DEVICES
     *                                  BLUETOOTH SCAN BUTTON
     *
     * ********************************************************************************************/

    private void bt_Scan_Method() {
        button_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                bt_Adapter.startDiscovery();
            }
        });
    }

    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            DeviceMap dev_Map = new DeviceMap();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                dev_Map.tryAddDevice(dev,string_AL, array_AdapterName, array_AdapterAddress);




                //if(dev.getName() == null) {                                                         //checks if name is null
                //    System.out.println("There is currently no name available for this device");
                //    System.out.println("The address is: " + dev.getAddress());
                //} else {                                                                            // check if appeared once before, see if array list has contains method
                //    if (!(string_AL.contains(dev.getName()) || string_AL.contains(dev.getAddress()))) {
                //        string_AL.add(dev.getName());                                                   // use map to have the name and address of the device, manipulate
                //        array_AdapterName.notifyDataSetChanged();
                //    }
                //}
            }



            /*
             * To link and edit our names in secondary activity,
             * Create and intent to start the secondary activity
             * so Intent intent = new Intent(this.SecondActivity.class);
             *
             * To Start the new Activity, startActivity (randomIntent);
             */
        }
    };


    /* *********************************************************************************************
     *
     *
     *                                  BLUETOOTH GET/SET NAME---------in progress
     *
     * ********************************************************************************************/

    private void bt_getName_Method() {
        button_Get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "localdevicename:" + bt_Adapter.getName()
                                + " localdeviceAddress: " + bt_Adapter.getAddress()
                        , Toast.LENGTH_LONG).show();

                bt_Adapter.setName("Foo");

                Toast.makeText(MainActivity.this, "localdevicename:" + bt_Adapter.getName()
                                + " localdeviceAddress: " + bt_Adapter.getAddress()
                        , Toast.LENGTH_LONG).show();
            }
        });
    }
}

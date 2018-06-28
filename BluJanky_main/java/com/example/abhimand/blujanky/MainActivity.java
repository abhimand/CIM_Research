/*
 * BluJanky is a preliminary version
 * Created by Abhishek Mandal
 * Summer 2018
 */
package com.example.abhimand.blujanky;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button b_on, b_off, b_scanner, b_list, b_discoverable;
    TextView text_scan;
    ListView list;

    ArrayList<String> stringArrayList = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;


    private static final int REQUEST_ENABLED = 0;
    private static final int REQUEST_DISCOVERABLE = 0;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    BroadcastReceiver myReceiver = new BroadcastReceiver() {                                        //BroadcastReceiver for b_scanner
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                stringArrayList.add(device.getName());
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b_on = (Button) findViewById(R.id.b_on);
        b_off = (Button) findViewById(R.id.b_off);
        b_scanner = (Button) findViewById(R.id.b_scanner);
        b_list = (Button) findViewById(R.id.b_list);
        b_discoverable = (Button) findViewById(R.id.b_discoverable);

        text_scan = (TextView) findViewById(R.id.text_scan);

        list = (ListView) findViewById(R.id.list);

        IntentFilter intentFilter = new IntentFilter (BluetoothDevice.ACTION_FOUND);
        IntentFilter scanIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

        registerReceiver(myReceiver, intentFilter);

        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,stringArrayList);
        list.setAdapter(arrayAdapter);





        BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {                                   //BroadcastReceiver for b_discoverable
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
                {
                    int modeValue = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                    if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                        text_scan.setText("The deivce is no discoverable mode but can still receive connections");
                    } else if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        text_scan.setText("The deivice is in discoverable mode");

                    } else if (modeValue == bluetoothAdapter.SCAN_MODE_NONE) {

                        text_scan.setText("The device is not in discoverable mode and cannot receive connections");
                    } else {
                        text_scan.setText("Error");
                    }
                }
            }
        };

        //check if bluetooth is supported
        if(bluetoothAdapter == null) {
            new AlertDialog.Builder(this)                       //if bluetoothadapter is null, alert
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            //Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            //finish();
        }
        b_on.setOnClickListener(new View.OnClickListener() {                                                //code snippet for on button                                                                      //ON BUTTON
            @Override
            public void onClick(View view) {
                //turn on bluetooth
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLED);
            }
        });
        b_off.setOnClickListener(new View.OnClickListener() {                                               //code snippet for off button
            @Override
            public void onClick(View view) {
                //turn off bluetooth
                bluetoothAdapter.disable();
                b_on.setBackgroundColor(Color.BLUE);
                b_on.setTextColor(Color.WHITE);
            }
        });
        b_scanner.setOnClickListener(new View.OnClickListener() {                                           //code snippet for scan button
            @Override
            public void onClick(View view) {
                bluetoothAdapter.startDiscovery();
            }
        });
        b_list.setOnClickListener(new View.OnClickListener() {                                              //code snippet for list
            @Override
            public void onClick(View view) {
               // list paired device
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                ArrayList<String> devices = new ArrayList<String>();
                for (BluetoothDevice bt : pairedDevices) {
                    devices.add(bt.getName());
                }
                ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, devices);
                list.setAdapter(arrayAdapter);
            }
        });
        b_discoverable.setOnClickListener(new View.OnClickListener() {                                      // code snippet for discoverable
            @Override
            public void onClick(View view) {
                //make the device discoverable
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 10);
                startActivity(discoverableIntent);

                //Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                //startActivityForResult(intent, REQUEST_DISCOVERABLE);

            }
        });
        registerReceiver(scanModeReceiver, scanIntentFilter);


    }
}

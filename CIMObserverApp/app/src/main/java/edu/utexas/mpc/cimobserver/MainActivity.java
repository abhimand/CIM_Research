/*
 * CIM Android Observer App
 * Created by Abhishek E. Mandal and Christine Julien
 * Summer 2018
 */
package edu.utexas.mpc.cimobserver;


import android.Manifest.permission;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // switch to allow control of Bluetooth on/off
    private Switch btSwitch;

    // pair of buttons to initiate Bluetooth activities (scan or advertise)
    private Button buttonDiscoverable, buttonScan, buttonClear;


    private ListView scan_ListName;
    private ArrayList<String> addressesForListView = new ArrayList<String>();
    //private ArrayAdapter<String> array_AdapterName;
    private ArrayAdapter<String> array_AdapterAddress;

    // A handle to the device's bluetooth adapter
    private BluetoothAdapter btAdapter;

    // data structure to map a discovered device's address to its corresponding BTDeviceStruct
    // the BTDeviceStruct is a data class in this project for storing info about a discovered device
    private HashMap<String, BTDeviceStruct> addressToNameMap = new HashMap<String, BTDeviceStruct>();

    public static final String SELECTED_ADDRESS = "edu.utexas.mpc.cimobserver.SELECTED_ADDRESS";
    public static final String BEACON_DATA = "edu.utexas.mpc.cimobserver.BEACON_DATA";
    public static final String NEW_NAME = "edu.utexas.mpc.cimobserver.NEW_NAME";

    private static final int REQ_ENABLE = 1;
    private static final int REQ_RENAME = 2;

    private Intent bt_Enable_Intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get a handle to the device's bluetooth adapter
        // if the device does not support bluetooth, this will be null
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        buttonDiscoverable = findViewById(R.id.button_discoverable);
        buttonScan = findViewById(R.id.button_scan);
        buttonClear = findViewById(R.id.clear);
        scan_ListName = findViewById(R.id.scan_list);

        // set up the switch to toggle the state of the device's bluetooth
        btSwitch = findViewById(R.id.btSwitch);
        btSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if the device does not support bluetooth, don't try to mess with it
                if (btAdapter == null) {
                    Toast.makeText(getApplicationContext(),
                            "Bluetooth is not supported on ths device",
                            Toast.LENGTH_LONG).show();
                }
                // if the switch is checked and the bt is off, then enable it
                else if (btSwitch.isChecked() && !btAdapter.isEnabled()) {
                    startActivityForResult(bt_Enable_Intent, REQ_ENABLE);
                }
                // if the switch is not checked and the bt is on, disable it
                else if (!btSwitch.isChecked() && btAdapter.isEnabled()) {
                    btAdapter.disable();
                    Toast.makeText(getApplicationContext(),
                            "Bluetooth is now disabled",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // set up the scan button to initiate a bluetooth scan
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btAdapter.isEnabled()) {
                    initiateBTScan();
                }
            }
        });

        // set up the discoverable button to start advertising
        buttonDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create intent that will actually activate advertising
                Intent intent_Discoverable = new Intent
                        (BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                // advertise for 30 seconds
                intent_Discoverable.putExtra
                        (BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
                startActivity(intent_Discoverable);
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressToNameMap.clear();
                addressesForListView.clear();
                array_AdapterAddress.notifyDataSetChanged();
            }
        });


        bt_Enable_Intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        //methods
        bt_listName_Method();

        array_AdapterAddress = new ArrayAdapter<String>(getApplicationContext(),                    //inclusion of bluetooth device address
                android.R.layout.simple_list_item_1, addressesForListView);
        scan_ListName.setAdapter(array_AdapterAddress);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // set the initial state of the switch to match the BT state on the device
        btSwitch.setChecked(btAdapter.isEnabled());

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_ENABLE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(),
                        "Bluetooth is now enabled",
                        Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        "Bluetooth enabling cancelled",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQ_RENAME) {
            if (resultCode == RESULT_OK) {
                String newName = data.getStringExtra(NEW_NAME);
                String address = data.getStringExtra(SELECTED_ADDRESS);
                if (newName != "") {
                    addressToNameMap.get(address).updateFriendlyName(newName);
                    int index = addressesForListView.indexOf(address); // this is brittle; we can only rename once...
                    String displayString = makeDisplayString(address, newName);
                    addressesForListView.set(index, displayString);
                    array_AdapterAddress.notifyDataSetChanged();
                }
            }
        }
    }


    /* *********************************************************************************************
     *
     *                                  DISCOVER DEVICES
     *                                  BLUETOOTH SCAN BUTTON
     *
     * ********************************************************************************************/

    private void initiateBTScan() {
        final int REQUEST_ACCESS_COARSE_LOCATION = 1;
        /*
         *  https://stackoverflow.com/a/36177638
         *  controlling for different permissions models relative to bluetooth scanning
         */
        // Only ask for these permissions at runtime when running Android 6.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switch (ContextCompat.checkSelfPermission(getBaseContext(), permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    ((TextView) new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Runtime Permissions Required")
                            .setMessage(Html.fromHtml("<p>To enable the app to find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>"))
                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ContextCompat.checkSelfPermission(getBaseContext(),
                                            permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{permission.ACCESS_COARSE_LOCATION},
                                                REQUEST_ACCESS_COARSE_LOCATION);
                                    }
                                }
                            })
                            .show()
                            .findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }
        }

        // only start discovery after we have the needed location permission on newer devices
        //btAdapter.startDiscovery();
        btAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice dev = result.getDevice();
                // if this device is already known to us, let's update its data
                if (isBLEndDevice(result.getScanRecord())) {
                    if (addressToNameMap.containsKey(dev.getAddress())) {
                        BTDeviceStruct knownDevice = addressToNameMap.get(dev.getAddress());
                        knownDevice.updateScanRecord(result.getScanRecord());
                    }
                    // if this device was not known to us, we need to create it
                    else {
                        BTDeviceStruct newDevice = new BTDeviceStruct(dev, null, result.getScanRecord());
                        addressToNameMap.put(dev.getAddress(), newDevice);
                        addressesForListView.add(dev.getAddress());
                        array_AdapterAddress.notifyDataSetChanged();
                    }
                }

            }
        });
    }

    private boolean isBLEndDevice(ScanRecord scanRecord) {
        byte[] beaconData = scanRecord.getBytes();
        if (beaconData[0] == 0x02 && beaconData[1] == 0x01 && beaconData[2] == 0x04
                && beaconData[3] == 0x1A && beaconData[4] == -1 && beaconData[5] == 0x59){
            return true;
        }
        else{
            return false;
        }
    }


    /* *********************************************************************************************
     *
     *
     *                                  BLUETOOTH LIST NAME METHOD---------in progress
     *
     *                                  Why is startActivity not working?
     *
     *
     * ********************************************************************************************/

    public void bt_listName_Method() {
        scan_ListName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id){

                //create intent
                Intent intentNew = new Intent(MainActivity.this, BTDeviceLocalNameActivity.class);
                // grab the selected address (it's a string) and pass it through to the intent
                String displayName = (String)scan_ListName.getItemAtPosition(position);
                String address = getAddressFromDisplayString(displayName);
                Log.d("MainActivity", "selected address: " + address);
                intentNew.putExtra(SELECTED_ADDRESS, address);
                ScanRecord record = addressToNameMap.get(address).getRecentAdvData();
                byte[] beaconData = record.getBytes();
                HashMap<Integer, Integer> beaconTimers = addressToNameMap.get(address).getBeaconTimers();
                String beaconDataAsString = prettyPrintBeaconData(beaconTimers);
                Log.d("MainActivity", beaconDataAsString);
                intentNew.putExtra(BEACON_DATA, beaconDataAsString);
                //start activity
                startActivityForResult(intentNew, REQ_RENAME);
            }
        });
    }


    private String prettyPrintBeaconData(HashMap<Integer, Integer> beaconTimers){
        StringBuilder sb = new StringBuilder("");
        for(int i = 0; i < 17; i++){
            Integer counter = beaconTimers.get(new Integer(i));
            sb.append(i).append(": ").append(counter.intValue()).append("\n");
        }
        /*int i = 0;
        int c = 0;
        while(i < 7){
            sb.append(Integer.toBinaryString(beaconData[i] & 255 | 256).substring(1)).append("   ");
            i++;
            c++;
            if(c == 3){
                c = 0;
                sb.append("\n");
            }
        }
        //int j = Math.min(beaconData.length-1,30);
        c = 0;
        sb.append("\n\n");
        /*while(j > 6){
            sb.append(Integer.toBinaryString(beaconData[j] & 255 | 256).substring(1)).append("   ");
            j--;
            c++;
            if(c == 3){
                c = 0;
                sb.append("\n");
            }
        }

        //TODO: all of this gets moved later!
        //create payload byte array
        byte[] payload = new byte[24];
        int beaconDataCounter = 30;
        int payloadCounter = 0;
        while(payloadCounter < payload.length){
            payload[payloadCounter] = beaconData[beaconDataCounter];
            beaconDataCounter--;
            payloadCounter++;
        }
        int j = 0;
        while(j < payload.length){
            sb.append(Integer.toBinaryString(payload[j] & 255 | 256).substring(1)).append("   ");
            j++;
            c++;
            if(c == 3){
                c = 0;
                sb.append("\n");
            }
        }

        BitUtils bit = new BitUtils(payload);
        bit.getNextInteger(8); // should be 0 because Arkan only uses 23 bytes
        for(int k = 0; k<17; k++){
            System.out.println("COUNTER[" + k + "]: " + (bit.getNextIntegerSigned(10)));
        }*/

        return sb.toString();
    }

    private String makeDisplayString(String address, String name){
        return (address + " [ " + name + " ]");
    }

    private String getAddressFromDisplayString(String displayString){
        String[] split = displayString.split(" ");
        return split[0];
    }

}




/*
 * CIM Android Observer App
 * Created by Abhishek E. Mandal and Christine Julien
 * Summer 2018
 */

package edu.utexas.mpc.cimobserver;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;

import java.util.HashMap;

import fr.devnied.bitlib.BitUtils;

/* This is a basic data class to encapsulate information about a discovered bluetooth device.
   It contains both the device itself and the friendly name of the device. It's primary use
   will be to help populate the ListView in the MainActivity; these BTDeviceStructs will
   be place in a map from BT address to one of these info structs.
 */
class BTDeviceStruct {

    private BluetoothDevice device;
    private String friendlyName;
    private ScanRecord recentAdvData;
    private HashMap<Integer, Integer> beaconTimers = new HashMap<>();

    // create a new BTDevice struct using the provided device object and friendly name
    BTDeviceStruct(BluetoothDevice device, String friendlyName, ScanRecord recentAdvData){
        this.device = device;
        this.friendlyName = friendlyName;
        this.recentAdvData = recentAdvData;
        generateBeaconTimers();
    }

    // retrieve the friendly name
    String getFriendlyName(){
        return friendlyName;
    }

    // update the friendly name
    void updateFriendlyName(String newName){
        this.friendlyName = newName;
    }

    // get the representative device
    BluetoothDevice getDevice(){
        return device;
    }

    // get the BT address associated with the device
    String getAddress(){
        return device.getAddress();
    }

    ScanRecord getRecentAdvData(){
        return recentAdvData;
    }

    HashMap<Integer, Integer> getBeaconTimers(){
        return beaconTimers;
    }

    void updateScanRecord(ScanRecord newScanRecord){
        recentAdvData = newScanRecord;
        generateBeaconTimers();
    }

    private void generateBeaconTimers(){
        byte[] temp = recentAdvData.getBytes();
        byte[] payload = new byte[24];
        int beaconDataCounter = 30;
        int payloadCounter = 0;
        while(payloadCounter < payload.length){
            payload[payloadCounter] = temp[beaconDataCounter];
            beaconDataCounter--;
            payloadCounter++;
        }

        BitUtils bit = new BitUtils(payload);
        bit.getNextInteger(8); // should be 0 because Arkan only uses 23 bytes; we just skip this one
        for(int k = 0; k<17; k++){
            //System.out.println("COUNTER[" + k + "]: " + (bit.getNextIntegerSigned(10)));
            beaconTimers.put(new Integer(16-k), new Integer(bit.getNextIntegerSigned(10)));
            System.out.println("Putting: " + (16-k) + ", " + beaconTimers.get(new Integer(16-k)));
        }


        //System.out.println(" ++++++++ " + prettyPrintBeaconData(temp));
        //System.out.println(" +++ first byte +++ " + bu.getNextHexaString(8));
        //System.out.println(" +++ second byte +++ " + bu.getNextHexaString(8));

    }

    private String prettyPrintBeaconData(byte[] beaconData) {
        StringBuilder sb = new StringBuilder("");
        int i = 0;
        while (i < 7) {
            sb.append(Integer.toBinaryString(beaconData[i] & 255 | 256).substring(1));
            i++;
        }
        int j = Math.min(beaconData.length - 1, 31);
        sb.append("\n\n");
        while (j > 6) {
            sb.append(Integer.toBinaryString(beaconData[j] & 255 | 256).substring(1));
            j--;
        }
        return sb.toString();
    }
}

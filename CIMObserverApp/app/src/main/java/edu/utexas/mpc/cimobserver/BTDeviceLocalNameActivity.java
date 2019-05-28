/*
 * in this portion of code, I want to click and edit the name and address of a device
 * that has been scanned on the listview from bluetooth, and locally store it in the origin device
 * must be done since nordic devices may or may not have names and addresses give in the first place
 */


package edu.utexas.mpc.cimobserver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BTDeviceLocalNameActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btrename);

        Intent intent = getIntent();
        final String address = intent.getStringExtra(MainActivity.SELECTED_ADDRESS);
        String beaconData = intent.getStringExtra(MainActivity.BEACON_DATA);

        // Capture the layout's TextView and set the string as its text
        TextView textViewAddress = findViewById(R.id.textViewAddress);
        textViewAddress.setText(address);

        TextView textViewBeaconData = findViewById(R.id.textViewBeaconData);
        textViewBeaconData.setText(beaconData);

        Button renameButton = findViewById(R.id.button_rename);
        final EditText newNameEditText = (EditText)findViewById(R.id.new_name);
        renameButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent result = new Intent();
                String newName = newNameEditText.getText().toString();
                result.putExtra(MainActivity.NEW_NAME, newName);
                result.putExtra(MainActivity.SELECTED_ADDRESS, address);
                setResult(RESULT_OK, result);
                finish();
            }
        });

        /*textName = (TextView) findViewById(R.id.textViewName);
        textAddress = (TextView) findViewById(R.id.textViewAddress);
        textEditName = (EditText) findViewById(R.id.editTextName);
        button_Rename = (Button) findViewById(R.id.button_rename);
        bt2_Adapter = BluetoothAdapter.getDefaultAdapter();*/

        //renameLocals();

    }

}

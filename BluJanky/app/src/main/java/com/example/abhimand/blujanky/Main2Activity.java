/*
 * in this portion of code, I want to click and edit the name and address of a device
 * that has been scanned on the listview from bluetooth, and locally store it in the origin device
 * must be done since nordic devices may or may not have names and addresses give in the first place
 */


package com.example.abhimand.blujanky;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.abhimand.blujanky.DeviceMap;
import com.example.abhimand.blujanky.R;

public class Main2Activity extends AppCompatActivity {

    TextView textName;
    TextView textAddress;
    EditText textEditName;
    EditText textEditAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        textName = (TextView) findViewById(R.id.textViewName);
        textAddress = (TextView) findViewById(R.id.textViewAddress);
        textEditName = (EditText) findViewById(R.id.editTextName);
        textEditAddress = (EditText) findViewById(R.id.editTextAddress);

        renameLocals();

    }

    public void renameLocals() {                                                                    //This method renames the local address and name of the device that is clicked on
        textEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceMap.bt_DeviceMap(textEditAddress.getText().toString(),                        // LEFT OFF HERE
                        textEditName.getText().toString());                                         // How can I make this work?


            }
        });
    }


}

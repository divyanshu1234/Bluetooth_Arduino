package divyanshu.bluetooth_arduino;

/**
 * Created by Divyanshu on 11/8/2016.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class EnterPinActivity extends Activity {

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    public static String door_pin = "1111";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module
    public String newAddress = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_pin);

        // Set up a pointer to the remote device using its address.
        //getting the bluetooth adapter value and calling checkBTstate function
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        door_pin = readFromFile();
        Log.d("Directory",getApplicationInfo().dataDir);
        activityenterpin();
    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("savePin.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            File file= new File("savePin.txt");
            Log.d("Holala",file.getAbsolutePath());


        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile() {

        Context context = getApplicationContext();
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("savePin.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        if (ret.equals("")) {
            return door_pin;
        } else {
            return ret;
        }
    }

    private void activityenterpin() {
        final TextInputLayout til_enter_pin = (TextInputLayout) findViewById(R.id.til_enter_pin);
        final Button b_enter_pin = (Button) findViewById(R.id.b_enter_pin);

        b_enter_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(til_enter_pin.getEditText().getText().toString().equals(door_pin))
                {
                    sendData("0");
                    menuactivity();
                }

                else{
                    til_enter_pin.setError("Wrong Pin");
                    til_enter_pin.getEditText().setText("");
                }

            }
        });
    }

    private void menuactivity() {
        setContentView(R.layout.activity_menu);
        final Button b_lock = (Button) findViewById(R.id.b_lock);
        final Button b_change_pin = (Button) findViewById(R.id.b_change_pin);

        b_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_enter_pin);
                sendData("1");
                activityenterpin();
            }
        });

        b_change_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changepinactivity();
            }
        });
    }

    private void changepinactivity() {
        setContentView(R.layout.activity_change_pin);
        final TextInputLayout til_enter_original_pin = (TextInputLayout) findViewById(R.id.til_enter_original_pin);
        final TextInputLayout til_enter_new_pin = (TextInputLayout) findViewById(R.id.til_enter_new_pin);
        final TextInputLayout til_reenter_new_pin = (TextInputLayout) findViewById(R.id.til_reenter_new_pin);

        final Button b_enter_change_pin = (Button) findViewById(R.id.b_enter_change_pin);

        b_enter_change_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(til_enter_new_pin.getEditText().getText().toString().equals(til_reenter_new_pin.getEditText().getText().toString()))
                {
                    if(til_enter_new_pin.getEditText().getText().toString().length()==4){
                        if(til_enter_original_pin.getEditText().getText().toString().equals(door_pin)) {
                            door_pin = til_enter_new_pin.getEditText().getText().toString();
                            writeToFile(door_pin);
                            menuactivity();
                        }
                        else{
                            til_enter_original_pin.setError("Incorrect Pin");
                        }
                    }
                    else{
                        til_enter_new_pin.setError("Pin should be 4 characters long");
                    }
                }

                else{
                    til_reenter_new_pin.setError("Pins do not match");
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // connection methods are best here in case program goes into the background etc

        //Get MAC address from DeviceListActivity
        Intent intent = getIntent();
        newAddress = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        // Set up a pointer to the remote device using its address.
        BluetoothDevice device = btAdapter.getRemoteDevice(newAddress);

        //Attempt to create a bluetooth socket for comms
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e1) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create Bluetooth socket", Toast.LENGTH_SHORT).show();
        }

        // Establish the connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();        //If IO exception occurs attempt to close socket
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "ERROR - Could not close Bluetooth socket", Toast.LENGTH_SHORT).show();
            }
        }

        // Create a data stream so we can talk to the device
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create bluetooth outstream", Toast.LENGTH_SHORT).show();
        }
        //When activity is resumed, attempt to send a piece of junk data ('x') so that it will fail if not connected
        // i.e don't wait for a user to press button to recognise connection failure
    }

    @Override
    public void onPause() {
        super.onPause();
        //Pausing can be the end of an app if the device kills it or the user doesn't open it again
        //close all connections so resources are not wasted

        //Close BT socket to device
        try     {
            btSocket.close();
        } catch (IOException e2) {
            Toast.makeText(getBaseContext(), "ERROR - Failed to close Bluetooth socket", Toast.LENGTH_SHORT).show();
        }
    }
    //takes the UUID and creates a comms socket
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    //same as in device list activity
    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "ERROR - Device does not support bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    // Method to send data
    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            //attempt to place data on the outstream to the BT device
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there
            Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    public void onBackPressed() {
    }
}
package com.example.mod5app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class FirstFragment extends Fragment {

    public BluetoothAdapter btAdapter;
    public BluetoothDevice btDevice;
    public BluetoothSocket btSocket;
    public static final String BT_MAC_ADDRESS = "CC:06:04:09:53:35"; // HC-05 BT ADDRESS
    public EditText editText;
    public TextView temperatureText;
    public TextView distanceText;
    Handler handler = new Handler();
    boolean getTemp = false;
    boolean getDistance = false;
    boolean threadRunning = false;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editText = view.findViewById(R.id.editText);
        view.findViewById(R.id.button_send_string).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btSocket != null)
                {
                    try
                    {
                        String text = editText.getText().toString();
                        OutputStream out = btSocket.getOutputStream();
                        out.write(text.getBytes());
                    }
                    catch(IOException e)
                    {

                    }

                    if (threadRunning) {
                        Runnable rb = new GetSensorValues();
                        new Thread(rb).start();
                    }
                }
            }
        });

        temperatureText = view.findViewById(R.id.textView_temp);
        view.findViewById(R.id.button_get_temp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getTemp = true;
                if (!threadRunning)
                {
                    Runnable rb = new GetSensorValues();
                    new Thread(rb).start();
                    threadRunning = true;
                }
            }
        });

        distanceText = view.findViewById(R.id.textView_Distance);
        view.findViewById(R.id.button_get_distance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getDistance = true;
                if (!threadRunning)
                {
                    Runnable rb = new GetSensorValues();
                    new Thread(rb).start();
                    threadRunning = true;
                }
            }
        });

        view.findViewById(R.id.button_establish_connection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btAdapter = BluetoothAdapter.getDefaultAdapter();
                btDevice = btAdapter.getRemoteDevice(BT_MAC_ADDRESS);

                if(btAdapter == null)
                {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("No Bluetooth Found")
                            .setMessage("")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
                else
                {
                    if(!btAdapter.isEnabled())
                    {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, 3);
                    }
                    else
                    {
                        ConnectThread connectThread = new ConnectThread(btDevice);
                        connectThread.start();
                    }
                }
            }
        });
    }

    public class ConnectThread extends Thread {
        private final BluetoothSocket thisSocket;
        private final BluetoothDevice thisDevice;

        public static final String SERVICE_ID = "00001101-0000-1000-8000-00805f9b34fb";

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            thisDevice = device;

            try {
                tmp = thisDevice.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_ID));
            } catch (IOException e) {
                Log.e("TEST", "Can't connect to service");
            }
            thisSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            btAdapter.cancelDiscovery();

            try {
                thisSocket.connect();
                Log.d("TESTING", "Connected to device");
            } catch (IOException connectException) {
                try {
                    thisSocket.close();
                } catch (IOException closeException) {
                    Log.e("TEST", "Can't close socket");
                }
                return;
            }

            btSocket = thisSocket;

        }
        public void cancel() {
            try {
                thisSocket.close();
            } catch (IOException e) {
                Log.e("TEST", "Can't close socket");
            }
        }
    }

    private void updateTempLabel(String temp) {
            temperatureText.setText(temp);
    }

    private void updateDistanceLabel(String distance) {
        distanceText.setText(distance);
    }

    public class GetSensorValues implements Runnable
    {
        public void run()
        {
            while (true)
            {
                if (btSocket != null)
                {
                    if (getTemp)
                    {
                        try
                        {
                            String text = editText.getText().toString();
                            OutputStream out = btSocket.getOutputStream();;
                            out.flush();
                            out.write(("get temp").getBytes());
                            out.flush();
                            Thread.sleep(500);
                        }
                        catch(IOException | InterruptedException e)
                        {

                        }

                        try
                        {
                            byte[] buffer = new byte[256];  // buffer store for the stream
                            int bytes = 0;
                            InputStream in = btSocket.getInputStream();
                            bytes = in.read(buffer);

                            final String temp = new String(buffer);

                            handler.post(new Runnable()
                            {
                                public void run()
                                {
                                    updateTempLabel(temp + " .C");
                                }
                            });
                        }
                        catch(IOException e)
                        {

                        }
                    }

                    if (getDistance)
                    {
                        try
                        {
                            String text = editText.getText().toString();
                            OutputStream out = btSocket.getOutputStream();;
                            out.flush();
                            out.write(("get distance").getBytes());
                            out.flush();
                            Thread.sleep(500);
                        }
                        catch(IOException | InterruptedException e)
                        {

                        }

                        try
                        {
                            byte[] buffer = new byte[256];  // buffer store for the stream
                            int bytes = 0;
                            InputStream in = btSocket.getInputStream();
                            bytes = in.read(buffer);

                            final String distance = new String(buffer);

                            handler.post(new Runnable()
                            {
                                public void run()
                                {
                                    updateDistanceLabel(distance);
                                }
                            });
                        }
                        catch(IOException e)
                        {

                        }
                    }
                }
            }
        }
    }
}
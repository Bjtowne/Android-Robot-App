package btowne.robotcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView connectionStatus;
    TextView connectionStrength;
    TextView messageText;
    BluetoothDevice hub;

    RobotControl robot;
    boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        findViewById(R.id.forward_button).setOnTouchListener(this::forwardButton);
        findViewById(R.id.backwards_button).setOnTouchListener(this::backwardsButton);
        findViewById(R.id.left_button).setOnTouchListener(this::leftButton);
        findViewById(R.id.right_button).setOnTouchListener(this::rightButton);
        findViewById(R.id.connect_button).setOnClickListener(this::connectButton);
        findViewById(R.id.first_led_button).setOnClickListener(this::firstLedButton);
        findViewById(R.id.second_led_button).setOnClickListener(this::secondLedButton);
        connectionStatus = findViewById(R.id.connection_status_text);
        connectionStrength = findViewById(R.id.connection_strength_text);
        messageText = findViewById(R.id.message_text);
    }

    /**
     * Handler for when the forward button is pressed.
     */
    private boolean forwardButton(View v, MotionEvent e){
        if(e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_CANCEL){
            messageText.setText("Forward Button Pressed");
            issueCommand(RobotControl.FORWARD_FAST);
        }else if(e.getAction() == MotionEvent.ACTION_UP){
            messageText.setText("Forward Button Released");
            issueCommand(RobotControl.ALL_STOP);
        }
        return true;
    }

    /**
     * Handler for when the backwards button is pressed
     */
    private boolean backwardsButton(View v, MotionEvent e){
        if(e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_CANCEL){
            messageText.setText("Backwards Button Pressed");
            issueCommand(RobotControl.BACKWARDS_FAST);
        }else if(e.getAction() == MotionEvent.ACTION_UP){
            messageText.setText("Backwards Button Released");
            issueCommand(RobotControl.ALL_STOP);
        }
        return true;
    }

    /**
     * Handler for when the left button is pressed
     */
    private boolean leftButton(View v, MotionEvent e){
        if(e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_CANCEL){
            messageText.setText("Left Button Pressed");
            issueCommand(RobotControl.LEFT_FAST);
        }else if(e.getAction() == MotionEvent.ACTION_UP){
            messageText.setText("Left Button Released");
            issueCommand(RobotControl.ALL_STOP);
        }
        return true;
    }

    /**
     * Handler for when the right button is pressed
     */
    private boolean rightButton(View v, MotionEvent e){
        if(e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_CANCEL){
            messageText.setText("Right Button Pressed");
            issueCommand(RobotControl.RIGHT_FAST);
        }else if(e.getAction() == MotionEvent.ACTION_UP){
            messageText.setText("Right Button Released");
            issueCommand(RobotControl.ALL_STOP);
        }
        return true;
    }

    /**
     * Handler for when the connect button is pressed
     */
    private boolean connectButton(View v){
        boolean notFound = true;
        for(Object i:BluetoothAdapter.getDefaultAdapter().getBondedDevices().toArray()){
            if(((BluetoothDevice)i).getName().equals("Mobile Hub")){
                notFound = false;
                connectToDevice((BluetoothDevice)i);
            }
        }
        if(notFound){
            messageText.setText(R.string.connection_not_found);
        }
        return true;
    }

    /**
     * Create a connection to the given Bluetooth Device
     *
     * @param device The device to connect too.
     */
    private void connectToDevice(BluetoothDevice device){
        connectionStatus.setText(R.string.connection_connecting);
        new Thread(() ->
        {
            try {
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                BluetoothGatt gatt = device.connectGatt(this, true, new BluetoothGattCallback() {
                    @Override
                    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                        updateStrength(rssi);
                    }
                });
                socket.connect();
                new ConnectionStatusListener(socket).onClose(this::connectionClosed)
                                                    .onUpdate(gatt::readRemoteRssi)
                                                    .start();
                connectionComplete(socket.getOutputStream());
            } catch (IOException e) {
                connectionComplete(null);
            }
        }).start();
    }

    /**
     * Handler for when the connection process completes.
     *
     * @param output An output stream used to communicate with the connected device. Null if the connection process failed.
     */
    private void connectionComplete(OutputStream output){
        if(output == null){
            connectionStatus.setText(R.string.connection_exception);
        }else{
            connectionStatus.setText(R.string.connection_successful);
            robot = new RobotControl(output);
            connected = true;
        }
    }

    /**
     * Handler for when the connection is closed.
     */
    private synchronized void connectionClosed(){
        connected = false;
        connectionStatus.setText(R.string.connection_closed);
        connectionStrength.setText("");
    }

    /**
     * Updates the signal strength indicator with the given value.
     *
     * @param rssi The value to update with
     */
    private synchronized void updateStrength(int rssi){
        connectionStrength.setText("Signal Strength: " + rssi);
    }

    /**
     * Handler for the first led button
     */
    private boolean firstLedButton(View v){
        if(issueCommand(RobotControl.TOGGLE_FIRST_LED)){
            messageText.setText(R.string.first_led_command_success);
        }else {
            messageText.setText(R.string.first_led_command_failure);
        }
        return true;
    }

    /**
     * Handler for the second led button
     */
    private boolean secondLedButton(View v){
        if(issueCommand(RobotControl.TOGGLE_SECOND_LED)){
            messageText.setText(R.string.second_led_command_success);
        }else{
            messageText.setText(R.string.second_led_command_failure);
        }
        return true;
    }

    /**
     * Send a command to the robot.
     *
     * @param command The command to issue to the robot.
     * @return True if the command was sent, false otherwise.
     */
    private boolean issueCommand(int command){
        if(connected){
            robot.command(command);
            return true;
        }else{
            return false;
        }
    }
}

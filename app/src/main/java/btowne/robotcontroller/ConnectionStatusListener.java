package btowne.robotcontroller;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * A thread that constantly pings the status of the connection and calls a handler to update the
 * signal strength and another handler when the connection closes.
 */
public class ConnectionStatusListener extends Thread{

    private BluetoothSocket socket;
    private Runnable onUpdate;
    private Runnable onClose;

    public ConnectionStatusListener(BluetoothSocket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            PrintWriter tester = new PrintWriter(socket.getOutputStream());
            while(!tester.checkError()){
                TimeUnit.MILLISECONDS.sleep(500);
                if(onUpdate != null){
                    onUpdate.run();
                }
            }
        } catch (Exception e) {
        }
        if(onClose != null){
            onClose.run();
        }
    }

    public synchronized ConnectionStatusListener onUpdate(Runnable action){
        onUpdate = action;
        return this;
    }

    public synchronized ConnectionStatusListener onClose(Runnable action){
        onClose = action;
        return this;
    }
}

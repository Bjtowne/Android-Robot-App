package btowne.robotcontroller;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstracts commands being issued to the robot so I dont have to remember things elsewhere in the code.
 */
public class RobotControl {

    OutputStream output;

    //Additional commands
    public static int TOGGLE_FIRST_LED = -1;
    public static int TOGGLE_SECOND_LED = -2;

    //Command matrix
    public static int ALL_STOP = 0;
    private static int FIRST_LED_ON = 1;
    private static int FIRST_LED_OFF = 2;
    private static int SECOND_LED_ON = 3;
    private static int SECOND_LED_OFF = 4;
    public static int FORWARD_SLOW = 5;
    public static int FORWARD_MEDIUM = 6;
    public static int FORWARD_FAST = 7;
    public static int BACKWARDS_SLOW = 8;
    public static int BACKWARDS_MEDIUM = 9;
    public static int BACKWARDS_FAST = 10;
    public static int LEFT_SLOW = 11;
    public static int LEFT_MEDIUM = 12;
    public static int LEFT_FAST = 13;
    public static int RIGHT_SLOW = 14;
    public static int RIGHT_MEDIUM = 15;
    public static int RIGHT_FAST = 16;


    private boolean firstLed = false;
    private boolean secondLed = false;

    public RobotControl(OutputStream output){
        this.output = output;
        command(FIRST_LED_OFF);
        command(SECOND_LED_OFF);
    }

    private void toggleFirstLed(){
        if(firstLed){
            firstLed = false;
            command(FIRST_LED_OFF);
        }else{
            firstLed = true;
            command(FIRST_LED_ON);
        }
    }

    private void toggleSecondLed(){
        if(secondLed){
            secondLed = false;
            command(SECOND_LED_OFF);
        }else{
            secondLed = true;
            command(SECOND_LED_ON);
        }
    }

    public boolean command(int command){
        if(command == TOGGLE_FIRST_LED){
            toggleFirstLed();
        }
        if(command == TOGGLE_SECOND_LED){
            toggleSecondLed();
        }

        try {
            output.write(command);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}

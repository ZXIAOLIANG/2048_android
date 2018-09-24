package lab4_205_05.uwaterloo.ca.lab4_205_05;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    public final int GAMEBOARD_DIMENSION = 700;
    //Accelerometer handler variables; used in tandem to determine FSM states
    public static double [] accelerometerHighValues = new double[3];
    public static List<double[]> accelerometerHistory = new ArrayList<double[]>();
    //Set up FSM
    public enum states {WAIT, RISE_A, FALL_A, RISE_B, FALL_B, DETERMINED};
    public enum types {A, B, X};
    public static classFSM ourFSMX = new classFSM();
    public static classFSM ourFSMY = new classFSM();
    public static TextView FSMstatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //The two layouts are so that we can display both the game and the direction of movement
        setContentView(R.layout.activity_main);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativelayout);
        RelativeLayout pl = (RelativeLayout) findViewById(R.id.parentlayout);

        //Set up gameboard
        rl.getLayoutParams().width = GAMEBOARD_DIMENSION;
        rl.getLayoutParams().height = GAMEBOARD_DIMENSION;
        rl.setBackgroundResource(R.drawable.gameboard);

        //Setting a clock for a game
        Timer myGameLoop = new Timer();
        GameLoopTask myGameLoopTask = new GameLoopTask(this, rl, getApplicationContext());
        myGameLoop.schedule(myGameLoopTask, 50, 50);

        //Textview for what direction the phone was moved in
        FSMstatus = new TextView(getApplicationContext());
        FSMstatus.setTextColor(Color.YELLOW);
        FSMstatus.setTextSize(60f);
        FSMstatus.setX(0);
        FSMstatus.setY(700);
        pl.addView(FSMstatus);

        //Set up accelerometer handler
        SensorManager senManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = senManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        SensorEventListener accelerometerListener = new AccelerometerEventListener(myGameLoopTask);
        senManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    //FSM class
    public static class classFSM {
        private states currentState;
        private double peak; //Realize when we reach the top or bottom of a curve
        private int eventCount; //So we can mark events, and count number of events since last marked event
        private types type; //Type A, B or X

        classFSM() {
            this.currentState = states.WAIT;
            this.peak = 0;
            this.eventCount = 0;
        }

        public states getCurrentState() {
            return currentState;
        }

        public void setState(states a) {
            this.currentState = a;
        }

        public double getPeak() {
            return this.peak;
        }

        public void setPeak(double a) {
            this.peak = a;
        }

        public int getCount() {
            return this.eventCount;
        }

        public void increaseCount() {
            this.eventCount++;
        }

        public void resetCount() {
            this.eventCount = 0;
        }

        public void setType(types a) {
            this.type = a;
        }

        public types getType() {
            return this.type;
        }
    }
}




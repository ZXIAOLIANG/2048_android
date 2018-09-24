package lab4_205_05.uwaterloo.ca.lab4_205_05;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Random;

public class GameBlock extends GameBlockTemplate {
    private float IMAGE_SCALE = 0.68f;                  //Set the proper size of the gameblock
    //Location of block
    private int myCoordX;
    private int myCoordY;
    //Location the block wishes to go to
    private int targetCoordX;
    private int targetCoordY;
    //Dummy version of targetCoordX and targetCoordY so we can test if game is over
    private int dummyCoordX;
    private int dummyCoordY;
    //Four contraints (top left corners of the 4 corner grid spots)
    private final int LEFTBOUND = -43;
    private final int RIGHTBOUND = 482;
    private final int TOPBOUND = -43;
    private final int BOTTOMBOUND = 482;
    private int blockNum;                               //Value of the block
    //Offset the value displayed so that it is centred on the block
    private final int textXOffset1 = 105;
    private final int textXOffset2 = 75;
    private final int textXOffset3 = 47;
    private final int textYOffset = 60;
    //For pseudo-Newtonian movement of blocks
    private int velocity = 1;
    private final int acceleration = 10;
    //Tells us whether the block is in movement; necessary to ignore new accelerometer handler readings before we finish moving every block
    //Also necessary to determine when we can create a new block (which is after all movement is done)
    private boolean inMotion = false;
    private TextView value;                             //Display for the blockNum
    Random rng = new Random();                          //For generation of the value of the block; used in constructor
    private RelativeLayout myRl;                        //Relative layout is passed in so we can add the block and its value to the view

    //Constructor
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public GameBlock(Context myContext, int x, int y, RelativeLayout MyRL){
        super(myContext);                               //Extends a class that extends AppCompatImageView; requires this for default constructor

        //Set up location
        this.myCoordX = x;
        this.myCoordY = y;
        this.targetCoordX = x;
        this.targetCoordY = y;
        this.setX(x);
        this.setY(y);

        //Set up block image
        this.setImageResource(R.drawable.gameblock);
        this.setScaleX(IMAGE_SCALE);
        this.setScaleY(IMAGE_SCALE);
        value = new TextView(myContext);
        blockNum = (rng.nextInt(2)+1) * 2;
        this.setXOffset();
        value.setY(y+textYOffset);
        value.setText(Integer.toString(blockNum));
        value.setTextColor(Color.YELLOW);
        value.setTextSize(50f);

        //Add to view
        myRl = MyRL;
        MyRL.addView(this);
        MyRL.addView(value);
    }

    //Check if block is moving
    public boolean getMotion() {
        return this.inMotion;
    }

    //Returns where the block is
    public int[] getCoordinates(){
        int [] coordinates = {myCoordX, myCoordY};
        return coordinates;
    }

    //Returns where the block wants to go
    public int[] getTarget(){
        int[] target = {targetCoordX, targetCoordY};
        return target;
    }

    //Returns value of block
    public int getVal() {
        return blockNum;
    }

    //For when 2 blocks merge, double blockNum and redisplay
    public void merge(){
        blockNum *= 2;
        setXOffset();
        value.setText(Integer.toString(blockNum));
        if (blockNum == 256) {
            GameLoopTask.win();
        }
    }

    //When 2 blocks merge, one is removed from view (and from the Linked List in GameLoopTask)
    public void destroy(){
        myRl.removeView(value);
        value = null;
        this.setScaleX(0);
        this.setScaleY(0);
        myRl.removeView(this);
    }

    //Make sure blockNum is centred
    public void setXOffset() {
        if (blockNum > 99) {
            value.setX(myCoordX + textXOffset3);
        } else if (blockNum > 9) {
            value.setX(myCoordX + textXOffset2);
        } else {
            value.setX(myCoordX + textXOffset1);
        }
        value.bringToFront();
    }

    //Set where this block wants to go
    public void setDestination(){
        boolean occupied;                           //For determining if spaces are occupied
        boolean merge = false;                      //Determines if this block will make a merge
        int checkVal[] = {-1, -2, -3};              //Stores values of the 3 potential blocks in front
        int arrayCount = 0;
        //Counts movement offset
        int blockCount = 0;
        int slotCount = 0;

        //LEFT--------------------------------------------------
        if(GameLoopTask.currentGameDirection == GameLoopTask.gameDirection.LEFT) { //Check direction we want to go in
            this.targetCoordX = LEFTBOUND;          //Start checking spaces from the leftmost
            while (targetCoordX != myCoordX) {
                occupied = GameLoopTask.isOccupied(targetCoordX,myCoordY);          //Check if occupied
                if (occupied) {
                    //Populate checkVal and see if we want to merge
                    checkVal[arrayCount] = GameLoopTask.myBlockList.get(GameLoopTask.findByCoord(targetCoordX, myCoordY)[0]).getVal();
                    if (checkVal[arrayCount] == blockNum) {
                        merge = true;
                    } else {
                        merge = false;
                    }
                    blockCount++;
                    arrayCount++;
                }
                slotCount++;
                targetCoordX += GameLoopTask.SLOT_ISOLATION;            //Check next space
            }
            if (merge) {                                                //This block will merge
                boolean fullArray = true;
                for (int i = 0; i < 3; i++) {
                    if (checkVal[i] < 0) {
                        fullArray = false;
                        targetCoordX = LEFTBOUND + ((myCoordX + GameLoopTask.LEFT_BOUNDARY) / GameLoopTask.SLOT_ISOLATION - slotCount + blockCount - 1) * GameLoopTask.SLOT_ISOLATION;
                        break;
                    }
                }
                if (fullArray) {
                    if(checkVal[0] == checkVal[1] && blockNum == checkVal[2]) {
                        targetCoordX = LEFTBOUND + ((myCoordX + GameLoopTask.LEFT_BOUNDARY) / GameLoopTask.SLOT_ISOLATION - slotCount + blockCount - 2) * GameLoopTask.SLOT_ISOLATION;
                    } else {
                        targetCoordX = LEFTBOUND + ((myCoordX+GameLoopTask.LEFT_BOUNDARY)/GameLoopTask.SLOT_ISOLATION - slotCount + blockCount - 1) * GameLoopTask.SLOT_ISOLATION;
                    }
                }
            } else {                                            //Block will not merge
                boolean futuremerge = false;                    //Used to determine if other blocks ahead will merge
                for (int i = 0; i < 2; i++) {
                    if (checkVal[i] == checkVal[i + 1]) {
                        futuremerge = true;
                        break;
                    }
                }
                if (futuremerge) {
                    targetCoordX = LEFTBOUND + ((myCoordX + GameLoopTask.LEFT_BOUNDARY) / GameLoopTask.SLOT_ISOLATION - slotCount + blockCount - 1) * GameLoopTask.SLOT_ISOLATION;
                } else {
                    targetCoordX = LEFTBOUND + ((myCoordX + GameLoopTask.LEFT_BOUNDARY) / GameLoopTask.SLOT_ISOLATION - slotCount + blockCount) * GameLoopTask.SLOT_ISOLATION;
                }
            }
        }
        //RIGHT--------------------------------------------
        else if(GameLoopTask.currentGameDirection == GameLoopTask.gameDirection.RIGHT){
            this.targetCoordX = RIGHTBOUND;
            while (targetCoordX != myCoordX) {
                occupied = GameLoopTask.isOccupied(targetCoordX,myCoordY);
                if (occupied) {
                    checkVal[arrayCount] = GameLoopTask.myBlockList.get(GameLoopTask.findByCoord(targetCoordX, myCoordY)[0]).getVal();
                    if (checkVal[arrayCount] == blockNum) {
                        merge = true;
                    } else {
                        merge = false;
                    }
                    blockCount++;
                    arrayCount++;
                }
                slotCount++;
                targetCoordX -= GameLoopTask.SLOT_ISOLATION;
            }
            if (merge) {
                boolean fullArray = true;
                for (int i = 0; i < 3; i++) {
                    if (checkVal[i] < 0) {
                        fullArray = false;
                        targetCoordX = LEFTBOUND + ((myCoordX + GameLoopTask.LEFT_BOUNDARY) / GameLoopTask.SLOT_ISOLATION + slotCount - blockCount + 1) * GameLoopTask.SLOT_ISOLATION;
                        break;
                    }
                }
                if (fullArray) {
                    if(checkVal[0] == checkVal[1] && blockNum == checkVal[2]) {
                        targetCoordX = LEFTBOUND + ((myCoordX + GameLoopTask.LEFT_BOUNDARY) / GameLoopTask.SLOT_ISOLATION + slotCount - blockCount + 2) * GameLoopTask.SLOT_ISOLATION;
                    } else {
                        targetCoordX = LEFTBOUND + ((myCoordX+GameLoopTask.LEFT_BOUNDARY)/GameLoopTask.SLOT_ISOLATION + slotCount - blockCount + 1) * GameLoopTask.SLOT_ISOLATION;
                    }
                }
            } else {
                boolean futuremerge = false;
                for (int i = 0; i < 2; i++) {
                    if (checkVal[i] == checkVal[i + 1]) {
                        futuremerge = true;
                        break;
                    }
                }
                if (futuremerge) {
                    targetCoordX = LEFTBOUND + ((myCoordX + GameLoopTask.LEFT_BOUNDARY) / GameLoopTask.SLOT_ISOLATION + slotCount - blockCount + 1) * GameLoopTask.SLOT_ISOLATION;
                } else {
                    targetCoordX = LEFTBOUND + ((myCoordX + GameLoopTask.LEFT_BOUNDARY) / GameLoopTask.SLOT_ISOLATION + slotCount - blockCount) * GameLoopTask.SLOT_ISOLATION;
                }
            }
        }
        //UP---------------------------------------------
        else if(GameLoopTask.currentGameDirection == GameLoopTask.gameDirection.UP){
            this.targetCoordY = TOPBOUND;
            while (targetCoordY != myCoordY) {
                occupied = GameLoopTask.isOccupied(myCoordX,targetCoordY);
                if (occupied) {
                    checkVal[arrayCount] = GameLoopTask.myBlockList.get(GameLoopTask.findByCoord(myCoordX, targetCoordY)[0]).getVal();
                    if (checkVal[arrayCount] == blockNum) {
                        merge = true;
                    } else {
                        merge = false;
                    }
                    blockCount++;
                    arrayCount++;
                }
                slotCount++;
                targetCoordY += GameLoopTask.SLOT_ISOLATION;
            }
            if (merge) {
                boolean fullArray = true;
                for (int i = 0; i < 3; i++) {
                    if (checkVal[i] < 0) {
                        fullArray = false;
                        targetCoordY = TOPBOUND + ((myCoordY + GameLoopTask.UP_BOUNDARY) / GameLoopTask.SLOT_ISOLATION - slotCount + blockCount - 1) * GameLoopTask.SLOT_ISOLATION;
                        break;
                    }
                }
                if (fullArray) {
                    if(checkVal[0] == checkVal[1] && blockNum == checkVal[2]) {
                        targetCoordY = TOPBOUND + ((myCoordY + GameLoopTask.UP_BOUNDARY) / GameLoopTask.SLOT_ISOLATION - slotCount + blockCount - 2) * GameLoopTask.SLOT_ISOLATION;
                    } else {
                        targetCoordY = TOPBOUND + ((myCoordY+GameLoopTask.UP_BOUNDARY)/GameLoopTask.SLOT_ISOLATION - slotCount + blockCount - 1) * GameLoopTask.SLOT_ISOLATION;
                    }
                }
            } else {
                boolean futuremerge = false;
                for (int i = 0; i < 2; i++) {
                    if (checkVal[i] == checkVal[i + 1]) {
                        futuremerge = true;
                        break;
                    }
                }
                if (futuremerge) {
                    targetCoordY = TOPBOUND + ((myCoordY + GameLoopTask.UP_BOUNDARY) / GameLoopTask.SLOT_ISOLATION - slotCount + blockCount - 1) * GameLoopTask.SLOT_ISOLATION;
                } else {
                    targetCoordY = TOPBOUND + ((myCoordY + GameLoopTask.UP_BOUNDARY) / GameLoopTask.SLOT_ISOLATION - slotCount + blockCount) * GameLoopTask.SLOT_ISOLATION;
                }
            }
        }
        //DOWN---------------------------------------------
        else if(GameLoopTask.currentGameDirection == GameLoopTask.gameDirection.DOWN){
            this.targetCoordY = BOTTOMBOUND;
            while (targetCoordY != myCoordY) {
                occupied = GameLoopTask.isOccupied(myCoordX,targetCoordY);
                if (occupied) {
                    checkVal[arrayCount] = GameLoopTask.myBlockList.get(GameLoopTask.findByCoord(myCoordX, targetCoordY)[0]).getVal();
                    if (checkVal[arrayCount] == blockNum) {
                        merge = true;
                    } else {
                        merge = false;
                    }
                    blockCount++;
                    arrayCount++;
                }
                slotCount++;
                targetCoordY -= GameLoopTask.SLOT_ISOLATION;
            }
            if (merge) {
                boolean fullArray = true;
                for (int i = 0; i < 3; i++) {
                    if (checkVal[i] < 0) {
                        fullArray = false;
                        targetCoordY = TOPBOUND + ((myCoordY + GameLoopTask.UP_BOUNDARY) / GameLoopTask.SLOT_ISOLATION + slotCount - blockCount + 1) * GameLoopTask.SLOT_ISOLATION;
                        break;
                    }
                }
                if (fullArray) {
                    if(checkVal[0] == checkVal[1] && blockNum == checkVal[2]) {
                        targetCoordY = TOPBOUND + ((myCoordY + GameLoopTask.UP_BOUNDARY) / GameLoopTask.SLOT_ISOLATION + slotCount - blockCount + 2) * GameLoopTask.SLOT_ISOLATION;
                    } else {
                        targetCoordY = TOPBOUND + ((myCoordY+GameLoopTask.UP_BOUNDARY)/GameLoopTask.SLOT_ISOLATION + slotCount - blockCount + 1) * GameLoopTask.SLOT_ISOLATION;
                    }
                }
            } else {
                boolean futuremerge = false;
                for (int i = 0; i < 2; i++) {
                    if (checkVal[i] == checkVal[i + 1]) {
                        futuremerge = true;
                        break;
                    }
                }
                if (futuremerge) {
                    targetCoordY = TOPBOUND + ((myCoordY + GameLoopTask.UP_BOUNDARY) / GameLoopTask.SLOT_ISOLATION + slotCount - blockCount + 1) * GameLoopTask.SLOT_ISOLATION;
                } else {
                    targetCoordY = TOPBOUND + ((myCoordY + GameLoopTask.UP_BOUNDARY) / GameLoopTask.SLOT_ISOLATION + slotCount - blockCount) * GameLoopTask.SLOT_ISOLATION;
                }
            }
        }
    }

    //Move the block
    @SuppressWarnings("ResourceType")
    public void move(){
        //LEFT----------------------------------------------
        if(GameLoopTask.currentGameDirection == GameLoopTask.gameDirection.LEFT){ //Check direction we want to go in
            if(this.myCoordX != this.targetCoordX) {    //Increase position by velocity, increase velocity by acceleration
                if (this.myCoordX - this.velocity > this.targetCoordX) {
                    this.myCoordX -= this.velocity;
                    this.velocity += this.acceleration;
                    this.setX(myCoordX);
                }
                else {
                    this.myCoordX = this.targetCoordX;
                    this.velocity = 1;
                    this.setX(myCoordX);
                    this.inMotion = false;
                }
                this.inMotion = true;
            }
            else {
                this.inMotion = false;
            }
        }
        //RIGHT------------------------------
        else if(GameLoopTask.currentGameDirection == GameLoopTask.gameDirection.RIGHT){
            if(this.myCoordX != this.targetCoordX) {
                if (this.myCoordX + this.velocity < this.targetCoordX) {
                    this.myCoordX += this.velocity;
                    this.velocity += this.acceleration;
                    this.setX(myCoordX);
                }
                else {
                    this.myCoordX = this.targetCoordX;
                    this.velocity = 1;
                    this.setX(myCoordX);
                    this.inMotion = false;
                }
                this.inMotion = true;
            }
            else {
                this.inMotion = false;
            }
        }
        //UP-----------------------------------------
        else if(GameLoopTask.currentGameDirection == GameLoopTask.gameDirection.UP){
            if(this.myCoordY != this.targetCoordY) {
                if (this.myCoordY - this.velocity > this.targetCoordY) {
                    this.myCoordY -= this.velocity;
                    this.velocity += this.acceleration;
                    this.setY(myCoordY);
                    value.setY(myCoordY + textYOffset);
                }
                else {
                    this.myCoordY = this.targetCoordY;
                    this.velocity = 1;
                    this.setY(myCoordY);
                    value.setY(myCoordY + textYOffset);
                    this.inMotion = false;
                }
                this.inMotion = true;
            }
            else {
                this.inMotion = false;
            }
        }
        //DOWN-----------------------------------------------
        else if(GameLoopTask.currentGameDirection == GameLoopTask.gameDirection.DOWN){

            if(this.myCoordY != this.targetCoordY) {
                if (this.myCoordY + this.velocity < this.targetCoordY) {
                    this.myCoordY += this.velocity;
                    this.velocity += this.acceleration;
                    this.setY(myCoordY);
                    value.setY(myCoordY + textYOffset);
                }
                else {
                    this.myCoordY = this.targetCoordY;
                    this.velocity = 1;
                    this.setY(myCoordY);
                    value.setY(myCoordY + textYOffset);
                    this.inMotion = false;
                }
                this.inMotion = true;
            }
            else {
                this.inMotion = false;
            }
        }
    }

    //Set where this block wants to go
    public boolean testDestinations() {
        boolean occupied;                           //For determining if spaces are occupied
        int checkVal = 0;
        boolean left = false;
        boolean right = false;
        boolean up = false;
        boolean down = false;

        //LEFT--------------------------------------------------
        if(this.myCoordX - GameLoopTask.SLOT_ISOLATION >= LEFTBOUND) { //Check if we are at the leftmost bound
            this.dummyCoordX = myCoordX - GameLoopTask.SLOT_ISOLATION;          //Start checking spaces from the leftmost
            checkVal = GameLoopTask.myBlockList.get(GameLoopTask.findByCoord(dummyCoordX, myCoordY)[0]).getVal();
            Log.d("left blocknum", Integer.toString(blockNum));
            Log.d("left checkVal", Integer.toString(checkVal));
            if (checkVal == blockNum) {
                return true;
            } else {
                left = false;
            }
        }
        //RIGHT--------------------------------------------
        if(this.myCoordX + GameLoopTask.SLOT_ISOLATION <= RIGHTBOUND){
            this.dummyCoordX = myCoordX + GameLoopTask.SLOT_ISOLATION;
            checkVal= GameLoopTask.myBlockList.get(GameLoopTask.findByCoord(dummyCoordX, myCoordY)[0]).getVal();
            Log.d("right blocknum", Integer.toString(blockNum));
            Log.d("right checkVal", Integer.toString(checkVal));
            if (checkVal == blockNum) {
                return true;
            } else {
                right = false;
            }
        }
        //UP---------------------------------------------
        if(this.myCoordY - GameLoopTask.SLOT_ISOLATION >= TOPBOUND){
            this.dummyCoordY = myCoordY - GameLoopTask.SLOT_ISOLATION;
            checkVal = GameLoopTask.myBlockList.get(GameLoopTask.findByCoord(myCoordX, dummyCoordY)[0]).getVal();
            Log.d("up blocknum", Integer.toString(blockNum));
            Log.d("up checkVal", Integer.toString(checkVal));
            if (checkVal == blockNum) {
                return true;
            } else {
                up = false;
            }
        }
        //DOWN---------------------------------------------
        if(this.myCoordY + GameLoopTask.SLOT_ISOLATION <= BOTTOMBOUND){
            this.dummyCoordY = myCoordY + GameLoopTask.SLOT_ISOLATION;
            checkVal = GameLoopTask.myBlockList.get(GameLoopTask.findByCoord(myCoordX, dummyCoordY)[0]).getVal();
            Log.d("down blocknum", Integer.toString(blockNum));
            Log.d("down checkVal", Integer.toString(checkVal));
            if (checkVal == blockNum) {
                return true;
            } else {
                down = false;
            }
        }
        if (!left && !right && !up && !down){

            return false;
        } else {
            return true;
        }
    }
}

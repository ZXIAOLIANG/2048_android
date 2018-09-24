package lab4_205_05.uwaterloo.ca.lab4_205_05;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.RelativeLayout;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.TimerTask;

public class GameLoopTask extends TimerTask {
    private Activity myActivity;
    private static Context myContext;
    private static RelativeLayout MyRL;
    enum gameDirection {UP, DOWN, LEFT, RIGHT, NO_MOVEMENT};
    public static gameDirection currentGameDirection = gameDirection.NO_MOVEMENT;
    public static gameDirection previousGameDirection = gameDirection.NO_MOVEMENT;
    public static LinkedList<GameBlock> myBlockList = new LinkedList();
    private static Random rng = new Random();
    private static int x, y;
    public static final int LEFT_BOUNDARY = 43;
    public static final int UP_BOUNDARY = 43;
    public static final int SLOT_ISOLATION = 175;
    private boolean inMotion = false;
    public boolean [] motions = new boolean[16];
    public GameBlock tempGameBlock;
    private boolean blockCreation = false;
    private boolean createOnce = false;
    private static boolean[][] grid = new boolean[4][4];
    private static int emptyBlockNum = 16;
    private static boolean winFlag = false;
    private static boolean loseFlag = false;

    public boolean winFlagStatus() { return winFlag; }
    public boolean loseFlagStatus() { return loseFlag; }

    public static void win() {
        winFlag = true;
        Log.wtf("Congratulations", "You Have Won");
    }

    public boolean getMotion() {
        return inMotion;
    }
    public GameLoopTask.gameDirection getCurrentGameDirection() { return this.currentGameDirection; }

    public GameLoopTask(Activity myActivity, RelativeLayout MyRL, Context myContext){
        this.myActivity = myActivity;
        this.MyRL = MyRL;
 //       System.out.println(myContext.toString());
        this.myContext = myContext;
        tempGameBlock = this.createBlock();
        myBlockList.add(tempGameBlock);
        //myBlock = this.createBlock();
        for (int i = 0; i < 16; i++){
            motions[i] = false;
        }
    }

    public void run(){
        myActivity.runOnUiThread(
                new Runnable(){
                    public void run(){
                        if (winFlag) {
                            MainActivity.FSMstatus.setTextColor(Color.GREEN);
                            MainActivity.FSMstatus.setText("YOU WIN!!!");
                            //Log.wtf("Congratulations", "You Have Won");
                        }
                        if (loseFlag) {
                            MainActivity.FSMstatus.setTextColor(Color.RED);
                            MainActivity.FSMstatus.setText("You lose. Try again.");
                            //Log.wtf("Sorry, you lose", "Try again");
                        }
                        else {
                            int motionCount = 0;
                            if (currentGameDirection == gameDirection.NO_MOVEMENT && createOnce) {
                                myBlockList.add(tempGameBlock);
                                createOnce = false;
                                Log.d("List Size before lose", Integer.toString(myBlockList.size()));
                                if (myBlockList.size() == 16) {
                                    boolean flag = true;
                                    for (GameBlock block : myBlockList) {
                                        if(block.testDestinations()) {
                                            flag = false;
                                            Log.d("flag in if", Boolean.toString(flag));
                                            break;
                                        }
                                    }
                                    Log.d("flag after loop", Boolean.toString(flag));
                                    if (flag) {
                                        loseFlag = true;
                                        Log.wtf("Sorry, you lose", "Try again");
                                    }
                                }
                            }
                            if (currentGameDirection != gameDirection.NO_MOVEMENT) {
                                for (GameBlock block : myBlockList) {
                                    if (!inMotion) {
                                        //if (previousGameDirection == gameDirection.NO_MOVEMENT) {
                                        if (createOnce == false) {
                                            block.setDestination();
                                        }

                                        if (!Arrays.equals(block.getCoordinates(), block.getTarget())) {
                                            blockCreation = true;
                                        }
                                    }
                                }
                                for (GameBlock block : myBlockList) {
                                    block.move();
                                    block.setXOffset();
                                    motions[motionCount++] = block.getMotion();
                                }
                                inMotion = false;
                                for (int i = 0; i < 16; i++) {
                                    if (motions[i]) {
                                        inMotion = true;
                                        break;
                                    }
                                }
                            }
                            if (inMotion == false && blockCreation) {
                                int blockArray[] = {-1, -1, -1, -1, -1, -1, -1, -1};
                                int blockArrayCount = 0;

                                for (int i = 0; i < myBlockList.size(); i++) {

                                    boolean blacklist = false;
                                    for (int j = 0; j < 8; j++) {
                                        if (i == blockArray[j]) {
                                            blacklist = true;
                                        }
                                    }

                                    if (!blacklist) {
                                        int tempBlockArray[] = findByCoord(myBlockList.get(i).getCoordinates()[0], myBlockList.get(i).getCoordinates()[1]);
                                        if (tempBlockArray[1] > 0) {
                                            myBlockList.get(tempBlockArray[0]).merge();
                                            blockArray[blockArrayCount++] = tempBlockArray[1];
                                        }
                                    }

                                }

                                blockArray = orderRemovable(blockArray);

                                for (int i = 0; blockArray[i] > 0; i++) {
                                    //Log.d("blockArray[i]", Integer.toString(blockArray[i]));
                                    //Log.d("List Size", Integer.toString(myBlockList.size()));
                                    myBlockList.get(blockArray[i]).destroy();
                                    myBlockList.remove(blockArray[i]);
                                }

                                resetGrid();
                                iteration();
                                tempGameBlock = GameLoopTask.createBlock();
                                Log.d("Created", "Block");
                                createOnce = true;
                                blockCreation = false;
                                currentGameDirection = gameDirection.NO_MOVEMENT;
                            }
                            //Log.d("Timer","Counter");
                        }
                    }
                }
        );
    }

    public void setDirection(gameDirection newDirection){
        this.currentGameDirection = newDirection;
        if(newDirection != gameDirection.NO_MOVEMENT ){
            blockCreation = false;
        }
        //this.myBlock.getBlockDirection(newDirection);
        //createBlock();
       // Log.d("Direction", this.currentGameDirection.toString());
    }

    public int[] orderRemovable(int[]array){
        boolean flag = true;
        int temp = 0;

        while(flag){
            flag = false;

            for (int j = 1; j < 8; j++) {
                if (array[j] > array[(j-1)]) {
                    temp = array[j];
                    array[j] = array[(j-1)];
                    array[(j-1)] = temp;
                    flag = true;
                }
            }
        }
        return array;
    }

    public void setPreviousDirection() {
        previousGameDirection = currentGameDirection;
    }

    private static GameBlock createBlock(){
        int location = rng.nextInt(emptyBlockNum);
        for(int i = 0; i < 4; i++){
            boolean flag = false;
            for (int j = 0; j < 4; j++){
                if(location == 0 && grid[i][j] == false){
                    x = i;
                    y = j;
                    flag = true;
                    break;
                }
                else if(location != 0 && grid[i][j] == false){
                    location--;
                }
            }
            if(flag){
                break;
            }
        }
        x = x * SLOT_ISOLATION - LEFT_BOUNDARY;
        y = y * SLOT_ISOLATION - UP_BOUNDARY;
        GameBlock newBlock = new GameBlock(myContext, x, y, MyRL); //(-43, -43) is our initial coordinate)
        //MyRL.addView(newBlock);
        return newBlock;
    }
    private void iteration(){
        emptyBlockNum = 16;
        for(GameBlock test : myBlockList){
            int[] coord = test.getCoordinates();
            int xCoord = (coord[0] + LEFT_BOUNDARY)/SLOT_ISOLATION;
            int yCoord = (coord[1] + UP_BOUNDARY)/SLOT_ISOLATION;
            grid[xCoord][yCoord] = true;
            emptyBlockNum--;
        }
    }

    private void resetGrid(){
        for (int i = 0; i < 4; i++) {
            Arrays.fill(grid[i], false);
        }
    }

    public static int[] findByCoord(int x, int y) {
        int [] blockIndexes = {-1, -1};
        int arrayCount = 0;
        for(int i = 0; i < myBlockList.size(); i++){
            int coords[] = myBlockList.get(i).getCoordinates();
            if(coords[0] == x && coords[1] == y){
                blockIndexes[arrayCount++] = i;
            }
        }
        return blockIndexes;
    }

    public static Boolean isOccupied(int x, int y){
        for(GameBlock block : myBlockList){
            int coords[] = block.getCoordinates();
            if(coords[0] == x && coords[1] == y){
                return true;
            }
        }
        return false;
    }
}

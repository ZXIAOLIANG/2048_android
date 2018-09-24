package lab4_205_05.uwaterloo.ca.lab4_205_05;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import static java.lang.Math.abs;

public class AccelerometerEventListener extends MainActivity implements SensorEventListener {
    private GameLoopTask gameLoopTask;

    //Constructor accepts the gameLoopTask so we can access and change it from here
    public AccelerometerEventListener( GameLoopTask myGameLoopTask) {
        gameLoopTask = myGameLoopTask;
    }

    public void onAccuracyChanged(Sensor s, int i) {
    }

    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION && !gameLoopTask.winFlagStatus() && !gameLoopTask.loseFlagStatus()) {
            //record the highest absolute value
            if (abs(se.values[0]) > accelerometerHighValues[0]) {
                accelerometerHighValues[0] = abs(se.values[0]);
            }
            if (abs(se.values[1]) > accelerometerHighValues[1]) {
                accelerometerHighValues[1] = abs(se.values[1]);
            }
            if (abs(se.values[2]) > accelerometerHighValues[2]) {
                accelerometerHighValues[2] = abs(se.values[2]);
            }

            //Filter our events
            float[] filteredReading = {0, 0, 0};
            filteredReading[0] += (se.values[0] - filteredReading[0]) / 20;
            filteredReading[1] += (se.values[1] - filteredReading[1]) / 20;
            filteredReading[2] += (se.values[2] - filteredReading[2]) / 20;

            //Add the values to the history list and remove the first one when the size reach 100
            double[] toBeWritten = {filteredReading[0], filteredReading[1], filteredReading[2]};
            accelerometerHistory.add(toBeWritten);

            int size = accelerometerHistory.size();
            if (size > 100) {
                accelerometerHistory.remove(0);
                size--;
            }

            //Make sure our method of checking for slopes is allowed
            if (size > 10) {
                //Check current event (Y) for different things based on value of FSM
                switch (ourFSMY.getCurrentState()) {
                    case WAIT:
                        //Check for down
                        if ((accelerometerHistory.get(size - 1)[1] - accelerometerHistory.get(size - 5)[1]) / 4 < -0.1 || (accelerometerHistory.get(size - 1)[1] - accelerometerHistory.get(size - 6)[1]) / 5 < -0.1 || (accelerometerHistory.get(size - 1)[1] - accelerometerHistory.get(size - 7)[1]) / 6 < -0.1) {
                            ourFSMY.setState(states.FALL_B);
                        }

                        //Check for up
                        else if (((accelerometerHistory.get(size - 1)[1] - accelerometerHistory.get(size - 5)[1]) / 4 > 0.1) || (accelerometerHistory.get(size - 1)[1] - accelerometerHistory.get(size - 6)[1]) / 5 > 0.1 || (accelerometerHistory.get(size - 1)[1] - accelerometerHistory.get(size - 7)[1]) / 6 > 0.1) {
                            ourFSMY.setState(states.RISE_A);
                        }
                        break;

                    case FALL_B:
                        if (ourFSMY.getPeak() == 0) {
                            if (accelerometerHistory.get(size - 1)[1] > accelerometerHistory.get(size - 2)[1]) {
                                ourFSMY.setPeak(accelerometerHistory.get(size - 2)[1]);
                                ourFSMY.resetCount();
                            } else if (ourFSMY.getCount() > 6) {                //Throwaway event if peak can't be properly found
                                ourFSMY.setType(types.X);
                                ourFSMY.setState(states.DETERMINED);
                                ourFSMY.setPeak(0);
                                ourFSMY.resetCount();
                            }
                            else {
                                ourFSMY.increaseCount();
                            }
                        }
                        else {
                            if (ourFSMY.getCount() == 4 || ourFSMY.getCount() == 5 || ourFSMY.getCount() == 6 || ourFSMY.getCount() == 7) {
                                if ((accelerometerHistory.get(size - 1)[1] - ourFSMY.getPeak()) / ourFSMY.getCount() > 0.15) {
                                    ourFSMY.setState(states.RISE_B);            //Get right slope, move to next state
                                    ourFSMY.setPeak(0);
                                    ourFSMY.resetCount();
                                }
                            }
                            else if (ourFSMY.getCount() > 7) {
                                ourFSMY.setType(types.X);                       //Throwaway event if type cannot be properly determined
                                ourFSMY.setState(states.DETERMINED);
                                ourFSMY.setPeak(0);
                                ourFSMY.resetCount();
                            }
                            else {
                                ourFSMY.increaseCount();
                            }
                        }
                        break;

                    case RISE_B:
                        if (ourFSMY.getPeak() == 0) {
                            if (accelerometerHistory.get(size - 1)[1] < accelerometerHistory.get(size - 2)[1]) {
                                ourFSMY.setPeak(accelerometerHistory.get(size - 2)[1]);
                                ourFSMY.resetCount();
                            }
                            else if (ourFSMY.getCount() > 6) {
                                ourFSMY.setType(types.X);
                                ourFSMY.setState(states.DETERMINED);
                                ourFSMY.setPeak(0);
                                ourFSMY.resetCount();
                            }
                            else {
                                ourFSMY.increaseCount();
                            }
                        }
                        else {
                            if (ourFSMY.getCount() == 4 || ourFSMY.getCount() == 5 || ourFSMY.getCount() == 6) {
                                if ((accelerometerHistory.get(size - 1)[1] - ourFSMY.getPeak()) / ourFSMY.getCount() < -0.1) {
                                    ourFSMY.setState(states.DETERMINED);
                                    ourFSMY.setType(types.B);
                                    ourFSMY.setPeak(0);
                                    ourFSMY.resetCount();
                                }
                            }
                            else if (ourFSMY.getCount() > 6) {
                                ourFSMY.setType(types.X);
                                ourFSMY.setState(states.DETERMINED);
                                ourFSMY.setPeak(0);
                                ourFSMY.resetCount();
                            }
                            else {
                                ourFSMY.increaseCount();
                            }
                        }
                        break;

                    case RISE_A:
                        if (ourFSMY.getPeak() == 0) {
                            if (accelerometerHistory.get(size - 1)[1] < accelerometerHistory.get(size - 2)[1]) {
                                ourFSMY.setPeak(accelerometerHistory.get(size - 2)[1]);
                                ourFSMY.resetCount();
                            } else if (ourFSMY.getCount() > 6) {
                                ourFSMY.setType(types.X);
                                ourFSMY.setState(states.DETERMINED);
                                ourFSMY.setPeak(0);
                                ourFSMY.resetCount();
                            }
                            else {
                                ourFSMY.increaseCount();
                            }
                        }
                        else {
                            if (ourFSMY.getCount() == 4 || ourFSMY.getCount() == 5 || ourFSMY.getCount() == 6 || ourFSMY.getCount() == 7) {
                                if ((accelerometerHistory.get(size - 1)[1] - ourFSMY.getPeak()) / ourFSMY.getCount() < -0.15) {
                                    ourFSMY.setState(states.FALL_A);
                                    ourFSMY.setPeak(0);
                                    ourFSMY.resetCount();
                                }
                            }
                            else if (ourFSMY.getCount() > 7) {
                                ourFSMY.setType(types.X);
                                ourFSMY.setState(states.DETERMINED);
                                ourFSMY.setPeak(0);
                                ourFSMY.resetCount();
                            }
                            else {
                                ourFSMY.increaseCount();
                            }
                        }
                        break;

                    case FALL_A:
                        if (ourFSMY.getPeak() == 0) {
                            if (accelerometerHistory.get(size - 1)[1] > accelerometerHistory.get(size - 2)[1]) {
                                ourFSMY.setPeak(accelerometerHistory.get(size - 2)[1]);
                                ourFSMY.resetCount();
                            } else if (ourFSMY.getCount() > 6) {
                                ourFSMY.setType(types.X);
                                ourFSMY.setState(states.DETERMINED);
                                ourFSMY.setPeak(0);
                                ourFSMY.resetCount();
                            }
                            else {
                                ourFSMY.increaseCount();
                            }
                        }
                        else {
                            if (ourFSMY.getCount() == 4 || ourFSMY.getCount() == 5 || ourFSMY.getCount() == 6) {
                                if ((accelerometerHistory.get(size - 1)[1] - ourFSMY.getPeak()) / ourFSMY.getCount() > 0.1) {
                                    ourFSMY.setState(states.DETERMINED);
                                    ourFSMY.setType(types.A);
                                    ourFSMY.setPeak(0);
                                    ourFSMY.resetCount();
                                }
                            }
                            else if (ourFSMY.getCount() > 6) {
                                ourFSMY.setType(types.X);
                                ourFSMY.setState(states.DETERMINED);
                                ourFSMY.setPeak(0);
                                ourFSMY.resetCount();
                            }
                            else {
                                ourFSMY.increaseCount();
                            }
                        }
                        break;
                    case DETERMINED:                            //Wait 50 events before resetting back to WAIT state
                        if (ourFSMY.getCount() < 50) {
                            ourFSMY.increaseCount();
                        } else {
                            ourFSMY.setState(states.WAIT);
                            ourFSMY.resetCount();
                        }
                        break;
                    default:
                        break;
                }

                //Check current event (X) for different things based on value of FSM
                switch (ourFSMX.getCurrentState()) {
                    case WAIT:
                        //Check for left
                        if ((accelerometerHistory.get(size - 1)[0] - accelerometerHistory.get(size - 5)[0]) / 4 < -0.1 || (accelerometerHistory.get(size - 1)[0] - accelerometerHistory.get(size - 6)[0]) / 5 < -0.1 || (accelerometerHistory.get(size - 1)[0] - accelerometerHistory.get(size - 7)[0]) / 6 < -0.1) {
                            ourFSMX.setState(states.FALL_B);
                        }

                        //Check for right
                        else if (((accelerometerHistory.get(size - 1)[0] - accelerometerHistory.get(size - 5)[0]) / 4 > 0.1) || (accelerometerHistory.get(size - 1)[0] - accelerometerHistory.get(size - 6)[0]) / 5 > 0.1 || (accelerometerHistory.get(size - 1)[0] - accelerometerHistory.get(size - 7)[0]) / 6 > 0.1) {
                            ourFSMX.setState(states.RISE_A);
                        }
                        break;

                    case FALL_B:
                        if (ourFSMX.getPeak() == 0) {
                            if (accelerometerHistory.get(size - 1)[0] > accelerometerHistory.get(size - 2)[0]) {
                                ourFSMX.setPeak(accelerometerHistory.get(size - 2)[0]);
                                ourFSMX.resetCount();
                            } else if (ourFSMX.getCount() > 6) {
                                ourFSMX.setType(types.X);
                                ourFSMX.setState(states.DETERMINED);
                                ourFSMX.setPeak(0);
                                ourFSMX.resetCount();
                            } else {
                                ourFSMX.increaseCount();
                            }
                        } else {
                            if (ourFSMX.getCount() == 4 || ourFSMX.getCount() == 5 || ourFSMX.getCount() == 6 || ourFSMX.getCount() == 7) {
                                if ((accelerometerHistory.get(size - 1)[0] - ourFSMX.getPeak()) / ourFSMX.getCount() > 0.15) {
                                    ourFSMX.setState(states.RISE_B);
                                    ourFSMX.setPeak(0);
                                    ourFSMX.resetCount();
                                }
                            } else if (ourFSMX.getCount() > 7) {
                                ourFSMX.setType(types.X);
                                ourFSMX.setState(states.DETERMINED);
                                ourFSMX.setPeak(0);
                                ourFSMX.resetCount();
                            } else {
                                ourFSMX.increaseCount();
                            }
                        }
                        break;

                    case RISE_B:
                        if (ourFSMX.getPeak() == 0) {
                            if (accelerometerHistory.get(size - 1)[0] < accelerometerHistory.get(size - 2)[0]) {
                                ourFSMX.setPeak(accelerometerHistory.get(size - 2)[0]);
                                ourFSMX.resetCount();
                            } else if (ourFSMX.getCount() > 6) {
                                ourFSMX.setType(types.X);
                                ourFSMX.setState(states.DETERMINED);
                                ourFSMX.setPeak(0);
                                ourFSMX.resetCount();
                            } else {
                                ourFSMX.increaseCount();
                            }
                        } else {
                            if (ourFSMX.getCount() == 4 || ourFSMX.getCount() == 5 || ourFSMX.getCount() == 6) {
                                if ((accelerometerHistory.get(size - 1)[0] - ourFSMX.getPeak()) / ourFSMX.getCount() < -0.1) {
                                    ourFSMX.setState(states.DETERMINED);
                                    ourFSMX.setType(types.B);
                                    ourFSMX.setPeak(0);
                                    ourFSMX.resetCount();
                                }
                            } else if (ourFSMX.getCount() > 6) {
                                ourFSMX.setType(types.X);
                                ourFSMX.setState(states.DETERMINED);
                                ourFSMX.setPeak(0);
                                ourFSMX.resetCount();
                            } else {
                                ourFSMX.increaseCount();
                            }
                        }
                        break;

                    case RISE_A:
                        if (ourFSMX.getPeak() == 0) {
                            if (accelerometerHistory.get(size - 1)[0] < accelerometerHistory.get(size - 2)[0]) {
                                ourFSMX.setPeak(accelerometerHistory.get(size - 2)[0]);
                                ourFSMX.resetCount();
                            } else if (ourFSMX.getCount() > 6) {
                                ourFSMX.setType(types.X);
                                ourFSMX.setState(states.DETERMINED);
                                ourFSMX.setPeak(0);
                                ourFSMX.resetCount();
                            } else {
                                ourFSMX.increaseCount();
                            }
                        } else {
                            if (ourFSMX.getCount() == 4 || ourFSMX.getCount() == 5 || ourFSMX.getCount() == 6 || ourFSMX.getCount() == 7) {
                                if ((accelerometerHistory.get(size - 1)[0] - ourFSMX.getPeak()) / ourFSMX.getCount() < -0.15) {
                                    ourFSMX.setState(states.FALL_A);
                                    ourFSMX.setPeak(0);
                                    ourFSMX.resetCount();
                                }
                            } else if (ourFSMX.getCount() > 7) {
                                ourFSMX.setType(types.X);
                                ourFSMX.setState(states.DETERMINED);
                                ourFSMX.setPeak(0);
                                ourFSMX.resetCount();
                            } else {
                                ourFSMX.increaseCount();
                            }
                        }
                        break;

                    case FALL_A:
                        if (ourFSMX.getPeak() == 0) {
                            if (accelerometerHistory.get(size - 1)[0] > accelerometerHistory.get(size - 2)[0]) {
                                ourFSMX.setPeak(accelerometerHistory.get(size - 2)[0]);
                                ourFSMX.resetCount();
                            } else if (ourFSMX.getCount() > 6) {
                                ourFSMX.setType(types.X);
                                ourFSMX.setState(states.DETERMINED);
                                ourFSMX.setPeak(0);
                                ourFSMX.resetCount();
                            } else {
                                ourFSMX.increaseCount();
                            }
                        } else {
                            if (ourFSMX.getCount() == 4 || ourFSMX.getCount() == 5 || ourFSMX.getCount() == 6) {
                                if ((accelerometerHistory.get(size - 1)[0] - ourFSMX.getPeak()) / ourFSMX.getCount() > 0.1) {
                                    ourFSMX.setState(states.DETERMINED);
                                    ourFSMX.setType(types.A);
                                    ourFSMX.setPeak(0);
                                    ourFSMX.resetCount();
                                }
                            } else if (ourFSMX.getCount() > 6) {
                                ourFSMX.setType(types.X);
                                ourFSMX.setState(states.DETERMINED);
                                ourFSMX.setPeak(0);
                                ourFSMX.resetCount();
                            } else {
                                ourFSMX.increaseCount();
                            }
                        }
                        break;
                    case DETERMINED:
                        if (ourFSMX.getCount() < 50) {
                            ourFSMX.increaseCount();
                        } else {
                            ourFSMX.setState(states.WAIT);
                            ourFSMX.resetCount();
                        }
                        break;
                    default:
                        break;
                }
            }

            //If X XOR Y are determined, display their type, otherwise display unknown
            //Then set game direction
            if (ourFSMX.getCurrentState() == states.DETERMINED && ourFSMY.getCurrentState() != states.DETERMINED) {
                if (ourFSMX.getType() == types.B) {
                    if (!gameLoopTask.getMotion()) { //Allow block to finish moving before declaring new direction; makes sure block doesn't get stuck halfway
                        FSMstatus.setText("LEFT");
                        gameLoopTask.setDirection(GameLoopTask.gameDirection.LEFT);
                    }
                } else if (ourFSMX.getType() == types.A) {
                    if (!gameLoopTask.getMotion()) {
                        FSMstatus.setText("RIGHT");
                        gameLoopTask.setDirection(GameLoopTask.gameDirection.RIGHT);
                    }
                } else {
                    if (!gameLoopTask.getMotion()) {
                        FSMstatus.setText("UNKNOWN");
                        gameLoopTask.setDirection(GameLoopTask.gameDirection.NO_MOVEMENT);
                    }
                }
            } else if (ourFSMY.getCurrentState() == states.DETERMINED && ourFSMX.getCurrentState() != states.DETERMINED) {
                if (ourFSMY.getType() == types.B) {
                    if (!gameLoopTask.getMotion()) {
                        FSMstatus.setText("DOWN");
                        gameLoopTask.setDirection(GameLoopTask.gameDirection.DOWN);
                    }
                } else if (ourFSMY.getType() == types.A) {
                    if (!gameLoopTask.getMotion()) {
                        FSMstatus.setText("UP");
                        gameLoopTask.setDirection(GameLoopTask.gameDirection.UP);
                    }
                } else {
                    if (!gameLoopTask.getMotion()) {
                        FSMstatus.setText("UNKNOWN");
                        gameLoopTask.setDirection(GameLoopTask.gameDirection.NO_MOVEMENT);
                    }
                }
            } else {
                if (!gameLoopTask.getMotion()) {
                    FSMstatus.setText("UNKNOWN");
                    gameLoopTask.setDirection(GameLoopTask.gameDirection.NO_MOVEMENT);
                }
            }
        }
    }
}
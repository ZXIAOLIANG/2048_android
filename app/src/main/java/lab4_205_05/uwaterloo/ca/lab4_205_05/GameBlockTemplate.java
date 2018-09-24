package lab4_205_05.uwaterloo.ca.lab4_205_05;

import android.content.Context;

public abstract class GameBlockTemplate extends android.support.v7.widget.AppCompatImageView {
    //Necessary to use default constructor for AppCompatImageView
    public GameBlockTemplate(Context context) {
        super(context);
    }

    public abstract void setDestination();

    public abstract void move();
}


package edu.uic.skatha2.microgolf;

import android.app.Activity;

/**
 * Created by sabask on 4/14/18.
 */

class Hole {
    private String status = MainActivity.NOT_OCCUPIED;
    private int color;

    public Hole(int color) {
        this.color = color;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}

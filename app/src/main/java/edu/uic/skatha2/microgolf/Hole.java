package edu.uic.skatha2.microgolf;

import android.app.Activity;

/**
 * Created by sabask on 4/14/18.
 */

class Hole {
    private int wThreadName;
    private String response;
    private String status = MainActivity.NOT_OCCUPIED;
    private int color;

    public Hole(int color, String response) {
        this.color = color;
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getwThreadName() {
        return wThreadName;
    }

    public void setwThreadName(int wThreadName) {
        this.wThreadName = wThreadName;
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

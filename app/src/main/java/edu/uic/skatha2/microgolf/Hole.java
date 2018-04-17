package edu.uic.skatha2.microgolf;

/**
 * Created by sabask on 4/14/18.
 *
 * Represents each hole in the array of holes
 */

class Hole {
    //representing state(OCCUPIED, NOT_OCCUPIED) of the Hole
    private String status = MainActivity.NOT_OCCUPIED;

    //representing color(BLUE: Player 1, RED: Player 2, BLACK: NOT_OCCUPIED, GREEN: Winning Hole) of the Hole
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

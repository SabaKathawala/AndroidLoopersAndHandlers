package edu.uic.skatha2.microgolf;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class MainActivity extends ListActivity {

    // state of each hole
    public static final String OCCUPIED = "1";
    public static final String NOT_OCCUPIED = "0";

    // four response strings and their code
    public static final String JACKPOT = "JACKPOT";
    public static final String NEAR_MISS = "NEAR MISS";
    public static final int NEAR_MISS_CODE = 200;
    public static final String NEAR_GROUP = "NEAR GROUP";
    public static final int NEAR_GROUP_CODE = 300;
    public static final String BAD_MISS = "BAD MISS";
    public static final int BAD_MISS_CODE = 400;
    public static final String CATASTROPHE = "CATASTROPHE";

    // references to the two player threads
    private PlayerThreadOne playerThreadOne;
    private PlayerThreadTwo playerThreadTwo;

    // references to UI elements
    private Button startButton;
    private ListView player1;
    private ListView player2;
    private TextView status;
    private TextView winningHoleText;

    // to check if game is over
    private Boolean gameOver = false;

    // to hold group limits
    private int[] groups = {10, 20, 30, 40, 50};

    // to hold winning hole position
    private int winningHole;

    // to hold group of winning hole
    private  int winningHoleGroup;

    // to hold shot played by each player
    private ArrayList<Integer> player1Shots;
    private ArrayList<Integer> player2Shots;

    // to synchronize the threads
    Object threadOne = new Object();
    Object threadTwo = new Object();
    boolean turn = true;

    // array of holes for ListView
    private Hole[] holes;

    private MainActivity context;

    // find group of the hole shot
    private int findGroup(int shot) {
        for(int i=0; i < groups.length; i++) {
            if(shot < groups[i]) {
                return i;
            }
        }
        return -1;
    }

    // check whether hole shot is winningHole
    private boolean isWinningHole(int shot) {
        return shot == winningHole;
    }

    // check whether the current shot hole is already occupied
    private boolean isOccupied(int shot) {
        return holes[shot].getStatus() == OCCUPIED;
    }

    // update the corresponding ListView for player
    private void updatePlayersListView(int shot, int code) {
        switch (code) {
            case 1:
                player1Shots.add(shot+1);
                ((BaseAdapter) player1.getAdapter()).notifyDataSetChanged();
                break;

            case 2:
                player2Shots.add(shot+1);
                ((BaseAdapter) player2.getAdapter()).notifyDataSetChanged();
                break;
        }

    }

    // Handler is associated with UI Thread
    public Handler mHandler = new Handler() {
        //what: shot played
        //arg1: thread code
        //arg2 thread color
        public void handleMessage(Message msg) {
            int shot = msg.what;
            int code = msg.arg1;
            updatePlayersListView(shot, code);
            String response;

            //if winningHole shot: display winner and end game
            if(isWinningHole(shot)) {
                endGame();
                Toast.makeText(context, "Player: " + code + " won", Toast.LENGTH_LONG).show();
                gameOver = true;
                status.setText("Player: " + code + " Shot: " + (shot+1) + " " + JACKPOT);
                return;
            }
            //if already occupied hole shot: display winner and end game
            else if(isOccupied(shot)) {
                endGame();
                Toast.makeText(context, "Player: " + (code == 1 ? 2 : 1) + " won", Toast.LENGTH_LONG).show();
                gameOver = true;
                status.setText("Player: " + code + "| Shot: " + (shot+1) + "| " + CATASTROPHE);
                return;
            }
            // color the hole
            else {
                holes[shot].setStatus(OCCUPIED);
                holes[shot].setColor(msg.arg2);
                //update holes listView
                ((BaseAdapter) getListView().getAdapter()).notifyDataSetChanged();
            }

            //identify the type of shot
            int group = findGroup(shot);
            int responseCode;
            if(group == winningHoleGroup) {
                response = NEAR_MISS;
                responseCode = NEAR_MISS_CODE;
            } else if(group+1 == winningHoleGroup || group-1 == winningHoleGroup){
                response = NEAR_GROUP;
                responseCode = NEAR_GROUP_CODE;
            } else {
                response = BAD_MISS;
                responseCode = BAD_MISS_CODE;
            }

            status.setText("Player: " + msg.arg1 + "| Shot: " + (shot+1) + "| " + response);

            Message message;
            //send response to worker thread
            switch (msg.arg1) {
                case 1:
                    message = playerThreadOne.myHandler.obtainMessage();
                    message.what = responseCode;
                    playerThreadOne.myHandler.sendMessage(message);
                    //send a runnable to play shot to next player
                    playerThreadTwo.myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            playerThreadTwo.playShot();
                        }
                    });

                    break;
                case 2:
                    message = playerThreadTwo.myHandler.obtainMessage();
                    message.what = responseCode;
                    playerThreadTwo.myHandler.sendMessage(message);
                    //send a runnable to play shot to next player
                    playerThreadOne.myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            playerThreadOne.playShot();
                        }
                    });
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);

        //Initialize UI elements references
        startButton = (Button) findViewById(R.id.start_button);
        winningHoleText = (TextView) findViewById(R.id.winning_hole);
        player1 = (ListView) findViewById(R.id.player1);
        player2 = (ListView) findViewById(R.id.player2);
        status = (TextView) findViewById((R.id.status));

        //initilialize game settings
        initializeSettings();

        //set listener
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //restart game
                if(gameOver) {
                    initializeSettings();
                }

                //start the threads
                playerThreadOne = new PlayerThreadOne("1");
                playerThreadOne.start();

                playerThreadTwo = new PlayerThreadTwo("2");
                playerThreadTwo.start();

            }
        });
    }

    //initialize all variables to start game
    private void initializeSettings() {
        gameOver = false;

        holes = new Hole[50];
        for(int i=0; i<50; i++) {
            holes[i] = new Hole(R.drawable.black);
        }
        //determine wining hole and set its color to green
        winningHole = new Random().nextInt(50);
        winningHoleGroup = findGroup(winningHole);
        holes[winningHole] = new Hole(R.drawable.green);
        setListAdapter(new MyAdapter<Hole>(this, holes));
        winningHoleText.setText("Winning Hole: " + (winningHole+1));

        player1Shots = new ArrayList<>();
        player2Shots = new ArrayList<>();
        player1.setAdapter(new ArrayAdapter<Integer>(this, R.layout.player_shots, player1Shots));
        player2.setAdapter(new ArrayAdapter<Integer>(this, R.layout.player_shots, player2Shots));

    }


    public class PlayerThreadOne extends Thread {
        private static final int COLOR = R.drawable.blue;
        // handler associated with this thread
        public Handler myHandler;
        // player name
        private static final int ME = 1;
        // lastShot played by the player
        private int lastShot;

        // set of unique shots
        private LinkedHashSet<Integer> shotsPlayed = new LinkedHashSet<>();

        // the type of shot played
        private int response;

        public PlayerThreadOne(String name) {
            super(name);
        }

        @Override
        public void run() {
            Looper.prepare();
            myHandler = new Handler() {
                public void handleMessage(Message msg) {
                    response = msg.what;
                }
            };

            synchronized (gameOver) {
                synchronized (threadTwo) {
                    if (!gameOver) {
                        // play first shot: random
                        int shot = randomShot(shotsPlayed);
                        lastShot = shot;
                        Message msg = mHandler.obtainMessage(shot);
                        msg.arg1 = ME;
                        msg.arg2 = COLOR;
                        mHandler.sendMessage(msg);
                        shotsPlayed.add(shot);
                        turn = true;
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            Looper.loop();
            // keep thread running till game is not over
            synchronized (threadOne) {
                threadOne.notify();
            }
            while (!gameOver) ;
            Looper.myLooper().quit();
        }

        // playing strategy:
        // NEAR_MISS: SAME GROUP
        // NEAR_GROUP: RANDOM
        // BAD MISS: CLOSE GROUP
        public void playShot() {
            synchronized (threadTwo) {
                if (!gameOver) {
                    try {
                        while (!turn) {
                            threadTwo.wait();
                        }
                        turn = false;
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg;
                    int nextShot;
                    switch (response) {
                        case NEAR_MISS_CODE:
                            nextShot = sameGroup(lastShot, shotsPlayed);
                            if (nextShot == -1) {
                                nextShot = randomShot(shotsPlayed);
                                msg = mHandler.obtainMessage(nextShot);
                                msg.arg1 = ME;
                                msg.arg2 = COLOR;
                                mHandler.sendMessage(msg);
                                shotsPlayed.add(nextShot);
                            }
                            break;

                        case NEAR_GROUP_CODE:
                            nextShot = randomShot(shotsPlayed);
                            msg = mHandler.obtainMessage(nextShot);
                            msg.arg1 = ME;
                            msg.arg2 = COLOR;
                            mHandler.sendMessage(msg);
                            shotsPlayed.add(nextShot);
                            break;

                        case BAD_MISS_CODE:
                            nextShot = closeGroup(lastShot, shotsPlayed);
                            if (nextShot == -1) {
                                nextShot = randomShot(shotsPlayed);
                                msg = mHandler.obtainMessage(nextShot);
                                msg.arg1 = ME;
                                msg.arg2 = COLOR;
                                mHandler.sendMessage(msg);
                                shotsPlayed.add(nextShot);
                            }
                            break;
                    }
                }
            }
            synchronized (threadTwo) {
                threadTwo.notify();
            }
        }
    }

    public class PlayerThreadTwo extends Thread {
        private static final int COLOR = R.drawable.red;
        // handler associated with this thread
        public Handler myHandler;
        private static final int ME = 2;
        private int lastShot;
        // unique set of shots played
        private LinkedHashSet<Integer> shotsPlayed = new LinkedHashSet<>();
        private int response;

        public PlayerThreadTwo(String name) {
            super(name);
        }

        @Override
        public void run() {
            Looper.prepare();
            myHandler = new Handler() {
                public void handleMessage(Message msg) {
                    response = msg.what;
                }
            };

            synchronized (gameOver) {
                synchronized (threadTwo) {
                    if (!gameOver) {
                        //play first shot : RANDOM
                        int shot = randomShot(shotsPlayed);
                        lastShot = shot;
                        Message msg = mHandler.obtainMessage(shot);
                        msg.arg1 = ME;
                        msg.arg2 = COLOR;
                        mHandler.sendMessage(msg);
                        shotsPlayed.add(shot);
                        turn = false;
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            Looper.loop();
            synchronized (threadTwo) {
                threadTwo.notify();
            }
             //keep thread running till game is not over
            while(!gameOver);
        }

        // playing strategy:
        // NEAR_MISS: CLOSE GROUP
        // NEAR_GROUP: CLOSE GROUP
        // BAD MISS: RANDOM
        public void playShot() {
            synchronized (threadTwo) {
                Message msg;
                int nextShot;
                if (!gameOver) {
                    try {
                        while (turn) {
                            threadTwo.wait();
                        }
                        turn = true;
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    switch (response) {
                        case NEAR_MISS_CODE:
                            nextShot = closeGroup(lastShot, shotsPlayed);
                            if (nextShot == -1) {
                                nextShot = randomShot(shotsPlayed);
                                msg = mHandler.obtainMessage(nextShot);
                                msg.arg1 = ME;
                                msg.arg2 = COLOR;
                                mHandler.sendMessage(msg);
                                shotsPlayed.add(nextShot);
                            }
                            break;

                        case NEAR_GROUP_CODE:
                            nextShot = closeGroup(lastShot, shotsPlayed);
                            if (nextShot == -1) {
                                nextShot = randomShot(shotsPlayed);
                                msg = mHandler.obtainMessage(nextShot);
                                msg.arg1 = ME;
                                msg.arg2 = COLOR;
                                mHandler.sendMessage(msg);
                                shotsPlayed.add(nextShot);
                            }
                            break;

                        case BAD_MISS_CODE:
                            nextShot = randomShot(shotsPlayed);
                            msg = mHandler.obtainMessage(nextShot);
                            msg.arg1 = ME;
                            msg.arg2 = COLOR;
                            mHandler.sendMessage(msg);
                            shotsPlayed.add(nextShot);
                            break;
                    }
                    synchronized (threadTwo) {
                        threadTwo.notify();
                    }
                }
            }
        }
    }

    // find a random shot
    private int randomShot(LinkedHashSet<Integer> shotsPlayed) {
        Random shot = new Random();
        int nextShot = shot.nextInt(50);
        while(shotsPlayed.contains(nextShot)) {
            nextShot = shot.nextInt(50);
        }
        return nextShot;
    }

    // find shot in adjacent group
    private int closeGroup(int lastShot, LinkedHashSet<Integer> shotsPlayed) {
        int group = findGroup(lastShot);
        int start = group == 0 ? group : (group-1)*10;
        int end = group == 4 ? (group+1)*10 : (group+2)*10;
        int nextShot = -1;
        for(int i = start; i < end; i++ ) {
            if(!shotsPlayed.contains(lastShot)) {
                nextShot = i;
                break;
            }
        }
        return nextShot;
    }

    // find shot in same group
    private int sameGroup(int lastShot, Set<Integer> shotsPlayed) {
        int group = findGroup(lastShot);
        int nextShot = -1;
        for(int i = group*10; i < group*10+10; i++ ) {
            if(!shotsPlayed.contains(lastShot)) {
                nextShot = i;
                break;
            }
        }
        return nextShot;
    }

    //quit loopers
    private void endGame() {
        playerThreadTwo.myHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                playerThreadTwo.myHandler.getLooper().quit();
            }
        });
        playerThreadOne.myHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                playerThreadOne.myHandler.getLooper().quit();
            }
        });
    }
}

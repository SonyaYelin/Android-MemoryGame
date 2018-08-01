package memoryGame;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sonya.myapplication.R;
import com.transitionseverywhere.Explode;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import memoryGame.bl.Player;
import memoryGame.bl.Score;
import memoryGame.ui.Card;

public class GameActivity extends AppCompatActivity implements IConstants,GameService.GameSensorListener {

    //view
    private TextView                    tvTimer;
    private Card[][]                    cards;
    private Card                        firstCard;
    private Card                        secondCard;
    private Set<Card>                   matchedPairs = new HashSet<>();
    private ArrayList<Integer>          allImages = new ArrayList<>();
    LinearLayout                        imgLayout;
    //logic
    private Player                      player;
    private int                         pressedCards;
    private boolean                     gameIsOn;
    private int                         currentTime;
    private int                         scoreValue;
    private ILevels.Level               level;

    private Database                    database = new Database(this);
    private Location                    location;
    private boolean                     isBound = false;
    private GameService                 gameService;
    private GameService.MyLocalBinder   binder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = new Intent(this, GameService.class);
        getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY);

        tvTimer = (TextView) findViewById(R.id.tv_timer);
        initLevel();

        TextView tvName = (TextView) findViewById(R.id.tv_name);
        tvName.setText(player.getName());

        initImages();
        initLayout();
        setListeners();
        setTimer();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            binder.DeleteSensorListener();
            unbindService(connection);
            isBound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, GameService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }


    //game
    private void setCardsEnabled(Boolean enabled) {
        for (int i = 0; i < cards.length; i++) {
            for (int j = 0; j < cards[0].length; j++)
                if (!matchedPairs.contains(cards[i][j]))
                    cards[i][j].setEnabled(enabled);
        }
    }

    private void onCardPressed(Card card) {
        //turn pressed card to front side
        card.turnToFront();
        pressedCards++;
        if ( pressedCards == PAIR )
            twoCardsPressed(card);
        else
            oneCardPressed(card);

        pressedCards = pressedCards % PAIR;
    }

    private void oneCardPressed(Card card) {
        card.setEnabled(false);
        firstCard = card;
    }

    private void twoCardsPressed(Card card) {
        secondCard = card;
        setCardsEnabled(false);

        //check if the selected images are equal
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkMatch();
            }
        }, 1000);
    }

    private void checkMatch() {
        //if images are equal
        if ( isMatchingPair() ) {
            matchedPairs.add(firstCard);
            checkWin();
        }
        else {
            firstCard.turnToBack();
            secondCard.turnToBack();
        }
        setCardsEnabled(true);
    }

    private boolean isMatchingPair() {
        return firstCard.equals(secondCard);
    }


    //win
    private void checkWin() {
        if ( matchedPairs.size() == level.pairsNum() )
            onWin();
    }

    private void onWin() {
        gameIsOn = false;
        scoreValue = calcScore();
        if (isScoreHighEnough())
            saveScore();
        winAnimation();
    }

    private void winAnimation() {
        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(HALF_SEC);
        imgLayout.startAnimation(rotate);
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                onEndGame(YOU_WON);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private boolean isScoreHighEnough() {
        ArrayList<Score> allScores = database.getScoreList();

        if ( allScores.size() < 10 || allScores.get(allScores.size() - 1).getValue() < scoreValue )
            return true;
        else
            return false;
    }

    private void saveScore() {
        if (isBound) {
            updateLocationFromService();

            Geocoder g = new Geocoder(this, Locale.getDefault());
            String address = "";
            try {
                List<Address> l = g.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                address += l.get(0).getCountryName().toString() + "\n" + l.get(0).getLocality() + "\n" + l.get(0).getThoroughfare();
            } catch (IOException e) {e.printStackTrace();}

            Score newScore = new Score(scoreValue, player.getName(), location, address);
            database.addScores(newScore);
        }
    }

    private int calcScore(){
        return currentTime - DIFF*( level.num()-1 );
    }


    //loss
    private void checkLoss() {
        if ( currentTime == level.maxTime() )
            onLoss();
    }

    private void onLoss() {
        gameIsOn = false;
        lossAnimation();
    }

    private void lossAnimation() {

        final Rect viewRect = new Rect();
        imgLayout.getGlobalVisibleRect(viewRect);

        // create Explode transition with epicenter
        Transition explode = new Explode()
                .setEpicenterCallback(new Transition.EpicenterCallback() {
                    @Override
                    public Rect onGetEpicenter(Transition transition) {
                        return viewRect;
                    }
                });
        explode.setDuration(ONE_SEC);
        explode.addListener(getTransitionListener(GAME_OVER));

        for(int i = 0; i < level.rowsNum(); i ++) {
            LinearLayout row = (LinearLayout) imgLayout.getChildAt(i);
            TransitionManager.beginDelayedTransition(row, explode);
        }

        removeAllCards();
    }

    private void removeAllCards(){

        for(int i = 0; i < level.rowsNum(); i ++){
            LinearLayout row = (LinearLayout) imgLayout.getChildAt(i);
            for ( int j = 0; j < level.colsNum(); j ++)
                row.removeView(cards[i][j]);
        }
    }

    private Transition.TransitionListener getTransitionListener(final String msg){
       return  new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
               onEndGame(msg);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        };
    }

    //end game
    private void onEndGame(final String msg) {

        Intent i = new Intent(getBaseContext(), EndGameActivity.class);
        i.putExtra(PLAYER, player );
        i.putExtra(MSG, msg);

        finish();

        startActivity(i);
    }


    //initial set-up
    private void setListeners() {

        for (int i = 0; i < cards.length; i++) {
            for (int j = 0; j < cards[0].length; j++) {
                cards[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onCardPressed((Card) view);
                    }
                });
            }
        }
    }

    private void setTimer() {
        final long startTime = System.currentTimeMillis();
        final Handler timerHandler = new Handler();

        Runnable timerRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                if ( gameIsOn ) {
                    long millis = System.currentTimeMillis() - startTime;
                    int seconds = (int) (millis / ONE_SEC);
                    currentTime = seconds;
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    tvTimer.setText(String.format("%d:%02d", minutes, seconds));
                    timerHandler.postDelayed(this, HALF_SEC);

                    checkLoss();
                }
                else
                    timerHandler.removeCallbacks(this);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void initLevel() {
        currentTime = 0;
        gameIsOn = true;
        Intent intent = getIntent();
        player = (Player) intent.getSerializableExtra(PLAYER);
        int level = intent.getIntExtra(LEVEL, 1);

        switch (level) {
            case LEVEL_1:
                this.level = ILevels.Level.LEVEL1;
                break;
            case LEVEL_2:
                this.level = ILevels.Level.LEVEL2;
                break;
            case LEVEL_3:
                this.level = ILevels.Level.LEVEL3;
                break;
            default:
                this.level = ILevels.Level.LEVEL1;
                break;
        }
    }

    private void initImages() {
        for ( int j = 1; j <= level.pairsNum() ; j++ ) {
            int img = this.getResources().getIdentifier("im" + j, "drawable", this.getPackageName());
            allImages.add(img);
        }
    }

    private void initLayout() {
        int rowsNum = level.rowsNum();
        int colsNum = level.colsNum();
        int cardsAdded = 0;

        cards = new Card[rowsNum][colsNum];

        List<Integer> tempImages = new ArrayList<>();
        imgLayout = (LinearLayout) findViewById(R.id.ll_images);
        imgLayout.setWeightSum(rowsNum);
        LinearLayout[] rows = new LinearLayout[rowsNum];

        //set rows in imgLayout and images in each row
        for (int i = 0; i < rowsNum; i++) {

            rows[i] = new LinearLayout(this);
            setRow(rows[i]);

            for (int j = 0; j < colsNum; j++) {

                //first card of each pair were initialized -> need to re-initialize list
                if (cardsAdded % level.pairsNum() == 0)
                    tempImages.addAll(allImages);

                int random = (new Random()).nextInt(tempImages.size());
                cards[i][j] = new Card(this, tempImages.get(random), R.drawable.back);
                tempImages.remove(random);

                cards[i][j].turnToBack();
                cards[i][j].setLayoutParams(new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT, (float) 1.0));
                rows[i].addView(cards[i][j]);
                cardsAdded++;
            }
            imgLayout.addView(rows[i]);
        }
    }

    private void setRow(LinearLayout row) {
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setWeightSum(level.colsNum());
        row.setLayoutParams
                (new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, (float) 1.0));
    }

    //location
    private void updateLocationFromService() {
        location = gameService.getLastLocation();
        if (location == null)
            location = gameService.setDummy();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (GameService.MyLocalBinder) service;
            binder.registerSensorListener(GameActivity.this);
            gameService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    //sensors
    @Override
    public void onOrientationChanged() {
        for( int i = 0 ; i < level.rowsNum(); i++ ){
            for (int j = 0 ; j < level.colsNum(); j++ )
                cards[i][j].turnToBack();
        }
        pressedCards = 0;
        matchedPairs.clear();
    }
}
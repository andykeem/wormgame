package com.example.foo.wormgameapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    protected static final String TAG = MainActivity.class.getSimpleName();
    protected GameView mGameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        mGameView = new GameView(this);
        this.setContentView(mGameView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGameView.startGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGameView.pauseGmae();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGameView.resumeGame();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGameView.stopGame();
    }
}
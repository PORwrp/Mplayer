package com.roshan.mplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.widget.Toast;

public class Player extends AppCompatActivity implements View.OnClickListener {
    static MediaPlayer mp;
    ArrayList<File> mysongs;
    SeekBar seekbar;
    Button btPlay, btFF, btFB, btNXT, btPv;
    int position;
    Thread updateSeekBar;
    Uri u;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    TextView tx1,tx2,tx3,tx4;
    public static int oneTimeOnly = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        btPlay = (Button) findViewById(R.id.btPlay);
        btFF = (Button) findViewById(R.id.btFF);
        btFB = (Button) findViewById(R.id.btFB);
        btNXT = (Button) findViewById(R.id.btNXT);
        btPv = (Button) findViewById(R.id.btPv);

        tx1 = (TextView)findViewById(R.id.textView3);
        tx2 = (TextView)findViewById(R.id.textView4);
        tx3 = (TextView)findViewById(R.id.textView5);
        tx4 = (TextView)findViewById(R.id.textView6);
        tx2.setText("Song.mp3");
        seekbar = (SeekBar)findViewById(R.id.seekBar);
        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int totalduration = mp.getDuration();
                int currentposition = 0;
                while(currentposition < totalduration) {
                    try {
                        sleep(3000);
                        currentposition = mp.getCurrentPosition();
                        seekbar.setProgress(currentposition);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                super.run();
            }
        };
        seekbar.setClickable(false);

        btPlay.setOnClickListener(this);
        btFF.setOnClickListener(this);
        btFB.setOnClickListener(this);
        btNXT.setOnClickListener(this);
        btPv.setOnClickListener(this);
        if(mp != null) {
            mp.stop();
            mp.release();
            seekbar.setProgress(0);
        }
        Intent i = getIntent();
        Bundle b = i.getExtras();
        /*ArrayList<File>*/ mysongs = (ArrayList) b.getParcelableArrayList("songlist");
        position = b.getInt("pos", 0);
        u = Uri.parse( mysongs.get(position).toString());
        /*if(mp.isPlaying()) {
            mp.stop();
            mp.release();
        }*/
        mp = MediaPlayer.create(getApplicationContext(), u);
        mp.start();
        tx4.setText(mysongs.get(position).getName().toString());
        seekbar.setMax(mp.getDuration());
        updateSeekBar.start();
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }
        });
        finalTime = mp.getDuration();
        startTime = mp.getCurrentPosition();

        if (oneTimeOnly == 0) {
            seekbar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }
        tx3.setText(String.format("%d:%d ",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)))
        );
        tx1.setText(String.format("%d:%d ",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                startTime)))
        );
        if(mp.getCurrentPosition() == mp.getDuration()) {
            mp.stop();
            mp.release();
            position = (position + 1) % mysongs.size();
            u = Uri.parse( mysongs.get(position + 1).toString());
            mp = MediaPlayer.create(getApplicationContext(), u);
            mp.start();
            seekbar.setMax(mp.getDuration());
            finalTime = mp.getDuration();
            startTime = mp.getCurrentPosition();
            if (oneTimeOnly == 0) {
                seekbar.setMax((int) finalTime);
                oneTimeOnly = 1;
            }
            seekbar.setProgress((int)startTime);
        }
        seekbar.setProgress((int)startTime);
        myHandler.postDelayed(UpdateSongTime,100);
        btPlay.setEnabled(true);
        //btPv.setEnabled(false);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btPlay:
                if(mp.isPlaying()) {
                    btPlay.setText(">");
                    mp.pause();
                }
                else {
                    mp.start();
                    btPlay.setText("||");
                }
                break;
            case R.id.btFF:
                mp.seekTo(mp.getCurrentPosition() + 5000);
                break;
            case R.id.btFB:
                //mp.seekTo(mp.getCurrentPosition() - 5000);
                int temp = (int)startTime;
                if((temp-5000)>0) {
                    startTime = startTime - 5000;
                    mp.seekTo((int) startTime);
                }
                else break;
                break;
            case R.id.btNXT:
                mp.stop();
                mp.release();
                position = (position + 1) % mysongs.size();
                u = Uri.parse( mysongs.get(position + 1).toString());
                mp = MediaPlayer.create(getApplicationContext(), u);
                mp.start();
                tx4.setText(mysongs.get(position).getName().toString());
                seekbar.setMax(mp.getDuration());
                finalTime = mp.getDuration();
                startTime = mp.getCurrentPosition();
                if (oneTimeOnly == 0) {
                    seekbar.setMax((int) finalTime);
                    oneTimeOnly = 1;
                }
                seekbar.setProgress((int)startTime);
                break;
            case R.id.btPv:
                mp.stop();
                mp.release();
                position = ((position - 1) < 0)? mysongs.size() - 1: position - 1;
                u = Uri.parse(mysongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(), u);
                mp.start();
                tx4.setText(mysongs.get(position).getName().toString());
                seekbar.setMax(mp.getDuration());
                break;
        }
    }
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mp.getCurrentPosition();
            tx1.setText(String.format("%d:%d ",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            seekbar.setProgress((int)startTime);
            myHandler.postDelayed(this, 100);
        }
    };
}

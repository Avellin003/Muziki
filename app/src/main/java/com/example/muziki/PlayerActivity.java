//Media Player Activity File

package com.example.muziki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button btnplay, btnnext, btnprev, btnff, btnfr; //Initalize variables to use
    TextView txtsname, txtsstart, txtsstop;
    SeekBar seekmusic;
    String sname;
    ImageView imageView;

    private MediaPlayerService mediaPlayerService;
    private boolean isServiceBound = false; //Initialize by Unbinding Foreground Service (MediaPlayerService.java)to do it later


    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateseekbar;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            isServiceBound = true;
        } //Binds the ForeGround Service (MediaPlayerService.java)

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player); //Calls the activity_player.xml in charge of player skeleton

        btnprev = findViewById(R.id.btnprev);//Links the initialized variables to the buttons in player skeleton using id
        btnnext = findViewById(R.id.btnnext);
        btnplay = findViewById(R.id.playbtn);
        btnff = findViewById(R.id.btnff);
        btnfr = findViewById(R.id.btnfr);
        txtsname = findViewById(R.id.txtsn);
        txtsstart = findViewById(R.id.txtsstart);
        txtsstop = findViewById(R.id.txtsstop);
        seekmusic = findViewById(R.id.seekbar);
        imageView = findViewById(R.id.imageview);

        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release(); //Checks if the media Player was playing this is done to avoid songs mixturing
        }

        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("position", 0);
        txtsname.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString()); //Checks the Songs Uri given after reading them in MainActivity.java
        sname = mySongs.get(position).getName(); //gets the name of the song using position
        txtsname.setText(sname);//Provides the name of the song to the txtsname linked to the song displayer in activity_player.xml
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);//Creates the session
        mediaPlayer.start();//Starts playing the song

        //seekbar is used to track the duration and time left for song to end using visuals
        updateseekbar = new Thread()
        {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();//Gets the song's total duration
                int currentposition = 0;//initializes the start at zero as the song just started

                while (currentposition < totalDuration)
                {
                    try
                    {
                        sleep(500);//updates the seekbar current location after every 0.5 seconds
                        currentposition = mediaPlayer.getCurrentPosition();//Tracks the current song position in order to show it visualy
                        seekmusic.setProgress(currentposition);//sets progress
                    }
                    catch (InterruptedException | IllegalStateException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        seekmusic.setMax(mediaPlayer.getDuration());//gets the duration
        updateseekbar.start();//Starts the process
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);//fills the time that we've exceeded in seekbar
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);//sets the color of the seekbar progression dot

        //seekmusic in charge of changing the seekbar progression manually

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mediaPlayer.seekTo(seekBar.getProgress());

            }
        });

        String endTime = createTime(mediaPlayer.getDuration());//gets the end time
        txtsstop.setText(endTime);//provides the end time to the place holder in the activity_player.xml

        final Handler handler = new Handler();
        final int delay = 1000;//delays 1sec before moving on

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtsstart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

        //play and pause listener
        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    btnplay.setBackgroundResource(R.drawable.play__1_);
                    mediaPlayer.pause();
                } else {
                    btnplay.setBackgroundResource(R.drawable.pause__1_);
                    mediaPlayer.start();
                }
            }
        });

        //next listener
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnnext.performClick();
            }
        });
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();

                String endTime = createTime(mediaPlayer.getDuration());
                txtsstop.setText(endTime);


                txtsname.setText(sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.pause__1_);
                startAnimation(imageView);
            }
        });

        //previous button listener

        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position - 1) < 0) ? (mySongs.size() - 1) : (position - 1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);

                String endTime = createTime(mediaPlayer.getDuration());
                txtsstop.setText(endTime);


                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.pause__1_);
                startAnimation(imageView);
            }
        });

        //Fast forward listener

        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+1000);
                }

            }
        });

        //Fast Backward Listener

        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-1000);
                }

            }
        });
    }

    //Starts the rotational animation of the logo when we press next or previous button

    public void startAnimation(View view)
    {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    //Creates the proper time display by dividing into minutes and seconds for a better seeing of time while playing

    public String createTime(int duration)
    {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time+=min+":";

        if (sec < 10)
        {
            time+="0";
        }
        time+=sec;
        return time;
    }

    //Defines what happens when we destroy or remove the app from currently working apps in android

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
}

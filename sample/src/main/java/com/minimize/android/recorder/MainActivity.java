package com.minimize.android.recorder;

import com.example.arkhitech.reactiveaudiorecord.R;
import com.minimize.recorder.RecorderOnSubscribe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import library.minimize.com.chronometerpersist.ChronometerPersist;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity
{
    private Subscription mSubscription;
    private RecorderOnSubscribe mRecorderOnSubscribe;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView textViewPath = (TextView) findViewById(R.id.textViewPath);

        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer);

        final ChronometerPersist chronometerPersist = ChronometerPersist.getInstance(chronometer,
                                                                                     getSharedPreferences(
                                                                                             "MyPrefs",
                                                                                             MODE_PRIVATE));
        final String filePath = Environment.getExternalStorageDirectory() + "/sample.wav";

        textViewPath.setText(filePath);

        mRecorderOnSubscribe = new RecorderOnSubscribe.Builder(filePath)
                .createSubscription();

        final View playButton = findViewById(R.id.buttonPlay);
        findViewById(R.id.buttonRecord).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //start recording
                if (mRecorderOnSubscribe.isRecording())
                {
                    ((TextView) view).setText("Record");
                    try
                    {
                        mRecorderOnSubscribe.stop();
                    } catch (IOException e)
                    {
                        Log.e("Recorder", e.getMessage());
                    }
                    playButton.setVisibility(View.VISIBLE);
                    chronometerPersist.stopChronometer();
                }
                else
                {
                    ((TextView) view).setText("Stop");
                    try
                    {
                        mRecorderOnSubscribe.start();
                    } catch (FileNotFoundException e)
                    {
                        Log.e("Recorder Error", e.getMessage());
                    }
                    chronometerPersist.startChronometer();
                }
            }
        });

        mSubscription = Observable.create(mRecorderOnSubscribe).subscribe(new Action1<short[]>()
        {
            @Override
            public void call(short[] shorts)
            {
                try
                {
                    mRecorderOnSubscribe.writeShortsToFile(shorts);
                } catch (IOException e)
                {
                    Log.e("Recorder", e.getMessage());
                }
            }
        }, new Action1<Throwable>()
        {
            @Override
            public void call(Throwable throwable)
            {
                Log.e("Recorder Error", throwable.getMessage());
            }
        });

        playButton.setVisibility(View.GONE);

        playButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                playFile(filePath);
            }
        });

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mSubscription != null)
        {
            try
            {
                mRecorderOnSubscribe.stop();
            } catch (IOException e)
            {
                Log.e("onPause", e.getMessage());
            }
            mSubscription.unsubscribe();
        }
    }

    private void playFile(String filePath)
    {
        //set up MediaPlayer
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(filePath);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        startActivity(intent);
    }
}

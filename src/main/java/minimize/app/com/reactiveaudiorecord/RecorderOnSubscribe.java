package minimize.app.com.reactiveaudiorecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by ahmedrizwan on 4/24/15.
 * Custom onSubscribe for an Observable, when start() is called, it sends out short buffers of audio data.
 * Pause/Resume/Stop/Stop methods to control the streaming/publishing of audio data to the Observer.
 */
public class RecorderOnSubscribe implements Observable.OnSubscribe<short[]>, Runnable {
    private AudioRecord audioRecorder = null;
    private int bufferSize;
    private String LOG_TAG = "Subcriber";
    private boolean isRecording = false;
    private final Object mPauseLock;
    private boolean mPaused;
    Subscriber<? super short[]> mSubscriber;
    Thread thread;

    public RecorderOnSubscribe() {
        mPauseLock = new Object();
        mPaused = false;
    }

    @Override
    public void call(Subscriber<? super short[]> subscriber) {
        Log.e(LOG_TAG,"Subscribed");
        mSubscriber = subscriber;
    }

    public void start(int sampleRate, int bufferSize, int channel, int audioSource){
        this.bufferSize = bufferSize;

        if (bufferSize != AudioRecord.ERROR_BAD_VALUE && bufferSize != AudioRecord.ERROR) {

            audioRecorder = new AudioRecord(audioSource,
                    sampleRate,
                    channel,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 10);

            if (audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                Log.e(LOG_TAG, "Audio Recorder Created");
                audioRecorder.startRecording();
                isRecording = true;
                thread = new Thread(this);
                thread.start();

            } else {
                Log.e(LOG_TAG, "Unable to create AudioRecord instance");
            }

        } else {
            Log.e(LOG_TAG, "Unable to get minimum buffer size");
        }
    }

    public void stop() {
        isRecording = false;
        if (audioRecorder != null) {
            if (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecorder.stop();
            }
            if (audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecorder.release();
            }
            mSubscriber.onCompleted();
        }
    }

    /**
     * Call this on pause.
     */
    public void pause() {
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    /**
     * Call this on resume.
     */
    public void resume() {
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }

    @Override
    public void run() {
        short[] tempBuf = new short[bufferSize / 2];

        while (isRecording && audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            // Do stuff.
            audioRecorder.read(tempBuf, 0, tempBuf.length);
            mSubscriber.onNext(tempBuf);

            synchronized (mPauseLock) {
                while (mPaused) {
                    try {
                        mPauseLock.wait();
                    } catch (InterruptedException e) {
                        mSubscriber.onError(e);
                    }
                }
            }
        }
    }


    public boolean isRecording() {
        return (audioRecorder != null) &&
                (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (audioRecorder != null && audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
            audioRecorder.stop();
            audioRecorder.release();
        }

        audioRecorder = null;
        thread = null;
    }

    public boolean isRecordingStopped() {
        return audioRecorder == null || audioRecorder.getRecordingState() == 1;
    }

}

package com.minimize.library;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by <a href="https://github.com/kvithayathil">kvithayathil</a> on 26/02/2017
 * <br/>
 * Custom onSubscribe for an Observable, when start() is called, it sends out short buffers of
 * audio data.
 * Pause/Resume/Stop/Stop methods to control the streaming/publishing of audio data to the
 * Observer.
 * <br/>
 * <br/>
 * Forked from <a href="https://github.com/ahmedrizwan">Ahmed Ahmedrizwan's</a>
 * <a href="https://github.com/ahmedrizwan/ReactiveAudioRecord">ReactiveAudioRecord</a> library
 */
public class ObservableAudioRecorder implements ObservableOnSubscribe<short[]>, Runnable {
  private String filePath;
  private final int sampleRate;
  private final int channels;
  private final int bitsPerSecond;
  private final int audioSource;
  private AudioRecord audioRecorder = null;
  private int bufferSize;
  private boolean isRecording = false;
  private final Object mPauseLock;
  private boolean isPaused;
  private ObservableEmitter<? super short[]> subscriber;
  private Thread thread;
  private DataOutputStream dataOutputStream;

  public static class Builder {
    int sampleRate;
    int bitsPerSecond;
    int channels;
    int audioSource;
    String filePath;

    public Builder(String filePath) {
      this.filePath = filePath;
      sampleRate = 44100;
      bitsPerSecond = AudioFormat.ENCODING_PCM_16BIT;
      audioSource = MediaRecorder.AudioSource.MIC;
      channels = 1;
    }

    public Builder sampleRate(int sampleRate) {
      this.sampleRate = sampleRate;
      return this;
    }

    public Builder stereo() {
      channels = 2;
      return this;
    }

    public Builder mono() {
      channels = 1;
      return this;
    }

    public Builder audioSourceMic() {
      audioSource = MediaRecorder.AudioSource.MIC;
      return this;
    }

    public Builder audioSourceCamcorder() {
      audioSource = MediaRecorder.AudioSource.CAMCORDER;
      return this;
    }

    public ObservableAudioRecorder build() {
      return new ObservableAudioRecorder(this.filePath, this.sampleRate, this.channels,
          this.bitsPerSecond, this.audioSource);
    }
  }

  private ObservableAudioRecorder(String filePath, int sampleRate, int channels, int bitsPerSecond,
      int audioSource) {
    this.filePath = filePath;
    this.sampleRate = sampleRate;
    this.channels = channels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
    this.bitsPerSecond = bitsPerSecond;
    this.audioSource = audioSource;

    mPauseLock = new Object();
    isPaused = false;
  }

  @Override public void subscribe(ObservableEmitter<short[]> e) throws Exception {
    subscriber = e;
  }

  public void start() throws RuntimeException, FileNotFoundException {
    this.dataOutputStream =
        new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.filePath)));

    this.bufferSize =
        AudioRecord.getMinBufferSize(sampleRate, channels, AudioFormat.ENCODING_PCM_16BIT);

    if (bufferSize != AudioRecord.ERROR_BAD_VALUE && bufferSize != AudioRecord.ERROR) {
      audioRecorder =
          new AudioRecord(audioSource, sampleRate, channels, AudioFormat.ENCODING_PCM_16BIT,
              bufferSize * 10);

      if (audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
        audioRecorder.startRecording();
        isRecording = true;
        thread = new Thread(this);
        thread.start();
      } else {
        throw new RuntimeException("Unable to create AudioRecord instance");
      }
    } else {
      throw new RuntimeException("Unable to get minimum buffer size");
    }
  }

  public void stop() throws IOException {
    isRecording = false;
    if (audioRecorder != null) {
      if (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
        audioRecorder.stop();
      }
      if (audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
        audioRecorder.release();
      }
    }
  }

  /**
   * Call this on pause.
   */
  public void pause() {
    synchronized (mPauseLock) {
      isPaused = true;
    }
  }

  /**
   * Call this on resume.
   */
  public void resume() {
    synchronized (mPauseLock) {
      isPaused = false;
      mPauseLock.notifyAll();
    }
  }

  @Override public void run() {
    short[] tempBuf = new short[bufferSize / 2];

    while (isRecording && audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
      audioRecorder.read(tempBuf, 0, tempBuf.length);
      subscriber.onNext(tempBuf);
      synchronized (mPauseLock) {
        while (isPaused) {
          try {
            mPauseLock.wait();
          } catch (InterruptedException e) {
            subscriber.onError(e);
          }
        }
      }
    }
  }

  public boolean isRecording() {
    return (audioRecorder != null) && (audioRecorder.getRecordingState()
        == AudioRecord.RECORDSTATE_RECORDING);
  }

  @Override protected void finalize() throws Throwable {
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

  private void convertFileToWave(File file) throws IOException {
    try {
      rawToWave(file, sampleRate, 16);
    } catch (IOException e) {
      throw new IOException("Unable to convert file");
    }
  }

  //channels -> Mono or Stereo
  //bitsPerSecond -> 16 or 8 (currently android supports 16 only)
  private void rawToWave(File rawFile, int sampleRate, int bitsPerSecond) throws IOException {
    RandomAccessFile randomAccessFile = new RandomAccessFile(rawFile, "rw");
    int channels = this.channels == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
    //seek to beginning
    randomAccessFile.seek(0);
    try {
      writeString(randomAccessFile, "RIFF"); // chunk id
      writeInt(randomAccessFile, (int) (36 + rawFile.length())); // chunk size
      writeString(randomAccessFile, "WAVE"); // format
      writeString(randomAccessFile, "fmt "); // subchunk 1 id
      writeInt(randomAccessFile, 16); // subchunk 1 size
      writeShort(randomAccessFile, (short) 1); // audio format (1 = PCM)
      writeShort(randomAccessFile, (short) channels); // number of channels
      writeInt(randomAccessFile, sampleRate); // sample rate
      writeInt(randomAccessFile, sampleRate * 2); // byte rate
      writeShort(randomAccessFile, (short) 2); // block align
      writeShort(randomAccessFile, (short) bitsPerSecond); // bits per sample
      writeString(randomAccessFile, "data"); // subchunk 2 id
      writeInt(randomAccessFile, (int) rawFile.length()); // subchunk 2 size
    } finally {
      randomAccessFile.close();
    }
  }

  private void writeInt(final RandomAccessFile output, final int value) throws IOException {
    output.write(value);
    output.write(value >> 8);
    output.write(value >> 16);
    output.write(value >> 24);
  }

  private void writeShort(final RandomAccessFile output, final short value) throws IOException {
    output.write(value);
    output.write(value >> 8);
  }

  private void writeString(final RandomAccessFile output, final String value) throws IOException {
    for (int i = 0; i < value.length(); i++) {
      output.write(value.charAt(i));
    }
  }

  public void writeShortsToFile(short[] shorts) throws IOException {
    for (int i = 0; i < shorts.length; i++) {
      dataOutputStream.writeByte(shorts[i] & 0xFF);
      dataOutputStream.writeByte((shorts[i] >> 8) & 0xFF);
    }
  }

  public void completeRecording() throws IOException {
    dataOutputStream.flush();
    dataOutputStream.close();

    File file = new File(filePath);
    convertFileToWave(file);
  }
}

package minimize.app.com.reactiveaudiorecord;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by ahmedrizwan on 1/13/15.
 * A helper methods for converting the recorded raw-data file into a wav-file.
 */
public class WaveHelper {

    //channels -> Mono or Stereo
    //bitsPerSecond -> 16 or 8 (currently android supports 16 only)
    public void rawToWave(File rawFile, int sampleRate, int channels, int bitsPerSecond) throws IOException {
        Log.e("File Length", rawFile.length() + "  " + channels + " " + bitsPerSecond);
        RandomAccessFile randomAccessFile = new RandomAccessFile(rawFile, "rw");
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
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
    }

    private void writeInt(final RandomAccessFile output, final int value)
            throws IOException {
        output.write(value);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final RandomAccessFile output, final short value)
            throws IOException {
        output.write(value);
        output.write(value >> 8);
    }

    private void writeString(final RandomAccessFile output,
                             final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

}

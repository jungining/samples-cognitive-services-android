package com.android.sample.cognitive.services.speaker.verification;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileHelper {

    private static final String TAG = FileHelper.class.getSimpleName();

    private static final int SAMPLING_RATE = 16000;

    private static final int RECORDER_BPP = 16;

    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";

    private static final String AUDIO_RECORDER_FOLDER = "audioRecorder";

    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    static String getFilename(String fileName) {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + fileName +
                AUDIO_RECORDER_FILE_EXT_WAV);
    }

    static String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

        if (tempFile.exists()) {
            tempFile.delete();
        }

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    static void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    static void copyWaveFile(int bufferSize, String fileName) {
        int channels = 1;
        long byteRate = RECORDER_BPP * SAMPLING_RATE * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            FileInputStream in = new FileInputStream(getTempFilename());
            FileOutputStream out = new FileOutputStream(getFilename(fileName));
            long totalAudioLen = in.getChannel().size();
            long totalDataLen = totalAudioLen + 36;

            Log.d(TAG, "File size: " + totalDataLen);

            writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    (long) SAMPLING_RATE, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy wave file.");
            throw new RuntimeException(e);
        }
    }

    private static void writeWaveFileHeader(
            FileOutputStream out,
            long totalAudioLen,
            long totalDataLen,
            long longSampleRate,
            int channels,
            long byteRate) throws IOException {
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private FileHelper() {
    }
}

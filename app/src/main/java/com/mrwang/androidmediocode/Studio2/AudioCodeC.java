package com.mrwang.androidmediocode.Studio2;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author chengwangyong
 * @date 2018/4/19
 */
public class AudioCodeC {
    // 输入源 从麦克风输入
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 采样率
    // 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private int sampleRateInHz = 44100;
    // 音频通道 单声道
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    // 音频格式 PCM 6bit PCM 18bit
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    private String mFileName = "AudioRecodeFile";
    private AudioRecord audioRecord;
    private boolean isRecording = true;
    private Context context;

    public void init(Context context) {
        this.context = context;
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
    }

    public void startRecode() {
        new Thread() {
            @Override
            public void run() {
                audioRecord.startRecording();
                isRecording = true;
                writeDataToFile();
            }
        }.start();

    }

    private void writeDataToFile() {
        try {
            byte[] audioData = new byte[bufferSizeInBytes];
            String fileName =context.getExternalCacheDir().getAbsolutePath() + File.separator + mFileName;
            FileOutputStream fos = new FileOutputStream(new File(fileName));
            while (isRecording) {
                int readSize = audioRecord.read(audioData, 0, bufferSizeInBytes);
                if (AudioRecord.ERROR_INVALID_OPERATION != readSize && fos != null) {
                    try {
                        fos.write(audioData);
                    } catch (IOException e) {
                        Log.e("AudioRecorder", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

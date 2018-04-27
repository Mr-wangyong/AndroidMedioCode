package com.mrwang.androidmediocode.Studio2

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.*
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import java.io.*
import java.lang.System.`in`


/**
 * @author chengwangyong
 * @date 2018/4/19
 */
class AudioCodeC {
    // 输入源 从麦克风输入
    private val audioSource = MediaRecorder.AudioSource.MIC
    // 采样率
    // 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private val sampleRateInHz = 44100
    // 音频通道 立体声 试过 单身道会报错
    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    // 音频格式 PCM 6bit PCM 18bit
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    // 缓冲区大小 从 AudioRecord里面获取
    private val bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
    private val mFileName = "AudioRecodeFile"

    private val audioRecord: AudioRecord by lazy {
        AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes)
    }

    @Volatile
    private var isRecording: Boolean = true

    private var context: Activity? = null

    fun init(context: Activity) {
        this.context = context
    }

    fun startRecode() {
        checkPermission {
            async(CommonPool) {
                audioRecord.startRecording()
                isRecording = true
                writeDataToFile()
            }
        }
    }

    fun stopRecode() {
        isRecording = false
    }

    private fun checkPermission(callBack: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context?.apply {
                if (checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    callBack.invoke()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 110)
                }
            }
        } else {
            callBack.invoke()
        }
    }

    private val fileName by lazy {
        Environment.getExternalStorageDirectory().absolutePath + File.separator + mFileName
    }

    private fun writeDataToFile() {
        try {
            val audioData = ByteArray(bufferSizeInBytes)
            val fos = FileOutputStream(File(fileName))
            while (isRecording) {
                val readSize = audioRecord.read(audioData, 0, bufferSizeInBytes)
                if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                    try {
                        fos.write(audioData)
                    } catch (e: IOException) {
                        Log.e("AudioRecorder", e.message)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Volatile
    private var isPlaying = false

    public fun play() {
        isPlaying = true
        val dis = DataInputStream(BufferedInputStream(FileInputStream(fileName)))
        val bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        val buffer = ByteArray(bufferSize)

        val track = AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM)
        //开始播放
        track.play()
        //由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
        while (isPlaying && dis.available() > 0) {
            var i = 0
            while (dis.available() > 0 && i < buffer.size) {
                buffer[i] = dis.readByte()
                i++
            }
            //然后将数据写入到AudioTrack中
            track.write(buffer, 0, buffer.size)
        }

        //播放结束
        track.stop()
        track.release()
        dis.close()
    }

    fun pcmToWav() {
        pcmToWav(fileName, "$fileName.wav")
    }

    /**
     * pcm文件转wav文件
     *
     * @param inFilename  源文件路径
     * @param outFilename 目标文件路径
     */
    fun pcmToWav(inFilename: String, outFilename: String) {
        val input: FileInputStream
        val out: FileOutputStream
        val totalAudioLen: Long
        val totalDataLen: Long
        val longSampleRate = sampleRateInHz
        val channels = 2
        val byteRate = 16 * sampleRateInHz * channels / 8
        val data = ByteArray(bufferSizeInBytes)
        try {
            input = FileInputStream(inFilename)
            out = FileOutputStream(outFilename)
            totalAudioLen = input.channel.size()
            totalDataLen = totalAudioLen + 36

            writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate)
            while (input.read(data) !== -1) {
                out.write(data)
            }
            `in`.close()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * 加入wav文件头
     */
    @Throws(IOException::class)
    private fun writeWaveFileHeader(out: FileOutputStream, totalAudioLen: Long,
                                    totalDataLen: Long, longSampleRate: Int, channels: Int, byteRate: Int) {
        val header = ByteArray(44)
        header[0] = 'R'.toByte() // RIFF/WAVE header
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte()  //WAVE
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte() // 'fmt ' chunk
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1   // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (2 * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = 16  // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte() //data
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }
}

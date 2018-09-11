package com.mrwang.androidmediocode.Studio4

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import kotlinx.coroutines.experimental.*
import java.io.File
import java.nio.ByteBuffer

/**
 *  学习 Android 平台的 MediaExtractor 和 MediaMuxer API，知道如何解析和封装 mp4 文件
 * https://blog.csdn.net/zhi184816/article/details/52514138
 * 主要用到 MediaExtractor 和 MediaMuxer
 *
 *  MediaExtractor
 *  分离视频
 *
 *  MediaMuxer
 *  合成视频
 *
 *
 * @date 2018/5/25
 * @author chengwangyong
 */
class MediaExtractorUtils {
    private val inputFileName = "mCameraVideo.mp4"
    private val outputFileName = "output_video_no_audio.mp4"
    private val audioOutputFileName = "output_audio_no_audio.aac"
    private val basePath = Environment.getExternalStorageDirectory().absolutePath + File.separator

    private val mediaExtractor by lazy(LazyThreadSafetyMode.NONE) {
        MediaExtractor()
    }

    private lateinit var muxer: MediaMuxer

    /********  分离视频    将有声视频分离成为 有声音频和无声视频************/
    fun startDetach(success: () -> Unit) {
        detachMedio(basePath + inputFileName, basePath + outputFileName, success)
    }

    /**
     * 利用 MediaExtractor
     * 分离音视频
     */
    private fun detachMedio(input: String, output: String, success: () -> Unit) {
        launch(CommonPool) {
            mediaExtractor.setDataSource(input)
            var videoJob: Deferred<Boolean>? = null
            var audioJob: Deferred<Boolean>? = null
            for (i in 0 until mediaExtractor.trackCount) {
                val format = mediaExtractor.getTrackFormat(i)
                val mime = format?.getString(MediaFormat.KEY_MIME)
                println("视频中包含的媒体类型 mime=$mime")
                mime?.let {
                    if (mime.startsWith("video")) {
                        videoJob = async(start = CoroutineStart.LAZY) {
                            muxerVideo(i, output, format)
                        }
                    } else if (mime.startsWith("audio")) {
                        audioJob = async(start = CoroutineStart.LAZY) {
                            muxerVideo(i, basePath + audioOutputFileName, format)
                        }
                    }
                }
            }
            if (videoJob?.await() == true && audioJob?.await() == true) {
                success.invoke()
            }
            mediaExtractor.release()
        }

    }

    private suspend fun muxerVideo(i: Int, output: String, format: MediaFormat?): Boolean {
        mediaExtractor.selectTrack(i)
        // 第二个参数好奇怪 明明是输出视频的格式 但是到了这里 音频也能用到
        muxer = MediaMuxer(output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val mVideoTackIndex = muxer.addTrack(format)
        muxer.start()


        val bufferInfo = MediaCodec.BufferInfo()
        bufferInfo.presentationTimeUs = 0
        val buffer = ByteBuffer.allocate(1024 * 1024 * 2)
        while (true) {
            val sampleSize = mediaExtractor.readSampleData(buffer, 0)
            if (sampleSize < 0) {
                break
            }
            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.flags = mediaExtractor.sampleFlags
            bufferInfo.presentationTimeUs = mediaExtractor.sampleTime
            val keyframe = (bufferInfo.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME) > 0
            println("write sample " + keyframe + ", " + sampleSize + ", " + bufferInfo.presentationTimeUs)
            muxer.writeSampleData(mVideoTackIndex, buffer, bufferInfo)
            mediaExtractor.advance()
        }
        muxer.stop()
        muxer.release()
        return true
    }

    /********  合成视频    将无声视频和有声音频 合成有声视频************/
}
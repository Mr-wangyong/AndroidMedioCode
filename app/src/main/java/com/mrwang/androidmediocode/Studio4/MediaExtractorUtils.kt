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
    private val videoOutputFileName = "output_video_no_audio.mp4"
    private val audioOutputFileName = "output_audio_no_audio.aac"
    private val mp4OutputFileName = "full_output.mp4"
    private val basePath = Environment.getExternalStorageDirectory().absolutePath + File.separator

    private val mediaExtractor by lazy(LazyThreadSafetyMode.NONE) {
        MediaExtractor()
    }

    private lateinit var muxer: MediaMuxer

    /********  分离视频    将有声视频分离成为 有声音频和无声视频************/
    fun startDetach(success: () -> Unit) {
        detachMedio(basePath + inputFileName, basePath + videoOutputFileName, success)
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

    private fun muxerVideo(i: Int, output: String, format: MediaFormat?): Boolean {
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

    fun mixtureMp4(success: () -> Unit) {
        mixtureMp4(basePath + audioOutputFileName, basePath + videoOutputFileName, basePath + mp4OutputFileName, success)
    }

    /********  合成视频    将无声视频和有声音频 合成有声视频************/

    private fun mixtureMp4(audioInput: String, videoInput: String, output: String, success: () -> Unit) {
        launch(CommonPool) {

            val audioExtractor = MediaExtractor()
            val videoExtractor = MediaExtractor()

            audioExtractor.setDataSource(audioInput)
            videoExtractor.setDataSource(videoInput)

            // 查找轨道
            var audioTrackIndex = 0
            for (i in 0 until audioExtractor.trackCount) {
                val format = audioExtractor.getTrackFormat(i)
                val mime = format?.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio") == true) {
                    audioTrackIndex = i
                    break
                }
            }
            var videoTrackIndex = 0
            for (i in 0 until videoExtractor.trackCount) {
                val format = videoExtractor.getTrackFormat(i)
                val mime = format?.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("video") == true) {
                    videoTrackIndex = i
                    break
                }
            }

            // 选择各自的轨道
            audioExtractor.selectTrack(audioTrackIndex)
            videoExtractor.selectTrack(videoTrackIndex)
            // 创建 MediaMuxer
            muxer = MediaMuxer(output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val newAudioTrack = muxer.addTrack(audioExtractor.getTrackFormat(audioTrackIndex))
            val newVideoTrack = muxer.addTrack(videoExtractor.getTrackFormat(videoTrackIndex))
            muxer.start()

            //
            muxerMedia(newVideoTrack, videoExtractor)
            muxerMedia(newAudioTrack, audioExtractor)

            audioExtractor.release()
            videoExtractor.release()
            muxer.stop()
            muxer.release()
            success.invoke()
        }
    }

    private fun muxerMedia(videoTrack: Int, mediaExtractor: MediaExtractor) {
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
            muxer.writeSampleData(videoTrack, buffer, bufferInfo)
            mediaExtractor.advance()
        }
    }


    private fun mixturVideo(i: Int, output: String, format: MediaFormat?): Boolean {
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

}

inline fun <T> T.isNotNull(block: () -> Unit) {
    if (this != null) {
        block()
    }

}
package com.mrwang.androidmediocode.Studio2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.button
import org.jetbrains.anko.verticalLayout

/**
 * @author chengwangyong
 * @date 2018/4/27
 */
class AudioActivity : AppCompatActivity() {

    private val audioCodeC:AudioCodeC by lazy {
        AudioCodeC()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioCodeC.init(this)
        createUI()
    }

    private fun createUI() {
        verticalLayout {
            button("开始录制") {
                setOnClickListener {
                    audioCodeC.startRecode()
                }
            }

            button("结束录制") {
                setOnClickListener {
                    audioCodeC.stopRecode()
                }
            }

            button("播放录音") {
                setOnClickListener {
                    audioCodeC.play()
                }
            }

            button("PCM 转 WAV") {
                setOnClickListener {
                    audioCodeC.pcmToWav()
                }
            }
        }
    }
}

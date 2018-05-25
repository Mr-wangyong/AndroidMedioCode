package com.mrwang.androidmediocode.Studio3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.button
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.surfaceView
import org.jetbrains.anko.verticalLayout
import java.io.File


/**
 * @author chengwangyong
 * @date 2018/4/27
 */
class CameraActivity : AppCompatActivity() {
    private val cameraRecorder by lazy {
        CameraRecorder()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraRecorder.init(this)
        createUI()
    }

    private fun createUI() {
        frameLayout {
            val surface = surfaceView {

            }
            cameraRecorder.initCamera(surface)
            verticalLayout {
                button(" 开启预览") {
                    setOnClickListener {
                        cameraRecorder.startPreview(surface)
                    }
                }

                button(" 关闭预览") {
                    setOnClickListener {
                        cameraRecorder.stopPreview()
                    }
                }

                button(" 拍照") {
                    setOnClickListener {
                        cameraRecorder.takePicture()
                    }
                }

                button("开始录像") {
                    setOnClickListener {
                        cameraRecorder.startRecorder()
                    }
                }

                button("保存录像") {
                    setOnClickListener {
                        cameraRecorder.stopRecorder()
                        val intent = Intent(Intent.ACTION_VIEW)
                        val path = cameraRecorder.videoFileName
                        val file = File(path)
                        val uri = Uri.fromFile(file)
                        intent.setDataAndType(uri, "video/*")
                        startActivity(intent)
                    }
                }
            }
        }
    }

    // 没处理 onPause 的情况，理论上应该要处理的 因为是 Demo 暂时不做处理
    override fun onPause() {
        super.onPause()
        cameraRecorder.onPause()
    }

    override fun onResume() {
        super.onResume()
        cameraRecorder.onResume()
    }



}

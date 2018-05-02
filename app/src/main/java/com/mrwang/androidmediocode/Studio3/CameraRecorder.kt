package com.mrwang.androidmediocode.Studio3

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


/**
 * @author chengwangyong
 * @date 2018/4/27
 */
class CameraRecorder : IRecorder {
    private val mPictureFileName = "mCameraRecode.jpg"
    private val mVideoFileName = "mCameraVideo.mp4"
    private val mPreviewFileName = "mCameraPreview.jpg"
    private lateinit var holder: SurfaceHolder

    private val pictureFileName by lazy(LazyThreadSafetyMode.NONE) {
        Environment.getExternalStorageDirectory().absolutePath + File.separator + mPictureFileName
    }

    val videoFileName by lazy(LazyThreadSafetyMode.NONE) {
        Environment.getExternalStorageDirectory().absolutePath + File.separator + mVideoFileName
    }

    val mPreviewFile by lazy(LazyThreadSafetyMode.NONE) {
        Environment.getExternalStorageDirectory().absolutePath + File.separator + mPreviewFileName
    }

    private lateinit var mContext: Context
    private val camera by lazy({
        val num = Camera.getNumberOfCameras()
        Camera.open(num - 1)
    })

    fun init(context: Context): Boolean {
        this.mContext = context
        return checkHardware(context)
    }


    fun initCamera(surfaceView: SurfaceView) {
        holder = surfaceView.holder
        holder.setFixedSize(356, 640)
        holder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceCreated(holder: SurfaceHolder?) {
                camera.setDisplayOrientation(90)
                startCameraPreview(holder)
                startFaceDetection() // 人脸识别
            }

            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                holder?.let {
                    if (holder.surface == null) return
                    camera.stopPreview()
                    startCameraPreview(holder)
                    startFaceDetection()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                camera.stopPreview()
            }


        })
    }

    private fun startFaceDetection() {
        val params = camera.parameters
        camera.cancelAutoFocus()
        if (params.maxNumDetectedFaces > 0) {
            camera.startFaceDetection()
            camera.setFaceDetectionListener { faces, camera ->
                if (faces.isNotEmpty()) {
                    Log.d("人脸区域", "face detected: " + faces.size +
                            " Face 1 Location X: " + faces[0].rect.centerX() +
                            "Y: " + faces[0].rect.centerY())
                }
            }
        } else {
            Log.e("tag", "【FaceDetectorActivity】类的方法：【startFaceDetection】: " + "不支持")
        }

    }

    override fun startPreview(surfaceView: SurfaceView) {
        startCameraPreview(holder)
    }

    override fun startPreview(textureView: TextureView) {
        startCameraPreview(holder)
    }

    override fun stopPreview() {
        camera.stopPreview()
    }


    private val mediaRecorder by lazy {
        MediaRecorder()
    }

    override fun startRecorder() {
        camera.unlock()
        mediaRecorder.setCamera(camera)
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
        mediaRecorder.setOutputFile(videoFileName)

        mediaRecorder.setPreviewDisplay(holder.surface)
        mediaRecorder.prepare()

        mediaRecorder.start()
    }

    private fun startCameraPreview(holder: SurfaceHolder?) {
        camera.setPreviewDisplay(holder)
        camera.startPreview()
        camera.setOneShotPreviewCallback { data, camera ->
            async(CommonPool) {
                camera.setOneShotPreviewCallback(null)
                val previewSize = camera.parameters.previewSize
                val yuvImage = YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null)
                val bos = ByteArrayOutputStream()
                // 将 YUVImage 转化成 JPEG
                yuvImage.compressToJpeg(Rect(0, 0, previewSize.width, previewSize.height), 80, bos)
                val rawByte = bos.toByteArray()

                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val bitmap = BitmapFactory.decodeByteArray(rawByte, 0, rawByte.size, options)
                bitmapToFile(bitmap)
            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap) {
        val f = File(mPreviewFile)
        if (!f.exists()) {
            f.createNewFile()
        }
        val bos = ByteArrayOutputStream()
        // 如果这里改为 JPEG 就会很多地方绿色的 图片色彩不对 因为格式错了
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
        val bitmapData = bos.toByteArray()

        val fos = FileOutputStream(f)
        fos.write(bitmapData)
        fos.flush()
        fos.close()
    }


    override fun stopRecorder() {
        mediaRecorder.stop()
        mediaRecorder.reset()
        mediaRecorder.release()
        camera.lock()
    }

    override fun startPlay() {

    }

    override fun stopPlay() {

    }

    // 拍照片
    override fun takePicture() {
        camera.takePicture(null, null, Camera.PictureCallback { data, _ ->
            async(CommonPool) {
                val file = File(pictureFileName)
                val fos = FileOutputStream(file)
                fos.write(data)
                fos.close()
            }
        })
    }

    private fun checkHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    fun onPause() {

    }

    fun onResume() {

    }
}

package com.mrwang.androidmediocode.Studio3

import android.view.SurfaceView
import android.view.TextureView

/**
 * @author chengwangyong
 * @date 2018/4/27
 */
interface IRecorder {
    public fun startPreview(surfaceView: SurfaceView)
    public fun startPreview(textureView: TextureView)
    public fun startRecorder()
    public fun stopPreview()
    public fun stopRecorder()
    public fun startPlay()
    public fun stopPlay()
    public fun takePicture()
}

package com.mrwang.androidmediocode.Studio4

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import org.jetbrains.anko.button
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

/**
 * @author chengwangyong
 * @date 2018/9/5
 */
class MediaExtractorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verticalLayout {
            button("提取文件") {
                setOnClickListener {
                    MediaExtractorUtils().startDetach {
                        runOnUiThread {
                            text = "提取成功"
                        }
                    }
                }
            }.lparams(wrapContent, wrapContent)
            gravity = Gravity.CENTER
        }
    }
}

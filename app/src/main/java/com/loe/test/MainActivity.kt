package com.loe.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.loe.camera.*
import com.loe.mvp.ext_app.onResultOk
import com.loe.mvp.ext_app.start
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonVideo.setOnClickListener()
        {
            LoeCamera.takeVideo(this, VideoConfig()
                .setBitRate(6000)
                .setFront(true)
                .setMaxSeconds(10)
                .setShowAlbum(true))
            {
                textView.text = it
            }
        }

        buttonPhoto.setOnClickListener()
        {
            LoeCamera.takePhoto(this, PhotoConfig()
                .setFront(false)
                .setShowAlbum(true))
            {
                textView.text = it
            }
        }

//                PermissionUtil.requestForce(this, PermissionUtil.STORAGE, "存储", false)
//                {
//                    link =
//                        LoeHttp.getFile("http://wxbtest.iflysec.com/wxb-server/app/appUpdate.apk")
//                            .save(HttpFileUtil.basePath + "down/updateTest.apk")
//                            .useTemp(true)
//                            .tempFlag("1.0.1")
//                            .progress()
//                            { now, len, p ->
//                                textView.text = "${(p * 10).toInt() / 10.0}%"
//                            }
//                            .ok()
//                            {
//                                textView.text = "下载完成！"
//                                HttpFileUtil.clearTemp()
//                            }
//                }
//            }
    }
}
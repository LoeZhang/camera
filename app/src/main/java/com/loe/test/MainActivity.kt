package com.loe.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.loe.camera.LoeCamera
import com.loe.camera.VideoConfig
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

                val name = it.split("/").last().split(".")[0]
                jzVideo.setUp(it, name)
                jzVideo.startVideo()
            }
        }

        var url = "http://192.168.1.106/test/%E7%AB%A0%E8%B7%AF%E9%A1%BA-%E8%BD%AC%E6%AD%A3%E7%94%B3%E8%AF%B7%E8%A1%A8.docx";

        buttonPhoto.setOnClickListener()
        {
//            LoeCamera.takePhoto(this, PhotoConfig()
//                .setFront(false)
//                .setShowAlbum(true))
//            {
//                textView.text = it
//            }


            LoeCamera.takeFile(this)
            {
                textView.text = it
                url = it
            }
        }

        buttonOpen.setOnClickListener()
        {

            LoeCamera.open(this, url)
        }


        val m3u8Url = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"

        val name = m3u8Url.split("/").last().split(".")[0]
        jzVideo.setUp(m3u8Url, name)
//        jzVideo.posterImageView.setImageURI(
//            Uri.parse("https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=2330582835,4253854595&fm=26&gp=0.jpg"))

    }
}
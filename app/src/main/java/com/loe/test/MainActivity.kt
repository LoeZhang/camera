package com.loe.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.loe.camera.*
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
    }
}
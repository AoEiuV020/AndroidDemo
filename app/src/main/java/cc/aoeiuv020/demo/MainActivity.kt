package cc.aoeiuv020.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRect.setOnClickListener {
            RectActivity.start(this)
        }

        btnFullScreen.setOnClickListener {
            FullscreenActivity.start(this)
        }
    }
}

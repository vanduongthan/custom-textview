package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.textview.VTextLayout
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var vTextLayout: VTextLayout
    private val BUFFER_SIZE = 256 * 1024

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var text = ""
        // Assetからテキストを読み込む
        try {
            val bis = BufferedInputStream(
                assets.open("SAMPLE.txt"),
                BUFFER_SIZE
            )
            val baos = ByteArrayOutputStream(BUFFER_SIZE)
            val buffer = ByteArray(BUFFER_SIZE)
            var length: Int
            while (bis.read(buffer).also { length = it } != -1) {
                baos.write(buffer, 0, length)
            }
            text = baos.toString()
        } catch (e: IOException) {
        }
        vTextLayout =
            findViewById<View>(R.id.vTextLayout) as VTextLayout
        vTextLayout.initContent("たいとる", text)
        vTextLayout.setFont(VTextLayout.Font.IPA)
        //vTextLayout.setScrollDisabled(true);
    }
}
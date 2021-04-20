package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.textview.VTextLayout
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.android.synthetic.main.activity_main.*
import java.util.regex.Pattern

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
        vTextLayout.initContent("たいとる", trimMarkDownAndAuthor(text))
        vTextLayout.setFont(VTextLayout.Font.IPA)
        btnRotate.setOnClickListener { vTextLayout.rotate() }
        //vTextLayout.setScrollDisabled(true);
    }

    private fun trimMarkDownAndAuthor(text: String): String{

        val regexMarkDown = "-{10,}\\n((.+\\n{1,2}){1,2})*-{10,}\\n"
        //Log.d("hihi", "match: " + Pattern.matches(regex, text))
        /*val pattern = Pattern.compile(regexMarkDown)
        val matcher = pattern.matcher(text)
        var outputText = matcher.replaceFirst("")*/

        //thay cho 3 dong tren
        //trim markdown
        var outputText = Pattern.compile(regexMarkDown).matcher(text).replaceFirst("")
        //trim author
        val regexAuthor = "底本：(.+\\n)+(初出：)(.+\\n)+.*"
        outputText = Pattern.compile(regexAuthor).matcher(outputText).replaceFirst("")
        return outputText
    }
}
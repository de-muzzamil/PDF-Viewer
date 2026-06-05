package com.example.pdfviewer
import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.pdfviewer.databinding.ActivityMainBinding
import com.artifex.mupdf.viewer.DocumentActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        intent?.data?.let { uri ->
            val mupdfIntent = Intent(this, DocumentActivity::class.java)
            mupdfIntent.action = Intent.ACTION_VIEW
            mupdfIntent.data = uri
            startActivity(mupdfIntent)
            finish()
        }
    }
}

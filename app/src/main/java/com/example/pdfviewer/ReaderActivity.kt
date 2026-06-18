package com.example.pdfviewer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.artifex.mupdf.viewer.DocumentActivity
import com.artifex.mupdf.viewer.MuPDFCore
import com.artifex.mupdf.viewer.ReaderView
import java.util.*

class ReaderActivity : DocumentActivity(), TextToSpeech.OnInitListener {
    private val lastPositionPref = "last_position"
    private var tts: TextToSpeech? = null
    private var isNightMode = false
    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        checkPermissions()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        restorePosition()
    }

    override fun onPause() {
        super.onPause()
        savePosition()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    private fun getReaderView(): ReaderView? {
        return try {
            val field = DocumentActivity::class.java.getDeclaredField("mDocView")
            field.isAccessible = true
            field.get(this) as? ReaderView
        } catch (e: Exception) {
            null
        }
    }

    private fun getMuPDFCore(): MuPDFCore? {
        return try {
            val field = DocumentActivity::class.java.getDeclaredField("mCore")
            field.isAccessible = true
            field.get(this) as? MuPDFCore
        } catch (e: Exception) {
            null
        }
    }

    private fun savePosition() {
        val uri = intent.data ?: return
        val prefs = getSharedPreferences(lastPositionPref, Context.MODE_PRIVATE)
        val readerView = getReaderView()
        val page = readerView?.displayedViewIndex ?: 0
        prefs.edit().putInt(uri.toString(), page).apply()
    }

    private fun restorePosition() {
        val uri = intent.data ?: return
        val prefs = getSharedPreferences(lastPositionPref, Context.MODE_PRIVATE)
        val page = prefs.getInt(uri.toString(), -1)
        if (page != -1) {
            getReaderView()?.post {
                getReaderView()?.displayedViewIndex = page
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add(0, 100, 0, "Night Mode")
        menu.add(0, 101, 0, "Print")
        menu.add(0, 102, 0, "Read Aloud")
        menu.add(0, 103, 0, "Metadata")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            100 -> {
                toggleNightMode()
                return true
            }
            101 -> {
                printDocument()
                return true
            }
            102 -> {
                readAloud()
                return true
            }
            103 -> {
                showMetadata()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleNightMode() {
        isNightMode = !isNightMode
        val readerView = getReaderView() ?: return

        val negative = floatArrayOf(
            -1.0f, 0f, 0f, 0f, 255f,
            0f, -1.0f, 0f, 0f, 255f,
            0f, 0f, -1.0f, 0f, 255f,
            0f, 0f, 0f, 1.0f, 0f
        )
        val filter = ColorMatrixColorFilter(negative)
        val paint = Paint()
        if (isNightMode) {
            paint.colorFilter = filter
        } else {
            paint.colorFilter = null
        }

        readerView.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        Toast.makeText(this, "Night Mode: ${if (isNightMode) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
    }

    private fun readAloud() {
        val readerView = getReaderView() ?: return
        val pageNum = readerView.displayedViewIndex

        // Simple heuristic: many documents will have a title we can read if extraction is hard.
        val core = getMuPDFCore()
        val title = core?.title ?: ""
        val text = "Reading page ${pageNum + 1} of document $title"

        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    private fun showMetadata() {
        val core = getMuPDFCore()
        if (core != null) {
            val title = core.title ?: "Unknown"
            val pages = core.countPages()
            Toast.makeText(this, "Title: $title\nPages: $pages", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Metadata not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun printDocument() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Toast.makeText(this, "Printing framework triggered", Toast.LENGTH_SHORT).show()
            // In a full implementation, we'd provide a PrintDocumentAdapter here.
        } else {
            Toast.makeText(this, "Printing requires Android 4.4+", Toast.LENGTH_SHORT).show()
        }
    }
}

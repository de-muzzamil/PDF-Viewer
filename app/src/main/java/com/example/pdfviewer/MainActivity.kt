package com.example.pdfviewer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.pdfviewer.databinding.ActivityMainBinding
import com.artifex.mupdf.viewer.DocumentActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var pendingUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        intent?.data?.let { uri ->
            handlePdfUri(uri)
        }
    }

    private fun handlePdfUri(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pendingUri = uri
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 101)
        } else {
            openPdf(uri)
        }
    }

    private fun openPdf(uri: Uri) {
        try {
            val finalUri = if (uri.scheme == "content") {
                copyToProviderUri(uri)
            } else {
                // If it's a file:// URI, MuPDF DocumentActivity might still struggle with exposure on 24+
                // Best to always use FileProvider for file:// too or copy it.
                copyToProviderUri(uri)
            }

            val mupdfIntent = Intent(this, DocumentActivity::class.java)
            mupdfIntent.action = Intent.ACTION_VIEW
            mupdfIntent.data = finalUri
            mupdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(mupdfIntent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyToProviderUri(uri: Uri): Uri {
        val tempFile = File(cacheDir, "temp_viewer.pdf")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", tempFile)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pendingUri?.let { openPdf(it) }
        } else {
            pendingUri?.let { openPdf(it) }
        }
    }
}

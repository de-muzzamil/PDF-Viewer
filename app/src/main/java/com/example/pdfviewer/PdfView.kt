package com.example.pdfviewer
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import java.io.File
import kotlin.math.*

class PdfView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var currentBitmap: Bitmap? = null
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val matrix = Matrix()
    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector): Boolean {
            matrix.postScale(d.scaleFactor, d.scaleFactor, d.focusX, d.focusY)
            invalidate(); return true
        }
    })
    override fun onTouchEvent(e: MotionEvent): Boolean { scaleDetector.onTouchEvent(e); return true }
    override fun onDraw(c: Canvas) { super.onDraw(c); currentBitmap?.let { c.drawBitmap(it, matrix, paint) } }
}

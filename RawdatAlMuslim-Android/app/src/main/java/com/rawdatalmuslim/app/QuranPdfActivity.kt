package com.rawdatalmuslim.app

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class QuranPdfActivity : Activity() {
    private var renderer: PdfRenderer? = null
    private var descriptor: ParcelFileDescriptor? = null
    private var pageIndex = 0
    private lateinit var image: ImageView
    private lateinit var counter: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(11, 46, 27))
            setPadding(8, 8, 8, 8)
        }
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val previous = Button(this).apply { text = "السابق" }
        val next = Button(this).apply { text = "التالي" }
        counter = TextView(this).apply {
            setTextColor(Color.WHITE); gravity = Gravity.CENTER; textSize = 16f
        }
        controls.addView(previous, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        controls.addView(counter, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        controls.addView(next, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        image = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.WHITE)
        }
        column.addView(controls)
        column.addView(image, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(column)
        previous.setOnClickListener { showPage(pageIndex - 1) }
        next.setOnClickListener { showPage(pageIndex + 1) }
        try {
            val pdf = File(cacheDir, "standard1-quran.pdf")
            if (!pdf.exists()) {
                assets.open("www/standard1-quran.pdf").use { input ->
                    FileOutputStream(pdf).use { output -> input.copyTo(output) }
                }
            }
            descriptor = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(descriptor!!)
            showPage(0)
        } catch (e: Exception) {
            Toast.makeText(this, "لم يتم العثور على standard1-quran.pdf داخل assets/www", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun showPage(index: Int) {
        val pdf = renderer ?: return
        if (index !in 0 until pdf.pageCount) return
        val page = pdf.openPage(index)
        val width = (resources.displayMetrics.widthPixels * 2).coerceAtLeast(page.width)
        val height = (width.toFloat() * page.height / page.width).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        image.setImageBitmap(bitmap)
        pageIndex = index
        counter.text = "${index + 1} / ${pdf.pageCount}"
    }

    override fun onDestroy() {
        renderer?.close()
        descriptor?.close()
        super.onDestroy()
    }
}

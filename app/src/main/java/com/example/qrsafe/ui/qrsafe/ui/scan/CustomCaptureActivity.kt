package com.example.qrsafe.ui.qrsafe.ui.scan

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.TextView
import com.example.qrsafe.R
import com.journeyapps.barcodescanner.CaptureActivity

class CustomCaptureActivity : CaptureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Încărcăm layout-ul pe care tocmai l-ai creat la Pasul 1
        val overlay = layoutInflater.inflate(R.layout.qr_overlay_cancel, null)
        addContentView(
            overlay,
            android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Logica butonului de X
        val cancelButton = overlay.findViewById<ImageButton>(R.id.cancelScanButton)
        cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        // Logica animației neon
        val scanText = overlay.findViewById<TextView>(R.id.scanStatusText)

        val colorAnim = ValueAnimator.ofObject(
            ArgbEvaluator(),
            0xFF00FF88.toInt(),
            0xFF66FFAA.toInt()
        ).apply {
            duration = 1200
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int
                scanText.setTextColor(color)
            }
            start()
        }

        ObjectAnimator.ofFloat(scanText, "alpha", 0.5f, 1f).apply {
            duration = 1000
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }
}
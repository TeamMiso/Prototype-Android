package com.miso.prototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.miso.prototype.databinding.ActivityMainBinding
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        binding.btnCamera.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    updateButtonState(onClick = true, button = view, binding.textCamera1, binding.textCamera2)
                    false
                }
                MotionEvent.ACTION_UP -> {
                    updateButtonState(onClick = false, button = view, binding.textCamera1, binding.textCamera2)
                    startActivity(Intent(this@MainActivity, CameraActivity::class.java))
                    false
                }
                else -> false
            }
        }

        binding.btnPoint.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    updateButtonState(onClick = true, button = view, binding.textPoint1, binding.textPoint2)
                    false
                }
                MotionEvent.ACTION_UP -> {
                    updateButtonState(onClick = false, button = view, binding.textPoint1, binding.textPoint2)
                    false
                }
                else -> false
            }
        }

        binding.btnInquiry.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    updateButtonState(onClick = true, button = view, binding.textInquiry1, binding.textInquiry2)
                    false
                }
                MotionEvent.ACTION_UP -> {
                    updateButtonState(onClick = false, button = view, binding.textInquiry1, binding.textInquiry2)
                    false
                }
                else -> false
            }
        }

        binding.btnInquiryCheck.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    updateButtonState(onClick = true, button = view, binding.textInquiryCheck1, binding.textInquiryCheck2)
                    false
                }
                MotionEvent.ACTION_UP -> {
                    updateButtonState(onClick = false, button = view, binding.textInquiryCheck1, binding.textInquiryCheck2)
                    false
                }
                else -> false
            }
        }
    }

    private fun updateButtonState(onClick: Boolean, button: View, text1: TextView, text2: TextView) {
        val backgroundColorResId = if (onClick) R.drawable.click_btn_background else R.drawable.btn_background
        val textColorResId = if (onClick) R.color.white else R.color.black

        val textColor = ContextCompat.getColor(this, textColorResId)

        button.setBackgroundResource(backgroundColorResId)
        (button as Button).setTextColor(textColor)
        text1.setTextColor(textColor)
        text2.setTextColor(textColor)
    }
}
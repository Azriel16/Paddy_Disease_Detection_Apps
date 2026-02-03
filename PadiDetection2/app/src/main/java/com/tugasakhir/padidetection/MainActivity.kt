package com.tugasakhir.padidetection

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tugasakhir.padidetection.cameradetection.CameraDetectionActivity
import com.tugasakhir.padidetection.databinding.ActivityMainBinding
import com.tugasakhir.padidetection.imagedetection.ImageDetectionActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraButton.setOnClickListener {
            startActivity(Intent(this, CameraDetectionActivity::class.java))
        }

        binding.galleryButton.setOnClickListener {
            startActivity(Intent(this, ImageDetectionActivity::class.java))
        }
    }
}

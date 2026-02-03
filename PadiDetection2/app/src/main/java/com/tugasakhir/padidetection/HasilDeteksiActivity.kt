package com.tugasakhir.padidetection

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tugasakhir.padidetection.classifier.ImageClassifierHelper
import com.tugasakhir.padidetection.databinding.ActivityHasilDeteksiBinding
import java.io.File

class HasilDeteksiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHasilDeteksiBinding
    private lateinit var classifierHelper: ImageClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHasilDeteksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        classifierHelper = ImageClassifierHelper(this)

        val imagePath = intent.getStringExtra("image_path")
        if (imagePath != null) {
            val file = File(imagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.imageView.setImageBitmap(bitmap)

                val result = classifierHelper.classifyStatic(bitmap)
                binding.tvResult.text = result
            } else {
                binding.tvResult.text = "Gambar tidak ditemukan!"
            }
        } else {
            binding.tvResult.text = "Tidak ada gambar yang diterima!"
        }
    }
}

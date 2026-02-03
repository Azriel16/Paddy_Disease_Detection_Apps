package com.tugasakhir.padidetection.classifier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.IOException

class ImageClassifierHelper(private val context: Context) {

    private var imageClassifier: ImageClassifier? = null
    private var labels: List<String> = emptyList()

    init {
        // Muat label saat inisialisasi
        loadLabels()
        setupImageClassifier()
    }

    // Membaca labels.txt di assets
    private fun loadLabels() {
        try {
            labels = context.assets.open("labels.txt").bufferedReader().useLines { it.toList() }
            Log.d("ImageClassifierHelper", "Label berhasil dimuat: $labels")
        } catch (e: IOException) {
            Log.e("ImageClassifierHelper", "Gagal memuat labels.txt: ${e.message}")
        }
    }

    private fun setupImageClassifier() {
        try {
            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setMaxResults(3)
                .setScoreThreshold(0.1f)
                .build()

            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                "model_padi_with_metadata_oversample10.tflite",
                options
            )
        } catch (e: IOException) {
            Log.e("ImageClassifierHelper", "Gagal memuat model: ${e.message}")
        }
    }

    // Normalisasi manual sesuai training
    private fun preprocessBitmap(bitmap: Bitmap): TensorImage {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val width = resized.width
        val height = resized.height

        val floatValues = FloatArray(width * height * 3)

        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std = floatArrayOf(0.229f, 0.224f, 0.225f)

        var idx = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = resized.getPixel(x, y)
                val r = (Color.red(pixel) / 255f - mean[0]) / std[0]
                val g = (Color.green(pixel) / 255f - mean[1]) / std[1]
                val b = (Color.blue(pixel) / 255f - mean[2]) / std[2]
                floatValues[idx++] = r
                floatValues[idx++] = g
                floatValues[idx++] = b
            }
        }

        val tensorBuffer = TensorBuffer.createFixedSize(intArrayOf(1, height, width, 3), DataType.FLOAT32)
        tensorBuffer.loadArray(floatValues)

        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(tensorBuffer)
        return tensorImage
    }

    fun classifyStatic(bitmap: Bitmap): String {
        val classifier = imageClassifier
        if (classifier == null) {
            return "Model tidak tersedia atau gagal dimuat"
        }

        return try {
            val tensorImage = preprocessBitmap(bitmap)
            val results: List<Classifications> = classifier.classify(tensorImage)

            if (results.isEmpty() || results[0].categories.isEmpty()) {
                "Tidak ada hasil deteksi"
            } else {

                val topResults = results[0].categories
                    .sortedByDescending { it.score }
                    .take(3)

                val resultText = topResults.joinToString("\n") { category ->

                    val idx = category.index
                    val labelName = if (idx in labels.indices) labels[idx] else category.label


                    var percent = category.score * 100

                    if (percent >= 99.995f) {
                        percent = 99.99f
                    }

                    "$labelName (%.2f%%)".format(percent)
                }

                "Hasil:\n$resultText"
            }
        } catch (e: Exception) {
            Log.e("ImageClassifierHelper", "Terjadi kesalahan saat klasifikasi: ${e.message}")
            "Terjadi kesalahan saat klasifikasi gambar"
        }
    }
}

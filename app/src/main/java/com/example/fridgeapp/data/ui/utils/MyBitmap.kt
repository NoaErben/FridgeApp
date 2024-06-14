package com.example.fridgeapp.data.ui.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object MyBitmap {

    fun compressBitmap(bitmap: Bitmap, maxSizeKb: Int): Bitmap {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        var quality = 100
        while (byteArrayOutputStream.toByteArray().size / 1024 > maxSizeKb) {
            byteArrayOutputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            quality -= 10
        }
        val compressedBitmap = BitmapFactory.decodeByteArray(
            byteArrayOutputStream.toByteArray(),
            0,
            byteArrayOutputStream.toByteArray().size
        )
        byteArrayOutputStream.close()
        return compressedBitmap
    }

}
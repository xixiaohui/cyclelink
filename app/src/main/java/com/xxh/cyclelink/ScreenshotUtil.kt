package com.xxh.cyclelink

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import androidx.core.graphics.createBitmap

object ScreenshotUtil {

    /**
     * 把任意 View 转换为透明背景 Bitmap
     */
    fun getTransparentBitmapFromView(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT) // 透明底色
        view.draw(canvas)
        return bitmap
    }

    /**
     * 保存 Bitmap 到 App 专属目录 (Android/data/包名/files)
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File? {
        return try {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "$fileName.png"
            )
            // 如果文件已存在，先删除
            if (file.exists()) file.delete()
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 保存 Bitmap 到系统相册 (MediaStore，Android Q 及以上必须用这种方式)
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        var imageUri: Uri? = null
        try {
            val fos: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots")
                }
                imageUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
                context.contentResolver.openOutputStream(imageUri!!)
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val file = File(directory, "$fileName.png")
                imageUri = Uri.fromFile(file)
                FileOutputStream(file)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                it.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imageUri
    }
}
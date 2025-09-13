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
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "$fileName.png")
            if (file.exists()) file.delete()

            // 确保 Bitmap 有透明通道
            val transparentBitmap = createBitmap(bitmap.width, bitmap.height)
            val canvas = Canvas(transparentBitmap)
            canvas.drawColor(android.graphics.Color.TRANSPARENT) // 填充透明背景
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            FileOutputStream(file).use { fos ->
                transparentBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
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
            val resolver = context.contentResolver
            val transparentBitmap = createBitmap(bitmap.width, bitmap.height)
            val canvas = Canvas(transparentBitmap)
            canvas.drawColor(android.graphics.Color.TRANSPARENT) // 透明底色
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 先删除同名文件（覆盖）
                val cursor = resolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Images.Media._ID),
                    "${MediaStore.Images.Media.DISPLAY_NAME}=?",
                    arrayOf("$fileName.png"),
                    null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                        resolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Images.Media._ID}=?", arrayOf(id.toString()))
                    }
                }

                // 保存 PNG
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                imageUri?.let { uri ->
                    resolver.openOutputStream(uri)?.use { fos ->
                        transparentBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                }
            } else {
                // Android 10 以下
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val file = File(directory, "$fileName.png")
                if (file.exists()) file.delete()
                FileOutputStream(file).use { fos ->
                    transparentBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                imageUri = Uri.fromFile(File(directory, "$fileName.png"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imageUri
    }

}
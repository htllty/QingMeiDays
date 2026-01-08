package com.qingmei.days.utils

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageUtils {

    // ✅ 这个保留：App 详情页保存图片到本地还是要用的
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? {
        return try {
            val fileName = "event_cover_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            outputStream.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ✅ 这个保留：删除图片文件
    fun deleteImage(path: String?) {
        try {
            if (!path.isNullOrEmpty()) File(path).delete()
        } catch (e: Exception) { e.printStackTrace() }
    }

}
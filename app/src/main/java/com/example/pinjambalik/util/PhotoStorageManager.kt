package com.example.pinjambalik.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object PhotoStorageManager {

    private const val PHOTOS_DIR = "rental_photos"
    private const val ID_DOCS_DIR = "identity_docs"

    fun getPhotoDirectory(context: Context, isIdentityDoc: Boolean = false): File {
        val dirName = if (isIdentityDoc) ID_DOCS_DIR else PHOTOS_DIR
        val dir = File(context.filesDir, dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getFileFromRelativePath(context: Context, relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        val file = File(context.filesDir, relativePath)
        return if (file.exists()) file else null
    }

    fun createTempCameraUri(context: Context): Uri {
        val cacheDir = File(context.cacheDir, "camera_temp")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val tempFile = File(cacheDir, "temp_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, tempFile)
    }

    suspend fun saveImageFromUri(
        context: Context,
        sourceUri: Uri,
        isIdentityDoc: Boolean = false,
        prefix: String = "img"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val targetDir = getPhotoDirectory(context, isIdentityDoc)
            val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val targetFile = File(targetDir, fileName)

            // Decode dimensions first
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val maxDimension = 1600
            var sampleSize = 1
            if (boundsOptions.outHeight > maxDimension || boundsOptions.outWidth > maxDimension) {
                val halfHeight = boundsOptions.outHeight / 2
                val halfWidth = boundsOptions.outWidth / 2
                while ((halfHeight / sampleSize) >= maxDimension && (halfWidth / sampleSize) >= maxDimension) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            var bitmap: Bitmap? = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@withContext null

            // Check Exif orientation
            try {
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    val exif = ExifInterface(input)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    val rotationDegrees = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                    if (rotationDegrees != 0f) {
                        val matrix = Matrix().apply { postRotate(rotationDegrees) }
                        val rotatedBitmap = Bitmap.createBitmap(
                            bitmap!!,
                            0,
                            0,
                            bitmap!!.width,
                            bitmap!!.height,
                            matrix,
                            true
                        )
                        bitmap?.recycle()
                        bitmap = rotatedBitmap
                    }
                }
            } catch (_: Exception) {
                // Ignore exif parsing fallback
            }

            // Write compressed JPEG
            FileOutputStream(targetFile).use { out ->
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            bitmap?.recycle()

            val dirName = if (isIdentityDoc) ID_DOCS_DIR else PHOTOS_DIR
            "$dirName/$fileName"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveBitmapDirectly(
        context: Context,
        bitmap: Bitmap,
        isIdentityDoc: Boolean = false,
        prefix: String = "capture"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val targetDir = getPhotoDirectory(context, isIdentityDoc)
            val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val targetFile = File(targetDir, fileName)

            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            val dirName = if (isIdentityDoc) ID_DOCS_DIR else PHOTOS_DIR
            "$dirName/$fileName"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteFile(context: Context, relativePath: String?): Boolean {
        if (relativePath.isNullOrBlank()) return false
        return try {
            val file = File(context.filesDir, relativePath)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }
}

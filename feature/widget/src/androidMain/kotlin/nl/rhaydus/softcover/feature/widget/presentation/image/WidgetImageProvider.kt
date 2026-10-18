package nl.rhaydus.softcover.feature.widget.presentation.image

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileNotFoundException

internal class WidgetImageProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun openFile(
        uri: Uri,
        mode: String,
    ): ParcelFileDescriptor {
        if (mode != "r") throw FileNotFoundException("Read-only provider")

        val name = uri.pathSegments.singleOrNull()
            ?.takeIf { WidgetImageFiles.NAME_PATTERN.matches(it) }
            ?: throw FileNotFoundException("Unknown image")

        val directory = WidgetImageFiles.directory(requireNotNull(context))

        return ParcelFileDescriptor.open(
            File(
                directory,
                name,
            ),
            ParcelFileDescriptor.MODE_READ_ONLY,
        )
    }

    override fun getType(uri: Uri): String? = uri.lastPathSegment?.let { WidgetImageFormat.forFileName(it)?.mimeType }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}

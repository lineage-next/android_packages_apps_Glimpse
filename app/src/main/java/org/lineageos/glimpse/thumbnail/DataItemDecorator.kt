package org.lineageos.glimpse.thumbnail

import android.database.Cursor
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.provider.MediaStore
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.State
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

class DataItemDecorator : ItemDecoration() {
    // Cursor
    private var cursor: Cursor? = null

    // Cursor indexes
    private var dateAddedIndex = -1

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
        val position = parent.getChildAdapterPosition(view)
        if (isHeader(position)) {
            outRect.setEmpty()
        } else {
            outRect.setEmpty()
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: State) {
        for (i in 0 until parent.childCount) {
            val view = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(view)
            if (isHeader(position)) {
                c.drawRect(
                    view.left.toFloat(),
                    view.top.toFloat(),
                    view.right.toFloat(),
                    view.top.toFloat(),
                    Paint()
                )
            }
        }
    }

    private val headersPositions = sortedSetOf<Int>()

    private fun isHeader(position: Int): Boolean {
        if (headersPositions.contains(position)) {
            return true
        }

        if (position == 0) {
            // First element must always be a header
            return true
        }

        val previousPosition = position - 1

        if (headersPositions.contains(previousPosition)) {
            // Before this position we have a header, next up there's a thumbnail
            return false
        }

        val currentMediaDate = getMediaDateFromMediaStore(position)
        val previousMediaDate = getMediaDateFromMediaStore(previousPosition)

        val before = previousMediaDate.toInstant().atZone(ZoneId.systemDefault())
        val after = currentMediaDate.toInstant().atZone(ZoneId.systemDefault())
        val days = ChronoUnit.DAYS.between(after, before)

        if (days >= 1 || before.dayOfMonth != after.dayOfMonth) {
            return true
        }

        return false
    }

    fun swapCursor(cursor: Cursor?) {
        if (this.cursor == cursor) {
            return
        }

        val oldCursor = this.cursor
        this.cursor = cursor

        cursor?.let {
            dateAddedIndex = it.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
        }

        oldCursor?.close()
    }

    private fun getMediaDateFromMediaStore(position: Int): Date {
        val cursor = cursor ?: return Date(0)
        cursor.moveToPosition(position)
        return Date(cursor.getLong(dateAddedIndex) * 1000)
    }
}

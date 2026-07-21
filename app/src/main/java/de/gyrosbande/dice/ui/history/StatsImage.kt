package de.gyrosbande.dice.ui.history

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import de.gyrosbande.dice.R
import de.gyrosbande.dice.domain.Stats
import de.gyrosbande.dice.domain.StatsPresentation

/**
 * Draws the hall of fame as a shareable PNG (the black/gold look of the
 * app) so the Gyrosbande can drop the year's stats into the group chat.
 * Plain Canvas/Paint - deterministic and independent of the Compose tree.
 */
object StatsImage {

    private const val WIDTH = 1080
    private const val PADDING = 64f
    private const val GOLD = 0xFFD4AF37.toInt()
    private const val GREY = 0xFFB5B5B5.toInt()

    fun render(context: Context, stats: Stats, subtitle: String): Bitmap {
        val facts = StatsPresentation.funFacts(stats)

        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo_gyrosbande)
        val logoSize = 260
        val logoScaled = logo?.let { Bitmap.createScaledBitmap(it, logoSize, logoSize, true) }

        // Measure height: header + one block per fun fact + tally box + footer.
        val rowHeight = 150f
        val headerHeight = logoSize + 150f
        val tallyBox = 190f
        val footerHeight = 80f
        val height = (headerHeight + facts.size * rowHeight + tallyBox + footerHeight + PADDING).toInt()

        val bitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)

        val bold = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        fun paint(size: Float, color: Int, face: Typeface = Typeface.DEFAULT, center: Boolean = false) =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = size
                this.color = color
                typeface = face
                if (center) textAlign = Paint.Align.CENTER
            }

        var y = PADDING

        // Header: logo, title, subtitle - all centered.
        logoScaled?.let {
            canvas.drawBitmap(it, (WIDTH - logoSize) / 2f, y, null)
            y += logoSize + 24f
        }
        canvas.drawText("Hall of Fame", WIDTH / 2f, y + 44f, paint(64f, Color.WHITE, bold, center = true))
        y += 74f
        canvas.drawText(subtitle, WIDTH / 2f, y + 30f, paint(34f, GOLD, center = true))
        y += 76f

        // Fun-fact rows: emoji, then title / holder / detail stacked.
        val emojiPaint = paint(64f, Color.WHITE)
        val titlePaint = paint(30f, GOLD)
        val holderPaint = paint(46f, Color.WHITE, bold)
        val detailPaint = paint(30f, GREY)
        for (fact in facts) {
            val left = PADDING
            canvas.drawText(fact.emoji, left, y + 64f, emojiPaint)
            val textLeft = left + 96f
            canvas.drawText(fact.title, textLeft, y + 26f, titlePaint)
            canvas.drawText(ellipsize(fact.holder, holderPaint, WIDTH - textLeft - PADDING), textLeft, y + 76f, holderPaint)
            canvas.drawText(ellipsize(fact.detail, detailPaint, WIDTH - textLeft - PADDING), textLeft, y + 118f, detailPaint)
            y += rowHeight
        }

        // Tally box in gold.
        y += 8f
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD }
        val boxRect = android.graphics.RectF(PADDING, y, WIDTH - PADDING, y + 150f)
        canvas.drawRoundRect(boxRect, 28f, 28f, boxPaint)
        canvas.drawText(StatsPresentation.tally(stats), PADDING + 32f, y + 58f, paint(38f, Color.BLACK, bold))
        canvas.drawText(StatsPresentation.revenueLine(stats), PADDING + 32f, y + 110f, paint(30f, Color.BLACK))
        y += 150f + 40f

        canvas.drawText("837 Dice · Gyrosbande", WIDTH / 2f, y, paint(28f, GREY, center = true))

        logo?.recycle()
        return bitmap
    }

    /** Cuts a string with an ellipsis so it never runs off the image. */
    private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 1 && paint.measureText(text.substring(0, end) + "…") > maxWidth) end--
        return text.substring(0, end) + "…"
    }
}

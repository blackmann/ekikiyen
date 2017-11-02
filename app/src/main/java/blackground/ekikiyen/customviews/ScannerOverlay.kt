package blackground.ekikiyen.customviews

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import blackground.ekikiyen.R

class ScannerOverlay : View {

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    private var currentX = 0.0f
    private var lineHeight = 0.0f
    private val paint: Paint = Paint()

    private var valueAnimator: ValueAnimator? = null

    init {
        paint.strokeWidth = 5.0f
        paint.style = Paint.Style.STROKE
        paint.color = context.resources.getColor(R.color.green)
    }

    private fun beginAnimation() {
        valueAnimator = ValueAnimator.ofFloat(0f, width.toFloat())
        valueAnimator?.repeatCount = ValueAnimator.INFINITE
        valueAnimator?.repeatMode = ValueAnimator.REVERSE
        valueAnimator?.duration = 2000L
        valueAnimator?.interpolator = LinearInterpolator()

        valueAnimator?.addUpdateListener {
            currentX = it.animatedValue as Float
            invalidate()
        }

        valueAnimator?.start()
    }

    private fun stopAnimation() {
        valueAnimator?.cancel()
        valueAnimator?.removeAllUpdateListeners()

        // free up
        valueAnimator = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        lineHeight = h.toFloat()
        beginAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawLine(currentX, 0f, currentX, lineHeight, paint)
    }
}
package com.andrewkingmarshall.pexels.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

private const val INVALID_POINTER_ID = -1

const val MAX_SCALE = 5.0f
const val MIN_SCALE = 1.0f

/**
 * This custom ImageView will allow the user to pinch to zoom and pan around an Image.
 *
 * It still could use some polish especially when preventing a user from panning an image off screen.
 *
 * Credit: I took a lot of inspiration from these sources and used them to better understand the
 *         concepts. Some helper functions were copied from other sources, but the 'meat' of this
 *         is original.
 *         - https://medium.com/a-problem-like-maria/understanding-android-matrix-transformations-25e028f56dc7
 *         - https://github.com/Baseflow/PhotoView/blob/master/photoview/src/main/java/com/github/chrisbanes/photoview/PhotoViewAttacher.java#L320
 */
class PinchToZoomImageView : AppCompatImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var scaleFactor = 1f

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = INVALID_POINTER_ID

    // The matrix that all transforms are applied to.
    private val baseMatrix = Matrix()

    //  The matrix used to transform the base matrix
    private var transMatrix = Matrix()

    // A helper matrix that is used when applying the matrix changes to the drawable
    private val drawMatrix = Matrix()

    private val matrixValues = FloatArray(9)

    init {
        // Start with FIT_CENTER so our animations look nice, then change to MATRIX mode once
        // you start pinching to Zoom
        scaleType = ScaleType.FIT_CENTER
    }

    /**
     * Resets the Image to FIT_CENTER in Matrix mode. Will also reset the scaling and panning.
     */
    private fun resetBaseMatrix() {
        scaleType = ScaleType.MATRIX

        val imageWidth = drawable.intrinsicWidth.toFloat()
        val imageHeight = drawable.intrinsicHeight.toFloat()

        val drawableRect = RectF(0f, 0f, imageWidth, imageHeight)
        val viewRect = RectF(0f, 0f, width.toFloat(), height.toFloat())

        baseMatrix.reset()
        transMatrix.reset()

        scaleFactor = 1.0f

        lastTouchX = 0f
        lastTouchY = 0f

        baseMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER)

        applyMatrix()
    }

    /**
     * Applies the transform matrix's to the base matrix and sets those changes to the ImageMatrix.
     */
    private fun applyMatrix() {
        if (drawable == null) {
            return
        }

        drawMatrix.set(baseMatrix)
        drawMatrix.postConcat(transMatrix)

        imageMatrix = drawMatrix
    }

    /**
     * Listens for Scale events. When a scale event is detected, it will alter the transform matrix
     * then apply those changes to the base matrix. Resulting in the drawable being scaled.
     */
    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {

            if (scaleType != ScaleType.MATRIX) {
                Timber.d("Resetting to FIT_CENTER in Matrix Mode")
                resetBaseMatrix()
            }

            // Keep track of our scale factor, while also making sure we never go beyond our bounds
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(MIN_SCALE, min(scaleFactor, MAX_SCALE))

            // The amount we want to change our scaling, this will be concatenated to our transMatrix
            val deltaScale = scaleFactor / getScale()

            /* Check to make sure we are not trying to scale past a threshold, if we are at a threshold
               but we are about to make it smaller/larger so it will continue to be in the threshold
               then we allow it. */
            if ((getScale() < MAX_SCALE || deltaScale < 1f) && (getScale() > MIN_SCALE || deltaScale > 1f)) {

                Timber.d("Scale Factor = $scaleFactor")

                transMatrix.postScale(deltaScale, deltaScale, width / 2f, height / 2f)

                applyMatrix()

            } else {
                Timber.d("At scaling threshold.")
            }

            return true
        }
    }

    private val scaleDetector = ScaleGestureDetector(context, scaleListener)

    /**
     * Listens for all touch events on this View. This is where the panning logic occurs.
     *
     * Much of this code is designed to track the active pointer and determining how far that pointer
     * has moved across the screen.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Let the ScaleGestureDetector know about all touch events
        scaleDetector.onTouchEvent(ev)

        val action = ev.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                lastTouchX = x
                lastTouchY = y
                activePointerId = ev.getPointerId(0)
                Timber.tag("touch").d("ACTION_DOWN:  x = $x   y = $y")
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = ev.findPointerIndex(activePointerId)
                val x = ev.getX(pointerIndex)
                val y = ev.getY(pointerIndex)

                /* FIXME: Check if moving the image would take it out of bounds
                    Turns out this is actually very difficult. Would need to take into account scaling as well.
                */

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!scaleDetector.isInProgress) {

                    val dx = x - lastTouchX
                    val dy = y - lastTouchY

                    transMatrix.postTranslate(dx, dy)
                    applyMatrix()
                }
                lastTouchX = x
                lastTouchY = y
                Timber.tag("touch").d("ACTION_MOVE:  x = $x   y = $y")
            }
            MotionEvent.ACTION_UP -> {
                activePointerId = INVALID_POINTER_ID
                Timber.tag("touch").d("ACTION_UP:  x = ${ev.x}   y = ${ev.y}")
            }
            MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
                Timber.tag("touch").d("ACTION_CANCEL:  x = ${ev.x}   y = ${ev.y}")
            }
            MotionEvent.ACTION_POINTER_UP -> {
                ev.actionIndex.also { pointerIndex ->
                    ev.getPointerId(pointerIndex)
                        .takeIf { it == activePointerId }
                        ?.run {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            lastTouchX = ev.getX(newPointerIndex)
                            lastTouchY = ev.getY(newPointerIndex)
                            activePointerId = ev.getPointerId(newPointerIndex)
                        }
                }
                Timber.tag("touch").d("ACTION_POINTER_UP:  x = ${ev.x}   y = ${ev.y}")
            }

        }

        return true
    }

    /**
     * Calculates the Scale from the transMatrix.
     *
     * @return The current scale applied to the transMatrix.
     */
    fun getScale(): Float {
        return sqrt(
            (getValue(transMatrix, Matrix.MSCALE_X).toDouble().pow(2.0)
                .toFloat() + getValue(transMatrix, Matrix.MSKEW_Y).toDouble().pow(2.0)
                .toFloat()).toDouble()
        ).toFloat()
    }


    /**
     * Helper method that 'unpacks' a Matrix and returns the required value
     *
     * @param matrix     Matrix to unpack
     * @param whichValue Which value from Matrix.M* to return
     * @return returned value
     */
    private fun getValue(matrix: Matrix, whichValue: Int): Float {
        matrix.getValues(matrixValues)
        return matrixValues[whichValue]
    }

}
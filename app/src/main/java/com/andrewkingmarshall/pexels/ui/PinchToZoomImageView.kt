package com.andrewkingmarshall.pexels.ui

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

class PinchToZoomImageView : AppCompatImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


    private var mScaleFactor = 1f

    private var mLastTouchX = 0f
    private var mLastTouchY = 0f
    private var mActivePointerId = INVALID_POINTER_ID

    private var mPosX = 0f
    private var mPosY = 0f

    //todo for tomorrow:
    /*
       So it looks like you need:
       1. BaseMatrix that is basically the setToFitFitCenterMatrix() matrix
       2. When you scale / move you are editing another "support Matrix"
       3. When you go to actually update the ImageView you are using a 3rd "drawMatrix" which is a combo of base and support

       private Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }


        Could use this if I don't want to do the hacky first scale trick, but then I lose the animation I think

        @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        // Update our base matrix, as the bounds have changed
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
            resetBaseMatrix(mImageView.getDrawable());
        }
    }


     */


    //    private var centerFitMatrix = Matrix()
    private val baseMatrix = Matrix()
    private var transMatrix = Matrix()
    private val drawMatrix = Matrix()

    private val mMatrixValues = FloatArray(9)

    // This works!
    fun resetBaseMatrix() {
        scaleType = ScaleType.MATRIX

        val imageWidth = drawable.intrinsicWidth.toFloat()
        val imageHeight = drawable.intrinsicHeight.toFloat()

        val drawableRect = RectF(0f, 0f, imageWidth, imageHeight)
        val viewRect = RectF(0f, 0f, width.toFloat(), height.toFloat())

        baseMatrix.reset()
        transMatrix.reset()

        baseMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER)

        applyMatrix()
    }

    private fun applyMatrix() {
        if (drawable == null) {
            return
        }

        drawMatrix.set(baseMatrix)
        drawMatrix.postConcat(transMatrix)

        imageMatrix = drawMatrix
    }

//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)
//        if (drawable == null) {
//            return
//        }
//        Timber.tag("akm").d("onLayout")
//        setToFitFitCenterMatrix()
//    }


    init {
        // Start with FIT_CENTER so our animations look nice, then change to MATRIX mode once
        // you start pinching to Zoom
        scaleType = ScaleType.FIT_CENTER
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {

            if (scaleType != ScaleType.MATRIX) {
                Timber.d("Resetting to FIT_CENTER in Matrix Mode")
                resetBaseMatrix()
            }

            mScaleFactor *= detector.scaleFactor

            val deltaScale = mScaleFactor / getScale()

            /* Check to make sure we are not trying to scale past a threshold, if we are at a threshold
               but we are about to make it smaller/larger so it will continue to be in the threshold
               then we allow it. */
            if ((getScale() < MAX_SCALE || deltaScale < 1f) && (getScale() > MIN_SCALE || deltaScale > 1f)) {

                Timber.d("Scale Factor = $mScaleFactor")
                Timber.d("deltaScale = $deltaScale")

                transMatrix.postScale(deltaScale, deltaScale, width / 2f, height / 2f)

                applyMatrix()

            } else {
                Timber.d("At scaling threshold.")
            }

//            // Don't let the object get too small or too large.
//            mScaleFactor = max(1.0f, min(mScaleFactor, 5.0f))
//
//            Timber.d("Scale Factor = $mScaleFactor")
//            Timber.d("deltaScale = ${deltaScale}")
//
//            // todo: See if I can just reset to Identify instead of recreating this object
//            // Apply the scale to the middle of the Image
////            transMatrix = Matrix().apply {
////
////                // Warning: using these treats it like it is still in the top left
////                val dWidth = drawable.intrinsicWidth
////                val dHeight = drawable.intrinsicHeight
////
////                setScale(mScaleFactor, mScaleFactor, width / 2f, height / 2f)
////            }
//            transMatrix.postScale(deltaScale, deltaScale, width / 2f, height / 2f)
//
//            applyMatrix()

            return true
        }
    }

    // todo: document, why is this 'MSKEW_Y' instead of `MSCALE_Y`
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
        matrix.getValues(mMatrixValues)
        return mMatrixValues[whichValue]
    }

    private val mScaleDetector = ScaleGestureDetector(context, scaleListener)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev)

//        val action = ev.action
//        when (action and MotionEvent.ACTION_MASK) {
//            MotionEvent.ACTION_DOWN -> {
//                val x = ev.x
//                val y = ev.y
//                mLastTouchX = x
//                mLastTouchY = y
//                mActivePointerId = ev.getPointerId(0)
//                Timber.tag("touch").d("ACTION_DOWN:  x = $x   y = $y")
//            }
//            MotionEvent.ACTION_MOVE -> {
//                val pointerIndex = ev.findPointerIndex(mActivePointerId)
//                val x = ev.getX(pointerIndex)
//                val y = ev.getY(pointerIndex)
//
//                // todo: Check if moving the image would take it off the screen, if so, don't do it
//                // Only move if the ScaleGestureDetector isn't processing a gesture.
//                if (!mScaleDetector.isInProgress) {
//                    val dx = x - mLastTouchX
//                    val dy = y - mLastTouchY
//                    mPosX += dx
//                    mPosY += dy
//
//                    transMatrix.postTranslate(dx, dy)
//                    applyMatrix()
//                }
//                mLastTouchX = x
//                mLastTouchY = y
//                Timber.tag("touch").d("ACTION_MOVE:  x = $x   y = $y")
//            }
//            MotionEvent.ACTION_UP -> {
//                mActivePointerId = INVALID_POINTER_ID
//                Timber.tag("touch").d("ACTION_UP:  x = ${ev.x}   y = ${ev.y}")
//            }
//            MotionEvent.ACTION_CANCEL -> {
//                mActivePointerId = INVALID_POINTER_ID
//                Timber.tag("touch").d("ACTION_CANCEL:  x = ${ev.x}   y = ${ev.y}")
//            }
//            MotionEvent.ACTION_POINTER_UP -> {
//                ev.actionIndex.also { pointerIndex ->
//                    ev.getPointerId(pointerIndex)
//                        .takeIf { it == mActivePointerId }
//                        ?.run {
//                            // This was our active pointer going up. Choose a new
//                            // active pointer and adjust accordingly.
//                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
//                            mLastTouchX = ev.getX(newPointerIndex)
//                            mLastTouchY = ev.getY(newPointerIndex)
//                            mActivePointerId = ev.getPointerId(newPointerIndex)
//                        }
//                }
//                Timber.tag("touch").d("ACTION_POINTER_UP:  x = ${ev.x}   y = ${ev.y}")
//            }
//
//        }

        return true
    }

}
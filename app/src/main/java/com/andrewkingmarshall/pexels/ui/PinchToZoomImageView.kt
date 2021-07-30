package com.andrewkingmarshall.pexels.ui

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import timber.log.Timber
import java.lang.Math.round
import kotlin.math.max
import kotlin.math.min

private const val INVALID_POINTER_ID = -1

class PinchToZoomImageView : AppCompatImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


    private var mScaleFactor = 1f

    private var mLastTouchX = 0f
    private var mLastTouchY = 0f
    private var mActivePointerId = INVALID_POINTER_ID

    private var mPosX = 0f
    private var mPosY = 0f

    private val theMatrix = Matrix()

    init {
        scaleType = ScaleType.MATRIX

        // TODO: Move / scale it to fit right in the middle of the screen, like CenterCrop
//        imageMatrix = Matrix().apply {
//
//            val dWidth = drawable.intrinsicWidth
//            val dHeight = drawable.intrinsicHeight
//
//            val vWidth = measuredWidth
//            val vHeight = measuredHeight
//
//            setTranslate(
//                ((vWidth - dWidth) * 0.5f),
//                ((vHeight - dHeight) * 0.5f)
//            )
//        }
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor

            // Don't let the object get too small or too large.
            mScaleFactor = max(1.0f, min(mScaleFactor, 5.0f))

            // Apply the scale to the middle of the Image
            imageMatrix = theMatrix.apply {

                val dWidth = drawable.intrinsicWidth
                val dHeight = drawable.intrinsicHeight

                setScale(mScaleFactor, mScaleFactor, dWidth / 2f, dHeight / 2f)
            }

            return true
        }
    }

    private val mScaleDetector = ScaleGestureDetector(context, scaleListener)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev)

        val action = ev.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                mLastTouchX = x
                mLastTouchY = y
                mActivePointerId = ev.getPointerId(0)
                Timber.tag("touch").d("ACTION_DOWN:  x = $x   y = $y")
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                val x = ev.getX(pointerIndex)
                val y = ev.getY(pointerIndex)

                // todo: Check if moving the image would take it off the screen, if so, don't do it
                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress) {
                    val dx = x - mLastTouchX
                    val dy = y - mLastTouchY
                    mPosX += dx
                    mPosY += dy
                    imageMatrix = theMatrix.apply {
                        setTranslate(
                            mPosX,
                            mPosY
                        )
                    }
                }
                mLastTouchX = x
                mLastTouchY = y
                Timber.tag("touch").d("ACTION_MOVE:  x = $x   y = $y")
            }
            MotionEvent.ACTION_UP -> {
                mActivePointerId = INVALID_POINTER_ID
                Timber.tag("touch").d("ACTION_UP:  x = ${ev.x}   y = ${ev.y}")
            }
            MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = INVALID_POINTER_ID
                Timber.tag("touch").d("ACTION_CANCEL:  x = ${ev.x}   y = ${ev.y}")
            }
            MotionEvent.ACTION_POINTER_UP -> {
                ev.actionIndex.also { pointerIndex ->
                    ev.getPointerId(pointerIndex)
                        .takeIf { it == mActivePointerId }
                        ?.run {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            mLastTouchX = ev.getX(newPointerIndex)
                            mLastTouchY = ev.getY(newPointerIndex)
                            mActivePointerId = ev.getPointerId(newPointerIndex)
                        }
                }
                Timber.tag("touch").d("ACTION_POINTER_UP:  x = ${ev.x}   y = ${ev.y}")
            }

        }

        return true
    }

}
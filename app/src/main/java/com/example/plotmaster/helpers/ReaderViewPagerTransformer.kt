package com.example.plotmaster.helpers

import android.view.View
import androidx.viewpager.widget.ViewPager

class ReaderViewPagerTransformer(private val transformType: TransformType) : ViewPager.PageTransformer {
    enum class TransformType {
        FLOW,
        DEPTH,
        ZOOM,
        SLIDE_OVER
    }

    companion object {
        private const val MIN_SCALE_DEPTH = 0.75f
        private const val MIN_SCALE_ZOOM = 0.85f
        private const val MIN_ALPHA_ZOOM = 0.5f
        private const val SCALE_FACTOR_SLIDE = 0.85f
        private const val MIN_ALPHA_SLIDE = 0.35f
    }

    override fun transformPage(page: View, position: Float) {
        val alpha: Float
        val scale: Float
        val translationX: Float
        when (transformType) {
            TransformType.FLOW -> {
                page.rotationY = position * -30f
                return
            }
            TransformType.SLIDE_OVER -> {
                if (position < 0 && position > -1) {
                    scale =
                        Math.abs(Math.abs(position) - 1) * (1.0f - SCALE_FACTOR_SLIDE) + SCALE_FACTOR_SLIDE
                    alpha = Math.max(MIN_ALPHA_SLIDE, 1 - Math.abs(position))
                    val pageWidth = page.width
                    val translateValue = position * -pageWidth
                    translationX = if (translateValue > -pageWidth) {
                        translateValue
                    } else {
                        0f
                    }
                } else {
                    alpha = 1f
                    scale = 1f
                    translationX = 0f
                }
            }
            TransformType.DEPTH -> {
                if (position > 0 && position < 1) {
                    alpha = (1 - position)
                    scale = MIN_SCALE_DEPTH + (1 - MIN_SCALE_DEPTH) * (1 - Math.abs(position))
                    translationX = (page.width * -position)
                } else {
                    alpha = 1f
                    scale = 1f
                    translationX = 0f
                }
            }
            TransformType.ZOOM -> {
                if (position >= -1 && position <= 1) {
                    scale = Math.max(MIN_SCALE_ZOOM, 1 - Math.abs(position))
                    alpha = MIN_ALPHA_ZOOM +
                            (scale - MIN_SCALE_ZOOM) / (1 - MIN_SCALE_ZOOM) * (1 - MIN_ALPHA_ZOOM)
                    val vMargin = page.height * (1 - scale) / 2
                    val hMargin = page.width * (1 - scale) / 2
                    translationX = if (position < 0) {
                        (hMargin - vMargin / 2)
                    } else {
                        (-hMargin + vMargin / 2)
                    }
                } else {
                    alpha = 1f
                    scale = 1f
                    translationX = 0f
                }
            }
            else -> return
        }
        page.alpha = alpha
        page.translationX = translationX
        page.scaleX = scale
        page.scaleY = scale
    }
}
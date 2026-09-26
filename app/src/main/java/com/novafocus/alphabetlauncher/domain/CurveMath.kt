package com.novafocus.alphabetlauncher.domain

import kotlin.math.abs
import kotlin.math.exp

object CurveMath {
    /**
     * Calculates the horizontal displacement for an item at distance [distancePx] from the touch point.
     * Uses a Gaussian falloff curve:
     *   influence = exp(- (d^2) / (2 * sigma^2))
     * The result is negative because displacement pulls toward the screen center (to the left from the right edge).
     */
    fun offsetFor(distancePx: Float, maxBulgePx: Float, sigmaPx: Float): Float {
        if (sigmaPx <= 0f) return 0f
        val influence = exp(-(distancePx * distancePx) / (2f * sigmaPx * sigmaPx))
        return -maxBulgePx * influence
    }

    /**
     * Finds the index of the nearest letter given the current touch Y coordinate and an array of
     * letter resting Y center positions.
     */
    fun nearestLetterIndex(touchY: Float, letterRestYs: FloatArray): Int {
        if (letterRestYs.isEmpty()) return 0
        var closest = 0
        var closestDist = Float.MAX_VALUE
        for (i in letterRestYs.indices) {
            val d = abs(letterRestYs[i] - touchY)
            if (d < closestDist) {
                closestDist = d
                closest = i
            }
        }
        return closest
    }
}

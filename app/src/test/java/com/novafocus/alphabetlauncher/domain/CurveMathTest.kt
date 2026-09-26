package com.novafocus.alphabetlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CurveMathTest {

    private val maxBulge = 120f
    private val sigma = 40f

    @Test
    fun offsetFor_atZeroDistance_returnsNegativeMaxBulge() {
        val offset = CurveMath.offsetFor(distancePx = 0f, maxBulgePx = maxBulge, sigmaPx = sigma)
        assertEquals(-maxBulge, offset, 0.0001f)
    }

    @Test
    fun offsetFor_isSymmetricForPositiveAndNegativeDistances() {
        val dist = 35f
        val positiveOffset = CurveMath.offsetFor(distancePx = dist, maxBulgePx = maxBulge, sigmaPx = sigma)
        val negativeOffset = CurveMath.offsetFor(distancePx = -dist, maxBulgePx = maxBulge, sigmaPx = sigma)
        assertEquals(positiveOffset, negativeOffset, 0.0001f)
    }

    @Test
    fun offsetFor_decaysTowardZeroBeyondMultipleSigmas() {
        val offsetAt3Sigma = CurveMath.offsetFor(distancePx = 3 * sigma, maxBulgePx = maxBulge, sigmaPx = sigma)
        val offsetAt5Sigma = CurveMath.offsetFor(distancePx = 5 * sigma, maxBulgePx = maxBulge, sigmaPx = sigma)

        // At 3 sigma, exp(-9/2) ≈ 0.0111 -> magnitude < 1.5% of maxBulge
        assertTrue(abs(offsetAt3Sigma) < maxBulge * 0.02f)
        // At 5 sigma, exp(-25/2) ≈ 3.7e-6 -> virtually 0
        assertTrue(abs(offsetAt5Sigma) < maxBulge * 0.0001f)
    }

    @Test
    fun offsetFor_sigmaTuningVisiblyMovesAroundFiveToSixLetters() {
        // Assume spacing between letters is 50px, and sigma is tuned to 2.8x spacing = 140px
        val letterSpacing = 50f
        val sigmaTuned = letterSpacing * 2.8f
        val maxB = 100f

        // Letters at distance: d=0 (1 letter), d=±50 (2 letters), d=±100 (2 letters), d=±150 (2 letters)
        val center = CurveMath.offsetFor(0f, maxB, sigmaTuned)
        val neighbor1 = CurveMath.offsetFor(letterSpacing, maxB, sigmaTuned)
        val neighbor2 = CurveMath.offsetFor(2 * letterSpacing, maxB, sigmaTuned)
        val distant = CurveMath.offsetFor(4 * letterSpacing, maxB, sigmaTuned)

        // Center gets full bulge
        assertEquals(-100f, center, 0.01f)
        // First neighbors (±1) retain > 90% influence
        assertTrue(abs(neighbor1) > 90f)
        // Second neighbors (±2) retain > 75% influence (visibly bending)
        assertTrue(abs(neighbor2) > 75f)
        // Letters 4 steps away settle near negligible displacement (< 36% and rapidly diminishing)
        assertTrue(abs(distant) < 40f)
    }

    @Test
    fun offsetFor_zeroSigma_returnsZeroSafely() {
        val offset = CurveMath.offsetFor(distancePx = 10f, maxBulgePx = 100f, sigmaPx = 0f)
        assertEquals(0f, offset, 0.0001f)
    }

    @Test
    fun nearestLetterIndex_exactRestY_returnsCorrectIndex() {
        val restYs = floatArrayOf(100f, 130f, 160f, 190f, 220f)
        assertEquals(0, CurveMath.nearestLetterIndex(100f, restYs))
        assertEquals(1, CurveMath.nearestLetterIndex(130f, restYs))
        assertEquals(2, CurveMath.nearestLetterIndex(160f, restYs))
        assertEquals(3, CurveMath.nearestLetterIndex(190f, restYs))
        assertEquals(4, CurveMath.nearestLetterIndex(220f, restYs))
    }

    @Test
    fun nearestLetterIndex_betweenRestYs_returnsNearestIndex() {
        val restYs = floatArrayOf(100f, 130f, 160f, 190f, 220f)

        // 112f is closer to 100f (diff 12) than 130f (diff 18)
        assertEquals(0, CurveMath.nearestLetterIndex(112f, restYs))

        // 118f is closer to 130f (diff 12) than 100f (diff 18)
        assertEquals(1, CurveMath.nearestLetterIndex(118f, restYs))

        // 145f midpoint between 130f and 160f (first index is picked when equal)
        assertEquals(1, CurveMath.nearestLetterIndex(145f, restYs))
    }

    @Test
    fun nearestLetterIndex_clampedAtExtremes() {
        val restYs = floatArrayOf(100f, 130f, 160f, 190f, 220f)

        // Far above top item
        assertEquals(0, CurveMath.nearestLetterIndex(20f, restYs))

        // Far below bottom item
        assertEquals(4, CurveMath.nearestLetterIndex(500f, restYs))
    }

    @Test
    fun nearestLetterIndex_emptyArray_returnsZeroGracefully() {
        assertEquals(0, CurveMath.nearestLetterIndex(100f, FloatArray(0)))
    }
}

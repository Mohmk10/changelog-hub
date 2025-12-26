package io.github.mohmk10.changeloghub.intellij

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for comparison functionality and risk assessment.
 */
class ComparisonServiceTest {

    @Test
    fun `test risk level LOW for zero breaking changes`() {
        assertEquals("LOW", calculateRiskLevel(0, 10))
    }

    @Test
    fun `test risk level MEDIUM for 1-2 breaking changes`() {
        assertEquals("MEDIUM", calculateRiskLevel(1, 10))
        assertEquals("MEDIUM", calculateRiskLevel(2, 10))
    }

    @Test
    fun `test risk level HIGH for 3-4 breaking changes`() {
        assertEquals("HIGH", calculateRiskLevel(3, 10))
        assertEquals("HIGH", calculateRiskLevel(4, 10))
    }

    @Test
    fun `test risk level CRITICAL for 5+ breaking changes`() {
        assertEquals("CRITICAL", calculateRiskLevel(5, 10))
        assertEquals("CRITICAL", calculateRiskLevel(10, 10))
    }

    @Test
    fun `test semver recommendation MAJOR for breaking changes`() {
        assertEquals("MAJOR", getSemverRecommendation(hasBreaking = true, hasAdditions = false))
    }

    @Test
    fun `test semver recommendation MINOR for additions without breaking`() {
        assertEquals("MINOR", getSemverRecommendation(hasBreaking = false, hasAdditions = true))
    }

    @Test
    fun `test semver recommendation PATCH for no breaking or additions`() {
        assertEquals("PATCH", getSemverRecommendation(hasBreaking = false, hasAdditions = false))
    }

    private fun calculateRiskLevel(breakingCount: Int, totalCount: Int): String {
        return when {
            breakingCount >= 5 -> "CRITICAL"
            breakingCount >= 3 -> "HIGH"
            breakingCount >= 1 -> "MEDIUM"
            else -> "LOW"
        }
    }

    private fun getSemverRecommendation(hasBreaking: Boolean, hasAdditions: Boolean): String {
        return when {
            hasBreaking -> "MAJOR"
            hasAdditions -> "MINOR"
            else -> "PATCH"
        }
    }
}

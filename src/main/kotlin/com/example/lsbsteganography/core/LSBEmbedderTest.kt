// src/test/kotlin/com/example/lsbsteganography/core/LSBEmbedderTest.kt
package com.example.lsbsteganography.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.awt.image.BufferedImage
import java.awt.Color

/**
 * Тесты для класса LSBEmbedder.
 */
class LSBEmbedderTest {

    private val lsbEmbedder = LSBEmbedder()

    /**
     * Тестирует встраивание и извлечение текста.
     */
    @Test
    fun testEmbedAndExtractText() {
        val image = createTestImage(100, 100)
        val testText = "Hello, World!"

        assertTrue(lsbEmbedder.canEmbedText(image, testText))

        val embeddedImage = lsbEmbedder.embedText(image, testText)
        val extractedText = lsbEmbedder.extractText(embeddedImage)

        assertEquals(testText, extractedText)
    }

    /**
     * Тестирует проверку возможности встраивания текста.
     */
    @Test
    fun testCanEmbedText() {
        val smallImage = createTestImage(10, 10) // 10*10*3 = 300 бит
        val largeText = "A".repeat(50) // 50 символов = 400 бит

        assertFalse(lsbEmbedder.canEmbedText(smallImage, largeText))

        val smallText = "Short"
        assertTrue(lsbEmbedder.canEmbedText(smallImage, smallText))
    }

    /**
     * Тестирует встраивание пустого текста.
     */
    @Test
    fun testEmbedEmptyText() {
        val image = createTestImage(100, 100)
        val emptyText = ""

        assertTrue(lsbEmbedder.canEmbedText(image, emptyText))

        val embeddedImage = lsbEmbedder.embedText(image, emptyText)
        val extractedText = lsbEmbedder.extractText(embeddedImage)

        assertEquals(emptyText, extractedText)
    }

    /**
     * Тестирует встраивание текста с пробелами и специальными символами.
     */
    @Test
    fun testEmbedTextWithSpecialCharacters() {
        val image = createTestImage(100, 100)
        val specialText = "Hello\nWorld\t!"

        assertTrue(lsbEmbedder.canEmbedText(image, specialText))

        val embeddedImage = lsbEmbedder.embedText(image, specialText)
        val extractedText = lsbEmbedder.extractText(embeddedImage)

        assertEquals(specialText, extractedText)
    }

    /**
     * Создает тестовое изображение.
     * @param width Ширина изображения
     * @param height Высота изображения
     * @return Тестовое изображение
     */
    private fun createTestImage(width: Int, height: Int): BufferedImage {
        return BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    }
}
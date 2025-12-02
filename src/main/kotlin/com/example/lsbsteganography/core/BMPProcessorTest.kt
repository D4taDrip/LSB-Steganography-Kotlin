// src/test/kotlin/com/example/lsbsteganography/core/BMPProcessorTest.kt
package com.example.lsbsteganography.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Тесты для класса BMPProcessor.
 */
class BMPProcessorTest {

    private val bmpProcessor = BMPProcessor()

    /**
     * Тестирует создание LSB изображения.
     */
    @Test
    fun testGetLSBImage() {
        val originalImage = createTestImageWithRandomPixels(10, 10)

        val lsbImage = bmpProcessor.getLSBImage(originalImage)

        assertEquals(originalImage.width, lsbImage.width)
        assertEquals(originalImage.height, lsbImage.height)

        for (y in 0 until 10) {
            for (x in 0 until 10) {
                val pixel = lsbImage.getRGB(x, y)
                val red = (pixel shr 16) and 0xFF
                val green = (pixel shr 8) and 0xFF
                val blue = pixel and 0xFF

                assertTrue(red == 0 || red == 255)
                assertTrue(green == 0 || green == 255)
                assertTrue(blue == 0 || blue == 255)
            }
        }
    }

    /**
     * Тестирует чтение и запись BMP файла.
     */
    @Test
    fun testReadWriteBMP() {
        val testImage = createTestImageWithRandomPixels(10, 10)

        val tempFile = File.createTempFile("test", ".bmp")
        tempFile.deleteOnExit()

        bmpProcessor.writeBMP(testImage, tempFile.absolutePath)

        val readImage = bmpProcessor.readBMP(tempFile.absolutePath)

        assertEquals(testImage.width, readImage.width)
        assertEquals(testImage.height, readImage.height)

        for (y in 0 until 10) {
            for (x in 0 until 10) {
                assertEquals(testImage.getRGB(x, y), readImage.getRGB(x, y))
            }
        }
    }

    /**
     * Создает тестовое изображение со случайными пикселями.
     * @param width Ширина изображения
     * @param height Высота изображения
     * @return Тестовое изображение
     */
    private fun createTestImageWithRandomPixels(width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                image.setRGB(x, y, 0xFF81A3C5) // Пример: 129, 163, 197
            }
        }
        return image
    }
}
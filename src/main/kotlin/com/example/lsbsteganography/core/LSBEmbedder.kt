// src/main/kotlin/com/example/lsbsteganography/core/LSBEmbedder.kt
package com.example.lsbsteganography.core

import com.example.lsbsteganography.utils.Logger
import java.awt.image.BufferedImage

/**
 * Класс для встраивания текста в изображение по алгоритму НЗБ.
 * Реализует методы для встраивания и извлечения текста.
 */
class LSBEmbedder {

    /**
     * Встраивает текст в изображение по алгоритму НЗБ.
     * @param image Изображение для встраивания
     * @param text Текст для встраивания
     * @return Новое изображение с встроенным текстом
     */
    fun embedText(image: BufferedImage, text: String): BufferedImage {
        Logger.info("Embedding text into image. Text length: ${text.length}")
        validateTextLength(image, text)

        val resultImage = copyImage(image)
        val textBits = prepareTextBits(text)
        val lengthBits = prepareLengthBits(text.length)

        embedTextLength(resultImage, lengthBits)
        embedTextContent(resultImage, textBits)

        Logger.info("Text successfully embedded into image")
        return resultImage
    }

    /**
     * Извлекает текст из изображения.
     * @param image Изображение с встроенным текстом
     * @return Извлеченный текст
     */
    fun extractText(image: BufferedImage): String {
        Logger.info("Extracting text from image")

        val textLength = extractTextLength(image)
        val extractedText = extractTextContent(image, textLength)

        Logger.info("Successfully extracted text: $extractedText")
        return extractedText
    }

    /**
     * Проверяет, можно ли встроить текст в изображение.
     * @param image Изображение для проверки
     * @param text Текст для встраивания
     * @return true, если текст можно встроить, иначе false
     */
    fun canEmbedText(image: BufferedImage, text: String): Boolean {
        val maxTextLength = calculateMaxTextLength(image)
        val result = text.length <= maxTextLength
        Logger.info("Can embed text: $result (max: $maxTextLength, actual: ${text.length})")
        return result
    }

    /**
     * Копирует изображение.
     * @param source Исходное изображение
     * @return Копия изображения
     */
    private fun copyImage(source: BufferedImage): BufferedImage {
        val result = BufferedImage(source.width, source.height, source.type)
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                result.setRGB(x, y, source.getRGB(x, y))
            }
        }
        return result
    }

    /**
     * Подготавливает биты текста для встраивания.
     * @param text Текст для встраивания
     * @return Массив битов текста
     */
    private fun prepareTextBits(text: String): BooleanArray {
        return stringToBooleanArray(text)
    }

    /**
     * Подготавливает биты длины текста для встраивания.
     * @param textLength Длина текста
     * @return Массив битов длины
     */
    private fun prepareLengthBits(textLength: Int): BooleanArray {
        return intToBooleanArray(textLength, 32)
    }

    /**
     * Встраивает длину текста в изображение.
     * @param image Изображение для встраивания
     * @param lengthBits Биты длины текста
     */
    private fun embedTextLength(image: BufferedImage, lengthBits: BooleanArray) {
        var bitIndex = 0
        val width = image.width
        val height = image.height

        for (i in 0 until 32.coerceAtMost(width * height * 3)) {
            val pixelX = bitIndex / (width * 3)
            val pixelY = (bitIndex % (width * 3)) / 3
            val colorIndex = bitIndex % 3

            val newPixel = modifyPixelLSB(image.getRGB(pixelY, pixelX), colorIndex, lengthBits[i])
            image.setRGB(pixelY, pixelX, newPixel)
            bitIndex++
        }
    }

    /**
     * Встраивает содержимое текста в изображение.
     * @param image Изображение для встраивания
     * @param textBits Биты текста
     */
    private fun embedTextContent(image: BufferedImage, textBits: BooleanArray) {
        var bitIndex = 32 // Начинаем после длины текста
        val width = image.width
        val height = image.height

        for (i in 0 until textBits.size.coerceAtMost(width * height * 3 - bitIndex)) {
            val pixelX = bitIndex / (width * 3)
            val pixelY = (bitIndex % (width * 3)) / 3
            val colorIndex = bitIndex % 3

            val newPixel = modifyPixelLSB(image.getRGB(pixelY, pixelX), colorIndex, textBits[i])
            image.setRGB(pixelY, pixelX, newPixel)
            bitIndex++
        }
    }

    /**
     * Извлекает длину текста из изображения.
     * @param image Изображение с встроенным текстом
     * @return Длина текста
     */
    private fun extractTextLength(image: BufferedImage): Int {
        val lengthBits = BooleanArray(32)
        var bitIndex = 0
        val width = image.width
        val height = image.height

        for (i in 0 until 32) {
            val pixelX = bitIndex / (width * 3)
            val pixelY = (bitIndex % (width * 3)) / 3
            val colorIndex = bitIndex % 3

            val pixel = image.getRGB(pixelY, pixelX)
            val value = getColorValueFromPixel(pixel, colorIndex)
            lengthBits[i] = (value and 1) == 1
            bitIndex++
        }

        return booleanArrayToInt(lengthBits)
    }

    /**
     * Извлекает содержимое текста из изображения.
     * @param image Изображение с встроенным текстом
     * @param textLength Длина текста
     * @return Извлеченный текст
     */
    private fun extractTextContent(image: BufferedImage, textLength: Int): String {
        var bitIndex = 32 // Начинаем после длины текста
        val width = image.width
        val height = image.height
        val textBits = BooleanArray(textLength * 8)

        for (i in 0 until textLength * 8) {
            val pixelX = bitIndex / (width * 3)
            val pixelY = (bitIndex % (width * 3)) / 3
            val colorIndex = bitIndex % 3

            val pixel = image.getRGB(pixelY, pixelX)
            val value = getColorValueFromPixel(pixel, colorIndex)
            textBits[i] = (value and 1) == 1
            bitIndex++
        }

        return booleanArrayToString(textBits, textLength)
    }

    /**
     * Изменяет младший бит пикселя.
     * @param pixel Исходный пиксель
     * @param colorIndex Индекс цвета (0-red, 1-green, 2-blue)
     * @param newLSB Новое значение младшего бита
     * @return Новый пиксель с измененным младшим битом
     */
    private fun modifyPixelLSB(pixel: Int, colorIndex: Int, newLSB: Boolean): Int {
        var red = (pixel shr 16) and 0xFF
        var green = (pixel shr 8) and 0xFF
        var blue = pixel and 0xFF

        when (colorIndex) {
            0 -> red = (red and 0xFE) or if (newLSB) 1 else 0
            1 -> green = (green and 0xFE) or if (newLSB) 1 else 0
            2 -> blue = (blue and 0xFE) or if (newLSB) 1 else 0
        }

        return (red shl 16) or (green shl 8) or blue
    }

    /**
     * Получает значение цвета из пикселя.
     * @param pixel Пиксель
     * @param colorIndex Индекс цвета
     * @return Значение цвета
     */
    private fun getColorValueFromPixel(pixel: Int, colorIndex: Int): Int {
        return when (colorIndex) {
            0 -> (pixel shr 16) and 0xFF
            1 -> (pixel shr 8) and 0xFF
            else -> pixel and 0xFF
        }
    }

    /**
     * Проверяет длину текста перед встраиванием.
     * @param image Изображение
     * @param text Текст
     */
    private fun validateTextLength(image: BufferedImage, text: String) {
        if (!canEmbedText(image, text)) {
            throw IllegalArgumentException("Text is too long for this image")
        }
    }

    /**
     * Рассчитывает максимальную длину текста для изображения.
     * @param image Изображение
     * @return Максимальная длина текста
     */
    private fun calculateMaxTextLength(image: BufferedImage): Int {
        return (image.width * image.height * 3 - 32) / 8
    }

    /**
     * Преобразует строку в массив битов.
     * @param text Строка для преобразования
     * @return Массив булевых значений, представляющих биты строки
     */
    private fun stringToBooleanArray(text: String): BooleanArray {
        val bytes = text.toByteArray()
        val bits = BooleanArray(bytes.size * 8)

        for (i in bytes.indices) {
            for (j in 0 until 8) {
                bits[i * 8 + j] = (bytes[i].toInt() and (1 shl j)) != 0
            }
        }

        return bits
    }

    /**
     * Преобразует целое число в массив битов заданной длины.
     * @param value Целое число
     * @param length Длина массива
     * @return Массив булевых значений
     */
    private fun intToBooleanArray(value: Int, length: Int): BooleanArray {
        val bits = BooleanArray(length)
        for (i in 0 until length) {
            bits[i] = (value and (1 shl i)) != 0
        }
        return bits
    }

    /**
     * Преобразует массив битов в строку заданной длины.
     * @param bits Массив битов
     * @param length Длина строки
     * @return Преобразованная строка
     */
    private fun booleanArrayToString(bits: BooleanArray, length: Int): String {
        val bytes = ByteArray(length)

        for (i in 0 until length) {
            var b = 0.toByte()
            for (j in 0 until 8) {
                if (bits[i * 8 + j]) {
                    b = (b.toInt() or (1 shl j)).toByte()
                }
            }
            bytes[i] = b
        }

        return String(bytes)
    }

    /**
     * Преобразует массив битов в целое число.
     * @param bits Массив битов
     * @return Преобразованное целое число
     */
    private fun booleanArrayToInt(bits: BooleanArray): Int {
        var value = 0
        for (i in 0 until bits.size.coerceAtMost(32)) {
            if (bits[i]) {
                value = value or (1 shl i)
            }
        }
        return value
    }
}
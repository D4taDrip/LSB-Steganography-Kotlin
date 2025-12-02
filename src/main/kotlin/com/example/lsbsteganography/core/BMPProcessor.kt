// src/main/kotlin/com/example/lsbsteganography/core/BMPProcessor.kt
package com.example.lsbsteganography.core

import com.example.lsbsteganography.utils.Logger
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.embed.swing.SwingFXUtils
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

/**
 * Класс для обработки BMP изображений.
 * Содержит методы для чтения, записи и преобразования изображений.
 */
class BMPProcessor {

    /**
     * Читает BMP изображение из файла.
     * @param imagePath Путь к BMP файлу
     * @return BufferedImage объект изображения
     */
    fun readBMP(imagePath: String): BufferedImage {
        Logger.info("Reading BMP image from: $imagePath")
        val imageFile = File(imagePath)
        validateImageFileExists(imageFile, imagePath)

        val image = ImageIO.read(imageFile)
        validateImageRead(image, imagePath)

        Logger.info("Successfully read BMP image. Size: ${image.width}x${image.height}")
        return image
    }

    /**
     * Сохраняет изображение в BMP формате.
     * @param image Изображение для сохранения
     * @param outputPath Путь для сохранения
     */
    fun writeBMP(image: BufferedImage, outputPath: String) {
        Logger.info("Writing BMP image to: $outputPath")
        val outputFile = File(outputPath)
        ImageIO.write(image, "BMP", outputFile)
        Logger.info("Successfully wrote BMP image")
    }

    /**
     * Создает изображение, отображающее младшие биты пикселей.
     * @param image Исходное изображение
     * @return Новое изображение с отображением младших битов
     */
    fun getLSBImage(image: BufferedImage): BufferedImage {
        Logger.info("Creating LSB visualization image")
        val width = image.width
        val height = image.height
        val lsbImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = image.getRGB(x, y)
                val lsbPixel = extractLSBPixel(pixel)
                lsbImage.setRGB(x, y, lsbPixel)
            }
        }

        Logger.info("LSB visualization created successfully")
        return lsbImage
    }

    /**
     * Преобразует BufferedImage в JavaFX Image.
     * @param bufferedImage Исходное изображение
     * @return JavaFX Image
     */
    fun bufferedImageToFXImage(bufferedImage: BufferedImage): Image {
        return SwingFXUtils.toFXImage(bufferedImage, WritableImage(bufferedImage.width, bufferedImage.height))
    }

    /**
     * Извлекает младшие биты из пикселя.
     * @param pixel Цвет пикселя
     * @return Пиксель с младшими битами
     */
    private fun extractLSBPixel(pixel: Int): Int {
        val red = (pixel shr 16) and 0xFF
        val green = (pixel shr 8) and 0xFF
        val blue = pixel and 0xFF

        val lsbRed = (red and 1) * 255
        val lsbGreen = (green and 1) * 255
        val lsbBlue = (blue and 1) * 255

        return (lsbRed shl 16) or (lsbGreen shl 8) or lsbBlue
    }

    /**
     * Проверяет существование файла изображения.
     * @param imageFile Файл изображения
     * @param imagePath Путь к файлу
     */
    private fun validateImageFileExists(imageFile: File, imagePath: String) {
        if (!imageFile.exists()) {
            throw IllegalArgumentException("File does not exist: $imagePath")
        }
    }

    /**
     * Проверяет успешное чтение изображения.
     * @param image Прочитанное изображение
     * @param imagePath Путь к файлу
     */
    private fun validateImageRead(image: BufferedImage?, imagePath: String) {
        if (image == null) {
            throw IllegalArgumentException("Cannot read image: $imagePath")
        }
    }
}
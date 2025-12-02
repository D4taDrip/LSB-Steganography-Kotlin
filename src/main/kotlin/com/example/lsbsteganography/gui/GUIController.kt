// src/main/kotlin/com/example/lsbsteganography/gui/GUIController.kt
package com.example.lsbsteganography.gui

import com.example.lsbsteganography.core.BMPProcessor
import com.example.lsbsteganography.core.LSBEmbedder
import com.example.lsbsteganography.utils.Logger
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.image.ImageView
import javafx.scene.image.Image
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.scene.paint.Color
import java.awt.image.BufferedImage
import javafx.embed.swing.SwingFXUtils
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonBar.ButtonData
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

/**
 * Контроллер графического интерфейса приложения.
 * Обрабатывает события и управляет компонентами интерфейса.
 */
class GUIController {
    private val bmpProcessor = BMPProcessor()
    private val lsbEmbedder = LSBEmbedder()

    private val sourcePathField = TextField().apply {
        promptText = "Путь к исходному изображению"
        isEditable = false
    }

    private val outputPathField = TextField().apply {
        promptText = "Путь для сохранения результата"
        isEditable = false
    }

    private val textArea = TextArea().apply {
        promptText = "Введите текст для встраивания"
    }

    private val originalImageView = ImageView().apply {
        fitWidth = 300.0
        fitHeight = 200.0
        preserveRatio = true
        isSmooth = true
    }

    private val resultImageView = ImageView().apply {
        fitWidth = 300.0
        fitHeight = 200.0
        preserveRatio = true
        isSmooth = true
    }

    private val lsbImageView = ImageView().apply {
        fitWidth = 300.0
        fitHeight = 200.0
        preserveRatio = true
        isSmooth = true
    }

    private val originalLabel = Label("Оригинальное изображение")
    private val resultLabel = Label("Изображение с вложением")
    private val lsbLabel = Label("Младшие биты")

    private val embedButton = Button("Встроить текст").apply {
        setOnAction {
            processEmbedding()
        }
    }

    private val browseSourceButton = Button("Обзор").apply {
        setOnAction {
            browseSourceImage()
        }
    }

    private val browseOutputButton = Button("Обзор").apply {
        setOnAction {
            browseOutputImage()
        }
    }

    /**
     * Создает основную панель интерфейса.
     * @return BorderPane с компоновкой интерфейса
     */
    fun createMainPane(): BorderPane {
        val controlPanel = createControlPanel()
        val imagePanel = createImagePanel()

        val mainPane = BorderPane()
        mainPane.left = controlPanel
        mainPane.center = imagePanel

        return mainPane
    }

    /**
     * Создает панель управления.
     * @return VBox с элементами управления
     */
    private fun createControlPanel(): VBox {
        val sourcePanel = HBox(10.0, sourcePathField, browseSourceButton).apply {
            alignment = Pos.CENTER_LEFT
        }

        val outputPanel = HBox(10.0, outputPathField, browseOutputButton).apply {
            alignment = Pos.CENTER_LEFT
        }

        val textScrollPane = ScrollPane(textArea).apply {
            prefHeight = 100.0
            fitToWidth = true
        }

        val controlPanel = VBox(10.0).apply {
            padding = Insets(10.0)
            children.addAll(
                createLabelWithBoldFont("Путь к исходному изображению:"),
                sourcePanel,
                createLabelWithBoldFont("Путь для сохранения:"),
                outputPanel,
                createLabelWithBoldFont("Текст для встраивания:"),
                textScrollPane,
                embedButton
            )
        }

        return controlPanel
    }

    /**
     * Создает панель изображений.
     * @return HBox с отображением изображений
     */
    private fun createImagePanel(): HBox {
        val originalBox = VBox(5.0, originalLabel, originalImageView).apply {
            alignment = Pos.CENTER
            style = "-fx-border-color: gray; -fx-border-width: 1px;"
            padding = Insets(5.0)
        }

        val resultBox = VBox(5.0, resultLabel, resultImageView).apply {
            alignment = Pos.CENTER
            style = "-fx-border-color: gray; -fx-border-width: 1px;"
            padding = Insets(5.0)
        }

        val lsbBox = VBox(5.0, lsbLabel, lsbImageView).apply {
            alignment = Pos.CENTER
            style = "-fx-border-color: gray; -fx-border-width: 1px;"
            padding = Insets(5.0)
        }

        return HBox(20.0, originalBox, resultBox, lsbBox).apply {
            alignment = Pos.CENTER
            padding = Insets(10.0)
        }
    }

    /**
     * Создает метку с жирным шрифтом.
     * @param text Текст метки
     * @return Label с жирным шрифтом
     */
    private fun createLabelWithBoldFont(text: String): Label {
        return Label(text).apply {
            font = Font.font("System", FontWeight.BOLD, 12.0)
        }
    }

    /**
     * Открывает диалог выбора исходного изображения.
     */
    private fun browseSourceImage() {
        val fileChooser = FileChooser().apply {
            title = "Выберите исходное BMP изображение"
            extensionFilters.add(FileChooser.ExtensionFilter("BMP файлы", "*.bmp"))
        }

        val stage = (sourcePathField.scene.window as Stage)
        val selectedFile = fileChooser.showOpenDialog(stage)

        if (selectedFile != null) {
            sourcePathField.text = selectedFile.absolutePath
            loadAndDisplayImage(selectedFile.absolutePath, originalImageView)
        }
    }

    /**
     * Открывает диалог выбора пути для сохранения результата.
     */
    private fun browseOutputImage() {
        val fileChooser = FileChooser().apply {
            title = "Сохранить результат как BMP"
            extensionFilters.add(FileChooser.ExtensionFilter("BMP файлы", "*.bmp"))
        }

        val stage = (outputPathField.scene.window as Stage)
        val selectedFile = fileChooser.showSaveDialog(stage)

        if (selectedFile != null) {
            var filePath = selectedFile.absolutePath
            if (!filePath.lowercase().endsWith(".bmp")) {
                filePath += ".bmp"
            }
            outputPathField.text = filePath
        }
    }

    /**
     * Обрабатывает процесс встраивания текста в изображение.
     */
    private fun processEmbedding() {
        val sourcePath = sourcePathField.text.trim()
        val outputPath = outputPathField.text.trim()
        val textToEmbed = textArea.text

        if (!validateInputFields(sourcePath, outputPath, textToEmbed)) {
            return
        }

        try {
            val originalImage = bmpProcessor.readBMP(sourcePath)

            if (!validateTextLength(originalImage, textToEmbed)) {
                return
            }

            val resultImage = lsbEmbedder.embedText(originalImage, textToEmbed)
            bmpProcessor.writeBMP(resultImage, outputPath)

            displayResultImages(resultImage)

            showInfoDialog("Текст успешно встроен в изображение и сохранен!")
            Logger.info("Text successfully embedded and saved to: $outputPath")

        } catch (ex: Exception) {
            showErrorDialog("Ошибка при обработке изображения: ${ex.message}")
            Logger.error("Error during embedding process", ex)
        }
    }

    /**
     * Загружает и отображает изображение.
     * @param imagePath Путь к изображению
     * @param imageView ImageView для отображения
     */
    private fun loadAndDisplayImage(imagePath: String, imageView: ImageView) {
        try {
            val image = bmpProcessor.readBMP(imagePath)
            val fxImage = bmpProcessor.bufferedImageToFXImage(image)
            imageView.image = fxImage
            Logger.info("Source image loaded successfully")
        } catch (ex: Exception) {
            showErrorDialog("Ошибка при загрузке изображения: ${ex.message}")
            Logger.error("Error loading source image", ex)
        }
    }

    /**
     * Отображает результаты визуализации.
     * @param resultImage Результирующее изображение
     */
    private fun displayResultImages(resultImage: BufferedImage) {
        val resultFXImage = bmpProcessor.bufferedImageToFXImage(resultImage)
        val lsbFXImage = bmpProcessor.bufferedImageToFXImage(bmpProcessor.getLSBImage(resultImage))

        resultImageView.image = resultFXImage
        lsbImageView.image = lsbFXImage
    }

    /**
     * Проверяет заполнение полей ввода.
     * @param sourcePath Путь к исходному изображению
     * @param outputPath Путь для сохранения
     * @param textToEmbed Текст для встраивания
     * @return true, если все поля заполнены, иначе false
     */
    private fun validateInputFields(sourcePath: String, outputPath: String, textToEmbed: String): Boolean {
        if (sourcePath.isEmpty() || outputPath.isEmpty() || textToEmbed.isEmpty()) {
            showErrorDialog("Пожалуйста, заполните все поля: путь к изображению, путь для сохранения и текст.")
            return false
        }
        return true
    }

    /**
     * Проверяет длину текста для встраивания.
     * @param originalImage Исходное изображение
     * @param textToEmbed Текст для встраивания
     * @return true, если текст можно встроить, иначе false
     */
    private fun validateTextLength(originalImage: BufferedImage, textToEmbed: String): Boolean {
        if (!lsbEmbedder.canEmbedText(originalImage, textToEmbed)) {
            val maxLength = (originalImage.width * originalImage.height * 3 - 32) / 8
            showErrorDialog(
                "Текст слишком длинный для этого изображения.\n" +
                        "Максимальная длина: $maxLength символов.\n" +
                        "Текущая длина: ${textToEmbed.length} символов."
            )
            return false
        }
        return true
    }

    /**
     * Показывает диалог с информационным сообщением.
     * @param message Сообщение для отображения
     */
    private fun showInfoDialog(message: String) {
        val alert = Alert(AlertType.INFORMATION).apply {
            title = "Информация"
            headerText = null
            contentText = message
        }
        alert.showAndWait()
    }

    /**
     * Показывает диалог с сообщением об ошибке.
     * @param message Сообщение об ошибке
     */
    private fun showErrorDialog(message: String) {
        val alert = Alert(AlertType.ERROR).apply {
            title = "Ошибка"
            headerText = null
            contentText = message
        }
        alert.showAndWait()
    }
}
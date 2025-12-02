// src/main/kotlin/com/example/lsbsteganography/gui/SteganographyApplication.kt
package com.example.lsbsteganography.gui

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

/**
 * Класс приложения JavaFX для стеганографии НЗБ.
 * Инициализирует и отображает графический интерфейс.
 */
class SteganographyApplication : Application() {

    override fun start(primaryStage: Stage) {
        val controller = GUIController()
        val root = BorderPane()
        root.center = controller.createMainPane()

        val scene = Scene(root, 1000.0, 600.0)
        primaryStage.title = "LSB Steganography Tool"
        primaryStage.scene = scene
        primaryStage.show()
    }
}
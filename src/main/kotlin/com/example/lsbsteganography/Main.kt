// src/main/kotlin/com/example/lsbsteganography/Main.kt
package com.example.lsbsteganography

import com.example.lsbsteganography.gui.SteganographyApplication
import javafx.application.Application

/**
 * Основной класс приложения для стеганографии по алгоритму НЗБ.
 * Запускает графический интерфейс приложения.
 */
fun main(args: Array<String>) {
    Application.launch(SteganographyApplication::class.java, *args)
}
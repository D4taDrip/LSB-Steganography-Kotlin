// src/main/kotlin/com/example/lsbsteganography/utils/Logger.kt
package com.example.lsbsteganography.utils

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Утилита для логирования событий приложения.
 * Использует log4j2 для логирования в консоль и файл.
 */
object Logger {
    private val logger: Logger = LogManager.getLogger(Logger::class.java)

    /**
     * Логирует информационное сообщение.
     * @param message Сообщение для логирования
     */
    fun info(message: String) {
        logger.info(message)
    }

    /**
     * Логирует сообщение об ошибке.
     * @param message Сообщение об ошибке
     */
    fun error(message: String) {
        logger.error(message)
    }

    /**
     * Логирует сообщение об ошибке с исключением.
     * @param message Сообщение об ошибке
     * @param throwable Исключение
     */
    fun error(message: String, throwable: Throwable) {
        logger.error(message, throwable)
    }

    /**
     * Логирует предупреждение.
     * @param message Сообщение предупреждения
     */
    fun warn(message: String) {
        logger.warn(message)
    }

    /**
     * Логирует отладочное сообщение.
     * @param message Сообщение для отладки
     */
    fun debug(message: String) {
        logger.debug(message)
    }
}
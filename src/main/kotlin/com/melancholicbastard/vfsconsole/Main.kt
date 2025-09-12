package com.melancholicbastard.vfsconsole

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    val viewModel = TerminalViewModel()
    var shouldExit by remember { mutableStateOf(false) }

    if (shouldExit) {
        exitApplication()
        return@application
    }

    Window(
        // `::exitApplication` - это ссылка на функцию, которая завершает приложение.
        onCloseRequest = ::exitApplication,
        title = "VFS",
        state = WindowState(width = 800.dp, height = 600.dp)
    ) {
        // Передаем лямбду, которая устанавливает `shouldExit = true` при необходимости выйти и viewModel
        MainScreen(
            viewModel = viewModel,
            onExit = { shouldExit = true }
        )
    }
}
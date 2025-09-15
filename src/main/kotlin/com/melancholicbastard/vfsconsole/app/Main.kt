package com.melancholicbastard.vfsconsole.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.melancholicbastard.vfsconsole.data.AppConfig
import com.melancholicbastard.vfsconsole.viewmodel.MainScreen
import com.melancholicbastard.vfsconsole.ScriptRunner
import com.melancholicbastard.vfsconsole.viewmodel.TerminalViewModel

fun main(args: Array<String>) = application {

    val viewModel = TerminalViewModel()
    val scriptRunner = ScriptRunner(viewModel)
    var shouldExit by remember { mutableStateOf(false) }

//     Запускаем скрипт если указан
    LaunchedEffect(Unit) {
//         Инициализируем конфигурацию
        AppConfig.initialize(args)
        AppConfig.arguments.scriptPath?.let { scriptPath ->
            scriptRunner.runScript(scriptPath)
        }
    }

    if (shouldExit) {
        exitApplication()
        return@application
    }

    Window(
//         `::exitApplication` - это ссылка на функцию, которая завершает приложение
        onCloseRequest = ::exitApplication,
        title = "VFS",
        state = WindowState(width = 800.dp, height = 600.dp)
    ) {
//         Передаем лямбду, которая устанавливает `shouldExit = true` при необходимости выйти и viewModel
        MainScreen(
            viewModel = viewModel,
            onExit = { shouldExit = true }
        )
    }
}
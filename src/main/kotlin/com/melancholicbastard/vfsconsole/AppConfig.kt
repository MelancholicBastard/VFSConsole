package com.melancholicbastard.vfsconsole

import com.melancholicbastard.vfsconsole.model.AppParams
import java.io.File

// Для хранения глобальных настроек приложения
object AppConfig {
    lateinit var arguments: AppParams
    lateinit var vfsPath: String
    lateinit var workingDir: String
    var hasScript: Boolean = false

    fun initialize(args: Array<String>) {
        arguments = AppParams.parse(args)

//         Получаем путь к исполняемому файлу эмулятора
        vfsPath = File(System.getProperty("java.class.path")?.split(File.pathSeparator)?.firstOrNull()
            ?: "unknown").absolutePath

//         Получаем текущую рабочую директорию
        workingDir = System.getProperty("user.dir") ?: "unknown"

        hasScript = arguments.scriptPath != null
    }
}
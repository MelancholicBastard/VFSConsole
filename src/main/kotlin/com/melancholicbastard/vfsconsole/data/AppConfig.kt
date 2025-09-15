package com.melancholicbastard.vfsconsole.data

import com.melancholicbastard.vfsconsole.data.vfs.VFSDirectory
import com.melancholicbastard.vfsconsole.data.vfs.VFSLoader
import com.melancholicbastard.vfsconsole.model.AppParams
import java.io.File

// Для хранения глобальных настроек приложения
object AppConfig {
    lateinit var arguments: AppParams
        private set
    lateinit var execPath: String
        private set
    lateinit var workingDir: String
        private set
    var hasScript: Boolean = false
//     Загруженная виртуальная файловая система
    var vfsRoot: VFSDirectory? = null
        private set

    fun initialize(args: Array<String>) {
        arguments = AppParams.parse(args)

//         Получаем путь к исполняемому файлу эмулятора
        execPath = File(
            System.getProperty("java.class.path")?.split(File.pathSeparator)?.firstOrNull()
                ?: "unknown"
        ).absolutePath

//         Получаем текущую рабочую директорию
        workingDir = System.getProperty("user.dir") ?: "unknown"

        hasScript = arguments.scriptPath != null


        // Загружаем VFS, если указан путь
        arguments.vfsPath?.let { path ->
            try {
                vfsRoot = VFSLoader.loadFromFile(path)
                println("VFS loaded successfully from: $path")
            } catch (e: Exception) {
                System.err.println("Failed to load VFS: ${e.message}")
                vfsRoot = null
            }
        } ?: run {
            println("No VFS path provided. Running without VFS.")
            vfsRoot = null
        }
    }
}
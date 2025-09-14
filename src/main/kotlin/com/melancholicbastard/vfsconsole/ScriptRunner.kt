package com.melancholicbastard.vfsconsole

import java.io.File

// Для выполнения стартовых скриптов
class ScriptRunner(private val viewModel: TerminalViewModel) {

    fun runScript(scriptPath: String) {
        val scriptFile = File(scriptPath)
        if (!scriptFile.exists()) {
            viewModel.addOutput("ERROR: Script file not found: $scriptPath.")
            return
        }

//         Выводим информацию о местоположении
        viewModel.addOutput("=== VFS Shell Startup Information ===")
        viewModel.addOutput("VFS path: ${AppConfig.vfsPath}")
        viewModel.addOutput("Working directory: ${AppConfig.workingDir}")
        viewModel.addOutput("Script location: ${scriptFile.absolutePath}")
        viewModel.addOutput("======================================")
        viewModel.addOutput("")
        viewModel.addOutput("=== Executing script: ${scriptFile.name} ===")

        try {
            executeScriptLines(scriptFile)
        } catch (e: Exception) {
            viewModel.addOutput("ERROR: Failed to execute script: ${e.message}")
        }
    }

    private fun executeScriptLines(scriptFile: File) {
        scriptFile.readLines().forEachIndexed { lineNumber, line ->
            if (shouldExecuteLine(line)) {
//                 Выполняем команду
                executeScriptLine(line, lineNumber)
            }
        }
        viewModel.addOutput("=== Script executed successfully ===")
    }

    private fun shouldExecuteLine(line: String): Boolean {
        val trimmedLine = line.trim()
        return trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")
    }

    private fun executeScriptLine(line: String, lineNumber: Int) {
//         Имитируем ввод команды
        viewModel.addOutput("$ $line")
        val success = viewModel.executeScriptCommand(line)

        if (!success) {
            throw ScriptExecutionException("Command failed at line ${lineNumber + 1}: $line")
        }
    }

    class ScriptExecutionException(message: String) : RuntimeException(message)
}



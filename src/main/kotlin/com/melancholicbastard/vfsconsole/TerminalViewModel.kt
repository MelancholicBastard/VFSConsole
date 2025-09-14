package com.melancholicbastard.vfsconsole

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.melancholicbastard.vfsconsole.model.CommandResult

class TerminalViewModel {

//     Парсер команд
    private val parser = CommandParser()
//     Приватный изменяемый список строк вывода
    private val _outputLines = mutableStateListOf<String>()
//     Предоставляет список только для чтения
    val outputLines: List<String> = _outputLines
//     Текущее значение в поле ввода
    val currentInput = mutableStateOf("")

    fun addOutput(text: String) {
        _outputLines.add(text)
    }

//     Обновляет текущее значение в поле ввода
    fun onInputChange(newInput: String) {
        currentInput.value = newInput
    }

//     Обработка введенной строки ползователем
    fun onCommandEntered(onExit: () -> Unit) {
        val input = currentInput.value.trim()
        if (input.isEmpty()) return

        processUserInput(input, onExit)
    }

    private fun processUserInput(input: String, onExit: () -> Unit) {
        addOutput("$ $input")
        currentInput.value = ""

        processCommand(input, stopOnError = false, onExit = onExit)
    }

//     Обработка строки введенной скриптом
    fun executeScriptCommand(command: String): Boolean {
        return processCommand(command, stopOnError = true)
    }

//     Обрабатывает выполнение команды
    private fun processCommand(
        input: String,
        stopOnError: Boolean = false,
        onExit: (() -> Unit)? = null
    ): Boolean {
        try {
            val parts = parser.parse(input)
            if (parts.isEmpty()) return true

            val expandedParts = parser.expandVariables(parts)
            val command = expandedParts.first()             // Имя команды
            val args = expandedParts.drop(1)            // Аргументы команды

            val result = executeInternalCommand(command, args)

            when (result) {
                is CommandResult.Success -> {
                    addOutput(result.output)
                    return true
                }
                is CommandResult.Error -> {
                    addOutput("ERROR: ${result.message}")
                    if (stopOnError) throw RuntimeException("Command failed: $input")
                    return false
                }
                CommandResult.Exit -> {
                    onExit?.invoke() ?: addOutput("exit command ignored in script mode")
                    return true
                }
            }

        } catch (e: Exception) {
            return false
        }
    }

//     Выполнение команд с применением из аргументов
    private fun executeInternalCommand(command: String, args: List<String>): CommandResult {
        return when (command) {
            "ls" -> CommandResult.Success("ls called with args: ${args.joinToString()}")
            "cd" -> CommandResult.Success("cd called with args: ${args.joinToString()}")
            "exit" -> CommandResult.Exit
            else -> CommandResult.Error("Command not found: $command")
        }
    }
}
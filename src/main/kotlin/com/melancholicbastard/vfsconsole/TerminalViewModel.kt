package com.melancholicbastard.vfsconsole

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.melancholicbastard.vfsconsole.model.CommandResult

class TerminalViewModel {

    // Парсер команд
    private val parser = CommandParser()
    // Приватный изменяемый список строк вывода
    private val _outputLines = mutableStateListOf<String>()
    // Предоставляет список только для чтения
    val outputLines: List<String> = _outputLines
    // Текущее значение в поле ввода.
    val currentInput = mutableStateOf("")

    // Обработка введенной строки
    fun onCommandEntered(onExit: () -> Unit) {
        val input = currentInput.value.trim()
        if (input.isEmpty()) return

        // Добавляем ввод в историю
        _outputLines.add("$ $input")
        currentInput.value = ""

        try {
            // Парсим команду
            val parts = parser.parse(input)
            if (parts.isEmpty()) return

            // Раскрываем переменные
            val expandedParts = parser.expandVariables(parts)
            val command = expandedParts.first()
            val args = expandedParts.drop(1)

            // Выполняем команду
            val result = executeCommand(command, args)


            when (result) {
                is CommandResult.Success -> _outputLines.add(result.output)
                is CommandResult.Error -> _outputLines.add("ERROR: ${result.message}")
                CommandResult.Exit -> onExit()
            }

        } catch (e: Exception) {
            _outputLines.add("ERROR: ${e.message}")
        }
    }

    // Выполнение команд с применением из аргументов
    private fun executeCommand(command: String, args: List<String>): CommandResult {
        return when (command) {
            "ls" -> CommandResult.Success("ls called with args: ${args.joinToString()}")
            "cd" -> CommandResult.Success("cd called with args: ${args.joinToString()}")
            "exit" -> CommandResult.Exit
            else -> CommandResult.Error("Command not found: $command")
        }
    }

    // Обновляет текущее значение в поле ввода.
    fun onInputChange(newInput: String) {
        currentInput.value = newInput
    }
}
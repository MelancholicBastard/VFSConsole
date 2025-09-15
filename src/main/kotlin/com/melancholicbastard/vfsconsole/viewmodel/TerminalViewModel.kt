package com.melancholicbastard.vfsconsole.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.melancholicbastard.vfsconsole.CommandParser
import com.melancholicbastard.vfsconsole.VFSNavigator
import com.melancholicbastard.vfsconsole.VFSOperationResult
import com.melancholicbastard.vfsconsole.data.AppConfig
import com.melancholicbastard.vfsconsole.data.vfs.VFSDirectory
import com.melancholicbastard.vfsconsole.data.vfs.VFSFile
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
    // Навигатор по VFS
    private val vfsNavigator = VFSNavigator()

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
            "pwd" -> {
                if (AppConfig.vfsRoot == null) {
                    CommandResult.Success("VFS not loaded. OS PWD: ${AppConfig.workingDir}")
                } else {
                    CommandResult.Success(vfsNavigator.getCurrentPath())
                }
            }

            "ls" -> {
                val showDetails = args.contains("-l") || args.contains("-la") || args.contains("-al")

                when (val result = vfsNavigator.listDirectory(showDetails)) {
                    is VFSOperationResult.Success -> CommandResult.Success(result.data.joinToString("\n"))
                    is VFSOperationResult.Error -> CommandResult.Error(result.message)
                }
            }

            "cd" -> {
                if (args.isEmpty()) {
//                     cd без аргументов ведет в корень VFS
                    return when (val res = vfsNavigator.changeDirectory("/")) {
                        is VFSOperationResult.Success -> CommandResult.Success("")
                        is VFSOperationResult.Error -> CommandResult.Error(res.message)
                    }
                }

                val path = args.first()

                val result = if (path == "-") {
                    vfsNavigator.changeToPreviousDirectory()
                } else {
                    vfsNavigator.changeDirectory(path)
                }

                when (result) {
                    is VFSOperationResult.Success -> CommandResult.Success("")
                    is VFSOperationResult.Error -> CommandResult.Error(result.message)
                }
            }

            "cat" -> {
                if (args.isEmpty()) {
                    return CommandResult.Error("cat: missing file operand")
                }

                val filePath = args.first()
                val node = vfsNavigator.resolvePath(filePath)

                when (node) {
                    null -> CommandResult.Error("cat: $filePath: No such file or directory")
                    is VFSDirectory -> CommandResult.Error("cat: $filePath: Is a directory")
                    is VFSFile -> {
                        try {
//                             Пытаемся получить содержимое как текст
                            val textContent = node.getContentAsText()
                            CommandResult.Success(textContent)
                        } catch (e: IllegalStateException) {
//                             Файл, вероятно, бинарный. Выводим как Base64
                            addOutput("cat: $filePath: Displaying as Base64 (likely binary file)")
                            CommandResult.Success(node.content) // Выводим Base64
                        }
                    }
                }
            }

            "head" -> {
                if (args.isEmpty()) {
                    return CommandResult.Error("head: missing file operand")
                }

                var linesToShow = 10
                val filePath: String

                if (args.size >= 2 && args[0] == "-n") {
                    try {
                        linesToShow = args[1].toInt()
                        if (linesToShow < 0) {
                            return CommandResult.Error("head: invalid number of lines: '${args[1]}'")
                        }
//                        Если аргументов меньше положенного (нет пути)
                        filePath = args.drop(2).firstOrNull() ?: return CommandResult.Error("head: option requires an argument -- 'n'")
                    } catch (e: NumberFormatException) {
                        return CommandResult.Error("head: invalid number of lines: '${args[1]}'")
                    }
                } else {
//                     Первый аргумент - либо путь к файлу, либо опция
                    if (args[0].startsWith("-")) {
//                         Это -n без числа
                        if (args[0] == "-n") {
                            return CommandResult.Error("head: option requires an argument -- 'n'")
                        }
//                         Иначе неизвестная опция
                        return CommandResult.Error("head: invalid option -- '${args[0].removePrefix("-")}'")
                    } else {
//                         Первый аргумент - путь к файлу
                        filePath = args[0]
                    }
                }

                val node = vfsNavigator.resolvePath(filePath)

                return when (node) {
                    null -> CommandResult.Error("head: cannot open '$filePath' for reading: No such file or directory")
                    is VFSDirectory -> CommandResult.Error("head: error reading '$filePath': Is a directory")
                    is VFSFile -> {
                        if (node.isBinary) {
                            CommandResult.Error("head: error reading '$filePath': Is a binary file")
                        } else {
                            try {
                                val content = node.getContentAsText()
                                val lines = content.lines()
                                val headLines = lines.take(linesToShow)
                                CommandResult.Success(headLines.joinToString("\n"))
                            } catch (e: IllegalStateException) {
                                CommandResult.Error("head: error reading '$filePath': ${e.message}")
                            }
                        }
                    }
                }
            }

            "echo" -> {
//                 Простая реализация echo
                CommandResult.Success(args.joinToString(" "))
            }

            "mkdir" -> {
                if (args.isEmpty()) {
                    return CommandResult.Error("mkdir: missing operand")
                }
                val dirName = args.first()
//                Создаем в текущей директории
                when (val result = vfsNavigator.createDirectory(dirName)) {
                    is VFSOperationResult.Success -> CommandResult.Success("")
                    is VFSOperationResult.Error -> CommandResult.Error(result.message)
                }
            }

            "touch" -> {
                if (args.isEmpty()) {
                    return CommandResult.Error("touch: missing file operand")
                }
                val fileName = args.first()
//                Создаем в текущей директории
                when (val result = vfsNavigator.createFile(fileName)) {
                    is VFSOperationResult.Success -> CommandResult.Success("")
                    is VFSOperationResult.Error -> CommandResult.Error(result.message)
                }
            }

            "rm" -> {
                if (args.isEmpty()) {
                    return CommandResult.Error("rm: missing operand")
                }

                var recursive = false
                val targetName: String

                if (args.size >= 2 && (args[0] == "-r" || args[0] == "-R")) {
                    recursive = true
                    targetName = args[1]
                } else {
//                    Если команда начинается с флага
                    if (args[0].startsWith("-")) {
                        return CommandResult.Error("rm: invalid option -- '${args[0].substring(1)}'")
                    }
                    targetName = args[0]
                }

                when (val result = vfsNavigator.remove(targetName, recursive)) {
                    is VFSOperationResult.Success -> CommandResult.Success("")
                    is VFSOperationResult.Error -> CommandResult.Error(result.message)
                }
            }

            "exit" -> CommandResult.Exit

            else -> CommandResult.Error("Command not found: $command")
        }
    }
}
package com.melancholicbastard.vfsconsole

class CommandParser {

    fun parse(input: String): List<String> {
        // Разбиваем по регулярному выражению "\\s+" (один или более пробельных символов) и фильтруем пустые строки
        return input.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    }

//    Извлекает введенные аргументы и переменные окружения
    fun expandVariables(args: List<String>): List<String> {
        return args.map { arg ->
            if (arg.startsWith('$')) {
                val varName = arg.substring(1)
//                Получаем значение переменной окружения.
                System.getenv(varName) ?: arg // или оригинал, если такой переменной окружения не найдено
            } else {
                arg
            }
        }
    }
}
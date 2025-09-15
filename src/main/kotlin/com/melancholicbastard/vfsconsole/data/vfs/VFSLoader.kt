package com.melancholicbastard.vfsconsole.data.vfs

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.Base64
import kotlin.IllegalArgumentException

// singleton объект для Виртуальной файловой системы
object VFSLoader {

    fun loadFromFile(jsonFilePath: String): VFSDirectory {
        val file = File(jsonFilePath)
        if (!file.exists()) {
            throw VFSLoadException("VFS JSON file not found: $jsonFilePath")
        }

        try {
            val jsonString = file.readText(Charsets.UTF_8)
            val jsonElement = Json.parseToJsonElement(jsonString)
            val rootNode = parseNode(jsonElement, null) // Корень не имеет родителя
//            Если удается привести к типу Директория, то return, иначе ошибка
            return rootNode as? VFSDirectory ?: throw VFSLoadException("Root element must be a directory")
        } catch (e: Exception) {
            throw VFSLoadException("Failed to load or parse VFS from $jsonFilePath", e)
        }
    }

//    Рекурсивно парсит JSON-элемент в VFSNode
    private fun parseNode(element: JsonElement, parent: VFSDirectory?): VFSNode {
        if (element !is JsonObject) throw IllegalArgumentException("VFS node must be a JSON object" )

//        Проверяется имя и тип содержимого
        val name = element["name"]?.jsonPrimitive?.content
            ?: throw VFSLoadException("VFS node is missing 'name' field")
        val typeString = element["type"]?.jsonPrimitive?.content
            ?: throw VFSLoadException("VFS node '$name' is missing 'type' field")

//        Попытка привести тип содержимого к VFSNodeType
        val type = try {
            VFSNodeType.valueOf(typeString.uppercase())
        } catch (e: IllegalArgumentException) {
            throw VFSLoadException("Unknown type '$typeString' for VFS node '$name'")
        }

        return when (type) {
            VFSNodeType.FILE -> {
                val content = element["content"]?.jsonPrimitive?.content
                    ?: throw VFSLoadException("VFS file '$name' is missing 'content' field")
//                 Проверяем, указан ли флаг isBinary в JSON
                val isBinaryFromJson = element["isBinary"]?.jsonPrimitive?.booleanOrNull

                val isBinary = if (isBinaryFromJson != null) {
//                     Используем значение из JSON
                    isBinaryFromJson
                } else {
//                     Определяем самостоятельно
                    try {
                        isBinaryContent(content)
                    } catch (e: Exception) {
//                         Если не удалось определить, считаем текстовым
                        false
                    }
                }
//                Возвращает сериализованный файл VFS
                VFSFile(name, content, isBinary).apply { this.parent = parent } // Устанавливаем родителя
            }
            VFSNodeType.DIRECTORY -> {
                val dir = VFSDirectory(name) // Создаем директорию с родителем
                dir.parent = parent

                val childrenJson = element["children"]?.jsonArray ?: JsonArray(emptyList())
//                 Парсим детей и добавляем их в директорию, что автоматически установит родителя
                childrenJson.forEach { childElement ->
                    val childNode = parseNode(childElement, dir) // Рекурентно парсим ребенка с указанием на родителя
                    dir.addChild(childNode) // Добавим ссылку на ребенка
                }
                dir
            }
        }
    }

    private fun isBinaryContent(base64Content: String): Boolean {
        val contentBytes = Base64.getDecoder().decode(base64Content)
        val checkLength = minOf(contentBytes.size, 1024)
        for (i in 0 until checkLength) {
            val b = contentBytes[i]
            if (b == 0.toByte()) return true // Нулевой байт часто указывает на бинарные данные
            if (b < 32 && b != 9.toByte() && b != 10.toByte() && b != 13.toByte()) {
                return true // Непечатаемый символ
            }
        }
        return false
    }

//     Исключение, возникающее при ошибках загрузки VFS
    class VFSLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)
}


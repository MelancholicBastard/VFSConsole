package com.melancholicbastard.vfsconsole.data.vfs

import kotlinx.serialization.Serializable
import java.util.Base64

@Serializable
sealed class VFSNode { // Изменено на класс, чтобы добавить состояние
    abstract val name: String
    abstract val type: VFSNodeType
    var parent: VFSDirectory? = null

//    Получает абсолютный путь к этому элементу в VFS
    fun getAbsolutePath(): String {
//         Рекурсивно строим путь от корня к этому узлу
        if (parent == null) {
//             Это корневая директория
            return "/"
        }

        val pathParts = mutableListOf<String>()
        var current: VFSNode = this
        while (current.parent != null) { // Пока не дойдем до корня
            pathParts.add(0, current.name)
            current = current.parent!!
        }
//         Путь строится как /<1>/<2>/..
        return "/" + pathParts.joinToString("/")
    }
}

@Serializable
enum class VFSNodeType {
    FILE, DIRECTORY
}

// Представление файла в VFS
@Serializable
data class VFSFile(
    override val name: String,
    val content: String,
    val isBinary: Boolean = false
) : VFSNode() {
    override val type: VFSNodeType = VFSNodeType.FILE

//    Получает декодированное содержимое файла.
    fun getDecodedContent(): ByteArray {
        return Base64.getDecoder().decode(content)
    }

//    Получает содержимое файла как текст
    fun getContentAsText(): String {
        if (isBinary) {
            throw IllegalStateException("Cannot get text content from a binary file.")
        }

        return try {
            val decodedBytes = getDecodedContent()
            decodedBytes.toString(Charsets.UTF_8)
//             Если байты не являются валидным UTF-8, будет брошено исключение
        } catch (e: Exception) {
//            При некоректной обработке файла (файл скорее всего бинарный)
            throw IllegalStateException("Cannot decode file content as text. File might be binary. Base64: $content", e)
        }
    }
}

// Представление директории в VFS
@Serializable
data class VFSDirectory(
    override val name: String,
) : VFSNode() {
    val children: MutableList<VFSNode> = mutableListOf()
    override val type: VFSNodeType = VFSNodeType.DIRECTORY

    fun getChild(childName: String): VFSNode? {
        return children.find { it.name == childName }
    }

    fun addChild(child: VFSNode) {
        if (children.find { it == child } == null) {
            child.parent = this
            children.add(child)
        } else {
            System.err.println("Same file is already in the '$name' directory")
        }
    }

//        Удаление по имени
    fun removeChild(childName: String): Boolean {
//         Находим на всякий случай все элементы для удаления
        val childrenToRemove = children.filter { it.name == childName }

        if (childrenToRemove.isNotEmpty()) {
            children.removeAll(childrenToRemove)
//            Для сборщика мусора удаляем обратную ссылку
            childrenToRemove.forEach { it.parent = null }
            return true
        }
        return false
    }

//    Удаление по VFSNode
    fun removeChild(child: VFSNode): Boolean {
        val childrenToRemove = children.filter { it == child }

        if (childrenToRemove.isNotEmpty()) {
            children.removeAll(childrenToRemove)
//                Для сборщика мусора удаляем обратную ссылку
            childrenToRemove.forEach { it.parent = null }
            return true
        }
        return false
    }
}
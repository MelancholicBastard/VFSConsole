package com.melancholicbastard.vfsconsole

import com.melancholicbastard.vfsconsole.data.AppConfig
import com.melancholicbastard.vfsconsole.data.vfs.VFSDirectory
import com.melancholicbastard.vfsconsole.data.vfs.VFSFile
import com.melancholicbastard.vfsconsole.data.vfs.VFSNode
import com.melancholicbastard.vfsconsole.data.vfs.VFSNodeType


// Класс для навигации по виртуальной файловой системе
class VFSNavigator {


//     Для поддержки команды `cd -` (возврат к предыдущей директории)
    private var previousDirectory: VFSDirectory? = null

//    Текущая рабочая директория в VFS (Иначе инициализация фиктивного корня)
    var currentDirectory: VFSDirectory = AppConfig.vfsRoot ?: VFSDirectory("/").apply { this.parent = null }
        private set

    fun getCurrentPath(): String {
        return currentDirectory.getAbsolutePath()
    }

    fun changeDirectory(path: String): VFSOperationResult<String> {
        if (AppConfig.vfsRoot == null) {
            return VFSOperationResult.Error("VFS is not loaded.")
        }

        val targetDir = resolvePath(path)

        return when (targetDir) {
            is VFSDirectory -> {
//                 Сохраняем текущую директорию как предыдущую (для `cd -`)
                previousDirectory = currentDirectory
                currentDirectory = targetDir
                VFSOperationResult.Success(getCurrentPath())
            }
            is VFSFile -> VFSOperationResult.Error("'$path' is not a directory.")
            null -> VFSOperationResult.Error("Directory '$path' not found.")
        }
    }

    fun changeToPreviousDirectory(): VFSOperationResult<String> {
        if (AppConfig.vfsRoot == null) {
            return VFSOperationResult.Error("VFS is not loaded.")
        }

        val prevDir = previousDirectory
        return if (prevDir != null) {
//             Меняем местами current и previous
            val temp = currentDirectory
            currentDirectory = prevDir
            previousDirectory = temp
            VFSOperationResult.Success(getCurrentPath())
        } else {
            VFSOperationResult.Error("No previous directory.")
        }
    }

//    Рекурсивно ищет путь относительно текущей директории или от корня
    fun resolvePath(path: String): VFSNode? {
        val root = AppConfig.vfsRoot ?: return null

        return if (path.startsWith("/")) {
//             Абсолютный путь
            resolvePathFrom(root, path.removePrefix("/").split("/").filter { it.isNotEmpty() })
        } else {
//             Относительный путь
            resolvePathFrom(currentDirectory, path.split("/").filter { it.isNotEmpty() })
        }
    }

//    Рекурсивно ищет путь от заданной начальной директории
    private fun resolvePathFrom(startNode: VFSNode, pathComponents: List<String>): VFSNode? {
        if (pathComponents.isEmpty()) return startNode
        if (startNode !is VFSDirectory) return null

        val currentComponent = pathComponents.first()
        val remainingComponents = pathComponents.drop(1)

        return when (currentComponent) {
            "." -> resolvePathFrom(startNode, remainingComponents)
            ".." -> {
//                 Переход к родительской директории
                val parentDir = startNode.parent
                if (parentDir != null) {
                    if (remainingComponents.isEmpty()) {
                        return parentDir
                    } else {
                        return resolvePathFrom(parentDir, remainingComponents)
                    }
                } else {
//                     Мы уже в корне, '..' ведет в корень
                    if (remainingComponents.isEmpty()) {
//                         Остаемся в корне
                        return startNode
                    } else {
//                         Остается в корне, после этого спускается дальше по пути
                        return resolvePathFrom(startNode, remainingComponents)
                    }
                }
            }
            else -> {
                val child = startNode.getChild(currentComponent)
//                Если следующий искомый элемент не существует или последний
                if (child == null || remainingComponents.isEmpty()) {
                    child
                } else {
                    resolvePathFrom(child, remainingComponents)
                }
            }
        }
    }

//    Список содержимого текущей директории
    fun listDirectory(showDetails: Boolean = false): VFSOperationResult<List<String>> {
        if (AppConfig.vfsRoot == null) {
            return VFSOperationResult.Error("VFS is not loaded.")
        }

//         Убедимся, что работаем с актуальным корнем
        if (currentDirectory.parent == null && currentDirectory.name == "/" && AppConfig.vfsRoot != null) {
            currentDirectory = AppConfig.vfsRoot!!
        }

        val outputLines = mutableListOf<String>()

        if (showDetails) {
//             Заголовок для подробного вывода (имитация 'ls -l')
            outputLines.add("total ${currentDirectory.children.size}")
        }

        currentDirectory.children.forEach { node ->
            if (showDetails) {
//                 Формат: тип права кол-во_ссылок владелец группа размер месяц день время имя
//                 Пример: drwxr-xr-x 1 user group 4096 Apr 5 12:34 documents
//                 Пример: -rw-r--r-- 1 user group 1024 Apr 5 12:34 file.txt
                val typeAndPerms = when (node.type) {
                    VFSNodeType.DIRECTORY -> "drwxr-xr-x"
                    VFSNodeType.FILE -> "-rw-r--r--"
                }
                val owner = "user"
                val group = "group"
                val size = when (node.type) {
//                     Условный размер директории как в Unix-системах
                    VFSNodeType.DIRECTORY -> "4096"
                    VFSNodeType.FILE -> {
                        val file = node as VFSFile
                        try {
                            file.getDecodedContent().size.toString()
                        } catch (e: Exception) {
                            "0" // Если не удалось определить размер
                        }
                    }
                }
//                 Фиктивная дата
                val month = "Apr"
                val day = "5"
                val time = "12:34"

                outputLines.add("$typeAndPerms $owner $group $size $month $day $time ${node.name}")
            } else {
                outputLines.add(node.name)
            }
        }
        return VFSOperationResult.Success(outputLines)
    }

    fun createDirectory(dirName: String): VFSOperationResult<Unit> {
        if (AppConfig.vfsRoot == null) {
            return VFSOperationResult.Error("VFS is not loaded.")
        }

        // Проверка, существует ли уже элемент с таким именем
        if (currentDirectory.getChild(dirName) != null) {
            return VFSOperationResult.Error("mkdir: cannot create directory '$dirName': File exists")
        }

        val newDir = VFSDirectory(dirName)
        currentDirectory.addChild(newDir) // addChild установит parent
        return VFSOperationResult.Success(Unit)
    }

//    Создает новый пустой файл в текущей директории.
    fun createFile(fileName: String): VFSOperationResult<Unit> {
        if (AppConfig.vfsRoot == null) {
            return VFSOperationResult.Error("VFS is not loaded.")
        }

//         Если элемент уже существует, то ничего не делаем
        if (currentDirectory.getChild(fileName) != null) {
            return VFSOperationResult.Success(Unit)
        }

        try {
//             Создаем файл с пустым содержимым (пустая строка Base64)
            val newFile = VFSFile(fileName, "", false)
            currentDirectory.addChild(newFile) // addChild установит parent
            return VFSOperationResult.Success(Unit)
        } catch (e: Exception) {
            return VFSOperationResult.Error("touch: cannot create file '$fileName': ${e.message}")
        }
    }


//    Удаляет файл или директорию из текущей директории.
    fun remove(name: String, recursive: Boolean = false): VFSOperationResult<Unit> {
        if (AppConfig.vfsRoot == null) {
            return VFSOperationResult.Error("VFS is not loaded.")
        }

        val nodeToRemove = currentDirectory.getChild(name)

        return when (nodeToRemove) {
            null -> VFSOperationResult.Error("rm: cannot remove '$name': No such file or directory")
            is VFSFile -> {
                currentDirectory.removeChild(nodeToRemove)
                VFSOperationResult.Success(Unit)
            }
            is VFSDirectory -> {
                if (!recursive && nodeToRemove.children.isNotEmpty()) {
                    return VFSOperationResult.Error("rm: cannot remove '$name': Directory not empty. Use -r flag to remove recursively.")
                }
                // Рекурсивное удаление: удаляем всех потомков в памяти
                // Сборщик мусора отработает корректно, так как ссылки parent будут очищены
                currentDirectory.removeChild(nodeToRemove)
                VFSOperationResult.Success(Unit)
            }
        }
    }
}

// Результат операции с VFS
sealed class VFSOperationResult<T> {
    data class Success<T>(val data: T) : VFSOperationResult<T>()
    data class Error<T>(val message: String) : VFSOperationResult<T>()
}
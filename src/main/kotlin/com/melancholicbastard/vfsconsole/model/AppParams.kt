package com.melancholicbastard.vfsconsole.model

import kotlin.system.exitProcess

// Для хранения параметров командной строки
data class AppParams(
    val scriptPath: String? = null,
    val vfsPath: String? = null
) {
    companion object {
        fun parse(args: Array<String>): AppParams {
            var scriptPath: String? = null
            var vfsPath: String? = null

            args.forEachIndexed { index, arg ->
                when (arg) {
                    "--script", "-s" -> if (index + 1 < args.size) scriptPath = args[index + 1]
                    "--vfs-path", "-v"-> if (index + 1 < args.size) vfsPath = args[index + 1]
                    "--help", "-h" -> printHelpAndExit()
                }
            }

            return AppParams(scriptPath, vfsPath)
        }

//        При --help -h
        private fun printHelpAndExit() {
            println("""
                VFS Console - Usage:
                
                --vfs-path, -v <path>    Path to VFS JSON file
                --script, -s <path>      Path to startup script
                --help, -h               Show this help message
                
                Example:
                vfs-console --vfs-path /path/to/vfs.json --script /path/to/script.vfs
            """.trimIndent())
            exitProcess(0)
        }
    }
}
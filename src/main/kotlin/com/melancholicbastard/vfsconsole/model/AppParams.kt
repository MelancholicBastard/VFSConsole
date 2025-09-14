package com.melancholicbastard.vfsconsole.model

import kotlin.system.exitProcess

// Для хранения параметров командной строки
data class AppParams(
    val scriptPath: String? = null
) {
    companion object {
        fun parse(args: Array<String>): AppParams {
            var scriptPath: String? = null

            args.forEachIndexed { index, arg ->
                when (arg) {
                    "--script", "-s" -> if (index + 1 < args.size) scriptPath = args[index + 1]
                    "--help", "-h" -> printHelpAndExit()
                }
            }

            return AppParams(scriptPath)
        }

//        При --help -h
        private fun printHelpAndExit() {
            println("""
                VFS Console - Usage:
                
                --script, -s <path>      Path to startup script
                --help, -h               Show this help message
                
                Example:
                vfs-console --script /path/to/script.vsh
            """.trimIndent())
            exitProcess(0)
        }
    }
}
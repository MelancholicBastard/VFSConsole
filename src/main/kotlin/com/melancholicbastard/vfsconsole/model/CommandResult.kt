package com.melancholicbastard.vfsconsole.model

sealed class CommandResult {
    data class Success(val output: String) : CommandResult()
    data class Error(val message: String) : CommandResult()
    object Exit : CommandResult()
}
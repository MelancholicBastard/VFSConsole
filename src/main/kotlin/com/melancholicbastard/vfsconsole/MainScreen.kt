package com.melancholicbastard.vfsconsole

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp


@Composable
fun MainScreen(
    viewModel: TerminalViewModel,
    onExit: () -> Unit
) {
    val outputLines = viewModel.outputLines
    val currentInput by remember { viewModel.currentInput }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
//         История ввода
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            outputLines.forEach { line ->
                Text(
                    text = line,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

//         Поле ввода
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                modifier = Modifier.padding(end = 4.dp),
                fontFamily = FontFamily.Monospace
            )

            BasicTextField(
                value = currentInput,
                onValueChange = viewModel::onInputChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),      // Для работы кнопки ввода ("enter")
                keyboardActions = KeyboardActions( onDone = { viewModel.onCommandEntered(onExit) } ),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )
        }
    }
}
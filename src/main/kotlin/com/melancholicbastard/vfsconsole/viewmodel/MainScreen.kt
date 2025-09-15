package com.melancholicbastard.vfsconsole.viewmodel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val listState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
//         История ввода
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            items(outputLines) { line ->
                Text(
                    text = line,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

//         Поле ввода
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 8.dp)
                .background(Color.LightGray)
                .border(1.dp, Color.Black)
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, color = Color.Black)
            )
        }
    }
//     Эффект для автоматической прокрутки вниз при добавлении новых строк
    LaunchedEffect(outputLines.size) {
        if (outputLines.isNotEmpty()) {
            listState.scrollToItem(outputLines.size - 1)
        }
    }
}
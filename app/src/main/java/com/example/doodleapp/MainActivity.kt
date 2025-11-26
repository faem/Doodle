package com.example.doodleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.doodleapp.ui.theme.DoodleAppTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoodleAppTheme {
                DrawingScreen()
            }
        }
    }
}

@Composable
fun DrawingScreen() {
    val paths = remember { mutableStateListOf<Path>() }
    var forceRecompose by remember { mutableStateOf(0) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(true) {
                detectDragGestures(onDragStart = {
                    paths.add(Path().apply { moveTo(it.x, it.y) })
                }, onDrag = { change, _ ->
                    change.consume()
                    paths.last().lineTo(change.position.x, change.position.y)
                    forceRecompose++
                })
            }) {
        paths.forEach { path ->
            drawPath(
                path = path, color = Color.Black, style = Stroke(width = 5f)
            )
        }
    }
}
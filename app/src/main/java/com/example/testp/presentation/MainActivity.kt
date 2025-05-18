package com.example.testp.presentation

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay

fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(500)
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            PomodoroTimerScreen(this)
        }
    }
}

@Composable
fun PomodoroTimerScreen(context: Context) {
    var isWorkPhase by remember { mutableStateOf(true) }
    var timeLeft by remember { mutableIntStateOf(20) }
    var isRunning by remember { mutableStateOf(true) }
    var completedCycles by remember { mutableIntStateOf(0) }

    var workDuration by remember { mutableIntStateOf(20) }
    var breakDuration by remember { mutableIntStateOf(5) }

    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, timeLeft, isWorkPhase) {
        if (!showSettings && isRunning && timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else if (!showSettings && timeLeft == 0) {
            if (isWorkPhase) {
                completedCycles++
                vibrate(context)
                isWorkPhase = false
                timeLeft = breakDuration
            } else {
                isWorkPhase = true
                timeLeft = workDuration
            }
        }
    }

    if (showSettings) {
        SettingsMenu(
            workDuration = workDuration,
            breakDuration = breakDuration,
            onWorkChange = { workDuration = it },
            onBreakChange = { breakDuration = it },
            onBack = {
                timeLeft = if (isWorkPhase) workDuration else breakDuration
                showSettings = false
            }
        )
    } else {
        TimerScreen(
            context = context,
            isWorkPhase = isWorkPhase,
            timeLeft = timeLeft,
            isRunning = isRunning,
            completedCycles = completedCycles,
            onToggleRunning = { isRunning = !isRunning },
            onReset = {
                isRunning = false
                isWorkPhase = true
                timeLeft = workDuration
                completedCycles = 0
            },
            onOpenSettings = { showSettings = true }
        )
    }
}

@Composable
fun TimerScreen(
    context: Context,
    isWorkPhase: Boolean,
    timeLeft: Int,
    isRunning: Boolean,
    completedCycles: Int,
    onToggleRunning: () -> Unit,
    onReset: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val phaseText = if (isWorkPhase) "Work" else "Break"

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = phaseText, style = MaterialTheme.typography.body1)
        Text(text = "%02d:%02d".format(minutes, seconds), style = MaterialTheme.typography.display1)
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = onToggleRunning) {
                Text(if (isRunning) "Pause" else "Start")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onReset) {
                Text("Reset")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onOpenSettings) {
            Text("Settings")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Completed: $completedCycles")
    }
}

@Composable
fun SettingsMenu(
    workDuration: Int,
    breakDuration: Int,
    onWorkChange: (Int) -> Unit,
    onBreakChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Set Durations", style = MaterialTheme.typography.title1)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Work: $workDuration sec")
        Row {
            Button(onClick = { if (workDuration > 5) onWorkChange(workDuration - 5) }) {
                Text("-5")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Button(onClick = { onWorkChange(workDuration + 5) }) {
                Text("+5")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Break: $breakDuration sec")
        Row {
            Button(onClick = { if (breakDuration > 5) onBreakChange(breakDuration - 5) }) {
                Text("-5")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Button(onClick = { onBreakChange(breakDuration + 5) }) {
                Text("+5")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}


package com.hoyn.common.lib.logging

import android.content.Context
import android.os.Process
import android.util.Log
import java.io.File
import java.lang.Process as JavaProcess
import java.util.concurrent.atomic.AtomicBoolean

internal const val CAMERA_LOG_FILE_NAME = "camera_log.txt"

internal fun prepareFreshLogFile(
    directory: File,
    fileName: String = CAMERA_LOG_FILE_NAME
): File {
    if (!directory.exists()) {
        directory.mkdirs()
    }
    val logFile = File(directory, fileName)
    if (logFile.exists() && !logFile.delete()) {
        logFile.writeText("")
    }
    return logFile
}

internal fun runtimeLogcatCommand(pid: Int): List<String> {
    return listOf("logcat", "--pid=$pid", "-v", "threadtime")
}

internal fun resolveRuntimeLogDirectory(context: Context): File {
    return context.getExternalFilesDir(null) ?: context.filesDir
}

internal fun resolveCurrentLogFilePath(logFile: File?): String? {
    return logFile?.absolutePath
}

object AppRuntimeLogCapture {
    private const val TAG = "AppRuntimeLogCapture"

    private val started = AtomicBoolean(false)

    @Volatile
    private var captureProcess: JavaProcess? = null

    @Volatile
    private var captureThread: Thread? = null

    @Volatile
    private var currentLogFile: File? = null

    fun start(context: Context): File? {
        if (!started.compareAndSet(false, true)) {
            return currentLogFile
        }
        val logFile = try {
            prepareFreshLogFile(resolveRuntimeLogDirectory(context.applicationContext))
        } catch (e: Exception) {
            started.set(false)
            currentLogFile = null
            Log.e(TAG, "Failed to prepare log file", e)
            return null
        }

        return try {
            val process = ProcessBuilder(runtimeLogcatCommand(Process.myPid()))
                .redirectErrorStream(true)
                .start()
            captureProcess = process
            val thread = Thread({
                process.inputStream.bufferedReader().use { reader ->
                    logFile.bufferedWriter().use { writer ->
                        var pendingLines = 0
                        while (!Thread.currentThread().isInterrupted) {
                            val line = reader.readLine() ?: break
                            writer.appendLine(line)
                            pendingLines += 1
                            if (pendingLines >= 32) {
                                writer.flush()
                                pendingLines = 0
                            }
                        }
                        writer.flush()
                    }
                }
            }, "CameraLogCapture")
            thread.isDaemon = true
            captureThread = thread
            currentLogFile = logFile
            thread.start()
            logFile
        } catch (e: Exception) {
            started.set(false)
            captureProcess = null
            captureThread = null
            currentLogFile = null
            Log.e(TAG, "Failed to start runtime log capture", e)
            null
        }
    }

    fun currentLogFilePath(): String? {
        return resolveCurrentLogFilePath(currentLogFile)
    }

    fun stop() {
        captureThread?.interrupt()
        captureThread = null
        captureProcess?.destroy()
        captureProcess = null
        currentLogFile = null
        started.set(false)
    }
}
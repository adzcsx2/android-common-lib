package com.hoyn.common.lib.logging

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class AppRuntimeLogCaptureTest {

    @Test
    fun `current log file path is null when file missing`() {
        assertEquals(null, resolveCurrentLogFilePath(logFile = null))
    }

    @Test
    fun `prepare fresh log file deletes previous contents`() {
        val directory = Files.createTempDirectory("camera-log").toFile()
        val existingFile = File(directory, CAMERA_LOG_FILE_NAME)
        existingFile.writeText("old logs")

        val resolvedFile = prepareFreshLogFile(directory)

        assertEquals(existingFile.absolutePath, resolvedFile.absolutePath)
        assertFalse(resolvedFile.exists())
        directory.deleteRecursively()
    }

    @Test
    fun `prepare fresh log file creates directory when missing`() {
        val rootDirectory = Files.createTempDirectory("camera-log-root").toFile()
        val directory = File(rootDirectory, "nested")

        val resolvedFile = prepareFreshLogFile(directory)

        assertTrue(directory.exists())
        assertFalse(resolvedFile.exists())
        rootDirectory.deleteRecursively()
    }

    @Test
    fun `runtime logcat command scopes output to current pid`() {
        val command = runtimeLogcatCommand(pid = 4321)

        assertEquals(listOf("logcat", "--pid=4321", "-v", "threadtime"), command)
    }

    @Test
    fun `current log file path returns absolute path`() {
        val directory = Files.createTempDirectory("camera-log-path").toFile()
        val file = File(directory, CAMERA_LOG_FILE_NAME)

        assertEquals(file.absolutePath, resolveCurrentLogFilePath(file))

        directory.deleteRecursively()
    }
}
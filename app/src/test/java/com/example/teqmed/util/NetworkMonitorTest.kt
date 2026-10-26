package com.example.teqmed.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeNetworkMonitor(initialOnline: Boolean = true) : NetworkMonitor {
    private val _isOnline = MutableStateFlow(initialOnline)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
}

class NetworkMonitorTest {

    @Test
    fun fakeNetworkMonitor_iniciaConEstadoConfigurado() {
        val monitorOnline = FakeNetworkMonitor(initialOnline = true)
        assertTrue(monitorOnline.isOnline.value)

        val monitorOffline = FakeNetworkMonitor(initialOnline = false)
        assertFalse(monitorOffline.isOnline.value)
    }

    @Test
    fun fakeNetworkMonitor_cambiaEstadoCorrectamente() {
        val monitor = FakeNetworkMonitor(initialOnline = false)
        assertFalse(monitor.isOnline.value)

        monitor.setOnline(true)
        assertTrue(monitor.isOnline.value)

        monitor.setOnline(false)
        assertFalse(monitor.isOnline.value)
    }
}

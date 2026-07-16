package com.lamba.app.chat

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceRecordingStateTest {

    @Test
    fun tapRequestsPermissionWhenMicrophonePermissionIsMissing() {
        val state = VoiceRecordingState()

        assertEquals(VoiceRecordingAction.REQUEST_PERMISSION, state.onMicrophoneTap(hasPermission = false))
        assertEquals(VoiceRecordingPhase.IDLE, state.phase)
    }

    @Test
    fun permittedTapsStartThenStopRecording() {
        val state = VoiceRecordingState()

        assertEquals(VoiceRecordingAction.START_RECORDING, state.onMicrophoneTap(hasPermission = true))
        assertEquals(VoiceRecordingPhase.RECORDING, state.phase)
        assertEquals(VoiceRecordingAction.STOP_AND_TRANSCRIBE, state.onMicrophoneTap(hasPermission = true))
        assertEquals(VoiceRecordingPhase.TRANSCRIBING, state.phase)
    }

    @Test
    fun completionRestoresIdleState() {
        val state = VoiceRecordingState()
        state.onMicrophoneTap(hasPermission = true)
        state.onMicrophoneTap(hasPermission = true)

        state.onTranscriptionFinished()

        assertEquals(VoiceRecordingPhase.IDLE, state.phase)
    }
}

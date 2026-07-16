package com.lamba.app.chat

enum class VoiceRecordingPhase {
    IDLE,
    RECORDING,
    TRANSCRIBING,
}

enum class VoiceRecordingAction {
    REQUEST_PERMISSION,
    START_RECORDING,
    STOP_AND_TRANSCRIBE,
    NONE,
}

class VoiceRecordingState {
    var phase: VoiceRecordingPhase = VoiceRecordingPhase.IDLE
        private set

    fun onMicrophoneTap(hasPermission: Boolean): VoiceRecordingAction {
        if (!hasPermission) return VoiceRecordingAction.REQUEST_PERMISSION

        return when (phase) {
            VoiceRecordingPhase.IDLE -> {
                phase = VoiceRecordingPhase.RECORDING
                VoiceRecordingAction.START_RECORDING
            }

            VoiceRecordingPhase.RECORDING -> {
                phase = VoiceRecordingPhase.TRANSCRIBING
                VoiceRecordingAction.STOP_AND_TRANSCRIBE
            }

            VoiceRecordingPhase.TRANSCRIBING -> VoiceRecordingAction.NONE
        }
    }

    fun onTranscriptionFinished() {
        phase = VoiceRecordingPhase.IDLE
    }
}

package com.systematics.monetization.ui.state

sealed interface DialogState {

    object None : DialogState
    data class Show(val type: String) : DialogState
}
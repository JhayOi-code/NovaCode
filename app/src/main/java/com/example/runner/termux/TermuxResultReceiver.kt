package com.example.runner.termux

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Data bundle returned by Termux RunCommandService when execution completes.
 */
data class TermuxExecutionResult(
    val executionId: String,
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val errCode: Int = 0,
    val errmsg: String? = null
)

/**
 * BroadcastReceiver registered in AndroidManifest.xml to receive asynchronous completion callbacks
 * from Termux via PendingIntent.
 */
class TermuxResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val executionId = intent.getStringExtra(EXTRA_EXECUTION_ID) ?: intent.action ?: "unknown"
        val stdout = intent.getStringExtra("stdout") ?: ""
        val stderr = intent.getStringExtra("stderr") ?: ""
        val exitCode = intent.getIntExtra("exitCode", 0)
        val errCode = intent.getIntExtra("errCode", 0)
        val errmsg = intent.getStringExtra("errmsg")

        val result = TermuxExecutionResult(
            executionId = executionId,
            stdout = stdout,
            stderr = stderr,
            exitCode = exitCode,
            errCode = errCode,
            errmsg = errmsg
        )
        _resultsFlow.tryEmit(result)
    }

    companion object {
        const val ACTION_TERMUX_RESULT = "com.example.TERMUX_RESULT"
        const val EXTRA_EXECUTION_ID = "execution_id"

        private val _resultsFlow = MutableSharedFlow<TermuxExecutionResult>(extraBufferCapacity = 64)
        val resultsFlow = _resultsFlow.asSharedFlow()

        fun postSyntheticResult(result: TermuxExecutionResult) {
            _resultsFlow.tryEmit(result)
        }
    }
}

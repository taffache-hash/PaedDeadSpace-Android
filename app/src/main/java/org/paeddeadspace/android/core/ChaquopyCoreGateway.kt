package org.paeddeadspace.android.core

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


interface CoreGateway {
    suspend fun calculate(request: CalculationRequest): CoreResponse
}

class ChaquopyCoreGateway(context: Context) : CoreGateway {
    private val appContext = context.applicationContext

    override suspend fun calculate(request: CalculationRequest): CoreResponse =
        withContext(Dispatchers.Default) {
            try {
                ensurePythonStarted()
                val requestJson = CalculationRequestCodec.encode(request)
                val responseJson = Python.getInstance()
                    .getModule("android_bridge")
                    .callAttr("compute_case", requestJson)
                    .toString()
                CoreResponseCodec.decode(responseJson)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                    Log.e(LOG_TAG, "Core bridge failure", failure)
                }
                CoreResponse.InputError("Calculation could not be completed.")
            }
        }

    private fun ensurePythonStarted() {
        if (!Python.isStarted()) {
            synchronized(Python::class.java) {
                if (!Python.isStarted()) {
                    Python.start(AndroidPlatform(appContext))
                }
            }
        }
    }

    private companion object {
        const val LOG_TAG = "PaedDeadSpaceCore"
    }
}

object CalculationRequestCodec {
    private val json = Json {
        encodeDefaults = true
        explicitNulls = true
    }

    fun encode(request: CalculationRequest): String = json.encodeToString(request)
}

object CoreResponseCodec {
    private val json = Json {
        ignoreUnknownKeys = false
        explicitNulls = true
    }

    fun decode(responseJson: String): CoreResponse {
        val root = json.parseToJsonElement(responseJson).jsonObject
        return when (root.getValue("status").jsonPrimitive.content) {
            "success" -> CoreResponse.Success(
                json.decodeFromString(root.getValue("result").toString()),
            )
            "input_error" -> CoreResponse.InputError(
                root.getValue("message").jsonPrimitive.content,
            )
            "model_breakdown" -> CoreResponse.ModelBreakdown(
                message = root.getValue("message").jsonPrimitive.content,
                partial = root["partial"]
                    ?.takeUnless { it is JsonNull }
                    ?.let { json.decodeFromString(it.toString()) },
                warnings = root["warnings"]
                    ?.jsonArray
                    ?.map { it.jsonPrimitive.content }
                    .orEmpty(),
            )
            else -> error("Unsupported Core bridge status")
        }
    }
}

package com.example.financial_chat

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class ChatModel(private val modelPath: String) {

    private lateinit var interpreter: Interpreter
    private lateinit var inputBuffer: ByteBuffer
    private lateinit var outputBuffer: ByteBuffer
    private val maxInputLength = 1024  // 최대 입력 길이
    private val maxOutputLength = 512 // 최대 출력 길이
    private val inputSize = 512       // 모델의 입력 크기

    // 모델 로드
    fun loadModel(context: Context) {
        try {
            val assetFileDescriptor = context.assets.openFd(modelPath)
            val inputStream = assetFileDescriptor.createInputStream()
            val modelBuffer = ByteBuffer.allocateDirect(assetFileDescriptor.declaredLength.toInt())
            inputStream.read(modelBuffer.array())
            modelBuffer.order(ByteOrder.nativeOrder())
            inputStream.close()

            interpreter = Interpreter(modelBuffer)
            Log.d("ChatModel", "Model loaded successfully.")
        } catch (e: Exception) {
            Log.e("ChatModel", "Error loading model: ${e.message}")
            throw RuntimeException("모델 로드 실패: ${e.message}")
        }

        // 입력 및 출력 버퍼 초기화
        inputBuffer = ByteBuffer.allocateDirect(maxInputLength * 4).apply {
            order(ByteOrder.nativeOrder())
        }
        outputBuffer = ByteBuffer.allocateDirect(maxOutputLength * 4).apply {
            order(ByteOrder.nativeOrder())
        }
    }

    // 입력 텍스트 처리
    private fun preprocessInput(inputText: String): ByteBuffer {
        inputBuffer.clear()
        val inputBytes = inputText.toByteArray(StandardCharsets.UTF_8)

        // 입력 데이터를 ByteBuffer로 변환
        for (i in inputBytes.indices) {
            if (i >= inputSize) break // 최대 입력 크기를 초과하지 않도록 제한
            inputBuffer.putInt(inputBytes[i].toInt())
        }

        // 패딩 처리
        for (i in inputBytes.size until inputSize) {
            inputBuffer.putInt(0)
        }

        return inputBuffer
    }

    // 출력 데이터 후처리
    private fun postprocessOutput(outputBuffer: ByteBuffer): String {
        outputBuffer.rewind()
        val outputBytes = ByteArray(maxOutputLength)
        for (i in outputBytes.indices) {
            val value = outputBuffer.get().toInt()
            if (value == 0) break // 패딩 값(0)에서 종료
            outputBytes[i] = value.toByte()
        }
        return String(outputBytes, StandardCharsets.UTF_8).trim()
    }

    // 예측 수행
    fun predict(inputText: String): String {
        val preprocessedInput = preprocessInput(inputText)

        // 출력 버퍼 초기화
        outputBuffer.clear()

        // 모델 실행
        interpreter.run(preprocessedInput, outputBuffer)

        // 후처리하여 출력 결과 반환
        return postprocessOutput(outputBuffer)
    }

    // Function Calling 처리
    fun processFunctionCall(inputText: String): String {
        val response = predict(inputText)

        // Function Calling 여부 확인
        if (response.contains("Function Call:")) {
            val functionName = extractFunctionName(response)
            val parameters = extractFunctionParameters(response)

            // Function Calling 수행
            val result = FunctionCalling.callFunction(functionName, parameters)
            return "Function Result: $result"
        }

        // Function Calling이 아닌 경우 모델 응답 그대로 반환
        return response
    }

    // Function Name 추출
    private fun extractFunctionName(response: String): String {
        val regex = Regex("Function Call: ([a-zA-Z_]+)")
        val matchResult = regex.find(response)
        return matchResult?.groupValues?.get(1) ?: throw IllegalArgumentException("Function name not found in response.")
    }

    // Function Parameters 추출
    private fun extractFunctionParameters(response: String): Map<String, Any> {
        val regex = Regex("Parameters: \\{(.*?)}")
        val matchResult = regex.find(response)
        val paramString = matchResult?.groupValues?.get(1) ?: return emptyMap()

        return paramString.split(",").map { param ->
            val (key, value) = param.split(":").map { it.trim() }
            key to parseParameterValue(value)
        }.toMap()
    }

    // Parameter 값 파싱
    private fun parseParameterValue(value: String): Any {
        return when {
            value.startsWith("\"") && value.endsWith("\"") -> value.trim('"')
            value.contains(".") -> value.toDoubleOrNull() ?: value
            else -> value.toIntOrNull() ?: value
        }
    }
}

/*
class ChatModel(private val assetFileName: String) {
    private lateinit var interpreter: Interpreter

    // 모델 로드
    fun loadModel(context: Context) {
        val assetManager = context.assets
        val modelInputStream = assetManager.open(assetFileName)
        val modelBuffer = modelInputStream.use { it.readBytes() }.let {
            ByteBuffer.allocateDirect(it.size).apply {
                order(ByteOrder.nativeOrder())
                put(it)
            }
        }

        interpreter = Interpreter(modelBuffer)
    }

    // 예측 수행
    fun predict(inputText: String): String {
        // 입력 데이터 전처리
        val inputTensor = preprocess(inputText)

        // 모델의 출력 크기 정의 (예: [1, 100])
        val outputTensor = Array(1) { FloatArray(100) }

        // 모델 실행
        interpreter.run(inputTensor, outputTensor)

        // 출력 데이터를 후처리하여 문자열로 변환
        return postprocess(outputTensor[0])
    }

    // 입력 데이터 전처리 (텍스트를 숫자로 변환)
    private fun preprocess(text: String): Array<FloatArray> {
        // 텍스트를 ASCII 값으로 변환 (사용자 모델에 맞게 수정)
        val maxLength = 100 // 입력 시퀀스 최대 길이
        val input = FloatArray(maxLength) { 0f }
        text.map { it.code.toFloat() }.forEachIndexed { index, value ->
            if (index < maxLength) input[index] = value
        }
        return arrayOf(input)
    }

    // 출력 데이터 후처리 (숫자를 텍스트로 변환)
    private fun postprocess(data: FloatArray): String {
        // 출력 데이터를 토큰 ID로 변환 후 문자열로 디코딩
        // 여기서는 간단히 FloatArray를 문자열로 변환
        return data.joinToString(" ") { it.toString() }
    }
}
*/

/*
import android.content.Context
import org.pytorch.Module
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
//import org.pytorch.Tensor // PyTorch 모델을 사용할 경우
import org.tensorflow.lite.Tensor // TensorFlow Lite 모델을 사용할 경우

class ChatModel(private val assetFileName: String) {
    private lateinit var model: Module

    // 모델 로드
    fun loadModel(context: Context) {
        model = LiteModuleLoader.load(assetFileName) // Lite Interpreter로 모델 로드
    }

    // 예측 수행
    fun predict(inputText: String): String {
        // 입력 텍스트를 텐서로 변환 (사용자 모델에 따라 맞춤화 필요)
        val inputTensor = Tensor.fromBlob(
            preprocess(inputText),
            longArrayOf(1, inputText.length.toLong()) // 입력 크기 설정
        )

        // 모델 실행
        val outputTensor = model.forward(IValue.from(inputTensor)).toTensor()
        val outputData = outputTensor.dataAsFloatArray

        // 출력 데이터를 문자열로 변환
        return postprocess(outputData)
    }

    // 예: 입력 전처리 (필요 시 수정)
    private fun preprocess(text: String): FloatArray {
        // 텍스트를 숫자 데이터로 변환 (예: ASCII 코드, 토큰 ID 등)
        return text.map { it.code.toFloat() }.toFloatArray()
    }

    // 예: 출력 후처리 (필요 시 수정)
    private fun postprocess(data: FloatArray): String {
        // 출력 데이터를 문자열로 변환 (예: 토큰 ID 디코딩)
        return data.joinToString(" ")
    }
}
 */
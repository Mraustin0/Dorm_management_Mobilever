import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object ClaudeApi {

    private val client = OkHttpClient()
    private const val API_KEY = "sk-ant-xxxxxxxx" // ใส่ key ของคุณ

    fun ask(message: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val body = JSONObject().apply {
            put("model", "claude-sonnet-4-6")
            put("max_tokens", 1024)
            put("messages", JSONArray().put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", message)
                }
            ))
        }

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", API_KEY)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(RequestBody.create("application/json".toMediaType(), body.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e.message ?: "Unknown error")
            }

            override fun onResponse(call: Call, response: Response) {
                val text = JSONObject(response.body!!.string())
                    .getJSONArray("content")
                    .getJSONObject(0)
                    .getString("text")
                onResult(text)
            }
        })
    }
}
package com.orca.app.auth

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log

/**
 * Invisible activity that captures OAuth redirect URLs (http://localhost:54545/callback).
 *
 * When the OAuth provider (e.g. Anthropic) redirects to localhost, Android would normally
 * show a "Choose activity" dialog. This activity's intent filter in AndroidManifest catches
 * the redirect URL instead, extracts the code/state parameters, and forwards them to the
 * OAuthCallbackServer via a local HTTP request.
 *
 * The activity finishes immediately (Theme.NoDisplay) — the user never sees it.
 *
 * It is `exported`, so the intent is attacker-reachable: any installed app can
 * start it with a crafted URI, and so can any web page via an `http://localhost:…`
 * link. Two consequences are handled here:
 *
 *  - The destination is rebuilt from [ALLOWED_ENDPOINTS] rather than from the
 *    incoming URI, so a caller cannot steer the forwarded request at some other
 *    loopback port (the debug RPC server included) by supplying its own.
 *  - The `code` and `state` values never reach logcat. Any app holding
 *    READ_LOGS, and anyone with adb, could otherwise lift a live authorization
 *    code straight out of the log.
 *
 * Injecting a foreign `code` is stopped downstream: OAuthManager requires the
 * `state` echoed back to match the one it generated for this login.
 */
class OAuthRedirectActivity : Activity() {

    companion object {
        private const val TAG = "OAuthRedirect"

        /**
         * Port → path pairs, mirroring this activity's intent filters in
         * AndroidManifest. Anything else is dropped.
         */
        private val ALLOWED_ENDPOINTS = mapOf(
            54545 to "/callback",      // Claude
            1455 to "/auth/callback",  // OpenAI
            8085 to "/oauth2callback", // Gemini
            3000 to "/callback",       // OpenRouter
            3001 to "/callback",       // OpenRouter fallback
            3002 to "/callback",       // OpenRouter fallback
        )

        private val LOOPBACK_HOSTS = setOf("localhost", "127.0.0.1", "::1")

        /**
         * True when [uri] is one of the loopback callbacks this activity exists
         * to serve. Visible for test.
         */
        internal fun isAllowedRedirect(uri: Uri): Boolean {
            if (!uri.scheme.equals("http", ignoreCase = true)) return false
            if (uri.host?.lowercase() !in LOOPBACK_HOSTS) return false
            return ALLOWED_ENDPOINTS[uri.port] == uri.path
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (uri == null) {
            Log.w(TAG, "OAuth redirect with no data URI")
            finish()
            return
        }

        if (!isAllowedRedirect(uri)) {
            Log.w(TAG, "Dropping OAuth redirect for unexpected endpoint: ${uri.host}:${uri.port}${uri.path}")
            finish()
            return
        }

        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")

        if (code.isNullOrEmpty() || state.isNullOrEmpty()) {
            Log.w(TAG, "Dropping OAuth redirect: missing code or state")
            finish()
            return
        }

        Log.i(TAG, "OAuth redirect accepted for port ${uri.port}")

        // Rebuild the loopback URL from the validated endpoint and re-encode the
        // two parameters we actually use, instead of replaying the raw query.
        val forwardUrl = Uri.Builder()
            .scheme("http")
            .encodedAuthority("127.0.0.1:${uri.port}")
            .path(uri.path)
            .appendQueryParameter("code", code)
            .appendQueryParameter("state", state)
            .build()
            .toString()

        Thread {
            try {
                val conn = java.net.URL(forwardUrl).openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.requestMethod = "GET"
                Log.i(TAG, "Local server response: ${conn.responseCode}")
                conn.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to forward to local server", e)
            }
        }.start()

        finish()
    }
}

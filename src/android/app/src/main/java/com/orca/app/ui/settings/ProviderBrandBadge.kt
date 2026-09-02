package com.orca.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orca.app.R

/** Vendor badge backed by the Lobe Icons static SVG assets. */
@Composable
internal fun ProviderBrandBadge(
    providerId: String,
    displayName: String,
    modifier: Modifier = Modifier,
) {
    val logo = providerLogoResource(providerId)
    if (logo != null) {
        Box(
            modifier = modifier.size(30.dp).background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(logo),
                contentDescription = displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(24.dp),
            )
        }
        return
    }
    val (mark, color) = providerBrand(providerId, displayName)
    Box(
        modifier = modifier.size(30.dp).background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = mark,
            color = Color.White,
            fontSize = if (mark.length > 2) 8.sp else 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

private fun providerLogoResource(id: String): Int? = when (id) {
    "openai" -> R.drawable.provider_openai
    "google", "gemini" -> R.drawable.provider_google
    "anthropic" -> R.drawable.provider_anthropic
    "deepseek" -> R.drawable.provider_deepseek
    "meta" -> R.drawable.provider_meta
    "qwen" -> R.drawable.provider_qwen
    "xai" -> R.drawable.provider_xai
    "openrouter" -> R.drawable.provider_openrouter
    "azure" -> R.drawable.provider_azure
    "volcengine" -> R.drawable.provider_volcengine
    "tencent", "tencentcloud" -> R.drawable.provider_tencent
    "wenxin", "baidu" -> R.drawable.provider_wenxin
    "bedrock", "aws" -> R.drawable.provider_bedrock
    "github", "githubcopilot" -> R.drawable.provider_github
    "zhipu" -> R.drawable.provider_zhipu
    "moonshot", "kimicode" -> R.drawable.provider_moonshot
    "nvidia" -> R.drawable.provider_nvidia
    "minimax" -> R.drawable.provider_minimax
    "xiaomi-mimo", "xiaomimimo" -> R.drawable.provider_xiaomi_mimo
    "ollama" -> R.drawable.provider_ollama
    "poolside" -> R.drawable.provider_poolside
    "inception" -> R.drawable.provider_inception
    else -> null
}

private fun providerBrand(id: String, name: String): Pair<String, Color> = when (id) {
    "openai" -> "AI" to Color(0xFF111111)
    "google", "gemini" -> "G" to Color(0xFF4285F4)
    "anthropic" -> "A" to Color(0xFFD97757)
    "deepseek" -> "DS" to Color(0xFF536DFE)
    "meta" -> "M" to Color(0xFF1877F2)
    "qwen" -> "Q" to Color(0xFF5B4BDB)
    "xai" -> "X" to Color(0xFF111111)
    "openrouter" -> "OR" to Color(0xFF00A7B5)
    "azure" -> "Az" to Color(0xFF0078D4)
    "volcengine" -> "火" to Color(0xFFE53935)
    "tencent" -> "TX" to Color(0xFF2F66D0)
    "wenxin" -> "文" to Color(0xFF315EF4)
    "bedrock" -> "B" to Color(0xFFFF9900)
    "github" -> "GH" to Color(0xFF24292F)
    "zhipu" -> "Z" to Color(0xFF6C3BFF)
    "moonshot", "kimicode" -> "K" to Color(0xFF202124)
    "nvidia" -> "N" to Color(0xFF76B900)
    "minimax" -> "M" to Color(0xFFFF3D71)
    "xiaomi-mimo" -> "Mi" to Color(0xFFFF6900)
    "ollama" -> "O" to Color(0xFF555555)
    "poolside" -> "P" to Color(0xFF607D8B)
    "inception" -> "I" to Color(0xFF7E57C2)
    else -> name.take(2).uppercase() to Color(0xFF607D8B)
}

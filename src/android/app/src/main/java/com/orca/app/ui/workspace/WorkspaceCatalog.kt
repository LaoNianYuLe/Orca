package com.orca.app.ui.workspace

data class CopywritingTemplate(
    val title: String,
    val description: String,
    val prompt: String,
)

val copywritingTemplates = listOf(
    CopywritingTemplate("短视频文案", "开场、节奏和行动引导", "帮我写一条短视频文案，主题是："),
    CopywritingTemplate("社交媒体文案", "适合朋友圈和社交平台发布", "帮我写一条社交媒体文案，内容是："),
    CopywritingTemplate("产品介绍", "把产品特点说清楚、说动人", "帮我写一段产品介绍，产品信息是："),
    CopywritingTemplate("文章提纲", "快速搭建文章结构和论点", "帮我列一份文章提纲，主题是："),
)

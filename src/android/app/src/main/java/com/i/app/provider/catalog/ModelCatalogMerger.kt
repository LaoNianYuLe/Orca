package com.i.app.provider.catalog

/** Merges provider model sources without allowing refreshes to erase user data. */
object ModelCatalogMerger {
    fun merge(
        builtin: List<ModelSpec>,
        existing: List<ModelSpec>,
        remote: List<ModelSpec>,
    ): List<ModelSpec> {
        val result = linkedMapOf<String, ModelSpec>()
        fun put(model: ModelSpec) {
            val key = "${model.providerId}\u0000${model.id}"
            val previous = result[key]
            result[key] = when {
                previous == null -> model
                previous.source == ModelSource.CUSTOM -> previous
                model.source == ModelSource.CUSTOM -> model
                else -> model.copy(
                    contextWindowTokens = model.contextWindowTokens ?: previous.contextWindowTokens,
                    maxOutputTokens = model.maxOutputTokens ?: previous.maxOutputTokens,
                    releasedAt = model.releasedAt ?: previous.releasedAt,
                    knowledgeCutoff = model.knowledgeCutoff ?: previous.knowledgeCutoff,
                )
            }
        }

        builtin.forEach(::put)
        existing.forEach(::put)
        remote.forEach(::put)
        return result.values.toList()
    }
}

# Orca Provider and Model Architecture Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild Orca's Android provider and model management around the confirmed LobeHub-style structure for 22 ordered providers, while preserving existing chat, encrypted credentials, model groups, and navigation.

**Architecture:** Add a Kotlin provider catalog with one specification per provider, one model catalog per provider, and protocol-level runtime adapters. Keep `ProviderRepository` as the persistence boundary, extend the existing provider database instead of replacing it, and let the settings flow expose `Provider List → Provider Detail → Config | Models`. Use shared OpenAI-compatible, Anthropic-compatible, Gemini, and Ollama paths, with provider-specific rules only where the official API requires them.

**Tech Stack:** Kotlin, Jetpack Compose, Room, existing `ProviderRepository`, existing `ProviderFactory`, existing encrypted preference/database layer, existing HTTP/JSON stack, Android unit tests.

---

## Fixed provider scope and display order

The built-in catalog must contain exactly these 22 display entries in this order:

```text
OpenAI
Google
Anthropic
DeepSeek
Meta
阿里云 / 通义千问
xAI
OpenRouter
Microsoft Azure
字节跳动 / 火山引擎
腾讯云 / 混元
百度 / 文心 / 千帆
Amazon Bedrock
GitHub Models / Copilot
智谱 Z.ai
月之暗面 Moonshot
NVIDIA
MiniMax
小米 MiMo
Ollama
Poolside
Inception
```

Slash-separated names are one display entry. Their internal connection routes may differ when authentication or protocol differs.

### Task 1: Establish provider specifications and exact ordering

**Files:**
- Create: `src/android/app/src/main/java/com/i/app/provider/catalog/ProviderCatalog.kt`
- Create: `src/android/app/src/main/java/com/i/app/provider/catalog/ProviderSpec.kt`
- Modify: `src/android/app/src/main/java/com/i/app/provider/ProviderFactory.kt`
- Test: `src/android/app/src/test/java/com/i/app/provider/catalog/ProviderCatalogTest.kt`

- [ ] **Step 1: Write the failing catalog tests**

Test that the catalog contains 22 unique stable IDs, that `displayOrder` is contiguous from 1 to 22, and that the ordered display names exactly match the fixed list above. Test that OpenAI, Anthropic, Gemini, Ollama, Poolside, and Inception map to the expected protocol/auth categories.

- [ ] **Step 2: Run the catalog test and verify the expected failure**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests com.i.app.provider.catalog.ProviderCatalogTest
```

Expected result: the new test fails because the catalog types and entries do not yet exist.

- [ ] **Step 3: Implement the catalog**

Define `ProviderProtocol` values for `OPENAI_COMPATIBLE`, `ANTHROPIC`, `GEMINI`, `OLLAMA`, `AZURE_OPENAI`, `BEDROCK`, `GITHUB_COPILOT`, and `UNVERIFIED`. Define `ProviderAuth` values for `API_KEY`, `OAUTH`, `AWS`, and `LOCAL`. Add each provider as a `ProviderSpec` with stable ID, display name, order, default base URL when verified, protocol, auth, model-fetch support, manual-model support, and test-model placeholder.

- [ ] **Step 4: Make the factory resolve protocol through the catalog**

Add a catalog lookup before the existing provider-type switch. Preserve current custom-provider behavior and route existing working providers through their current implementations until their shared adapter is covered by later tasks.

- [ ] **Step 5: Run the focused test and commit**

Run the catalog test again and commit:

```bash
git add src/android/app/src/main/java/com/i/app/provider/catalog src/android/app/src/main/java/com/i/app/provider/ProviderFactory.kt src/android/app/src/test/java/com/i/app/provider/catalog/ProviderCatalogTest.kt
git commit -m "feat: add ordered Orca provider catalog"
```

### Task 2: Normalize model metadata and merge sources

**Files:**
- Create: `src/android/app/src/main/java/com/i/app/provider/catalog/ModelSpec.kt`
- Modify: `src/android/app/src/main/java/com/i/app/data/model/LLMModel.kt`
- Modify: `src/android/app/src/main/java/com/i/app/provider/ModelsDevApi.kt`
- Modify: `src/android/app/src/main/java/com/i/app/data/repository/ProviderRepository.kt`
- Test: `src/android/app/src/test/java/com/i/app/provider/catalog/ModelCatalogMergeTest.kt`

- [ ] **Step 1: Write failing merge tests**

Cover builtin, remote, and custom model sources. Assert that a remote refresh updates an existing remote model, preserves a user-created model with the same ID, deduplicates by provider ID plus model ID, and retains context length and capability metadata when the remote response omits those fields.

- [ ] **Step 2: Verify the tests fail**

Run the single test class with Gradle and confirm failure is caused by the missing normalized merge behavior rather than test setup errors.

- [ ] **Step 3: Implement the normalized model specification**

Add model type, family, generation, context window, maximum output, pricing, release date, knowledge cutoff, and capability fields for vision, files, function call, reasoning, search, audio, image output, and video. Keep the current `LLMModel` compatibility fields and provide conversion functions rather than changing every chat consumer at once.

- [ ] **Step 4: Implement source-aware merging in the repository**

Use provider ID plus model ID as the stable key. Apply builtin metadata first, remote metadata second, and custom fields last. Never delete or overwrite custom entries during a refresh. Keep the existing disk cache and Room persistence paths.

- [ ] **Step 5: Run tests and commit**

Run the focused merge tests and commit the normalized metadata layer.

### Task 3: Build the shared runtime adapter boundary

**Files:**
- Create: `src/android/app/src/main/java/com/i/app/provider/runtime/ProviderRuntime.kt`
- Create: `src/android/app/src/main/java/com/i/app/provider/runtime/OpenAICompatibleRuntime.kt`
- Create: `src/android/app/src/main/java/com/i/app/provider/runtime/AnthropicRuntime.kt`
- Create: `src/android/app/src/main/java/com/i/app/provider/runtime/GeminiRuntime.kt`
- Create: `src/android/app/src/main/java/com/i/app/provider/runtime/OllamaRuntime.kt`
- Modify: `src/android/app/src/main/java/com/i/app/provider/ProviderFactory.kt`
- Modify: existing provider-specific runtime files only where required
- Test: `src/android/app/src/test/java/com/i/app/provider/runtime/ProviderRuntimeRoutingTest.kt`

- [ ] **Step 1: Write failing routing tests**

Assert that each of the 22 catalog entries resolves to a runtime category and that OpenAI-compatible providers do not create duplicate protocol implementations. Do not treat StealthGPT as a normal model provider: it is a separate text-transformation integration if it is ever added.

- [ ] **Step 2: Verify routing tests fail**

Run the focused test class and confirm missing runtime resolution is the expected failure.

- [ ] **Step 3: Define the adapter interface**

The interface must expose chat streaming, connection testing, model listing, request transformation, response transformation, and provider-specific error normalization. Keep credentials in the existing repository/factory inputs and never place secrets in the catalog.

- [ ] **Step 4: Route existing implementations through the boundary**

Wrap current OpenAI, Anthropic, Gemini, xAI, OpenRouter, and model-fetch implementations. Add common OpenAI-compatible behavior for providers whose official API is OpenAI-compatible. Keep special handling for Azure, Bedrock, GitHub Copilot, and Ollama explicit.

- [ ] **Step 5: Add verified handling for Poolside and Inception**

Register the three providers in the UI and persistence layer. Enable a real runtime only after an official API base URL, auth method, model endpoint, and request format are verified. Until then, show configuration-required status and allow manual model entries without making network calls to an invented endpoint.

- [ ] **Step 6: Run focused tests and commit**

Run the routing tests and existing provider unit tests, then commit the adapter boundary.

### Task 4: Implement provider model fetching and persistence

**Files:**
- Modify: `src/android/app/src/main/java/com/i/app/provider/ModelsDevApi.kt`
- Modify: `src/android/app/src/main/java/com/i/app/provider/openai/OpenAIModelsApi.kt`
- Modify: `src/android/app/src/main/java/com/i/app/provider/anthropic/AnthropicModelsApi.kt`
- Modify: `src/android/app/src/main/java/com/i/app/provider/gemini/GeminiModelsApi.kt`
- Modify: `src/android/app/src/main/java/com/i/app/provider/openrouter/OpenRouterModelsApi.kt`
- Modify: `src/android/app/src/main/java/com/i/app/data/repository/ProviderRepository.kt`
- Test: existing provider model API tests plus `src/android/app/src/test/java/com/i/app/provider/ProviderModelRefreshTest.kt`

- [ ] **Step 1: Write failing refresh behavior tests**

Test success, empty response, unauthorized response, malformed response, cached fallback, and custom-model preservation. Verify that a provider without a verified fetcher remains usable through manual model entry.

- [ ] **Step 2: Verify red tests**

Run the focused refresh tests and confirm the new cache/source behavior is absent.

- [ ] **Step 3: Implement provider model fetch contracts**

Use the provider specification to select the endpoint and parser. Normalize all successful responses into `ModelSpec`; filter embeddings or unrelated model types from chat selection while preserving them for future modality support.

- [ ] **Step 4: Persist and expose refresh state**

Store last refresh time, source, count, and last error without storing API keys in model records. Preserve the current encrypted credential storage and provider model cache.

- [ ] **Step 5: Run focused refresh and existing provider tests, then commit**

Commit the refresh and persistence changes after the tests pass.

### Task 5: Rebuild the LobeHub-style provider settings UI

**Files:**
- Modify: `src/android/app/src/main/java/com/i/app/ui/settings/ProviderListScreen.kt`
- Modify: `src/android/app/src/main/java/com/i/app/ui/settings/ProviderDetailScreen.kt`
- Create or modify: `src/android/app/src/main/java/com/i/app/ui/settings/ProviderConfigTab.kt`
- Create or modify: `src/android/app/src/main/java/com/i/app/ui/settings/ProviderModelsTab.kt`
- Create or modify: `src/android/app/src/main/java/com/i/app/ui/settings/TestModelPickerSheet.kt`
- Modify: settings navigation file that owns provider detail routes
- Test: Compose UI tests for provider list, tabs, model switches, and back navigation

- [ ] **Step 1: Write failing UI tests**

Assert the 22 providers render in the fixed order; provider detail shows the top provider switch; Config and Models tabs switch without losing state; Models shows search, available count, fetch button, enabled count, capability badges, and row switches; Config shows API key, proxy URL, test model selector, and test button; the test model selector opens as a bottom sheet.

- [ ] **Step 2: Verify the UI tests fail**

Run the targeted Compose tests and confirm missing labels or controls fail as expected.

- [ ] **Step 3: Implement the provider list**

Render catalog order from `ProviderCatalog`, use Orca branding and generic/provider-approved icons, show enabled state, configured state, and model count. Do not copy LobeHub visual assets.

- [ ] **Step 4: Implement provider detail tabs**

Keep the provider switch at the top. Config contains masked API key input, proxy/base URL, auth status, test model field, test button, and encryption note. Models contains search, refresh, counts, model rows, capability chips, context length, and per-model switches.

- [ ] **Step 5: Implement test model bottom sheet and back behavior**

The sheet lists only the current provider's available models. Closing the sheet returns to Config; Android back from Models/Config returns to provider list, and back from provider list returns to Settings. Preserve unsaved input according to the existing settings state model.

- [ ] **Step 6: Run UI tests and commit**

Run targeted Compose tests and commit the settings UI.

### Task 6: Connect enabled models to chat selection

**Files:**
- Modify: `src/android/app/src/main/java/com/i/app/ui/components/UnifiedModelPickerSheet.kt`
- Modify: `src/android/app/src/main/java/com/i/app/ui/chat/ChatModelPickerSheet.kt`
- Modify: `src/android/app/src/main/java/com/i/app/data/repository/ProviderRepository.kt`
- Test: model picker tests covering provider grouping and enabled state

- [ ] **Step 1: Write failing picker tests**

Assert the chat picker includes only enabled models, groups them by provider, preserves provider order, displays provider and model names, and removes a model immediately after it is disabled in settings.

- [ ] **Step 2: Verify red tests**

Run the focused picker tests and confirm the current flat/legacy behavior fails the new grouping assertions.

- [ ] **Step 3: Implement the enabled-model projection**

Expose a single repository projection for chat selection. Keep model groups and scheduled tasks compatible by resolving the same stable provider/model IDs.

- [ ] **Step 4: Run picker and repository tests, then commit**

Commit the chat integration.

### Task 7: Regression verification and documentation

**Files:**
- Modify: `README.md`
- Modify: `BUILDING.md`
- Modify: `CONTRIBUTING.md`
- Modify: `docs/superpowers/specs/2026-09-01-orca-provider-model-architecture-design.md` only if implementation decisions change

- [ ] **Step 1: Run all targeted provider, repository, and Compose tests**

Run the complete focused test set after all tasks and record any pre-existing unrelated failures separately.

- [ ] **Step 2: Build the debug APK**

Run:

```bash
./gradlew :app:assembleDebug
```

Confirm the APK is produced at `src/android/app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 3: Perform manual verification**

Verify first launch, settings navigation, provider list order, provider switch, Config/Models tabs, model refresh with and without credentials, model enable/disable, test-model sheet, theme switching, chat model picker, and existing conversation sending.

- [ ] **Step 4: Scan for secrets and stale branding**

Confirm no API keys are committed, no LobeHub logo or copied UI assets were added, and user-facing provider/config text consistently uses Orca branding.

- [ ] **Step 5: Update documentation and commit**

Document the 22 built-in providers, required user-owned credentials, the exclusion of StealthGPT from the normal model-provider list, and the fact that model availability depends on each provider. Commit the final documentation and verification notes.

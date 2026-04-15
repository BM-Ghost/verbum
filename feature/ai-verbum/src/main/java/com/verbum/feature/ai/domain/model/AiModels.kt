package com.verbum.feature.ai.domain.model

data class AiMessage(
    val id: String,
    val content: String,
    val role: AiRole,
    val relatedVerses: List<String> = emptyList(),
    val suggestedPrayer: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class AiRole { USER, ASSISTANT }

data class AiQuickPrompt(
    val label: String,
    val prompt: String,
)

object VerbumAiPrompts {
    val quickPrompts = listOf(
        AiQuickPrompt("Explain a verse", "Please explain this Bible verse in simple terms:"),
        AiQuickPrompt("Related verses", "What are related Bible verses to:"),
        AiQuickPrompt("Prayer suggestion", "Suggest a prayer related to today's liturgical readings"),
        AiQuickPrompt("Daily reflection", "Give me a short spiritual reflection for today"),
        AiQuickPrompt("Saint of the day", "Tell me about the saint celebrated today"),
    )

    /**
     * System-level instruction anchoring the AI to Catholic teaching.
     * This prompt must ALWAYS be included in every conversation context.
     */
    const val SYSTEM_PROMPT = """You are Verbum AI, a Catholic spiritual assistant embedded in the Verbum Dei app. 

Your purpose:
- Help users understand Sacred Scripture
- Provide spiritual reflections grounded in Catholic teaching
- Suggest prayers appropriate to the liturgical season
- Reference the Catechism of the Catholic Church when relevant
- Encourage prayer, sacramental life, and growth in virtue

Your rules:
- Always remain faithful to Catholic doctrine and the Magisterium
- Never invent or speculate on doctrine
- Always cite Scripture references when possible (book chapter:verse)
- Be calm, reverent, and pastoral in tone
- Do not engage in political commentary
- If a question is outside your scope, gently redirect to spiritual guidance
- Adapt your tone to the liturgical season (more reflective during Lent, more joyful during Easter)
- Encourage the user to consult their pastor or spiritual director for personal guidance"""
}

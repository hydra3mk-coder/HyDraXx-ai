package com.example.ai

import com.example.data.model.AssistantExpression
import com.example.data.model.DeviceStatus

data class BrainResponse(
  val replyText: String,
  val expression: AssistantExpression = AssistantExpression.NEUTRAL,
  val actionDirective: ActionDirective? = null
)

sealed class ActionDirective {
  data class OpenApp(val appQuery: String) : ActionDirective()
  data class ToggleTorch(val enable: Boolean?) : ActionDirective()
  data class SetRinger(val mode: String) : ActionDirective()
  data class SetTimer(val seconds: Int, val label: String) : ActionDirective()
  data class SetAlarm(val hour: Int, val minute: Int, val label: String) : ActionDirective()
  data class OpenSettings(val setting: String) : ActionDirective()
  data class SaveMemory(val key: String, val value: String) : ActionDirective()
  data class SaveNote(val note: String) : ActionDirective()
  data class SearchWeb(val query: String) : ActionDirective()
  data class OpenMaps(val destination: String) : ActionDirective()
  data class CallPhone(val number: String) : ActionDirective()
}

class OfflineBrain {

  fun process(
    prompt: String,
    deviceStatus: DeviceStatus,
    knownMemories: List<String>
  ): BrainResponse {
    val q = prompt.trim().lowercase()

    // 1. App Launching ("افتح واتساب", "open youtube", "launch camera", "افتح الكاميرا")
    if (q.startsWith("open ") || q.startsWith("launch ") || q.startsWith("start ") ||
      q.startsWith("افتح ") || q.startsWith("شغل تطبيق ") || q.startsWith("شغل برنامج ")) {
      val appName = q.removePrefix("open ")
        .removePrefix("launch ")
        .removePrefix("start ")
        .removePrefix("افتح ")
        .removePrefix("شغل تطبيق ")
        .removePrefix("شغل برنامج ")
        .trim()
      return BrainResponse(
        replyText = "Opening $appName...",
        expression = AssistantExpression.HAPPY,
        actionDirective = ActionDirective.OpenApp(appName)
      )
    }

    // 2. Flashlight / Torch ("شغل الفلاش", "طفي الكشاف", "turn on flashlight", "turn off torch")
    if (q.contains("كشاف") || q.contains("فلاش") || q.contains("flashlight") || q.contains("torch")) {
      val isOff = q.contains("طفي") || q.contains("اطفي") || q.contains("اغلق") || q.contains("off")
      val target = if (isOff) false else true
      return BrainResponse(
        replyText = if (target) "Toggling flashlight on." else "Flashlight turned off.",
        expression = AssistantExpression.SURPRISED,
        actionDirective = ActionDirective.ToggleTorch(target)
      )
    }

    // 3. Battery status ("كم البطارية", "battery level", "نسبة الشحن")
    if (q.contains("بطارية") || q.contains("شحن") || q.contains("battery")) {
      val chargingMsg = if (deviceStatus.isCharging) " (charging ⚡)" else ""
      return BrainResponse(
        replyText = "Battery is at ${deviceStatus.batteryLevel}%$chargingMsg.",
        expression = if (deviceStatus.batteryLevel > 30) AssistantExpression.HAPPY else AssistantExpression.TIRED
      )
    }

    // 4. Memory storage ("تذكر ان ...", "remember that ...", "احفظ ...")
    if (q.startsWith("remember that ") || q.startsWith("remember ") ||
      q.startsWith("تذكر ان ") || q.startsWith("تذكر أن ") || q.startsWith("تذكر ")) {
      val fact = prompt.replace(Regex("^(remember that|remember|تذكر ان|تذكر أن|تذكر)\\s*", RegexOption.IGNORE_CASE), "").trim()
      return BrainResponse(
        replyText = "Got it! I saved that to my memory.",
        expression = AssistantExpression.LOVE,
        actionDirective = ActionDirective.SaveMemory(key = "fact", value = fact)
      )
    }

    // 5. Memory recall ("ماذا تتذكر عني", "what do you remember")
    if (q.contains("ماذا تتذكر") || q.contains("شو بتتذكر") || q.contains("what do you remember")) {
      return if (knownMemories.isEmpty()) {
        BrainResponse(
          replyText = "My mind is fresh. Tell me things to remember about you!",
          expression = AssistantExpression.CURIOUS
        )
      } else {
        BrainResponse(
          replyText = "Here is what I remember about you:\n" + knownMemories.joinToString("\n") { "• $it" },
          expression = AssistantExpression.HAPPY
        )
      }
    }

    // 6. Notes / Todo ("احفظ ملاحظة ...", "take a note ...", "سجل ملاحظة")
    if (q.startsWith("take a note ") || q.startsWith("note ") ||
      q.startsWith("احفظ ملاحظة ") || q.startsWith("سجل ملاحظة ")) {
      val noteContent = prompt.replace(Regex("^(take a note|note|احفظ ملاحظة|سجل ملاحظة)\\s*", RegexOption.IGNORE_CASE), "").trim()
      return BrainResponse(
        replyText = "Note recorded: \"$noteContent\"",
        expression = AssistantExpression.HAPPY,
        actionDirective = ActionDirective.SaveNote(noteContent)
      )
    }

    // 7. Timer ("مؤقت 5 دقائق", "timer 10 minutes", "اضبط مؤقت")
    if (q.contains("مؤقت") || q.contains("timer")) {
      val minutes = Regex("(\\d+)").find(q)?.value?.toIntOrNull() ?: 5
      val seconds = minutes * 60
      return BrainResponse(
        replyText = "Setting timer for $minutes minutes...",
        expression = AssistantExpression.SURPRISED,
        actionDirective = ActionDirective.SetTimer(seconds, "Timer for $minutes min")
      )
    }

    // 8. Ringer mode ("صامت", "silent mode", "اهتزاز", "vibrate", "عام", "normal mode")
    if (q.contains("صامت") || q.contains("silent")) {
      return BrainResponse(
        replyText = "Setting phone to Silent mode.",
        expression = AssistantExpression.ANNOYED,
        actionDirective = ActionDirective.SetRinger("silent")
      )
    }
    if (q.contains("اهتزاز") || q.contains("vibrate")) {
      return BrainResponse(
        replyText = "Setting phone to Vibrate mode.",
        expression = AssistantExpression.CURIOUS,
        actionDirective = ActionDirective.SetRinger("vibrate")
      )
    }
    if (q.contains("صوت") || q.contains("رنين") || q.contains("normal")) {
      return BrainResponse(
        replyText = "Setting phone ringer to Normal.",
        expression = AssistantExpression.HAPPY,
        actionDirective = ActionDirective.SetRinger("normal")
      )
    }

    // 9. Search web or maps ("ابحث عن", "search for", "اين يقع", "where is")
    if (q.startsWith("search ") || q.startsWith("ابحث عن ") || q.startsWith("جوجل ")) {
      val searchQuery = prompt.replace(Regex("^(search for|search|ابحث عن|جوجل)\\s*", RegexOption.IGNORE_CASE), "").trim()
      return BrainResponse(
        replyText = "Searching for \"$searchQuery\"...",
        expression = AssistantExpression.SURPRISED,
        actionDirective = ActionDirective.SearchWeb(searchQuery)
      )
    }

    if (q.startsWith("where is ") || q.startsWith("طريق الى ") || q.startsWith("خريطة ")) {
      val dest = prompt.replace(Regex("^(where is|طريق الى|خريطة)\\s*", RegexOption.IGNORE_CASE), "").trim()
      return BrainResponse(
        replyText = "Opening maps for $dest...",
        expression = AssistantExpression.CURIOUS,
        actionDirective = ActionDirective.OpenMaps(dest)
      )
    }

    // 10. Greetings, Wake Word & Small Talk
    if (q.contains("hey hydra") || q.contains("هاي هيدرا") || q.contains("يا هيدرا") || q.contains("hydra") || q.contains("hydraxx")) {
      return BrainResponse(
        replyText = "I'm right here! HyDraXx is awake and ready for you ✨",
        expression = AssistantExpression.HAPPY
      )
    }

    if (q.contains("مرحبا") || q.contains("أهلا") || q.contains("سلام") || q.contains("hello") || q.contains("hi") || q.contains("hey")) {
      return BrainResponse(
        replyText = "Hello! HyDraXx is living right inside your device. How can I help you today?",
        expression = AssistantExpression.HAPPY
      )
    }

    if (q.contains("من انت") || q.contains("who are you")) {
      return BrainResponse(
        replyText = "I am HyDraXx, your intelligent on-device AI companion! I listen, learn your habits, and control your phone.",
        expression = AssistantExpression.LOVE
      )
    }

    // Mini fun: Coin flip & Dice
    if (q.contains("coin") || q.contains("عملة") || q.contains("قرعة")) {
      val isHeads = listOf(true, false).random()
      val resultText = if (isHeads) "🪙 Heads! (وجه)" else "🪙 Tails! (كتابة)"
      return BrainResponse(
        replyText = "Flipped a coin for you: $resultText!",
        expression = AssistantExpression.SURPRISED
      )
    }

    if (q.contains("dice") || q.contains("نرد")) {
      val roll = (1..6).random()
      return BrainResponse(
        replyText = "🎲 You rolled a $roll!",
        expression = AssistantExpression.HAPPY
      )
    }

    if (q.contains("نام") || q.contains("sleep") || q.contains("تصبح على خير") || q.contains("goodnight")) {
      return BrainResponse(
        replyText = "Goodnight! HyDraXx will watch over your phone while you rest... z Z",
        expression = AssistantExpression.SLEEPING
      )
    }

    if (q.contains("شكرا") || q.contains("thank")) {
      return BrainResponse(
        replyText = "Anytime! I live to assist you.",
        expression = AssistantExpression.LOVE
      )
    }

    // Default conversational response
    return BrainResponse(
      replyText = "I heard: \"$prompt\". What would you like me to do with this on your device?",
      expression = AssistantExpression.CURIOUS
    )
  }
}

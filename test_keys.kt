import java.io.File
import java.util.regex.Pattern

fun main() {
    val dir = File("server/package/input/KoColor")
    val allFiles = dir.walkTopDown().filter { it.extension == "json" && !it.name.contains("notes") }.toList()
    
    val macroCategories = mutableMapOf<String, Int>()
    
    for (file in allFiles) {
        val content = file.readText()
        val matcher = Pattern.compile("\"macro_category\"\\s*:\\s*\"([^\"]+)\"").matcher(content)
        if (matcher.find()) {
            val macro = matcher.group(1)
            macroCategories[macro] = (macroCategories[macro] ?: 0) + 1
        }
    }
    
    println("Actual JSON Macros Count:")
    macroCategories.forEach { (k, v) -> println("$k = $v") }
}

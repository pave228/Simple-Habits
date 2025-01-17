import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalDateAdapter : JsonSerializer<Date>, JsonDeserializer<Date> {
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun serialize(src: Date, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(formatter.format(src)) // Преобразование Date в строку
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Date {
        return formatter.parse(json.asString) ?: throw JsonParseException("Invalid date format")
    }
}

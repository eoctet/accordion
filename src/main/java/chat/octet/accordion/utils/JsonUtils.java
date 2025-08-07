package chat.octet.accordion.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TimeZone;

/**
 * Utility class for JSON serialization and deserialization operations.
 *
 * <p>JsonUtils provides a centralized, thread-safe way to handle JSON operations
 * throughout the Accordion framework. It uses Jackson ObjectMapper with optimized
 * configuration for the framework's needs, including proper time zone handling
 * and Java 8 time support.</p>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li><strong>Thread Safety</strong>: Uses a shared, thread-safe ObjectMapper instance</li>
 *   <li><strong>Time Support</strong>: Configured with JavaTimeModule for Java 8+ time types</li>
 *   <li><strong>Error Handling</strong>: Graceful error handling with logging</li>
 *   <li><strong>Type Safety</strong>: Generic methods for type-safe conversions</li>
 *   <li><strong>Collection Support</strong>: Specialized methods for maps and lists</li>
 * </ul>
 *
 * <p>Configuration:</p>
 * <ul>
 *   <li>Uses system default time zone</li>
 *   <li>Supports Java 8 time types (LocalDateTime, Instant, etc.)</li>
 *   <li>Preserves insertion order for maps and lists</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 *
 * <p><strong>Object Serialization:</strong></p>
 * <pre>{@code
 * AccordionConfig config = new AccordionConfig(...);
 * String json = JsonUtils.toJson(config);
 * }</pre>
 *
 * <p><strong>Object Deserialization:</strong></p>
 * <pre>{@code
 * String json = "{ ... }";
 * AccordionConfig config = JsonUtils.parseToObject(json, AccordionConfig.class);
 * }</pre>
 *
 * <p><strong>Map Operations:</strong></p>
 * <pre>{@code
 * String json = "{ \"key1\": \"value1\", \"key2\": \"value2\" }";
 * LinkedHashMap<String, String> map = JsonUtils.parseJsonToMap(json, String.class, String.class);
 * }</pre>
 *
 * <p><strong>List Operations:</strong></p>
 * <pre>{@code
 * String json = "[\"item1\", \"item2\", \"item3\"]";
 * LinkedList<String> list = JsonUtils.parseJsonToList(json, String.class);
 * }</pre>
 *
 * <p>Error Handling: All methods handle exceptions gracefully, logging errors
 * and returning null for failed operations. Callers should check for null
 * return values and handle them appropriately.</p>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see ObjectMapper
 * @see JavaTimeModule
 * @since 1.0.0
 */
@Slf4j
public class JsonUtils {

    /**
     * Shared ObjectMapper instance configured for Accordion framework use.
     * Thread-safe and optimized for the framework's JSON processing needs.
     */
    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();

    static {
        JACKSON_MAPPER.setTimeZone(TimeZone.getDefault());
        JACKSON_MAPPER.registerModule(new JavaTimeModule());
    }

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private JsonUtils() {
    }

    /**
     * Parses a JSON string into an object of the specified type.
     *
     * <p>This method deserializes JSON content into a Java object using
     * Jackson's ObjectMapper. It handles null and empty JSON strings
     * gracefully by returning null.</p>
     *
     * <p>Error Handling:</p>
     * <ul>
     *   <li>Returns null for null or blank JSON strings</li>
     *   <li>Logs parsing errors and returns null on failure</li>
     *   <li>Handles malformed JSON gracefully</li>
     * </ul>
     *
     * @param <T>   the target type for deserialization
     * @param json  the JSON string to parse, may be null or empty
     * @param clazz the target class type, may be null
     * @return the deserialized object, or null if parsing fails or input is invalid
     * @since 1.0.0
     */
    public static <T> T parseToObject(String json, @Nullable Class<T> clazz) {
        try {
            if (StringUtils.isNotBlank(json)) {
                return JACKSON_MAPPER.readValue(json, clazz);
            }
        } catch (Exception ex) {
            log.error("Parse JSON to Object error", ex);
        }
        return null;
    }

    /**
     * Serializes an object to its JSON string representation.
     *
     * <p>This method converts a Java object into a JSON string using
     * Jackson's ObjectMapper. It handles complex object graphs including
     * nested objects, collections, and Java 8 time types.</p>
     *
     * <p>Supported Types:</p>
     * <ul>
     *   <li>POJOs with standard getters/setters</li>
     *   <li>Collections (List, Set, Map)</li>
     *   <li>Java 8 time types (LocalDateTime, Instant, etc.)</li>
     *   <li>Primitive types and their wrappers</li>
     *   <li>Arrays</li>
     * </ul>
     *
     * @param obj the object to serialize, may be null
     * @return the JSON string representation, or null if serialization fails
     * @since 1.0.0
     */
    public static String toJson(Object obj) {
        try {
            return JACKSON_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            log.error("Parse Object to JSON error", ex);
        }
        return null;
    }

    /**
     * Parses a JSON string into a LinkedHashMap with specified key and value types.
     *
     * <p>This method deserializes JSON content into a LinkedHashMap, preserving
     * the insertion order of elements. It's particularly useful for parsing
     * JSON objects where order matters or for configuration data.</p>
     *
     * <p>Features:</p>
     * <ul>
     *   <li>Preserves insertion order using LinkedHashMap</li>
     *   <li>Type-safe key and value conversion</li>
     *   <li>Handles nested objects and complex value types</li>
     *   <li>Graceful error handling with null return</li>
     * </ul>
     *
     * <p>Example Usage:</p>
     * <pre>{@code
     * String json = "{ \"name\": \"John\", \"age\": \"30\" }";
     * LinkedHashMap<String, String> map = JsonUtils.parseJsonToMap(json, String.class, String.class);
     * }</pre>
     *
     * @param <K>   the type of map keys
     * @param <V>   the type of map values
     * @param json  the JSON string to parse, may be null or empty
     * @param key   the class type for map keys, may be null
     * @param value the class type for map values, may be null
     * @return the parsed LinkedHashMap, or null if parsing fails or input is invalid
     * @since 1.0.0
     */
    public static <K, V> LinkedHashMap<K, V> parseJsonToMap(String json, @Nullable Class<K> key, @Nullable Class<V> value) {
        JavaType javaType = JACKSON_MAPPER.getTypeFactory().constructMapType(LinkedHashMap.class, key, value);
        try {
            if (StringUtils.isNotBlank(json)) {
                return JACKSON_MAPPER.readValue(json, javaType);
            }
        } catch (Exception ex) {
            log.error("Parse JSON to MAP error", ex);
        }
        return null;
    }

    /**
     * Parses a JSON array string into a LinkedList with specified element type.
     *
     * <p>This method deserializes JSON array content into a LinkedList, preserving
     * the order of elements. It's useful for parsing JSON arrays where order
     * matters or for configuration lists.</p>
     *
     * <p>Features:</p>
     * <ul>
     *   <li>Preserves element order using LinkedList</li>
     *   <li>Type-safe element conversion</li>
     *   <li>Handles complex element types and nested structures</li>
     *   <li>Graceful error handling with null return</li>
     * </ul>
     *
     * <p>Example Usage:</p>
     * <pre>{@code
     * String json = "[\"item1\", \"item2\", \"item3\"]";
     * LinkedList<String> list = JsonUtils.parseJsonToList(json, String.class);
     *
     * String configJson = "[{\"id\":1,\"name\":\"config1\"}, {\"id\":2,\"name\":\"config2\"}]";
     * LinkedList<ConfigItem> configs = JsonUtils.parseJsonToList(configJson, ConfigItem.class);
     * }</pre>
     *
     * @param <E>   the type of list elements
     * @param json  the JSON array string to parse, may be null or empty
     * @param clazz the class type for list elements, may be null
     * @return the parsed LinkedList, or null if parsing fails or input is invalid
     * @since 1.0.0
     */
    public static <E> LinkedList<E> parseJsonToList(String json, @Nullable Class<E> clazz) {
        JavaType javaType = JACKSON_MAPPER.getTypeFactory().constructParametricType(LinkedList.class, clazz);
        try {
            if (StringUtils.isNotBlank(json)) {
                return JACKSON_MAPPER.readValue(json, javaType);
            }
        } catch (Exception ex) {
            log.error("Parse JSON to List error", ex);
        }
        return null;
    }

}

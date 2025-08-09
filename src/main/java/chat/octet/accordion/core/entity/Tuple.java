package chat.octet.accordion.core.entity;


import chat.octet.accordion.core.enums.DataType;
import chat.octet.accordion.core.handler.DataTypeConvert;
import chat.octet.accordion.utils.JsonUtils;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe key-value container with type-safe data access methods.
 *
 * <p>Tuple extends {@link ConcurrentHashMap} to provide a thread-safe storage
 * mechanism with convenient type conversion methods. It serves as the foundation
 * for parameter storage and data exchange throughout the Accordion framework.</p>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li><strong>Thread Safety</strong>: Built on ConcurrentHashMap for concurrent access</li>
 *   <li><strong>Type Conversion</strong>: Automatic type conversion using DataTypeConvert</li>
 *   <li><strong>JSON Integration</strong>: Built-in JSON serialization support</li>
 *   <li><strong>Convenience Methods</strong>: Type-specific getters for common data types</li>
 * </ul>
 *
 * <p>Supported Data Types:</p>
 * <ul>
 *   <li>Primitive types: Long, Integer, Double, Boolean</li>
 *   <li>String and Date types</li>
 *   <li>BigDecimal for precise decimal calculations</li>
 *   <li>Complex objects via JSON deserialization</li>
 *   <li>Collections (Lists) with type safety</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 *
 * <p><strong>Basic Operations:</strong></p>
 * <pre>{@code
 * Tuple<String, Object> tuple = new Tuple<>();
 * tuple.put("count", "42");
 * tuple.put("name", "John Doe");
 * tuple.put("active", "true");
 *
 * Integer count = tuple.getInt("count");        // 42
 * String name = tuple.getString("name");        // "John Doe"
 * boolean active = tuple.getBoolean("active");  // true
 * }</pre>
 *
 * <p><strong>Complex Objects:</strong></p>
 * <pre>{@code
 * tuple.put("config", "{\"timeout\":30,\"retries\":3}");
 * ConfigObject config = tuple.getObject("config", ConfigObject.class);
 * }</pre>
 *
 * <p><strong>Collections:</strong></p>
 * <pre>{@code
 * tuple.put("items", "[\"item1\",\"item2\",\"item3\"]");
 * LinkedList<String> items = tuple.getList("items", String.class);
 * }</pre>
 *
 * <p>Thread Safety: This class is thread-safe for concurrent read and write
 * operations, making it suitable for use in multi-threaded execution environments.</p>
 *
 * @param <K> the type of keys maintained by this tuple
 * @param <V> the type of mapped values
 * @author <a href="https://github.com/eoctet">William</a>
 * @see ConcurrentHashMap
 * @see DataTypeConvert
 * @see JsonUtils
 * @since 1.0.0
 */
public class Tuple<K, V> extends ConcurrentHashMap<K, V> implements Serializable {

    /**
     * Retrieves a Long value with automatic type conversion.
     *
     * @param key the key whose associated value is to be returned
     * @return the Long value, or null if the key is not found or conversion fails
     * @since 1.0.0
     */
    public Long getLong(final String key) {
        return DataTypeConvert.getValue(DataType.LONG, this.get(key));
    }

    /**
     * Retrieves an Integer value with automatic type conversion.
     *
     * @param key the key whose associated value is to be returned
     * @return the Integer value, or null if the key is not found or conversion fails
     * @since 1.0.0
     */
    public Integer getInt(final String key) {
        return DataTypeConvert.getValue(DataType.INT, this.get(key));
    }

    /**
     * Retrieves a Double value with automatic type conversion.
     *
     * @param key the key whose associated value is to be returned
     * @return the Double value, or null if the key is not found or conversion fails
     * @since 1.0.0
     */
    public Double getDouble(final String key) {
        return DataTypeConvert.getValue(DataType.DOUBLE, this.get(key));
    }

    /**
     * Retrieves a String value with automatic type conversion.
     *
     * @param key the key whose associated value is to be returned
     * @return the String value, or null if the key is not found or conversion fails
     * @since 1.0.0
     */
    public String getString(final String key) {
        return DataTypeConvert.getValue(DataType.STRING, this.get(key));
    }

    /**
     * Retrieves a Date value with automatic type conversion.
     *
     * @param key the key whose associated value is to be returned
     * @return the Date value, or null if the key is not found or conversion fails
     * @since 1.0.0
     */
    public Date getDate(final String key) {
        return DataTypeConvert.getValue(DataType.DATETIME, this.get(key));
    }

    /**
     * Retrieves a BigDecimal value with automatic type conversion.
     *
     * @param key the key whose associated value is to be returned
     * @return the BigDecimal value, or null if the key is not found or conversion fails
     * @since 1.0.0
     */
    public BigDecimal getDecimal(final String key) {
        return DataTypeConvert.getValue(DataType.DECIMAL, this.get(key));
    }

    /**
     * Retrieves a boolean value with automatic type conversion.
     *
     * @param key the key whose associated value is to be returned
     * @return the boolean value, or false if the key is not found or conversion fails
     * @since 1.0.0
     */
    public boolean getBoolean(final String key) {
        return DataTypeConvert.getValue(DataType.BOOLEAN, this.get(key));
    }

    /**
     * Retrieves a complex object by deserializing JSON content.
     *
     * <p>This method converts the stored value to a string and attempts to
     * deserialize it as JSON into the specified object type.</p>
     *
     * @param <T>   the type of object to deserialize
     * @param key   the key whose associated value is to be returned
     * @param clazz the target class for deserialization
     * @return the deserialized object, or null if deserialization fails
     * @since 1.0.0
     */
    public <T> T getObject(final String key, @Nullable final Class<T> clazz) {
        return JsonUtils.parseToObject(String.valueOf(this.get(key)), clazz);
    }

    /**
     * Retrieves a typed list by deserializing JSON array content.
     *
     * <p>This method converts the stored value to a string and attempts to
     * deserialize it as a JSON array into a LinkedList of the specified type.</p>
     *
     * @param <E>   the type of list elements
     * @param key   the key whose associated value is to be returned
     * @param clazz the target class for list elements
     * @return the deserialized LinkedList, or null if deserialization fails
     * @since 1.0.0
     */
    public <E> LinkedList<E> getList(final String key, @Nullable final Class<E> clazz) {
        return JsonUtils.parseJsonToList(String.valueOf(this.get(key)), clazz);
    }

    /**
     * Converts this tuple to a regular HashMap.
     *
     * <p>Creates a new HashMap containing all the key-value pairs from this tuple.
     * This is useful when a non-concurrent map is needed or for compatibility
     * with APIs that expect HashMap.</p>
     *
     * @return a new HashMap containing all entries from this tuple
     * @since 1.0.0
     */
    public Map<K, V> toMap() {
        return Maps.newHashMap(this);
    }

    /**
     * Returns a JSON string representation of this tuple.
     *
     * <p>Serializes all key-value pairs in this tuple to a JSON string
     * for debugging, logging, or persistence purposes.</p>
     *
     * @return JSON string representation of this tuple
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }

}

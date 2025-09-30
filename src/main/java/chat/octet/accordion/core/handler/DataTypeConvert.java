package chat.octet.accordion.core.handler;

import chat.octet.accordion.core.enums.Constant;
import chat.octet.accordion.core.enums.DataType;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


/**
 * Data type converter.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Slf4j
public final class DataTypeConvert {

    private DataTypeConvert() {
        // Utility class - prevent instantiation
    }

    private static final String POINT = ".";
    private static final String SLASH = "/";
    private static final String TIME = "T";
    private static final String ZONE = "Z";
    private static final String COLON = ":";
    private static final String LINE = "-";
    private static final String TRUE_NUMBER = "1";
    private static final String TRUE_FLAG = "yes";
    private static final String FALSE_NUMBER = "0";
    private static final String FALSE_FLAG = "no";
    private static final int DECIMAL_SCALE = 10;

    public static <T extends Serializable> T getValue(final String dataType, final Object value) {
        if (StringUtils.isEmpty(dataType)) {
            throw new IllegalArgumentException("DataType cannot be null.");
        }
        DataType dt = DataType.valueOfType(dataType);
        return getValue(dt, value);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T getValue(final DataType dataType, final Object value) {
        Preconditions.checkNotNull(dataType, MessageFormat.format("Unsupported data type {0}", dataType));
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return (T) dataType.getDefaultValue();
        }
        return (T) convert(dataType.getClassType(), String.valueOf(value));
    }

    private static <T extends Serializable> T convert(final Class<T> clazz, final String value) {
        if (clazz == Integer.class) {
            try {
                String processedValue = value;
                if (processedValue.contains(POINT)) {
                    processedValue = StringUtils.substringBefore(processedValue, POINT);
                }
                return clazz.cast(Integer.parseInt(processedValue));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse integer value: {}, using default (0)", value);
                return clazz.cast(0);
            }
        } else if (clazz == Long.class) {
            try {
                String processedValue = value;
                if (processedValue.contains(POINT)) {
                    processedValue = StringUtils.substringBefore(processedValue, POINT);
                }
                return clazz.cast(Long.parseLong(processedValue));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse long value: {}, using default (0)", value);
                return clazz.cast(0L);
            }
        } else if (clazz == String.class) {
            return clazz.cast(value);
        } else if (clazz == Double.class) {
            try {
                return clazz.cast(Double.parseDouble(value));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse double value: {}, using default (0.0)", value);
                return clazz.cast(0.0d);
            }
        } else if (clazz == Boolean.class) {
            boolean b;
            if (TRUE_NUMBER.equals(value) || FALSE_NUMBER.equals(value)) {
                b = TRUE_NUMBER.equals(value);
            } else if (TRUE_FLAG.equalsIgnoreCase(value) || FALSE_FLAG.equalsIgnoreCase(value)) {
                b = TRUE_FLAG.equalsIgnoreCase(value);
            } else {
                b = Boolean.parseBoolean(value);
            }
            return clazz.cast(b);
        } else if (clazz == BigDecimal.class) {
            try {
                return clazz.cast(new BigDecimal(value).setScale(DECIMAL_SCALE, RoundingMode.HALF_UP));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse BigDecimal value: {}, using default (0)", value);
                return clazz.cast(BigDecimal.valueOf(0, DECIMAL_SCALE));
            }
        } else if (clazz == LocalDateTime.class) {
            String format = null;
            String processedValue = value;
            if (processedValue.contains(TIME) && processedValue.contains(ZONE)) {
                processedValue = processedValue.replace(TIME, StringUtils.SPACE).replace(ZONE, StringUtils.EMPTY);
            }
            if (processedValue.contains(SLASH) && processedValue.indexOf(SLASH) == 4) {
                if (processedValue.contains(COLON) && processedValue.contains(POINT)) {
                    format = Constant.DATE_FORMAT_WITH_MILLIS2;
                } else if (processedValue.contains(COLON) && !processedValue.contains(POINT)) {
                    format = Constant.DATE_FORMAT_WITH_TIME2;
                } else {
                    format = Constant.DATE_FORMAT2;
                }
            } else if (processedValue.contains(LINE) && processedValue.contains(COLON) && processedValue.indexOf(LINE) == 4) {
                if (processedValue.contains(COLON) && processedValue.contains(POINT)) {
                    format = Constant.DATE_FORMAT_WITH_MILLIS;
                } else if (processedValue.contains(COLON) && !processedValue.contains(POINT)) {
                    format = Constant.DATE_FORMAT_WITH_TIME;
                } else {
                    format = Constant.DATE_FORMAT;
                }
            }
            try {
                if (format == null) {
                    return clazz.cast(LocalDateTime.parse(processedValue));
                }
                return clazz.cast(LocalDateTime.parse(processedValue, DateTimeFormatter.ofPattern(format)));
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse date value: {}, using default", value);
                return clazz.cast(LocalDateTime.now());
            }
        }
        return clazz.cast(value);
    }

}

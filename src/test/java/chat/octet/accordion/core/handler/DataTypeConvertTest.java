package chat.octet.accordion.core.handler;

import chat.octet.accordion.core.enums.DataType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DataTypeConvert class.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
class DataTypeConvertTest {

    @Nested
    @DisplayName("Number Format Exception Handling Tests")
    class NumberFormatExceptionTests {

        @Test
        @DisplayName("Should return default value (0) when integer parsing fails")
        void shouldReturnDefaultValueForInvalidInteger() {
            Integer result = DataTypeConvert.getValue(DataType.INT, "invalid");
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return default value (0L) when long parsing fails")
        void shouldReturnDefaultValueForInvalidLong() {
            Long result = DataTypeConvert.getValue(DataType.LONG, "abc123");
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should return default value (0.0) when double parsing fails")
        void shouldReturnDefaultValueForInvalidDouble() {
            Double result = DataTypeConvert.getValue(DataType.DOUBLE, "not-a-number");
            assertThat(result).isEqualTo(0.0d);
        }

        @Test
        @DisplayName("Should return default value when BigDecimal parsing fails")
        void shouldReturnDefaultValueForInvalidBigDecimal() {
            BigDecimal result = DataTypeConvert.getValue(DataType.DECIMAL, "xyz");
            assertThat(result).isEqualTo(BigDecimal.valueOf(0, 10));
        }

        @Test
        @DisplayName("Should parse valid integer successfully")
        void shouldParseValidInteger() {
            Integer result = DataTypeConvert.getValue(DataType.INT, "123");
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("Should parse valid long successfully")
        void shouldParseValidLong() {
            Long result = DataTypeConvert.getValue(DataType.LONG, "9876543210");
            assertThat(result).isEqualTo(9876543210L);
        }

        @Test
        @DisplayName("Should parse valid double successfully")
        void shouldParseValidDouble() {
            Double result = DataTypeConvert.getValue(DataType.DOUBLE, "123.45");
            assertThat(result).isEqualTo(123.45);
        }

        @Test
        @DisplayName("Should parse valid BigDecimal successfully")
        void shouldParseValidBigDecimal() {
            BigDecimal result = DataTypeConvert.getValue(DataType.DECIMAL, "123.456");
            assertThat(result.doubleValue()).isEqualTo(123.456, org.assertj.core.api.Assertions.within(0.0000000001));
        }

        @Test
        @DisplayName("Should parse integer from double string")
        void shouldParseIntegerFromDoubleString() {
            Integer result = DataTypeConvert.getValue(DataType.INT, "123.99");
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("Should parse long from double string")
        void shouldParseLongFromDoubleString() {
            Long result = DataTypeConvert.getValue(DataType.LONG, "999.88");
            assertThat(result).isEqualTo(999L);
        }
    }

    @Nested
    @DisplayName("Boolean Conversion Tests")
    class BooleanConversionTests {

        @Test
        @DisplayName("Should parse '1' as true")
        void shouldParseOneAsTrue() {
            Boolean result = DataTypeConvert.getValue(DataType.BOOLEAN, "1");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should parse '0' as false")
        void shouldParseZeroAsFalse() {
            Boolean result = DataTypeConvert.getValue(DataType.BOOLEAN, "0");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should parse 'yes' as true (case insensitive)")
        void shouldParseYesAsTrue() {
            Boolean result1 = DataTypeConvert.getValue(DataType.BOOLEAN, "yes");
            Boolean result2 = DataTypeConvert.getValue(DataType.BOOLEAN, "YES");
            Boolean result3 = DataTypeConvert.getValue(DataType.BOOLEAN, "Yes");

            assertThat(result1).isTrue();
            assertThat(result2).isTrue();
            assertThat(result3).isTrue();
        }

        @Test
        @DisplayName("Should parse 'no' as false (case insensitive)")
        void shouldParseNoAsFalse() {
            Boolean result1 = DataTypeConvert.getValue(DataType.BOOLEAN, "no");
            Boolean result2 = DataTypeConvert.getValue(DataType.BOOLEAN, "NO");
            Boolean result3 = DataTypeConvert.getValue(DataType.BOOLEAN, "No");

            assertThat(result1).isFalse();
            assertThat(result2).isFalse();
            assertThat(result3).isFalse();
        }

        @Test
        @DisplayName("Should parse 'true' as true")
        void shouldParseTrueAsTrue() {
            Boolean result = DataTypeConvert.getValue(DataType.BOOLEAN, "true");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should parse 'false' as false")
        void shouldParseFalseAsFalse() {
            Boolean result = DataTypeConvert.getValue(DataType.BOOLEAN, "false");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should parse other values as false")
        void shouldParseOtherValuesAsFalse() {
            Boolean result = DataTypeConvert.getValue(DataType.BOOLEAN, "maybe");
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Null and Empty Value Tests")
    class NullAndEmptyValueTests {

        @Test
        @DisplayName("Should return default value for null integer")
        void shouldReturnDefaultForNullInteger() {
            Integer result = DataTypeConvert.getValue(DataType.INT, null);
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return default value for empty string integer")
        void shouldReturnDefaultForEmptyInteger() {
            Integer result = DataTypeConvert.getValue(DataType.INT, "");
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return default value for blank string double")
        void shouldReturnDefaultForBlankDouble() {
            Double result = DataTypeConvert.getValue(DataType.DOUBLE, "   ");
            assertThat(result).isEqualTo(0.0d);
        }
    }

    @Nested
    @DisplayName("String Conversion Tests")
    class StringConversionTests {

        @Test
        @DisplayName("Should convert any value to string")
        void shouldConvertToString() {
            String result = DataTypeConvert.getValue(DataType.STRING, "test");
            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("Should convert number to string")
        void shouldConvertNumberToString() {
            String result = DataTypeConvert.getValue(DataType.STRING, 123);
            assertThat(result).isEqualTo("123");
        }

        @Test
        @DisplayName("Should return empty string for null")
        void shouldReturnEmptyStringForNull() {
            String result = DataTypeConvert.getValue(DataType.STRING, null);
            assertThat(result).isEmpty();
        }
    }
}
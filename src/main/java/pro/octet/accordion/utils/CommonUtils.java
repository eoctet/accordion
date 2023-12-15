package pro.octet.accordion.utils;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class CommonUtils {

    private static final String CHARACTER_SET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final ExpressionFactory EXPRESSION_FACTORY = new ExpressionFactoryImpl();

    private CommonUtils() {
    }

    public static String randomString(String prefixString) {
        String randomString = IntStream.range(0, 10).map(i -> new SecureRandom().nextInt(CHARACTER_SET.length())).mapToObj(randomInt -> CHARACTER_SET.substring(randomInt, randomInt + 1)).collect(Collectors.joining());
        return StringUtils.join(prefixString, "-", randomString);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static String parameterFormat(Map<String, Object> params, String text) {
        SimpleContext context = new SimpleContext();
        params.forEach((key, value) -> context.setVariable(key, EXPRESSION_FACTORY.createValueExpression(value, value.getClass())));
        ValueExpression expression = EXPRESSION_FACTORY.createValueExpression(context, text, String.class);
        return expression.getValue(context).toString();
    }

}

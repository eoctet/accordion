package chat.octet.accordion.core.enums;


import lombok.Getter;

/**
 * Condition operator define.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
public enum ConditionOperator {
    EQ("=="),
    NEQ("!="),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    IN("contains"),
    NOT_IN("!contains");

    private final String operator;

    ConditionOperator(String operator) {
        this.operator = operator;
    }

}

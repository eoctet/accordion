package pro.octet.accordion.core.enums;


import lombok.Getter;

@Getter
public enum ConditionOperator {
    EQUALS("=="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUALS(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUALS("<="),
    CONTAINS("contains"),
    NOT_CONTAINS("!contains"),
    AND("and"),
    OR("or");

    private final String operator;

    ConditionOperator(String operator) {
        this.operator = operator;
    }

}

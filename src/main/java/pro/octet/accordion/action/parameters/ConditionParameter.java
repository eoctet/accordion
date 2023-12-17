package pro.octet.accordion.action.parameters;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;


@Getter
@Builder
@ToString
@Jacksonized
public class ConditionParameter {
    private String expression;
}

package pro.octet.accordion.action.parameters;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Condition action parameter.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
@Builder
@ToString
@Jacksonized
public class ConditionParameter {
    /**
     * Condition expression. For example: Arg == 100.
     */
    private String expression;
}

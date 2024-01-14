package chat.octet.accordion.action.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.Arrays;
import java.util.List;

/**
 * Switch action parameter.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
@Builder
@ToString
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SwitchParameter {

    /**
     * Switch branches list.
     */
    @Builder.Default
    private List<Branch> branches = Lists.newArrayList();

    private boolean debug;

    /**
     * Add a branch to the switch.
     *
     * @param branch Action branch condition.
     * @return SwitchParameter
     */
    public SwitchParameter addBranch(Branch branch) {
        return addBranch(branch);
    }

    /**
     * Add multiple branches to the switch.
     *
     * @param branch Action branch condition.
     * @return SwitchParameter
     */
    public SwitchParameter addBranch(Branch... branch) {
        branches.addAll(Arrays.asList(branch));
        return this;
    }

    /**
     * Switch branch parameter.
     */
    @Getter
    @Builder
    @ToString
    @Jacksonized
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Branch {
        /**
         * Branch name.
         */
        private String name;
        /**
         * Action id for branch connection.
         */
        private String actionId;
        /**
         * Branch Condition expression. For example: Arg == 100.
         */
        private String expression;
        /**
         * Reverse branch condition boolean, such as: !true or !false.
         */
        private boolean negation;
    }

}

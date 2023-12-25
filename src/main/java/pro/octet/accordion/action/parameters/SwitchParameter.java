package pro.octet.accordion.action.parameters;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import pro.octet.accordion.core.condition.Condition;

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
         * Branch condition.
         *
         * @see Condition
         */
        private Condition condition;
        /**
         * Reverse branch condition boolean, such as: !true or !false.
         */
        private boolean negation;
    }

}

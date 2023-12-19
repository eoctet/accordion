package pro.octet.accordion.action.parameters;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import pro.octet.accordion.action.Condition;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder
@ToString
@Jacksonized
public class SwitchParameter {

    @Builder.Default
    private List<Branch> branches = Lists.newArrayList();

    public SwitchParameter addBranch(Branch branch) {
        branches.add(branch);
        return this;
    }

    public SwitchParameter addBranch(Branch... branch) {
        branches.addAll(Arrays.asList(branch));
        return this;
    }

    @Getter
    @Builder
    @ToString
    @Jacksonized
    public static class Branch {
        private String name;
        private String actionId;
        private Condition condition;
        private boolean negation;
    }

}

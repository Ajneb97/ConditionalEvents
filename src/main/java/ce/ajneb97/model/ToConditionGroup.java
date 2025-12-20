package ce.ajneb97.model;

import java.util.List;

public class ToConditionGroup {

    private String name;
    private List<String> conditions;

    public ToConditionGroup(String name, List<String> conditions) {
        this.name = name;
        this.conditions = conditions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }
}

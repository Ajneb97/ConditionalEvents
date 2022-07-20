package ce.ajneb97.model.actions;

import java.util.List;

public class ActionGroup {
    private String name;
    private List<CEAction> actions;

    public ActionGroup(String name, List<CEAction> actions) {
        this.name = name;
        this.actions = actions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CEAction> getActions() {
        return actions;
    }

    public void setActions(List<CEAction> actions) {
        this.actions = actions;
    }
}

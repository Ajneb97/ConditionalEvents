package ce.ajneb97.model.actions;

public class CEAction {

    private ActionType type;
    private String apiType; //Just for API events.
    private ActionTargeter targeter;
    private String actionLine;

    public CEAction(ActionType type, String actionLine, ActionTargeter targeter) {
        this.type = type;
        this.actionLine = actionLine;
        this.targeter = targeter;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getActionLine() {
        return actionLine;
    }

    public void setActionLine(String actionLine) {
        this.actionLine = actionLine;
    }

    public ActionTargeter getTargeter() {
        return targeter;
    }

    public void setTargeter(ActionTargeter targeter) {
        this.targeter = targeter;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }
}

package ce.ajneb97.model.actions;

public class ActionTargeter {
    private ActionTargeterType type;
    private String parameter;

    public ActionTargeter(ActionTargeterType type) {
        this.type = type;
    }

    public ActionTargeterType getType() {
        return type;
    }

    public void setType(ActionTargeterType type) {
        this.type = type;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}

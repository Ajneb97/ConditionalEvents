package ce.ajneb97.model.actions;

public enum ActionTargeter {
    TO_ALL,
    TO_TARGET,
    TO_WORLD,
    TO_RANGE,
    TO_CONDITION,
    NORMAL;

    private String parameter;

    ActionTargeter() {
    }

    public String getParameter(){
        return this.parameter;
    }

    public void setParameter(String parameter){
        this.parameter = parameter;
    }
}

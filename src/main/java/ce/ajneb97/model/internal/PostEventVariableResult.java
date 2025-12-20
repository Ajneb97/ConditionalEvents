package ce.ajneb97.model.internal;

public class PostEventVariableResult {

    private String variable;
    private boolean replaced;

    public PostEventVariableResult(String variable, boolean replaced) {
        this.variable = variable;
        this.replaced = replaced;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public boolean isReplaced() {
        return replaced;
    }

    public void setReplaced(boolean replaced) {
        this.replaced = replaced;
    }

    public static PostEventVariableResult replaced(String variable) {
        return new PostEventVariableResult(variable, true);
    }

    public static PostEventVariableResult noReplaced() {
        return new PostEventVariableResult(null, false);
    }
}

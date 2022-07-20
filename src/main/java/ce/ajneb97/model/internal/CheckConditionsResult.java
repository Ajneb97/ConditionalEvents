package ce.ajneb97.model.internal;

public class CheckConditionsResult {

    private boolean conditionsAccomplished;
    private String executeActionGroup;

    public CheckConditionsResult(boolean conditionsAccomplished, String executeActionGroup) {
        this.conditionsAccomplished = conditionsAccomplished;
        this.executeActionGroup = executeActionGroup;
    }

    public boolean isConditionsAccomplished() {
        return conditionsAccomplished;
    }

    public String getExecuteActionGroup() {
        if(executeActionGroup == null){
            return "default";
        }else{
            return executeActionGroup;
        }
    }

}

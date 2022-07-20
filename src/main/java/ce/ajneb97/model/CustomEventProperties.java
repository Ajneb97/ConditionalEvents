package ce.ajneb97.model;

import java.util.List;

public class CustomEventProperties {

    private String eventPackage;
    private String playerVariable;
    private List<String> variablesToCapture;

    public CustomEventProperties(String eventPackage, String playerVariable, List<String> variablesToCapture) {
        this.eventPackage = eventPackage;
        this.playerVariable = playerVariable;
        this.variablesToCapture = variablesToCapture;
    }

    public String getEventPackage() {
        return eventPackage;
    }

    public void setEventPackage(String eventPackage) {
        this.eventPackage = eventPackage;
    }

    public String getPlayerVariable() {
        return playerVariable;
    }

    public void setPlayerVariable(String playerVariable) {
        this.playerVariable = playerVariable;
    }

    public List<String> getVariablesToCapture() {
        return variablesToCapture;
    }

    public void setVariablesToCapture(List<String> variablesToCapture) {
        this.variablesToCapture = variablesToCapture;
    }
}

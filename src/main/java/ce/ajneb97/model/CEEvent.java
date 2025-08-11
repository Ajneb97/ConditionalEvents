package ce.ajneb97.model;

import ce.ajneb97.managers.RepetitiveManager;
import ce.ajneb97.model.actions.ActionGroup;
import org.bukkit.Bukkit;

import java.util.List;

public class CEEvent {

    private String name;
    private String filePath;
    private List<String> conditions;
    private EventType eventType;
    private List<ActionGroup> actionGroups;

    private boolean oneTime;

    private String ignoreWithPermission;

    private long cooldown;

    private boolean enabled;
    private boolean ignoreIfCancelled;
    private boolean allowMathFormulasInConditions;

    private RepetitiveManager repetitiveManager;

    private CustomEventProperties customEventProperties;

    private List<String> preventCooldownActivationActionGroups;
    private List<String> preventOneTimeActivationActionGroups;



    public CEEvent(String name){
        this.name = name;
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

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public List<ActionGroup> getActionGroups() {
        return actionGroups;
    }

    public void setActionGroups(List<ActionGroup> actionGroups) {
        this.actionGroups = actionGroups;
    }

    public boolean isOneTime() {
        return oneTime;
    }

    public void setOneTime(boolean oneTime) {
        this.oneTime = oneTime;
    }

    public String getIgnoreWithPermission() {
        return ignoreWithPermission;
    }

    public void setIgnoreWithPermission(String ignoreWithPermission) {
        this.ignoreWithPermission = ignoreWithPermission;
    }

    public long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIgnoreIfCancelled() {
        return ignoreIfCancelled;
    }

    public void setIgnoreIfCancelled(boolean ignoreIfCancelled) {
        this.ignoreIfCancelled = ignoreIfCancelled;
    }

    public List<String> getPreventCooldownActivationActionGroups() {
        return preventCooldownActivationActionGroups;
    }

    public void setPreventCooldownActivationActionGroups(List<String> preventCooldownActivationActionGroups) {
        this.preventCooldownActivationActionGroups = preventCooldownActivationActionGroups;
    }

    public List<String> getPreventOneTimeActivationActionGroups() {
        return preventOneTimeActivationActionGroups;
    }

    public void setPreventOneTimeActivationActionGroups(List<String> preventOneTimeActivationActionGroups) {
        this.preventOneTimeActivationActionGroups = preventOneTimeActivationActionGroups;
    }

    public boolean isAllowMathFormulasInConditions() {
        return allowMathFormulasInConditions;
    }

    public void setAllowMathFormulasInConditions(boolean allowMathFormulasInConditions) {
        this.allowMathFormulasInConditions = allowMathFormulasInConditions;
    }

    public void enable(){
        this.enabled = true;
        if(repetitiveManager != null && !repetitiveManager.isStarted()){
            repetitiveManager.start();
        }
    }

    public void disable(){
        this.enabled = false;
        if(repetitiveManager != null){
            repetitiveManager.end();
        }
    }

    public RepetitiveManager getRepetitiveManager() {
        return repetitiveManager;
    }

    public void setRepetitiveManager(RepetitiveManager repetitiveManager) {
        this.repetitiveManager = repetitiveManager;
    }

    public CustomEventProperties getCustomEventProperties() {
        return customEventProperties;
    }

    public void setCustomEventProperties(CustomEventProperties customEventProperties) {
        this.customEventProperties = customEventProperties;
    }



    public ActionGroup getActionGroup(String name){
        for(ActionGroup actionGroup : actionGroups){
            if(actionGroup.getName().equals(name)){
                return actionGroup;
            }
        }
        return null;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

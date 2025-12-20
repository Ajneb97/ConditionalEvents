package ce.ajneb97.model.player;

public class EventData {

    private String name;
    private long cooldown; //Represents the millis time when the event was executed
    private boolean oneTime;

    public EventData(String name, long cooldown, boolean oneTime) {
        this.name = name;
        this.cooldown = cooldown;
        this.oneTime = oneTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isOneTime() {
        return oneTime;
    }

    public void setOneTime(boolean oneTime) {
        this.oneTime = oneTime;
    }
}

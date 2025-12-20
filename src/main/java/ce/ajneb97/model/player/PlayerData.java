package ce.ajneb97.model.player;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerData {

    private UUID uuid;
    private String name;
    private boolean modified;
    private ArrayList<EventData> eventData;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.eventData = new ArrayList<>();
        this.modified = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<EventData> getEventData() {
        return eventData;
    }

    public void setEventData(ArrayList<EventData> eventData) {
        this.eventData = eventData;
    }

    public EventData getEventData(String eventName) {
        for (EventData e : eventData) {
            if (e.getName().equals(eventName)) {
                return e;
            }
        }
        return null;
    }

    public void resetEvent(String eventName) {
        eventData.removeIf(c -> c.getName().equals(eventName));
        modified = true;
    }

    public void resetAll() {
        eventData.clear();
        modified = true;
    }

    public void setCooldown(String eventName, long currentMillis) {
        EventData e = getEventData(eventName);
        if (e == null) {
            e = new EventData(eventName, currentMillis, false);
            eventData.add(e);
        } else {
            e.setCooldown(currentMillis);
        }
    }

    public long getCooldown(String eventName) {
        EventData e = getEventData(eventName);
        if (e == null) {
            return 0;
        } else {
            return e.getCooldown();
        }
    }

    public void setOneTime(String eventName, boolean oneTime) {
        EventData e = getEventData(eventName);
        if (e == null) {
            e = new EventData(eventName, 0, oneTime);
            eventData.add(e);
        } else {
            e.setOneTime(oneTime);
        }
    }

    public boolean isEventOneTime(String eventName) {
        EventData e = getEventData(eventName);
        if (e == null) {
            return false;
        } else {
            return e.isOneTime();
        }
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}

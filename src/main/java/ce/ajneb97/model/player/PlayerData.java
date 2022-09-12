package ce.ajneb97.model.player;

import org.bukkit.Bukkit;

import java.util.ArrayList;

public class PlayerData {
    private String uuid;
    private String name;
    private ArrayList<EventData> eventData;

    public PlayerData(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.eventData = new ArrayList<EventData>();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
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

    public EventData getEventData(String eventName){
        for(EventData e : eventData){
            if(e.getName().equals(eventName)){
                return e;
            }
        }
        return null;
    }

    public void resetAll(){
        eventData.clear();
    }

    public void resetCooldown(String eventName){
        setCooldown(eventName,0);
    }

    public void setCooldown(String eventName,long currentMillis){
        EventData e = getEventData(eventName);
        if(e == null){
            e = new EventData(eventName,currentMillis,false);
            eventData.add(e);
        }else{
            e.setCooldown(currentMillis);
        }
    }

    public long getCooldown(String eventName){
        EventData e = getEventData(eventName);
        if(e == null){
            return 0;
        }else{
            return e.getCooldown();
        }
    }

    public void setOneTime(String eventName,boolean oneTime){
        EventData e = getEventData(eventName);
        if(e == null){
            e = new EventData(eventName,0,oneTime);
            eventData.add(e);
        }else{
            e.setOneTime(oneTime);
        }
    }

    public boolean isEventOneTime(String eventName){
        EventData e = getEventData(eventName);
        if(e == null){
            return false;
        }else{
            return e.isOneTime();
        }
    }

}

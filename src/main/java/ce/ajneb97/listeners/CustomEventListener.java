package ce.ajneb97.listeners;

import ce.ajneb97.ConditionalEvents;
import ce.ajneb97.managers.MessagesManager;
import ce.ajneb97.model.CEEvent;
import ce.ajneb97.model.CustomEventProperties;
import ce.ajneb97.model.EventType;
import ce.ajneb97.model.StoredVariable;
import ce.ajneb97.model.internal.ConditionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CustomEventListener implements Listener {

    public ConditionalEvents plugin;
    public CustomEventListener(ConditionalEvents plugin) {
        this.plugin = plugin;
        configure();
    }

    public void configure(){
        ArrayList<CEEvent> validEvents = plugin.getEventsManager().getEventsByType(EventType.CUSTOM);
        for(CEEvent ceEvent : validEvents) {
            CustomEventProperties properties = ceEvent.getCustomEventProperties();
            String eventPackage = properties.getEventPackage();
            try {
                Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName(eventPackage);
                EventExecutor eventExecutor = (listener, event) -> executeEvent(event, ceEvent.getName());
                plugin.getServer().getPluginManager().registerEvent(eventClass, this, EventPriority.MONITOR, eventExecutor, plugin);
            } catch (ClassNotFoundException ex) {
                Bukkit.getConsoleSender().sendMessage(ConditionalEvents.prefix
                    + MessagesManager.getColoredMessage("&cClass "+eventPackage+" &cdoesn't exists for custom event &e"+ceEvent.getName()+"&c."));
            }
        }
    }

    public void executeEvent(Event event,String ceEventName) {
        Class<?> classObject = event.getClass();

        CEEvent ceEvent = plugin.getEventsManager().getEvent(ceEventName);
        if(ceEvent == null || !ceEvent.isEnabled()){
            return;
        }

        CustomEventProperties properties = ceEvent.getCustomEventProperties();
        try {
            if (!Class.forName(properties.getEventPackage()).isAssignableFrom(classObject)) {
                return;
            }
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        //Get variables
        List<String> variablesToCapture = properties.getVariablesToCapture();
        ArrayList<StoredVariable> storedVariables = new ArrayList<StoredVariable>();
        for(String line : variablesToCapture) {
            String[] sepLine = line.split(";");
            String variable = sepLine[0];
            String methodName = sepLine[1].replace("(", "").replace(")", "");
            Object objectFinal = null;
            if(methodName.contains(".")) {
                objectFinal = getFinalObjectFromMultipleMethods(methodName,event,classObject);
            }else {
                Method m = obtainMethod(classObject,methodName);
                objectFinal = obtainObjectFromEvent(m,event);
            }

            if(objectFinal != null){
                storedVariables.add(new StoredVariable(variable,objectFinal.toString()));
            }
        }

        //Get player
        Object playerObjectFinal = null;
        if(properties.getPlayerVariable() != null) {
            String methodName = properties.getPlayerVariable().replace("(", "").replace(")", "");
            if(methodName.contains(".")) {
                playerObjectFinal = getFinalObjectFromMultipleMethods(methodName,event,classObject);
            }else {
                Method m = obtainMethod(classObject,methodName);
                playerObjectFinal = obtainObjectFromEvent(m,event);
            }
        }

        try {
            Player player = null;
            if(playerObjectFinal != null) {
                player = (Player) playerObjectFinal;
            }

            ConditionEvent conditionEvent = new ConditionEvent(plugin, player, event, EventType.CUSTOM, null)
                    .addVariables(storedVariables);
            plugin.getEventsManager().checkSingularEvent(conditionEvent,ceEvent);
        }catch(Exception ex) {

        }
    }

    private Object getFinalObjectFromMultipleMethods(String method,Event event,Class<?> classObject){
        Object objectFinal = null;
        String[] methods = method.split("\\.");
        Class<?> newClass = classObject;
        try{
            for(int i=0;i<methods.length;i++) {
                String currentMethod = methods[i];

                Method m = obtainMethod(newClass, currentMethod);
                if(i == 0) {
                    objectFinal = obtainObjectFromEvent(m,event);
                }else {
                    objectFinal = obtainObjectFromObject(m,objectFinal);
                }
                newClass = objectFinal.getClass();
            }
        }catch(Exception e){

        }
        return objectFinal;
    }

    private Method obtainMethod(Class<?> classObject,String methodName) {
        try {
            return classObject.getMethod(methodName);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object obtainObjectFromEvent(Method m,Event event) {
        try {
            return m.invoke(event);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object obtainObjectFromObject(Method m,Object object) {
        try {
            return m.invoke(object);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}

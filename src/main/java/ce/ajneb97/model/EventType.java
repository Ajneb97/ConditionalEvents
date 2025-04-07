package ce.ajneb97.model;

public enum EventType {
    PLAYER_RESPAWN,
    PLAYER_DEATH,
    PLAYER_ATTACK,
    PLAYER_KILL,
    PLAYER_DAMAGE,
    PLAYER_COMMAND,
    PLAYER_CHAT,
    PLAYER_PRE_JOIN,
    PLAYER_JOIN,
    PLAYER_LEAVE,
    PLAYER_LEVELUP,
    PLAYER_WORLD_CHANGE,
    PLAYER_ARMOR,
    PLAYER_TELEPORT,
    PLAYER_BED_ENTER,
    PLAYER_SWAP_HAND,
    PLAYER_FISH,
    PLAYER_OPEN_INVENTORY,
    PLAYER_CLOSE_INVENTORY,
    PLAYER_CLICK_INVENTORY,
    PLAYER_STATISTIC,
    PLAYER_SNEAK,
    PLAYER_RUN,
    PLAYER_REGAIN_HEALTH,
    PLAYER_CHANGE_AIR, //1.10+
    PLAYER_CHANGE_FOOD, //1.16+
    PLAYER_TAB_COMPLETE,
    BLOCK_INTERACT,
    BLOCK_BREAK,
    BLOCK_PLACE,
    ITEM_INTERACT,
    ITEM_CONSUME,
    ITEM_PICKUP,
    ITEM_CRAFT,
    ITEM_DROP,
    ITEM_MOVE,
    ITEM_SELECT,
    ITEM_ENCHANT,
    ITEM_REPAIR,
    REPETITIVE,
    REPETITIVE_SERVER,
    ENTITY_SPAWN,
    ENTITY_INTERACT,
    CONSOLE_COMMAND,
    SERVER_START,
    SERVER_STOP,
    CITIZENS_RIGHT_CLICK_NPC,
    WGEVENTS_REGION_ENTER,
    WGEVENTS_REGION_LEAVE,
    PROTOCOLLIB_RECEIVE_MESSAGE,
    CUSTOM,
    CALL
}

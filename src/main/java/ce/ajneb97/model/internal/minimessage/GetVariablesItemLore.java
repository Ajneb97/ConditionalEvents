package ce.ajneb97.model.internal.minimessage;

import java.util.List;

public class GetVariablesItemLore {

    private final List<String> loreList;
    private final List<String> colorFormatLoreList;
    private final String loreString;
    private final String colorFormatLoreString;

    public GetVariablesItemLore(List<String> loreList, List<String> colorFormatLoreList, String loreString, String colorFormatLoreString) {
        this.loreList = loreList;
        this.colorFormatLoreList = colorFormatLoreList;
        this.loreString = loreString;
        this.colorFormatLoreString = colorFormatLoreString;
    }

    public List<String> getLoreList() {
        return loreList;
    }

    public List<String> getColorFormatLoreList() {
        return colorFormatLoreList;
    }

    public String getLoreString() {
        return loreString;
    }

    public String getColorFormatLoreString() {
        return colorFormatLoreString;
    }
}

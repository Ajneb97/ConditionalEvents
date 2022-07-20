package ce.ajneb97.model;

public enum ConditionalType {
    EQUALS("=="),
    NOT_EQUALS("!="),
    EQUALS_LEGACY("equals"),
    NOT_EQUALS_LEGACY("!equals"),
    EQUALS_IGNORE_CASE("equalsIgnoreCase"),
    NOT_EQUALS_IGNORE_CASE("!equalsIgnoreCase"),
    STARTS_WITH("startsWith"),
    NOT_STARTS_WITH("!startsWith"),
    CONTAINS("contains"),
    NOT_CONTAINS("!contains"),
    GREATER(">"),
    GREATER_EQUALS(">="),
    LOWER("<"),
    LOWER_EQUALS("<=");

    private String text;
    ConditionalType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

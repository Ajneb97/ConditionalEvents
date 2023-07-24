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
    ENDS_WITH("endsWith"),
    NOT_ENDS_WITH("!endsWith"),
    MATCHES_WITH("matchesWith"),
    NOT_MATCHES_WITH("!matchesWith"),
    CONTAINS("contains"),
    NOT_CONTAINS("!contains"),
    GREATER(">"),
    GREATER_EQUALS(">="),
    LOWER("<"),
    LOWER_EQUALS("<="),
    IS_MULTIPLE_OF("isMultipleOf"),
    NOT_IS_MULTIPLE_OF("!isMultipleOf");
    private String text;
    ConditionalType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

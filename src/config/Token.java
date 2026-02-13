package config;

public class Token {
    String tokenKind;
    String value;

    public Token(String tokenKind, String value) {
        this.tokenKind = tokenKind;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "(" + tokenKind + ", " + value + ")";
    }
}
package config;

import customError.FormatException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class SimpleConfigLexer {

    private final String configText;
    private Integer index;

    public SimpleConfigLexer(String configText) {
        this.configText = configText;
        this.index = 0;

    }

    public Object tokenize() throws FormatException {
        skipWhitespace();
        Object result = parseValue();
    
        if (!(result instanceof LinkedHashSet)) {
            throw new FormatException("Expected Array of Object");
        }
        return result;
    }

    private Map<String, Object> parseObject() throws FormatException {

        Map<String, Object> map = new HashMap<>();
        index++; // skip '{'
        skipWhitespace();

        while (index < configText.length() && configText.charAt(index) != '}') {

            skipWhitespace();
            if (configText.charAt(index) != '"') {
                throw new FormatException("Expected \" before key");
            }

            String key = extractString();
            skipWhitespace();
            if (configText.charAt(index) != ':') {
                throw new FormatException("Expected ':' after key");
            }
            index++; // skip :

            skipWhitespace();
            Object value = parseValue();

            map.put(key, value);

            skipWhitespace();
            if (configText.charAt(index) != ',' && configText.charAt(index) != '}') {
                throw new FormatException("Expected ',' or '}'.");
            }
            if (configText.charAt(index) == ',') {
                index++;
            }

            skipWhitespace();
        }

        index++; // skip '}'
        return map;
    }

    private Object parseValue() throws FormatException {
        if (index >= configText.length()) {
            throw new FormatException("Unexpected end of input");
        }

        char current = configText.charAt(index);

        if (current == '"') {
            return extractString();
        }

        if (current == '{') {
            return parseObject();
        }

        if (current == '[') {
            return parseArray();
        }

        if (Character.isDigit(current) || current == '-') {
            return extractNumber();
        }
        if (current == 't' || current == 'f') {
            return parseBoolean();
        }

        if (current == 'n') {
            return parseNull();
        }

        throw new FormatException("Unexpected character: " + current);
    }

    private Boolean parseBoolean() throws FormatException {
        skipWhitespace();

        if (configText.startsWith("true", index)) {
            index += 4;
            return true;
        }

        if (configText.startsWith("false", index)) {
            index += 5;
            return false;
        }

        throw new FormatException("Invalid boolean value." + index);

    }

    private Object parseNull() throws FormatException {
        skipWhitespace();

        if (configText.startsWith("null", index)) {
            index += 4;
            return null;
        }

        throw new FormatException("Invalid null value." + index);

    }

    private Object parseArray() throws FormatException {

        LinkedHashSet<Object> list = new LinkedHashSet<>();
        index++;
        skipWhitespace();

        while (index < configText.length() && configText.charAt(index) != ']') {

            skipWhitespace();
            list.add(parseValue());

            skipWhitespace();

            if (configText.charAt(index) != ',' && configText.charAt(index) != ']') {
                throw new FormatException("Expected ',' or ']'.");
            }
            if (configText.charAt(index) == ',') {
                index++;
            }

            skipWhitespace();
        }
        skipWhitespace();

        index++; // skip ']'
        return list;
    }

    private Number extractNumber() throws FormatException {

    StringBuilder sb = new StringBuilder();

    while (index < configText.length() && (Character.isDigit(configText.charAt(index)) ||
            configText.charAt(index) == '.' || configText.charAt(index) == 'e' ||
            configText.charAt(index) == 'E' || configText.charAt(index) == '+' ||
            configText.charAt(index) == '-')) {
        sb.append(configText.charAt(index));
        index++;
    }

    String number = sb.toString();

    try {
        if (number.contains(".") || number.contains("e") || number.contains("E")) {
            return Double.valueOf(number);
        }
        try {
            return Integer.valueOf(number);
        } catch (NumberFormatException e1) {
            try {

                return Long.valueOf(number);
            } catch (NumberFormatException e2) {
                return new BigInteger(number);
            }
        }

    } catch (NumberFormatException e) {
        throw new FormatException("Invalid number: " + number);
    }
}

    private String extractString() throws FormatException {

        index++; // skip opening "

        StringBuilder sb = new StringBuilder();

        while (index < configText.length() && configText.charAt(index) != '"') {

            if (index + 1 < configText.length() && configText.charAt(index) == '\\'
                    && configText.charAt(index + 1) == '"') {
                index++;

            }

            sb.append(configText.charAt(index));

            index++;

        }
        if (index >= configText.length()) {
            throw new FormatException("Unexpected end of input");
        }

        if (configText.charAt(index) != '"') {
            throw new FormatException("all Strings need a close \"");
        }

        index++; // skip closing "
        return sb.toString();
    }

    private void skipWhitespace() {
        while (index < configText.length() &&
                Character.isWhitespace(configText.charAt(index))) {
            index++;
        }
    }

}

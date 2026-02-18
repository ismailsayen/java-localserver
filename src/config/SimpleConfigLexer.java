package config;

import customError.FormatException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        return result;
    }

    private Map<String, Object> parseObject() throws FormatException {

        Map<String, Object> map = new HashMap<>();
        index++; // skip '{'
        skipWhitespace();

        while (index < configText.length() && configText.charAt(index) != '}') {

            skipWhitespace();
            String key = extractString();

            skipWhitespace();
            if (configText.charAt(index) != ':') {
                throw new FormatException("Expected ':' after key");
            }
            index++; // skip :

            skipWhitespace();
            Object value = parseValue(); // valeur

            map.put(key, value);

            skipWhitespace();

            if (configText.charAt(index) == ',') {
                index++;
            }
            skipWhitespace();
        }
        if (configText.charAt(index) != '}') {
            throw new FormatException("all Object need a close }");
        }


        index++; // skip '}'
        return map;
    }

    private Object parseValue() throws FormatException {

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

        if (current >= '0' && current <= '9') {
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

        throw new FormatException("Invalid boolean value at position " + index);

    }

    private Object parseNull() throws FormatException {
        skipWhitespace();

        if (configText.startsWith("null", index)) {
            index += 4;
            return null;
        }

        throw new FormatException("Invalid null value at position " + index);

    }

    private List<Object> parseArray() throws FormatException {

        List<Object> list = new ArrayList<>();
        index++;
        skipWhitespace();

        while (index < configText.length() && configText.charAt(index) != ']') {

            skipWhitespace();
            list.add(parseValue());

            skipWhitespace();

            if (configText.charAt(index) == ',') {
                index++;
            }

            skipWhitespace();
        }
        skipWhitespace();
        if (configText.charAt(index) != ']') {
            throw new FormatException("all Arrays need a close ]");
        }


        index++; // skip ']'
        return list;
    }

    private String extractNumber() {

        StringBuilder sb = new StringBuilder();

        while (index < configText.length() && configText.charAt(index) != '"') {
            sb.append(configText.charAt(index));
            index++;
        }

        return sb.toString();
    }

    private String extractString() throws FormatException {

        index++; // skip opening "

        StringBuilder sb = new StringBuilder();

        while (index < configText.length() && configText.charAt(index) != '"') {
            System.out.println(sb.toString()+"---->"+index);

            if (index + 1 < configText.length() && configText.charAt(index) == '\\' && configText.charAt(index + 1) == '"' ) {
                System.out.println("te");
                index++;

            }

            sb.append(configText.charAt(index));

            index++;
            System.out.println(sb.toString()+"==>"+index);

        }

        if (index >= configText.length() || configText.charAt(index) != '"'  ) {
            System.out.println("yoooooooo");
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

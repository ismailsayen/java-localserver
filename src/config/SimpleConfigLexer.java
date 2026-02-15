package config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleConfigLexer {

    private final String configText;
    private final List<Token> tokens;
    private Integer index;

    public SimpleConfigLexer(String configText) {
        this.configText = configText;
        this.index = 0;
        this.tokens = tokenize();
    }

    public List<Token> getTokens() {
        return tokens;
    }

    private List<Token> tokenize() {
        skipWhitespace();

        Object result = parseValue(); 

        System.out.println("result = " + result);

        return tokens;
    }

    private Map<String, Object> parseObject() {

        Map<String, Object> map = new HashMap<>();
        index++; // skip '{'
        skipWhitespace();

        while (index < configText.length() && configText.charAt(index) != '}') {

            skipWhitespace();
            String key = extractString();

            skipWhitespace();
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

        index++; // skip '}'
        return map;
    }

    private Object parseValue() {

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

        return null;
    }

    private List<Object> parseArray() {

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

        index++; // skip ']'
        return list;
    }

    private String extractString() {

        index++; // skip opening "

        StringBuilder sb = new StringBuilder();

        while (index < configText.length() && configText.charAt(index) != '"') {
            sb.append(configText.charAt(index));
            index++;
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

package config;

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
        Character charachtere = configText.charAt(index);

        if (charachtere == '{') {

            Map<String, Object> object = parseObject();
            System.out.println(object);
        }
        return tokens;
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new HashMap<>();
        index++; // skip '{'
        skipWhitespace();
        String key = extractKey();
        map.put(key, key);
        System.out.println("KEY=>"+key);
        while (configText.charAt(index) != '}' && index < configText.length() - 1) {
            if (configText.charAt(index) == ':') {
                System.out.println("zz");
                index++;
            }
            if (configText.charAt(index) == ',') {
                break;
            }
            if (configText.charAt(index) == '{') {
                Map<String, Object> object1 = parseObject();
                System.out.println("recursive" + object1);
            }
            if (configText.charAt(index) == '[') {
             parseArray();
            }

            index++;

            System.out.println("obj" + configText.charAt(index));

        }
        return map;

    }

    private void parseArray() {
       
        index++; // skip '['
        skipWhitespace();
        // String key = extractKey();
       

        while (configText.charAt(index) != '}' && index < configText.length() - 1) {
            
            

            index++;

            System.out.println("arr" + configText.charAt(index));

        }
 

    }

    private String extractKey() {
        index++;
        StringBuilder key = new StringBuilder();
        while (configText.charAt(index) != '"' && index < configText.length() - 1) {
            if (configText.charAt(index) == '\\' && configText.charAt(index + 1) == '"') {
                index++;
            }
            key.append(configText.charAt(index));
            index++;
        }

        return key.toString().trim();
    }

    private void skipWhitespace() {
        while (index < configText.length() &&
                Character.isWhitespace(configText.charAt(index))) {
            index++;
        }
    }

}

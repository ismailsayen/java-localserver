package config;

import java.util.List;

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
            index++;
            while (charachtere != '}' && index < configText.length()) {
                skipWhitespace();
                if (configText.charAt(index) != '\n') {
                    System.out.println(configText.charAt(index));
                    index++;
                } else {
                    index++;
                }

            }
        }
        return tokens;
    }

    private void skipWhitespace() {
        while (index < configText.length() &&
                Character.isWhitespace(configText.charAt(index))) {
            index++;
        }
    }
}

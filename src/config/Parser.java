package config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;



public class Parser {
    private final  String configPath;
    private String content;

    public Parser(String configPath) {
        this.configPath = configPath;
        this.content = "";
    }

    public String getConfigPath() {
        return this.configPath;
    }

    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public Object parse() throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(this.configPath))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            this.content = sb.toString();
        }

        return new SimpleConfigLexer(this.content).getTokens();
    }
}

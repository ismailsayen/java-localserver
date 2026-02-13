import config.Parser;

public class Main {
    public static void main(String[] args) throws Exception {

        Parser pa = new Parser("config.json");
        pa.parse();
    }
}

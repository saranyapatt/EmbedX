package EmbedX;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String token = (dotenv.get("BOT_TOKEN"));
        String username = ("BOT_USERNAME");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("ไม่พบ BOT_TOKEN ใน environment variables");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("ไม่พบ BOT_USERNAME ใน environment variables");
        }

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new HachFix.org.Bot(username, token));

        System.out.println("Bot started as @" + username);
    }
}

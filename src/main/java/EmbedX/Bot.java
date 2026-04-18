package EmbedX;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bot extends TelegramLongPollingBot {
    private final String username;
    private final String token;
    private long botStartTime = System.currentTimeMillis() / 1000;
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://)?(www\\.)?(x\\.com|twitter\\.com)/\\S+",
            Pattern.CASE_INSENSITIVE
    );
    private static final String TARGET_DOMAIN = "fxtwitter.com";

    public Bot(String username, String token) {
        this.username = username;
        this.token = token;
    }

    @Override public String getBotUsername() { return username; }
    @Override public String getBotToken() { return token; }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;
        Message msg = update.getMessage();
        if (msg.getChat() == null) return;

        if (msg.getDate() < botStartTime) {
            return;
        }

        String chatType = msg.getChat().getType();
        if (!"group".equals(chatType) && !"supergroup".equals(chatType)) return;

        if (!msg.hasText()) return;

        List<String> urls = extractXorTwitterUrls(msg);
        if (urls.isEmpty()) return;

        List<String> converted = urls.stream()
                .map(this::convertToEmbedUrl)
                .distinct()
                .toList();

        for (String u : converted) {
            SendMessage out = SendMessage.builder()
                    .chatId(msg.getChatId().toString())
                    .replyToMessageId(msg.getMessageId())
                    .text(u)
                    .disableWebPagePreview(false)
                    .build();
            try { execute(out); } catch (TelegramApiException e) { e.printStackTrace(); }
        }
    }

    private List<String> extractXorTwitterUrls(Message msg) {
        Set<String> hits = new LinkedHashSet<>();
        String text = msg.getText();

        if (msg.hasEntities()) {
            for (MessageEntity ent : msg.getEntities()) {
                String type = ent.getType();
                if ("url".equals(type)) {
                    String url = text.substring(ent.getOffset(), ent.getOffset() + ent.getLength());
                    if (isTargetDomain(url)) hits.add(normalizeUrl(url));
                } else if ("text_link".equals(type) && ent.getUrl() != null) {
                    String url = ent.getUrl();
                    if (isTargetDomain(url)) hits.add(normalizeUrl(url));
                }
            }
        }

        Matcher m = URL_PATTERN.matcher(text);
        while (m.find()) {
            hits.add(normalizeUrl(m.group()));
        }
        return new ArrayList<>(hits);
    }

    private boolean isTargetDomain(String url) {
        String u = url.toLowerCase(Locale.ROOT);
        return u.contains("://x.com/") || u.contains("://www.x.com/")
                || u.contains("://twitter.com/") || u.contains("://www.twitter.com/")
                || (!u.startsWith("http") && (u.startsWith("x.com/") || u.startsWith("twitter.com/")));
    }

    private String normalizeUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        return "https://" + url;
    }

    private String convertToEmbedUrl(String original) {
        String u = normalizeUrl(original);
        return u.replaceFirst("https?://(www\\.)?x\\.com", "https://" + TARGET_DOMAIN)
                .replaceFirst("https?://(www\\.)?twitter\\.com", "https://" + TARGET_DOMAIN);
    }
}

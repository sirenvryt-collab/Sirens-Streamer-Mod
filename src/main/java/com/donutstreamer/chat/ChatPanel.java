package com.donutstreamer.chat;

import com.donutstreamer.Ui;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * On-screen Twitch / YouTube chat (a normal HUD widget: draggable, scalable, toggleable). Shows the newest 8
 * lines with a small platform marker, a coloured name and the message. Older lines fade out. Names and message
 * text are trimmed to fit once per message on the render thread and cached; each frame only draws strings.
 */
public final class ChatPanel {
    private ChatPanel() {}

    public static final int WIDTH = 230;
    public static final int HEIGHT = 100;
    public static final int ACCENT = 0xFF9146FF;

    private static final int LINES = 8;
    private static final int LABEL = 0xFF8B949E;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int TWITCH = 0xFF9146FF;
    private static final int YOUTUBE = 0xFFFF3B3B;
    private static final int NAME_MAX = 78;

    private static final ChatMessage[] VIEW = new ChatMessage[LINES];
    private static String hint = "";
    private static String hintFor = null;

    public static int width(TextRenderer tr) {
        return WIDTH;
    }

    private static String fit(TextRenderer tr, String s, int maxWidth) {
        if (tr.getWidth(s) <= maxWidth) return s;
        return tr.trimToWidth(s, Math.max(0, maxWidth - tr.getWidth("..."))) + "...";
    }

    private static void layout(TextRenderer tr, ChatMessage m, int w) {
        if (m.laidOutFor == w) return;
        m.nameShown = fit(tr, m.name, NAME_MAX);
        m.nameWidth = tr.getWidth(m.nameShown);
        int textX = 16 + m.nameWidth + tr.getWidth(": ");
        m.textShown = fit(tr, m.text, w - textX - 7);
        m.laidOutFor = w;
    }

    /** Draws inside the widget box (origin = top-left; the card background is already drawn). */
    public static void draw(DrawContext ctx, TextRenderer tr, int w, int h) {
        long now = System.currentTimeMillis();

        // header: marker per connected platform, title, status
        ctx.fill(2, 1, w - 2, 13, 0x12FFFFFF);
        int mx = 8;
        if (StreamChat.twitchOn()) {
            ctx.fill(mx, 5, mx + 4, 9, TWITCH);
            mx += 6;
        }
        if (StreamChat.youtubeOn()) {
            ctx.fill(mx, 5, mx + 4, 9, YOUTUBE);
            mx += 6;
        }
        if (mx == 8) {
            ctx.fill(mx, 5, mx + 4, 9, 0x55FFFFFF);
            mx += 6;
        }
        ctx.drawTextWithShadow(tr, "STREAM CHAT", mx + 2, 3, 0xFFB794F6);
        ctx.fill(6, 13, w - 6, 14, 0x18FFFFFF);

        int n = StreamChat.newest(VIEW);
        if (n == 0) {
            String status = StreamChat.configured() ? StreamChat.statusLine() : "Set up: /ds chat twitch <channel>";
            if (!status.equals(hintFor)) {
                hintFor = status;
                hint = fit(tr, status, w - 16);
            }
            ctx.drawTextWithShadow(tr, hint, 8, 20, LABEL);
            if (!StreamChat.configured()) {
                ctx.drawTextWithShadow(tr, "or /ds chat youtube <link>", 8, 32, LABEL);
            }
            return;
        }

        int y = h - 4 - n * 10;
        for (int i = 0; i < n; i++) {
            ChatMessage m = VIEW[i];
            VIEW[i] = null;
            layout(tr, m, w);
            long age = now - m.time;
            int alpha = age > 90_000 ? 0x66 : (age > 40_000 ? 0xAA : 0xFF);
            ctx.fill(8, y + 2, 12, y + 6, Ui.withAlpha(m.platform == ChatMessage.YOUTUBE ? YOUTUBE : TWITCH, alpha));
            ctx.drawTextWithShadow(tr, m.nameShown, 16, y, Ui.withAlpha(m.color, alpha));
            ctx.drawTextWithShadow(tr, ": ", 16 + m.nameWidth, y, Ui.withAlpha(LABEL, alpha));
            ctx.drawTextWithShadow(tr, m.textShown, 16 + m.nameWidth + tr.getWidth(": "), y, Ui.withAlpha(TEXT, alpha));
            y += 10;
        }
    }
}

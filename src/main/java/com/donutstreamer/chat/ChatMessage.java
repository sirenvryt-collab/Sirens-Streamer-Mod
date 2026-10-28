package com.donutstreamer.chat;

/** One line of stream chat. Layout strings are filled lazily on the render thread (text widths need the font). */
final class ChatMessage {
    static final int TWITCH = 0, YOUTUBE = 1;

    final int platform;
    final String name;
    final String text;
    final int color;          // ARGB name colour
    final long time;

    // render-thread cache
    String nameShown;
    String textShown;
    int nameWidth;
    int laidOutFor = -1;      // width the cache was built for

    ChatMessage(int platform, String name, String text, int color) {
        this.platform = platform;
        this.name = name;
        this.text = text;
        this.color = color;
        this.time = System.currentTimeMillis();
    }
}

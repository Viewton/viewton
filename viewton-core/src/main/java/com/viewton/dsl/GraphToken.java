package com.viewton.dsl;

final class GraphToken {
    private final GraphTokenType type;
    private final String text;

    GraphToken(GraphTokenType type, String text) {
        this.type = type;
        this.text = text;
    }

    GraphTokenType getType() {
        return type;
    }

    String getText() {
        return text;
    }
}

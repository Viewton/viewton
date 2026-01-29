package com.viewton.dsl;

import java.math.BigDecimal;

final class GraphTokenizer {
    private final String input;
    private int index;

    GraphTokenizer(String input) {
        this.input = input;
        this.index = 0;
    }

    GraphToken nextToken() {
        skipWhitespace();
        if (index >= input.length()) {
            return new GraphToken(GraphTokenType.EOF, "");
        }
        char current = input.charAt(index);
        switch (current) {
            case '{':
                index++;
                return new GraphToken(GraphTokenType.LBRACE, "{");
            case '}':
                index++;
                return new GraphToken(GraphTokenType.RBRACE, "}");
            case '(':
                index++;
                return new GraphToken(GraphTokenType.LPAREN, "(");
            case ')':
                index++;
                return new GraphToken(GraphTokenType.RPAREN, ")");
            case '[':
                index++;
                return new GraphToken(GraphTokenType.LBRACKET, "[");
            case ']':
                index++;
                return new GraphToken(GraphTokenType.RBRACKET, "]");
            case ':':
                index++;
                return new GraphToken(GraphTokenType.COLON, ":");
            case ',':
                index++;
                return new GraphToken(GraphTokenType.COMMA, ",");
            case '"':
                return readString();
            default:
                if (isIdentifierStart(current)) {
                    return readIdentifier();
                }
                if (isNumberStart(current)) {
                    return readNumber();
                }
                throw new IllegalArgumentException("Unexpected character: " + current);
        }
    }

    private GraphToken readString() {
        index++; // skip opening quote
        StringBuilder builder = new StringBuilder();
        while (index < input.length()) {
            char current = input.charAt(index);
            if (current == '"') {
                index++;
                return new GraphToken(GraphTokenType.STRING, builder.toString());
            }
            if (current == '\\') {
                index++;
                if (index >= input.length()) {
                    throw new IllegalArgumentException("Unterminated string literal");
                }
                char escaped = input.charAt(index);
                builder.append(escaped);
                index++;
                continue;
            }
            builder.append(current);
            index++;
        }
        throw new IllegalArgumentException("Unterminated string literal");
    }

    private GraphToken readIdentifier() {
        int start = index;
        index++;
        while (index < input.length() && isIdentifierPart(input.charAt(index))) {
            index++;
        }
        return new GraphToken(GraphTokenType.IDENTIFIER, input.substring(start, index));
    }

    private GraphToken readNumber() {
        int start = index;
        index++;
        while (index < input.length() && (Character.isDigit(input.charAt(index)) || input.charAt(index) == '.')) {
            index++;
        }
        String numberText = input.substring(start, index);
        new BigDecimal(numberText);
        return new GraphToken(GraphTokenType.NUMBER, numberText);
    }

    private void skipWhitespace() {
        while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
            index++;
        }
    }

    private boolean isIdentifierStart(char current) {
        return Character.isLetter(current) || current == '_';
    }

    private boolean isIdentifierPart(char current) {
        return Character.isLetterOrDigit(current) || current == '_';
    }

    private boolean isNumberStart(char current) {
        return Character.isDigit(current) || current == '-';
    }
}

package com.compiler.model;

/**
 * Token类
 * */
public class Token {
    private int row;    //token所在的行
    private int col;    //token所在的列
    private TokenType type;     //token的类型
    private String content;     //token的内容

    public Token() {
    }

    public Token(int row, int col, TokenType type, String content) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.content = content;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Token{" +
                "row=" + row +
                ", col=" + col +
                ", type=" + type +
                ", content='" + content + '\'' +
                '}';
    }
}

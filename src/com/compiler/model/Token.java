package com.compiler.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Token类
 * */
public class Token {
    private int row;    //token所在的行
    private int col;    //token所在的列
    private TokenType type;     //token的类型
    private String content;     //token的内容
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "if", "while", "for", "continue", "break", "return", "do", "goto", "class", "func",
            "int", "char", "short", "string", "boolean", "long", "float", "double", "auto", "void", "true", "false"
    ));
    private static final Set<String> QUALIFIER = new HashSet<>(Arrays.asList(
            "public", "private", "protected", "final", "static"
    ));
    private static final Set<String> OPERATOR = new HashSet<>(Arrays.asList(
            "+", "-", "*", "/", "!", "~", "^", "|", "||", "&", "&&", "<", ">", "=", "<=", ">=", "==", "++", "--", "%",
            "+=", "-=", "*=", "/="
    ));
    private static final Set<String> SYMBOL = new HashSet<>(Arrays.asList(
            ",", ";", ".", "[", "]", "{", "}", "(", ")", "\"", "'"
    ));

    public Token() {
    }

    public Token(int row, int col, TokenType type, String content) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.content = content;
    }

    public void setType(){
        this.type = genType();
    }

    /**
     * 根据token的content自动设置token的类型
     * @return 匹配的token类型
     * */
    public TokenType genType(){
        //判断是否为常量
        if((content.charAt(0) >= '0' && content.charAt(0) <= '9')
        || (content.charAt(0) == '-' && content.length() >= 2 && content.charAt(1) != '-')
        || (content.charAt(0) == '.' && content.length() > 1)
        || (content.charAt(0) == 'e' && content.length() >= 2 && (content.charAt(1) == '+' || content.charAt(1) == '-'))
        ){
            return TokenType.CONST;
        }
        if(KEYWORDS.contains(content))
            return TokenType.KEYWORDS;
        if(QUALIFIER.contains(content))
            return TokenType.QUALIFIER;
        if(SYMBOL.contains(content))
            return TokenType.SYMBOL;
        if(OPERATOR.contains(content))
            return TokenType.OPERATOR;
        return TokenType.IDENTIFIER;
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
                "row=" + (row + 1) +
                ",\tcol=" + (col + 1) +
                ",\t\tcontent='" + content + '\'' +
                ",\t\ttype=" + type +
                '}';
    }
}

package com.compiler.model;

import java.util.Objects;

/**
 * 文法中的符号
 * */
public class Symbol {
    public static final String EPSILON = "ε";       //空符号
    public static final Symbol END = new Symbol("#",true);
    private final String content;    //符号的具体内容
    private final boolean isEnd;     //该符号是否是结束符号
    private final boolean isVt;      //是否是终结符

    public boolean isVt(String content){
        if(content.compareTo("a") >= 0 && content.compareTo("z") <= 0){
            return true;        //终结符
        }
        return false;       //非终结符
    }

    public Symbol(String content, boolean isEnd) {
        this.content = content;
        this.isEnd = isEnd;
        this.isVt = isVt(content);
    }

    public Symbol(String content) {
        this.content = content;
        this.isEnd = false;
        this.isVt = isVt(content);
    }

    public static String getEPSILON() {
        return EPSILON;
    }

    public String getContent() {
        return content;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public boolean isVt() {
        return isVt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return isEnd == symbol.isEnd && Objects.equals(content, symbol.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, isEnd);
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "content='" + content + '\'' +
                ", isEnd=" + isEnd +
                ", isVt=" + isVt +
                '}';
    }

}

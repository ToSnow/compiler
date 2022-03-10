package com.compiler.model;

import java.util.List;

/**
 *  产生式
 * */
public class Production {
    private final Symbol left;          //产生式左部
    private final List<Symbol> right;   //产生式右部

    public Production(Symbol left, List<Symbol> right) {
        this.left = left;
        this.right = right;
    }

    public Symbol getLeft() {
        return left;
    }

    public List<Symbol> getRight() {
        return right;
    }
}

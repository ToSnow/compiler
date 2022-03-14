package com.compiler.model;

import java.util.ArrayList;
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

    /**
     * 创建一个产生式
     * @param left 产生式左部;
     * @param right 产生式右部（List）
     * @return 创建的产生式
     * */
    public static Production create(Symbol left,List<Symbol> right){
        if(left == null || right.isEmpty()){
            throw new RuntimeException("产生式左/右部不能为空");
        }
        return new Production(left,right);
    }

    public Symbol getLeft() {
        return left;
    }

    public List<Symbol> getRight() {
        return right;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("Production{").append(left.getContent()).append("->");
        for(Symbol symbol : right){
            string.append(symbol.getContent());
            string.append("|");
        }
        //右侧为空语句的情况
        if(right.isEmpty()){
            string.append("ε");
        }
        string.append("}");
        return string.toString();
    }

    public static void main(String args[]){
        List<Symbol> right = new ArrayList<>();
        right.add(new Symbol("C"));
        right.add(new Symbol(Symbol.EPSILON));
        right.add(new Symbol("D"));
        right.add(Symbol.END);
        Symbol left = new Symbol("A");
        Production p = new Production(left,right);
        System.out.println(p.toString());
    }
}
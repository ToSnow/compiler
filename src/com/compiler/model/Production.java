package com.compiler.model;

import java.util.ArrayList;
import java.util.List;

/**
 *  产生式
 * */
public class Production {
    private final Symbol left;          //产生式左部
    private final List<Symbol> right;   //产生式右部
    private final boolean isEpsilon;    //是否是空
    public static int count = 0;       //产生式计数器
    private final int index;            //产生式序号

    public Production(Symbol left, List<Symbol> right) {
        this.left = left;
        this.right = right;
        if(right.size() == 1 && right.get(0).getContent().equals(Symbol.EPSILON))
            isEpsilon = true;
        else
            isEpsilon = false;
        index = count++;
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

    /**
     * 创建一个产生式
     * @param left  产生式左部
     * @param right 产生式右部，字符串中的每一个字符都会被当成一个文法符号
     * @return      创建好的产生式
     * */
    public static Production create(Symbol left,String right){
        if(left == null || right == null || right.isEmpty()){
            throw new RuntimeException("产生式左/右部不能为空");
        }
        List<Symbol> symbolList = new ArrayList<>();
        int length = right.length();
        for(int i = 0; i < length; ++i){
            String str = String.valueOf(right.charAt(i));
            symbolList.add(new Symbol(str));
        }
        return new Production(left,symbolList);
    }

    public static Production create(String left,String right){
        if(left == null || left.isEmpty())
            throw new RuntimeException("产生式左/右部不能为空");
        //将左部封装为Symbol
        Symbol symbol = new Symbol(left);
        return create(symbol,right);
    }

    public Symbol getLeft() {
        return left;
    }

    public List<Symbol> getRight() {
        return right;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("Production").append(index).append("{").append(left.getContent()).
                append("->");
        for(Symbol symbol : right){
            string.append(symbol.getContent());
        }
        string.append("}");
        return string.toString();
    }

    public boolean isEpsilon() {
        return isEpsilon;
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
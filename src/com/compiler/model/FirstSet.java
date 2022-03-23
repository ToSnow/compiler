package com.compiler.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * First集
 * */
public class FirstSet {
    //非终结符
    private final List<Symbol> start;
    //其对应的first集
    private final Set<Symbol> set = new HashSet<>();
    //是否包含ε
    private boolean hasEpsilon;

    /**
     * 构造函数，用于创建一个first集
     * */
    public FirstSet(List<Symbol> start){
        this.start = start;
    }

    /**
     * 用于向first集中添加元素
     * @param next 需要添加的元素
     * */
    public void add(Symbol next){
        set.add(next);
    }

    /**
     * 用于向first集中添加一个元素集合
     * @param set 需要添加的元素集合
     * */
    public void add(Set<Symbol> set){
        this.set.addAll(set);
    }

    public List<Symbol> getStart() {
        return start;
    }

    public Set<Symbol> getSet() {
        return set;
    }

    public boolean isHasEpsilon() {
        return hasEpsilon;
    }

    public void setHasEpsilon(boolean hasEpsilon) {
        this.hasEpsilon = hasEpsilon;
    }

    @Override
    public String toString() {
        //First(A)=a|b
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("First(");
        for(Symbol symbol : start){
            stringBuilder.append(symbol.getContent());
        }
        stringBuilder.append(")=");
        for(Symbol symbol : set){
            stringBuilder.append(symbol.getContent()).append("|");
        }
        if(isHasEpsilon())
            //FIXME:“ε”符号的显示存在问题
            stringBuilder.append(Symbol.EPSILON);
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
}

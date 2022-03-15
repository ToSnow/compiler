package com.compiler.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Follow集
 * */
public class FollowSet {
    //非终结符
    private final Symbol start;
    //非终结符对应的Follow集
    private final Set<Symbol> set = new HashSet<>();

    /**
     * 构造函数，用于创建一个follow集
     * */
    public FollowSet(Symbol start){
        this.start = start;
    }

    /**
     * 用于向follow集中添加元素
     * @param next 需要添加的元素
     * */
    public void add(Symbol next){
        set.add(next);
    }

    /**
     * 用于向follow集中添加一个元素集合
     * @param set 需要添加的元素集合
     * */
    public void add(Set<Symbol> set){
        this.set.addAll(set);
    }

    public Symbol getStart() {
        return start;
    }

    public Set<Symbol> getSet() {
        return set;
    }

    @Override
    public String toString() {
        return "FollowSet{" +
                "start=" + start +
                ", set=" + set +
                '}';
    }
}

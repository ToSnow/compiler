package com.compiler.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  语法类
 * */
public class Grammar {
    private final Symbol start;         //文法的开始符号
    private Set<Symbol> VtSet;    //文法中的终结符
    private final Set<Symbol> VnSet;    //文法中的非终结符
    private Map<Symbol,FirstSet> firstSetMap;   //文法中所有非终结符对应的First集
    private Map<Symbol,FollowSet> followSetMap; //文法中所有非终结符对应的Follow集
    //以symbol为键，记录以symbol开头的产生式
    private LinkedHashMap<Symbol, List<Production>> productionMap;

    /**
     * 构造函数
     * VtSet和 VnSet 由函数自动判断进行生成
     * @param start 文法的开始符号
     * @param productionMap 以Symbol为键的产生式集合
     * */
    public Grammar(Symbol start,LinkedHashMap<Symbol, List<Production>> productionMap){
        this.start = start;
        this.productionMap = productionMap;
        //非终结符集合一定是产生式的键值
        this.VnSet = productionMap.keySet();
        //终结符集合需要遍历获得
        for(Map.Entry<Symbol,List<Production>> entry : productionMap.entrySet()){
            for(Production production : entry.getValue()){
                for(Symbol symbol : production.getRight()){
                    if(symbol.isVt())
                        this.VtSet.add(symbol);
                }
            }
        }
    }

    public Symbol getStart() {
        return start;
    }

    public Set<Symbol> getVtSet() {
        return VtSet;
    }

    public Set<Symbol> getVnSet() {
        return VnSet;
    }

    public Map<Symbol, FirstSet> getFirstSetMap() {
        return firstSetMap;
    }

    public Map<Symbol, FollowSet> getFollowSetMap() {
        return followSetMap;
    }

    public LinkedHashMap<Symbol, List<Production>> getProductionMap() {
        return productionMap;
    }
}

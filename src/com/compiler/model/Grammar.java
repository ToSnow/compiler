package com.compiler.model;

import java.util.*;

/**
 *  语法类
 * */
public class Grammar {
    private final Symbol start;         //文法的开始符号
    private Set<Symbol> VtSet;          //文法中的终结符
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

    /**
     * 创建文法的函数
     * @param start 文法的开始符号
     * @param productions  文法的产生式集合
     * */
    public static Grammar creat(Symbol start,Production... productions){
        if(productions.length == 0)
            throw new RuntimeException("产生式为空！");
        //从可变参数中获取产生式列表
        List<Production> productionList = new ArrayList<>(Arrays.asList(productions));
        return creat(start, productionList);
    }

    /**
     * 创建文法的函数
     * 最后生成的文法形式为：
     * A -> a | b
     * B -> A | Bb
     * @param start 文法的开始符号
     * @param productionList 产生式列表
     * */
    public static Grammar creat(Symbol start,List<Production> productionList){
        if(productionList == null || productionList.isEmpty()){
            throw new RuntimeException("产生式为空");
        }
        if(!start.equals(productionList.get(0).getLeft())){
            throw new RuntimeException("第一个产生式左部应该与开始符号相同！");
        }
        LinkedHashMap<Symbol,List<Production>> symbolListLinkedHashMap = new LinkedHashMap<>();
        for(Production p : productionList){
            Symbol left = p.getLeft();
            //获取左符号对应的所有产生式
            List<Production> leftProductions = symbolListLinkedHashMap.computeIfAbsent(left, k -> new ArrayList<>());
            /*
            //等效代码
            if(leftProductions == null){
                //没有则新建
                leftProductions = new ArrayList<>();
                symbolListLinkedHashMap.put(left,leftProductions);
            }
            */
            //添加产生式
            leftProductions.add(p);
        }
        return new Grammar(start, symbolListLinkedHashMap);
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

package com.compiler.lr1;

import com.compiler.model.*;

import java.util.*;

/**
 * LR(1)语法分析的工具类
 * */
public class LR1Utils {

    /**
     * LR(1)项目集的闭包函数
     * ①假定I是一个项目集，I的任何项目都属于CLOSURE(I)
     * ②若有项目A->α•Bβ,a属于CLOSURE(I),B->δ是文法中的产生式，β∈V*，b∈FIRST(βa),则B->•δ,b也属于CLOSURE(I)
     * ③重复②，直到CLOSURE(I)不再增大为止
     * */
    public static Set<ProductionItem> closure(Set<ProductionItem> itemSet, Grammar grammar){
        Set<ProductionItem> resultItems = new HashSet<>();      //结果项目集
        Stack<ProductionItem> stack = new Stack<>();            //用于判断项目集是否不再增大
        stack.addAll(itemSet);      //栈初始化
        while(!stack.isEmpty()){
            //获取文法的项目
            ProductionItem productionItem = stack.pop();
            int delimiterPos = productionItem.getDelimiterPos();    //获取分隔符位置
            Symbol delimiterSymbol;     //记录分割符的后一个元素
            if(delimiterPos == productionItem.getProduction().getRight().size())
                //说明分割符在最后
                delimiterSymbol=Symbol.END;
            else delimiterSymbol = productionItem.getProduction().getRight().get(delimiterPos);
            //判断分隔符的后一个元素是否是非终结符
            if(!delimiterSymbol.isVt()){
                //是非终结符则获取该非终结符对应的产生式
                List<Production> productionList = grammar.getProductionMap().get(delimiterSymbol);
                //获取FIRST(βa)中的βa
                List<Symbol> firstSymbols = productionItem.getFirstSymbol();
                //获取FIRST(βa)
                Set<Symbol> firstSet = grammar.getFirstSetBySymbols(firstSymbols).getSet();
                //根据First集合，生成项目集中新增的项目
                for (Production production : productionList){
                    for(Symbol symbol : firstSet){
                        ProductionItem currentItem = ProductionItem.create(production,symbol);
                        //查找原项目集
                        if(!resultItems.contains(currentItem)){
                            //原项目集中没有当前项目则添加
                            resultItems.add(currentItem);
                            //将其添加到处理栈中，进行闭包运算
                            stack.push(currentItem);
                        }
                    }
                }
            }
        }
        return resultItems;
    }

    //GOTO函数的缓存，避免重复计算
    public static final Map<ProductionItemSet,Map<Symbol,ProductionItemSet>> GOTO_MAP = new HashMap<>();
    /**
     * 项目集的转换函数GOTO
     * GOTO(I,X) = CLOSURE(J)
     * I为 LR(1)的项目集,X是文法符号,J={任何形如[A->αX•β,a]的项目 | [A->α•Xβ,a]∈I}
     * 首先以[S'->•S,#]为初态集的初始项目，对其求闭包和转换函数，直到项目集不再增大为止
     * @param productionItemSet 原项目集
     * @param symbol            当前匹配的文法符号
     * @param grammar           文法
     * @return                  GOTO操作后的项目集(可能与原项目集相同),也可能为空
     * */
    public static ProductionItemSet Goto(ProductionItemSet productionItemSet,Symbol symbol,Grammar grammar){
        if(GOTO_MAP.containsKey(productionItemSet)){
            Map<Symbol,ProductionItemSet> map = GOTO_MAP.get(productionItemSet);
            if(map.containsKey(symbol))
                //如果GOTO_MAP中包含当前的项目集和需要匹配的文法符号，则直接返回，否则进行求解
                return map.get(symbol);
        }
        Set<ProductionItem> currentProductionItemSet = new HashSet<>();
        for(ProductionItem productionItem : productionItemSet.getProductionItemSet()){
            //对于原项目集中的每一个文法项目，判断其是否与文法符号匹配
            int delimiterPos = productionItem.getDelimiterPos();    //获取分割符位置
            if(delimiterPos < productionItem.getProduction().getRight().size()){
                //只有当分隔符不在文法项目的末尾时才可以进行匹配
                Symbol nextSymbol = productionItem.getProduction().getRight().get(delimiterPos);
                if(nextSymbol.equals(symbol)){
                    //如果相同（匹配）,则将分隔符进行移动，并将移动后的结果到结果集中
                    currentProductionItemSet.add(ProductionItem.create(productionItem));
                }
            }
        }
        if(currentProductionItemSet.isEmpty())  return null;
        //创建后继项目集
        ProductionItemSet resultItemSet = ProductionItemSet.create(closure(currentProductionItemSet,grammar));
        //添加到GOTO_MAP中
        LR1Utils.addToDoubleMap(GOTO_MAP,productionItemSet,symbol,resultItemSet);
        return resultItemSet;
    }

    public static <OuterK, InnerK, InnerV> InnerV addToDoubleMap(
            Map<OuterK, Map<InnerK, InnerV>> outerMap, OuterK outerK, InnerK innerK, InnerV innerV) {
        Map<InnerK, InnerV> innerMap = outerMap.computeIfAbsent(outerK, k -> new HashMap<>());
        return innerMap.put(innerK, innerV);
    }
}

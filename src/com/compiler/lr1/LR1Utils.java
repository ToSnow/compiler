package com.compiler.lr1;

import com.compiler.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

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

}

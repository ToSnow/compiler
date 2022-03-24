package com.compiler.lr1;

import com.compiler.model.Grammar;
import com.compiler.model.Production;
import com.compiler.model.ProductionItemSet;
import com.compiler.model.Symbol;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LR1Test {

    public static void main(String[] args){
        //创建文法符号
        Symbol start = new Symbol("S'");
        //创建语法
        /*
         * S'-> S
         * S -> aAd
         * S -> bAc
         * S -> aec
         * S -> bed
         * A -> e
         * */
        Grammar grammar = Grammar.creat(start,
                Production.create(start,"S"),
                Production.create("S","aAd"),
                Production.create("S","bAc"),
                Production.create("S","aec"),
                Production.create("S","bed"),
                Production.create("A","e")
        );
//        Grammar grammar = Grammar.creat(start,
//                Production.create(start,"S"),
//                Production.create("S","AB"),
//                Production.create("S","bC"),
//                Production.create("A","ε"),
//                Production.create("A","b"),
//                Production.create("B","ε"),
//                Production.create("B","aD"),
//                Production.create("C","AD"),
//                Production.create("C","b"),
//                Production.create("D","aS"),
//                Production.create("D","c")
//        );
//        Grammar grammar = Grammar.creat(start,
//                Production.create(start,"S"),
//                Production.create("S","BB"),
//                Production.create("B","aB"),
//                Production.create("B","b")
//        );
//        Grammar grammar = Grammar.creat(start,
//                Production.create(start,"S"),
//                Production.create("S","LaR"),
//                Production.create("S","R"),
//                Production.create("L","bR"),
//                Production.create("L","i"),
//                Production.create("R","L")
//        );
        LinkedHashMap<Symbol, List<Production>> map = grammar.getProductionMap();
        //输出产生式
        for(Map.Entry<Symbol,List<Production>> m : map.entrySet()){
            List<Production> productionList = m.getValue();
            for(Production production : productionList){
                System.out.println(production);
            }
        }
        List<ProductionItemSet> productionItemSetList = LR1Utils.generateProductionItemSets(grammar);
        System.out.println(grammar);
        for(ProductionItemSet productionItemSet : productionItemSetList){
            System.out.println(productionItemSet);
        }
        //创建LR1分析表
        Map<ProductionItemSet,Map<Symbol,ActionItem>> actionMap = new HashMap<>();
        Map<ProductionItemSet,Map<Symbol,GotoItem>> gotoMap = new HashMap<>();
        System.out.println("LR(1)分析表：");
        LR1Utils.createLR1Table(grammar,productionItemSetList,actionMap,gotoMap);
    }
}

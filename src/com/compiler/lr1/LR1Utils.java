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
        resultItems.addAll(itemSet);        //先添加原项目
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
                //FIXME:对于含空串的产生式，这里将发生空指针错误
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

    /**
     * 生成当前文法的项目集集合
     * @param grammar 文法
     * @return        项目集集合
     * */
    public static List<ProductionItemSet> generateProductionItemSets(Grammar grammar){
        Production startProduction = grammar.getProductionMap().get(grammar.getStart()).get(0);
        //创建增广文法对应的项目
        ProductionItem startProductionItem = ProductionItem.create(startProduction,Symbol.END);
        //封装为Set
        Set<ProductionItem> tmpSet = new HashSet<>();
        tmpSet.add(startProductionItem);
        //创建对应的项目集
        ProductionItemSet startProductionItemSet = ProductionItemSet.create(closure(tmpSet, grammar));
        List<ProductionItemSet> resultItemSets = new ArrayList<>();
        resultItemSets.add(startProductionItemSet);
        //使用栈来进行项目集的求闭包操作
        Stack<ProductionItemSet> stack = new Stack<>();
        stack.push(startProductionItemSet);
        while (!stack.isEmpty()){
            ProductionItemSet currentItemSet = stack.pop();
            //对项目集中的每一个文法项目，求其可能的后继文法符号
            List<Symbol> nextSymbolList = new ArrayList<>();
            for(ProductionItem productionItem : currentItemSet.getProductionItemSet()){
                int delimiterPos = productionItem.getDelimiterPos();
                int productionLength = productionItem.getProduction().getRight().size();
                if(delimiterPos < productionLength){
                    //只有分隔符不在最后时才有后继的文法符号
                    //获取分隔符之后的符号
                    nextSymbolList.add(productionItem.getProduction().getRight().get(delimiterPos));
                }
            }
            //对于每一个可能的后继符号，用GOTO函数求其后继项目集
            for(Symbol symbol : nextSymbolList){
                ProductionItemSet nextItemSet = Goto(currentItemSet,symbol,grammar);
                //如果得到的后继项目集在结果集中没有出现，则加入到结果集
                if(nextItemSet != null && !resultItemSets.contains(nextItemSet)){
                    resultItemSets.add(nextItemSet);
                    //将其加入到栈中
                    stack.push(nextItemSet);
                }
            }
        }
        return resultItemSets;
    }

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
        LinkedHashMap<Symbol,List<Production>> map = grammar.getProductionMap();
        List<ProductionItemSet> productionItemSetList = generateProductionItemSets(grammar);
        System.out.println(grammar);
        for(ProductionItemSet productionItemSet : productionItemSetList){
            System.out.println(productionItemSet);
        }
    }
}

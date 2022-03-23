package com.compiler.model;

import java.io.File;
import java.util.*;

/**
 *  语法类
 * */
public class Grammar {
    private final Symbol start;         //文法的开始符号
    private final Set<Symbol> VtSet;          //文法中的终结符
    private final Set<Symbol> VnSet;    //文法中的非终结符
    private Map<Symbol,FirstSet> firstSetMap;   //文法中所有非终结符对应的First集
    private Map<Symbol,FollowSet> followSetMap; //文法中所有非终结符对应的Follow集
    //以symbol为键，记录以symbol开头的产生式
    private final LinkedHashMap<Symbol, List<Production>> productionMap;

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
        this.VtSet = new HashSet<>();
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

    /**
     * 求解非终结符的 First集合
     * 求法：
     * 对于产生式 A -> Xβ
     * ① 若 X ∈ Vt，则 FIRST(A) = {X}
     * ② 若 X ∈ Vn，且有产生式X ->δ,δ ∈ Vt，则δ ∈ FIRST(A) (非终结符，将首个终结符加入First集)
     * ③ 若 X ∈ Vn，X →ε，则ε ∈ FIRST(A) (直接推导)
     * ④ 若 X → Y1,Y2,……,Yn ∈ Vn,且Y1,Y2,……,Y(i-1)直接推出ε时,
     *    FIRST(Y1) -ε, FIRST(Y2) -ε, …… ,FIRST(Yi)都包含在FIRST(X)中(无ε)
     * ⑤ 若 ④ 中所有 Yi 都推出ε时,FIRST(X) = FIRST(Y1) ∪ …… ∪ FIRST(Yn) ∪ {ε}
     * 反复运用 ② - ⑤ 步骤计算，直到FIRST集合不再增大为止
     * @param VNSymbol 非终结符
     * @return         该非终结符对应的First集
     * */
    public FirstSet getFirstSet(Symbol VNSymbol){
        //已经求过则直接返回
        if(firstSetMap.containsKey(VNSymbol))
            return firstSetMap.get(VNSymbol);
        //创建一个First集
        FirstSet firstSet = new FirstSet(Collections.singletonList(VNSymbol));
        //获取该非终结符对应的产生式
        List<Production> productionList = productionMap.get(VNSymbol);
        List<Production> badProductions = new ArrayList<>();
        for(Production production : productionList){
            if(!production.isEpsilon()){
                //该产生式非空
                List<Symbol> rightSymbols = production.getRight();
                if(rightSymbols.contains(VNSymbol)){
                    /*
                    * 当产生式右部包含左部符号时，形如A->aAb时
                    * 则会出现递归调用的死循环的情况，需要特殊处理
                    * */
                    badProductions.add(production);
                }
                else{
                    boolean isAllEpsilon = true;    //记录右侧的非终结符的First集合都包含ε
                    //遍历产生式右部
                    for(Symbol symbol : rightSymbols){
                        if(symbol.isVt()){
                            //该字符是终结符，则直接得到First集合
                            firstSet.add(symbol);
                            isAllEpsilon = false;
                            break;
                        }
                        //如果是非终结符X，则递归调用这个函数
                        //FIXME:对于左递归文法，如 A -> Ba, B -> A, 调用时会产生死循环
                        FirstSet nextFirstSet = getFirstSet(symbol);
                        firstSet.add(nextFirstSet.getSet());
                        //如果X包含空，则继续往下一个字符推导
                        if(!nextFirstSet.isHasEpsilon()){
                            isAllEpsilon = false;
                            break;
                        }
                    }
                    if(isAllEpsilon){
                        firstSet.setHasEpsilon(true);
                    }
                }
            }
            else{
                firstSet.setHasEpsilon(true);   //First集为ε
            }
        }
        //对形如A -> aAb的产生式进行特殊处理
        for(Production production : badProductions){
            List<Symbol> rightSymbols = production.getRight();
            for(Symbol symbol : rightSymbols){
                if(symbol.isVt()) {
                    //是终结符
                    firstSet.add(symbol);
                    break;
                }
                if(symbol.equals(VNSymbol)){
                    //形如A -> Aa 的形式
                    if(!firstSet.isHasEpsilon())
                        //如果A不能推出空，则该产生式是无效产生式，直接跳出
                        break;
                }
                else{
                    //形如A -> Ba 的形式
                    FirstSet nextFirstSet = getFirstSet(symbol);
                    firstSet.add(nextFirstSet.getSet());
                    if(!nextFirstSet.isHasEpsilon())
                        //如果B能推出空，则继续查找下一个字符，否则跳出
                        break;
                }
            }
        }
        firstSetMap.put(VNSymbol,firstSet);
        return firstSet;
    }

    /**
     * 获取所有非终结符对应的First集
     * @return 以非终结符为键，值为该非终结符的 First集
     * */
    public Map<Symbol,FirstSet> getFirstSetMap(){
        if(firstSetMap != null)
            //求过了就不用再求一遍
            return firstSetMap;
        firstSetMap = new HashMap<>();
        productionMap.keySet().forEach(
                this::getFirstSet
        );
        return firstSetMap;
    }

    /**
     * 获取文法符号串对应的First集
     * @param symbols 文法符号串，如BaC
     * @return        该文法符号串的First集合
     * */
    public FirstSet getFirstSetBySymbols(List<Symbol> symbols){
        //求解所有非终结符对应的First集
        Map<Symbol,FirstSet> firstSetMap = getFirstSetMap();
        //FIXME 由于Symbol类的限制，这里实际只传了一个Symbol进去，而不是整个列表，可能导致最后结果错误
        FirstSet firstSet = new FirstSet(symbols);
        boolean isAllEpsilon = true;    //产生式中的所有元素是都包含空
        for(Symbol symbol : symbols){
            //终结符则直接得出结果
            if(symbol.isVt()){
                firstSet.add(symbol);
                isAllEpsilon = false;
                break;
            }
            //非终结符则查找map
            FirstSet current = firstSetMap.get(symbol);
            if(current == null)
                continue;
            firstSet.add(current.getSet());
            //如果当前非终结的First集包含空串，则需要遍历下一个，否则直接得出结果
            if(!current.isHasEpsilon()){
                isAllEpsilon = false;
                break;
            }
        }
        if(isAllEpsilon)
            firstSet.setHasEpsilon(true);
        return firstSet;
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

    public Map<Symbol, FollowSet> getFollowSetMap() {
        return followSetMap;
    }

    public LinkedHashMap<Symbol, List<Production>> getProductionMap() {
        return productionMap;
    }

    @Override
    public String toString() {
        //生成Grammar头部
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Grammar{\n");
        //输出所有产生式
        stringBuilder.append("\tProductions{\n");
        for(Map.Entry<Symbol,List<Production>> entry : productionMap.entrySet()){
            //先读入产生式左部
            stringBuilder.append("\t\t").append(entry.getKey().getContent()).append("->");
            for(Production production : entry.getValue()){
                for(Symbol symbol : production.getRight()){
                    stringBuilder.append(symbol.getContent());
                }
                //插入产生式之间的分隔符
                stringBuilder.append("|");
            }
            //去除多余的分隔符
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            //插入换行符
            stringBuilder.append("\n");
        }
        stringBuilder.append("\t}\n");
        //输出所有非终结符的First集
        stringBuilder.append("\tFirstSet{\n");
        for(Map.Entry<Symbol,FirstSet> entry : firstSetMap.entrySet()){
            stringBuilder.append("\t\t").append(entry.getValue().toString()).append("\n");
        }
        stringBuilder.append("\t}\n");
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}

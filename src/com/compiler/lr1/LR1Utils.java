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
    /**
     * 原ProductionItemSet + Symbol = 现ProductionItemSet
     * */
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

    /**
     * 得到Action表和Goto表
     * @param grammar       in   语法
     * @param productionItemSetList in  项目集
     * @param actionTable   out  action表
     * @param gotoTable     out  goto表
     */
    public static void createLR1Table(Grammar grammar,List<ProductionItemSet> productionItemSetList,
                                      Map<ProductionItemSet, Map<Symbol,ActionItem>> actionTable,
                                      Map<ProductionItemSet, Map<Symbol,GotoItem>> gotoTable){
        //获取语法的开始符号
        Symbol start = grammar.getStart();
        //遍历文法的所有项目集
        //Collections.sort(productionItemSetList);
        for(ProductionItemSet itemSet : productionItemSetList){
            //遍历项目集中的项目
            for(ProductionItem item : itemSet.getProductionItemSet()){
                //获得当前项目的符号
                Symbol currentSymbol;
                {
                    int position = item.getDelimiterPos();
                    List<Symbol> right = item.getProduction().getRight();
                    int length = right.size();
                    if(position < length)
                        currentSymbol = right.get(position);
                    else currentSymbol = Symbol.END;
                }
                //非终结符，则加入到GOTO表中
                if(!currentSymbol.isVt()){
                    ProductionItemSet productionItemSet = Goto(itemSet,currentSymbol,grammar);
                    if (productionItemSet != null) {
                        GotoItem gotoItem = new GotoItem(productionItemSet);
                        //TODO:增加冲突处理
                        addToDoubleMap(gotoTable,itemSet,currentSymbol,gotoItem);
                    }
                }
                else {
                    //如果是终结符且是结束符号
                    if(Symbol.END.equals(currentSymbol)){
                        //项目为A->b•,a的形式，则进行归约操作
                        Production production = item.getProduction();
                        if(start.equals(production.getLeft())){
                            //如果该产生式是S'->S•,#，则为ACC
                            ActionItem actionItem = ActionItem.createActionACC();
                            //TODO:增加冲突处理
                            addToDoubleMap(actionTable,itemSet,currentSymbol,actionItem);
                        }
                        else {
                            //根据展望符进行归约操作
                            ActionItem actionItem = ActionItem.createActionR(production);
                            //TODO:增加冲突处理
                            addToDoubleMap(actionTable,itemSet,item.getExpect(),actionItem);
                        }
                    }
                    else{
                        //如果是终结符但不是结束符号，则进行移进操作
                        ActionItem actionItem = ActionItem.createActionS(Goto(itemSet,currentSymbol,grammar));
                        //TODO:增加冲突处理
                        addToDoubleMap(actionTable,itemSet,currentSymbol,actionItem);
                    }
                }
            }
        }
        //打印LR1分析表
        printLR1Table(productionItemSetList,grammar,actionTable,gotoTable);
    }

    /**
     * 打印LR(1)分析表
     * @param productionItemSetList 项目集
     * @param grammar               语法
     * @param actionMap             action表
     * @param gotoMap               goto表
     * */
    private static void printLR1Table(List<ProductionItemSet> productionItemSetList, Grammar grammar,
                                      Map<ProductionItemSet,Map<Symbol,ActionItem>> actionMap,
                                      Map<ProductionItemSet,Map<Symbol,GotoItem>> gotoMap){
        //打印Action表头
        System.out.println("LR(1)------------ACTION:");
        System.out.print("state\t");
        //所有终结符
        for(Symbol symbol : grammar.getVtSet()){
            System.out.print(symbol.getContent() + "\t");
        }
        System.out.print(Symbol.END.getContent() + "\t");
        System.out.println();
        for(ProductionItemSet itemSet : productionItemSetList){
            System.out.print(itemSet.getIndex() + "\t\t");
            //根据项目集获得action表
            Map<Symbol,ActionItem> map = actionMap.get(itemSet);
            //查找每个终结符
            for(Symbol symbol : grammar.getVtSet()){
                ActionItem actionItem = map.get(symbol);
                if(actionItem != null){
                    System.out.print(actionItem);
                }
                System.out.print("\t");
            }
            //作为终结符的#号
            ActionItem actionItem = map.get(Symbol.END);
            if(actionItem != null){
                System.out.print(actionItem);
            }
            System.out.print("\t");
            System.out.println();
        }
        //打印GOTO表表头
        System.out.println("LR(1)------------GOTO:");
        System.out.print("state\t");
        //对于所有非终结符
        for(Symbol symbol : grammar.getVnSet()){
            //跳过增广文法的S'
            if(symbol.equals(grammar.getStart()))
                continue;
            System.out.print(symbol.getContent() + "\t");
        }
        System.out.println();
        for(ProductionItemSet itemSet : productionItemSetList){
            System.out.print(itemSet.getIndex() + "\t\t");
            Map<Symbol,GotoItem> map = gotoMap.get(itemSet);
            for(Symbol symbol : grammar.getVnSet()){
                //跳过增广文法的S'
                if(map == null || symbol.equals(grammar.getStart()))
                    continue;
                GotoItem gotoItem = map.get(symbol);
                if(gotoItem != null){
                    System.out.print(gotoItem.getNumber());
                }
                System.out.print("\t");
            }
            System.out.println();
        }
    }

    /**
     * 根据生成的Action表和Goto表对输入的符号串进行匹配
     * @param inputSymbols      待匹配的符号串
     * @param startItemSet      开始项目集
     * @param actionMap         Action表
     * @param gotoMap           Goto表
     * @return                  是否匹配成功
     * */
    public static boolean match(List<Symbol> inputSymbols,ProductionItemSet startItemSet,
                                Map<ProductionItemSet,Map<Symbol,ActionItem>> actionMap,
                                Map<ProductionItemSet,Map<Symbol,GotoItem>> gotoMap){
        inputSymbols.add(Symbol.END);
        //状态栈
        Stack<ProductionItemSet> stateStack = new Stack<>();
        //符号栈
        Stack<Symbol> symbolStack = new Stack<>();
        //放入开始状态
        stateStack.push(startItemSet);
        //放入结束符号#
        symbolStack.push(Symbol.END);
        int currentSymbolPosition = 0;  //输入符号串目前读取到的位置
        while(true){
            //获取状态栈栈顶元素
            if(stateStack.isEmpty()){
                System.out.println("匹配错误：状态栈为空！");
                return false;
            }
            ProductionItemSet currentItemSet = stateStack.peek();
            //获取当前待匹配的输入元素
            if(currentSymbolPosition >= inputSymbols.size()){
                System.out.println("匹配错误：输入串为空！");
                return false;
            }
            Symbol currentSymbol = inputSymbols.get(currentSymbolPosition);
            //根据栈顶元素和待匹配的输入元素获取对应的action
            ActionItem actionItem = actionMap.get(currentItemSet).get(currentSymbol);
            GotoItem gotoItem = null;
            if(actionItem == null){
                System.out.println("匹配" + currentSymbol.getContent() + "时出错：未找到与栈顶项目集、栈顶输入元素匹配的Action操作！");
                return false;
            }
            else if(actionItem.getActionType().equals(ActionItem.ACTION_ACC)){
                //如果是ACC
                //只有当输入字符串匹配到最后一个字符#时，才算匹配成功
                if(currentSymbol.equals(Symbol.END)) {
                    printMatch(inputSymbols,stateStack,currentSymbolPosition,actionItem,gotoItem);
                    System.out.println("分析成功！");
                    return true;
                }
                else{
                    //匹配失败
                    System.out.println("匹配" + currentSymbol.getContent() + "时出错：在输入串不为空时匹配了ACC！");
                    return false;
                }
            }
            else if(actionItem.getActionType().equals(ActionItem.ACTION_S)){
                //移进操作
                stateStack.push(actionItem.getProductionItemSet());     //向状态栈中添加新项目集
                symbolStack.push(currentSymbol);    //向符号栈中添加当前符号
                currentSymbolPosition++;    //匹配下一个输入符号
            }
            else if(actionItem.getActionType().equals(ActionItem.ACTION_R)){
                //归约操作
                Production currentProduction = actionItem.getProduction();  //获取归约用的产生式
                //根据产生式右部的符号数目，弹出对应数目的状态栈和符号栈
                int length = currentProduction.getRight().size();
                if(stateStack.size() < length || symbolStack.size() - 1 < length){
                    System.out.println("匹配" + currentSymbol.getContent() + "时出错：归约异常——符号栈/状态栈元素数量小于归约用的产生式右部的长度！");
                    return false;
                }
                for(int i = 0; i < currentProduction.getRight().size(); ++i){
                    stateStack.pop();
                    symbolStack.pop();
                }
                //将产生式左部添加到符号栈
                symbolStack.push(currentProduction.getLeft());
                //获取这个字符在gotoMap中对应的状态，添加到状态栈
               gotoItem = gotoMap.get(stateStack.peek()).get(currentProduction.getLeft());
                if(gotoItem == null){
                    System.out.println("匹配" + currentSymbol.getContent() + "时出错：归约异常——对应的goto表项为空！");
                    return false;
                }
                ProductionItemSet nextItemSet = gotoItem.getNextProductionItemSet();
                stateStack.push(nextItemSet);
            }
            //复制栈,Java对自定义对象是引用传递
            Stack<ProductionItemSet> tmpStack = (Stack<ProductionItemSet>) stateStack.clone();
            printMatch(inputSymbols,tmpStack,currentSymbolPosition,actionItem,gotoItem);
        }
    }

    private static int index = 1;       //当前是第几步
    /**
     * 输出LR(1)的分析过程
     * index=1, state={0}, symbol={#}, input={bccd#}, action=S3, goto=null;
     * @param symbolList                符号串
     * @param currentSymbolPosition     当前匹配到的符号的位置
     * @param stateStack                状态栈
     * @param actionItem                action对象
     * @param gotoItem                  goto对象
     * */
    private static void printMatch(List<Symbol> symbolList,Stack<ProductionItemSet> stateStack,
                                   int currentSymbolPosition,ActionItem actionItem,GotoItem gotoItem){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("index=").append(index).append(",\t");
        index++;
        //从状态栈中提取状态
        Stack<Integer> state = new Stack<>();
        while(!stateStack.isEmpty()){
            ProductionItemSet productionItemSet = stateStack.pop();
            state.push(productionItemSet.getIndex());
        }
        //输出状态栈
        stringBuilder.append("state={");
        while(!state.isEmpty()){
            stringBuilder.append(state.pop()).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("},\t");
        //输出符号栈
        stringBuilder.append("symbol={#");
        int i = 0;
        for(; i < currentSymbolPosition; ++i){
            stringBuilder.append(symbolList.get(i).getContent());
        }
        stringBuilder.append("},\t");
        //输出输入串
        stringBuilder.append("input={");
        for(; i < symbolList.size(); ++i){
            stringBuilder.append(symbolList.get(i).getContent());
        }
        stringBuilder.append("}\t");
        //输出Action
        stringBuilder.append("action=").append(actionItem).append(",\t");
        //输出Goto
        stringBuilder.append("goto=");
        //判断是否需要输出
        if(actionItem.getActionType().equals(ActionItem.ACTION_R)){
            stringBuilder.append(gotoItem.getNumber());
        }else{
            stringBuilder.append("null");
        }
        System.out.println(stringBuilder.toString());
    }
}

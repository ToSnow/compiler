package com.compiler.parser;

import com.compiler.model.Production;
import com.compiler.model.Symbol;

import java.util.*;

/**
 * DFA工具类
 * */
public class DFAUtils {

    /**
     * 对NFA结点求空闭包
     * @param nfaStates 需要求空闭包的NFA状态集合
     * @return          求空闭包之后的状态集合
     * */
    public static Set<NFAState> closure(Set<NFAState> nfaStates){
        Set<NFAState> resultSet = new HashSet<>();      //结果集
        resultSet.addAll(nfaStates);        //一定要把原始集合添加回来
        Stack<NFAState> stack = new Stack<>();
        stack.addAll(nfaStates);
        //对每个结点求闭包
        while(!stack.isEmpty()){
            NFAState currentState = stack.pop();
            Set<NFAState> epsilonStateSet = currentState.getEdges().get(NFAState.EPSILON);
            if(epsilonStateSet != null){
                for(NFAState epsilonState : epsilonStateSet){
                    //判断新结点是否在结果集中
                    if(!resultSet.contains(epsilonState)){
                        //不存在则添加
                        resultSet.add(epsilonState);
                        //加入到闭包栈中
                        stack.push(epsilonState);
                    }
                }
            }
        }
        return resultSet;
    }

    //DFA集合
    public final static Set<DFAState> DFAStateSet = new HashSet<>();
    //DFA转换图，第一个DFAState用于定位DFA结点，第二个map用于描述DFA结点的转换关系
    public final static Map<DFAState,Map<String,DFAState>> DFAGraph = new HashMap<>();
    /**
     * 子集法将NFA转换为DFA
     * 1.对NFA的开始状态求空闭包，得到NFASet
     * 2.根据NFASet创建DFA的开始状态
     * 3.DFAState入DFA栈
     * 4.while(DFA栈不空)
     * 5.   获得当前的DFA结点
     * 6.   for(每个非终结符)
     * 7.       对DFA结点中的每个NFA结点，求move后的结果
     * 8.       对move后的结果求空闭包
     * 9.       if(DFA结果集中不包含新的DFA)
     * 10.          添加状态转换图，新DFA入DFA栈
     * */
    public static void NFAToDFA(NFAState startNFA){
        //得到NFA开始状态对应的空闭包NFA集合
        Set<NFAState> startNFASet = closure(Collections.singleton(startNFA));
        DFAState startDFA = DFAState.create(startNFASet);       //得到DFA开始结点
        Stack<DFAState> dfaStateStack = new Stack<>();
        dfaStateStack.push(startDFA);
        DFAStateSet.add(startDFA);
        while(!dfaStateStack.isEmpty()){
            DFAState currentDFA = dfaStateStack.pop();
            //对于每个终结符进行move操作
            for(Symbol symbol : ParserUtils.vtSet){
                if(symbol.getContent().equals(Symbol.EPSILON))
                    //不能对空求move
                    continue;
                Set<NFAState> movedNFASet = move(currentDFA,symbol);
                //对move后的结果集求空闭包
                Set<NFAState> closureNFASet = closure(movedNFASet);
                //根据求空闭包后的结果创建对应的DFA
                DFAState dfaState = DFAState.create(closureNFASet);
                //如果是新的DFA则添加到结果集中
                if(!DFAStateSet.contains(dfaState)){
                    DFAStateSet.add(dfaState);
                    dfaStateStack.push(dfaState);       //新DFA入栈
                }
                //添加DFA转换图
                addEdge(currentDFA,symbol,dfaState);
            }
        }
    }

    /**
     * 对DFA结点求move操作，得到move操作后的NFA结点集合
     * @param currentDFAState 需要求move操作的DFA结点
     * @param path            move的路径(一定为终结符)
     * @return                move操作后的NFA Set集合
     * */
    public static Set<NFAState> move(DFAState currentDFAState, Symbol path){
        Set<NFAState> resultSet = new HashSet<>();
        //对DFA中的每个NFA结点进行move
        for(NFAState currentNFAState : currentDFAState.getNFAStates()){
            //判断该NFA是否包含这条路径
            if(currentNFAState.getEdges().containsKey(path.getContent())){
                //包含则将move后的NFA结点加入结果集
                resultSet.addAll(currentNFAState.getEdges().get(path.getContent()));
            }
        }
        return resultSet;
    }

    /**
     * 创建DFA转换图
     * @param currentDFA 当前的DFA结点
     * @param path       转换路径
     * @param nextDFA    下一个DFA结点
     * */
    public static void addEdge(DFAState currentDFA, Symbol path, DFAState nextDFA){
        Map<String,DFAState> stringDFAStateMap = DFAGraph.get(currentDFA);
        if(stringDFAStateMap == null || stringDFAStateMap.isEmpty()){
            //如果为空则创建新的map
            stringDFAStateMap = new HashMap<>();
            DFAGraph.put(currentDFA,stringDFAStateMap);
        }
        stringDFAStateMap.put(path.getContent(),nextDFA);
    }

    /**
     * 输出DFA状态图
     * */
    public static void printDFAMap(){
        System.out.println("--------------DFA State Map----------------");
        for(Map.Entry<DFAState,Map<String,DFAState>> dfaMap : DFAGraph.entrySet()){
            System.out.println(dfaMap.getKey());
            StringBuffer buffer = new StringBuffer();
            buffer.append("Edges{\n");
            for(Map.Entry<String,DFAState> edge : dfaMap.getValue().entrySet()){
                buffer.append("\t").append(edge.getKey()).append("->").append(edge.getValue()).append('\n');
            }
            buffer.append("}");
            System.out.println(buffer.toString());
        }
    }

    public static void main(String[] args){
        String path = "src/com/compiler/parser/test.txt";
        ParserUtils.readParseTXT(path);
        //生成grammar
        System.out.println("");
        for(Production production : ParserUtils.productionList){
            System.out.println(production);
        }
        ParserUtils.regularGrammarToNFA();
        ParserUtils.printNFAState();
        NFAToDFA(ParserUtils.startNFA);
        printDFAMap();
    }
}

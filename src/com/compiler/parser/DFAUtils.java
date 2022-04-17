package com.compiler.parser;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

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
}

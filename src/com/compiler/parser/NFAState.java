package com.compiler.parser;

import com.compiler.model.Symbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NFAState implements Comparable<NFAState>{
    public static final String EPSILON = "ε";       //空符号

    private Symbol id;                              //当前NFA对应的符号
    private Map<String, Set<NFAState>> edges;       //NFA转换图
    private boolean isEnd = false;                  //是否是终态
    private boolean isStart = false;                //是否为初态

    NFAState(Symbol symbol){
        id = symbol;
        edges = new HashMap<>();
    }

    NFAState(boolean isEnd){
        this.isEnd = true;
        id = new Symbol("END STATE", true);
        edges = new HashMap<>();
    }

    /**
     * 为NFA结点的转换图添加一条边
     * @param symbol 转换的条件
     * @param nextNFAState 下一个NFA结点
     * */
    public void addEdges(Symbol symbol,NFAState nextNFAState){
        Set<NFAState> stateSet = edges.get(symbol.getContent());     //获取当前文法符号对应的NFA集合
        if(stateSet == null){
            //为空则创建
            stateSet = new HashSet<>();
            edges.put(symbol.getContent(), stateSet);     //添加到边的集合中
        }
        stateSet.add(nextNFAState);     //添加边
    }

    public Symbol getId() {
        return id;
    }

    public void setId(Symbol id) {
        this.id = id;
    }

    public Map<String, Set<NFAState>> getEdges() {
        return edges;
    }

    public void setEdges(Map<String, Set<NFAState>> edges) {
        this.edges = edges;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    /**
     * NFAState1{A->2;B->3;}
     * */
    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        if(!isEnd)
            buffer.append("NFASate:").append(id.getContent()).append("{");
        else
            buffer.append("NFASate:").append("{");
        if(!isEnd) {
            for (Map.Entry<String, Set<NFAState>> edge : edges.entrySet()) {
                Set<NFAState> stateSet = edge.getValue();
                for (NFAState nfaState : stateSet) {
                    buffer.append(edge.getKey()).append("->").append(nfaState.getId().getContent()).append(";");
                }
            }
        }
        buffer.append("}");
        if(isEnd)
            buffer.append("END STATE");
        if(isStart)
            buffer.append("START STATE");
        return buffer.toString();
    }

    @Override
    public int compareTo(NFAState o) {
        return id.getContent().compareTo(o.id.getContent());
    }
}

package com.compiler.parser;

import java.util.Set;

public class DFAState {
    private final Set<NFAState> NFAStates;      //DFA对应的NFA状态集合
    private final Boolean isStart;              //是否是开始结点
    private final Boolean isEnd;                //是否是结束结点
    private final String key;                   //当前DFA的key，用于判断DFA是否相等

    public DFAState(Set<NFAState> NFAStates, Boolean isStart, Boolean isEnd, String key) {
        this.NFAStates = NFAStates;
        this.isStart = isStart;
        this.isEnd = isEnd;
        this.key = key;
    }

    /**
     * 创建DFA结点
     * @param NFAStates 该DFA结点对应的NFA结点结合
     * @return          创建好的DFA结点
     * */
    public DFAState create(Set<NFAState> NFAStates){
        //生成唯一key
        StringBuffer stringBuffer = new StringBuffer();
        NFAStates.stream().sorted().forEach(
                nfaState -> stringBuffer.append(nfaState.getId().getContent()).append(",")
        );
        //如果NFAStates集合中有一个结点为开始/结束结点，则对应的DFA结点也是
        boolean isStart = false;
        boolean isEnd = false;
        for(NFAState nfaState : NFAStates){
            if(nfaState.isStart())
                isStart = true;
            if(nfaState.isEnd())
                isEnd = true;
        }
        return new DFAState(NFAStates,isStart,isEnd,stringBuffer.toString());
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;
        if(obj == null || obj.getClass() != getClass())
            return false;
        return key.equals(((DFAState) obj).key);
    }

    @Override
    public String toString(){
        return "DFAState{" + key + "}";
    }

    public Set<NFAState> getNFAStates() {
        return NFAStates;
    }

    public Boolean getStart() {
        return isStart;
    }

    public Boolean getEnd() {
        return isEnd;
    }

    public String getKey() {
        return key;
    }
}

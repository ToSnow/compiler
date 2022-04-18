package com.compiler.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DFAState {
    private static final Map<String,DFAState>  DFAMap = new HashMap<>();        //存储key对应的DFA对象，避免重复创建
    private final Set<NFAState> NFAStates;      //DFA对应的NFA状态集合
    private final Boolean isStart;              //是否是开始结点
    private final Boolean isEnd;                //是否是结束结点
    private final String key;                   //当前DFA的key，用于判断DFA是否相等

    public DFAState(Set<NFAState> NFAStates, Boolean isStart, Boolean isEnd, String key) {
        this.NFAStates = NFAStates;
        this.isStart = isStart;
        this.isEnd = isEnd;
        this.key = key;
        DFAMap.put(key,this);       //存储创建的DFA结点
    }

    /**
     * 创建DFA结点
     * @param NFAStates 该DFA结点对应的NFA结点结合
     * @return          创建好的DFA结点
     * */
    public static DFAState create(Set<NFAState> NFAStates){
        //生成唯一key
        StringBuffer stringBuffer = new StringBuffer();
        NFAStates.stream().sorted().forEach(
                nfaState -> stringBuffer.append(nfaState.getId().getContent()).append(",")
        );
        if(stringBuffer.length() > 0)
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        //如果hashmap中存在key，则表示相同的DFA已经创建过，直接返回即可
        if(DFAMap.containsKey(stringBuffer.toString())){
            return DFAMap.get(stringBuffer.toString());
        }
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

    //根据key判断是否相等
    @Override
    public int hashCode(){
        return Objects.hash(key);
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

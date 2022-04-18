package com.compiler.parser;

import com.compiler.model.Production;
import com.compiler.model.Symbol;

import java.io.*;
import java.util.*;

public class ParserUtils {
    public final static List<Production> productionList = new ArrayList<>();      //产生式列表
    public final static Map<Symbol,NFAState> nfaStateMap = new HashMap<>();     //当前非终结符对应的NFA结点
    public final static Set<Symbol> vtSet = new HashSet<>();
    public final static Set<Symbol> vnSet = new HashSet<>();
    public static NFAState startNFA;        //NFA的开始结点
    /**
     * 读取正规文法，最后得到List<Production>的列表，并得到终结符和非终结符的列表
     * @param filePath  正规文法的路径
     * */
    public static void readParseTXT(String filePath){
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            InputStreamReader reader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String parse = "";
            while(true){
                try {
                    //读取每行的内容
                    if (((parse = bufferedReader.readLine()) != null)) {
                        //根据String的内容生成对应的产生式
                        //先获取开始符号
                        StringBuffer startString = new StringBuffer();
                        int index = 0;
                        int length = parse.length();
                        while(parse.charAt(index) != '-'){
                            startString.append(parse.charAt(index));
                            ++index;
                        }
                        //获取终结符
                        index += 2;
                        String vtString = String.valueOf(parse.charAt(index));
                        index++;
                        //获取非终结符
                        StringBuffer vnString = new StringBuffer();
                        while(index < length){
                            vnString.append(parse.charAt(index));
                            ++index;
                        }
                        //生成symbol
                        Symbol startSymbol = new Symbol(startString.toString());
                        Symbol vtSymbol = new Symbol(vtString);
                        Symbol vnSymbol = new Symbol(vnString.toString());
                        List<Symbol> symbolList = new ArrayList<>();
                        symbolList.add(vtSymbol);
                        vtSet.add(vtSymbol);
                        vnSet.add(startSymbol);
                        if(vnString.length() != 0) {        //当产生式右侧存在非终结符时
                            symbolList.add(vnSymbol);
                            vnSet.add(vnSymbol);
                        }
                        //根据symbol生成产生式
                        Production production = new Production(startSymbol,symbolList);
                        productionList.add(production);
                    }
                    else{
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 右线性正规文法转 NFA
     * 1.增加终态结点，开始符号对应的结点作为初态
     * 2.对形如A->b的文法，添加一条从A到终态的路径，路径为b
     * 3.对形如A->bB的文法，添加一条从A到B的路径，路径为b
     * */
    public static void regularGrammarToNFA(){
        //获取开始状态
        if(productionList.size() == 0){
            return;
        }
        //获取开始符号
        Symbol startSymbol = productionList.get(0).getLeft();
        //根据开始符号创建初态
        NFAState startNFAState = new NFAState(startSymbol);
        startNFAState.setStart(true);
        nfaStateMap.put(startSymbol,startNFAState);
        startNFA = startNFAState;
        //创建终态
        NFAState endState = new NFAState(true);
        nfaStateMap.put(new Symbol("END STATE",true),endState);
        //遍历产生式集合
        for(Production production : productionList){
            //先获取当前的NFAState
            Symbol leftSymbol = production.getLeft();
            NFAState currentNFAState = nfaStateMap.get(leftSymbol);
            if(currentNFAState == null){
                //没有则创建新的NFAState
                currentNFAState = new NFAState(leftSymbol);
                nfaStateMap.put(leftSymbol,currentNFAState);
            }
            if(production.getRight().size() == 1){
                //形如A->b的文法
                //连接到终态
                currentNFAState.addEdges(production.getRight().get(0),endState);
            }
            else{
                //形如A->bB的文法
                //先获取两个文法符号
                Symbol firstSymbol = production.getRight().get(0);
                Symbol secondSymbol = production.getRight().get(1);
                //获取下一个NFAState
                NFAState nextNFAState = nfaStateMap.get(secondSymbol);
                if(nextNFAState == null){
                    nextNFAState = new NFAState(secondSymbol);
                    nfaStateMap.put(secondSymbol,nextNFAState);
                }
                //添加边
                currentNFAState.addEdges(firstSymbol,nextNFAState);
            }
        }
    }

    public static void printNFAState(){
        System.out.println("--------------NFA State Map----------------");
        for(Map.Entry<Symbol,NFAState> stateMap : nfaStateMap.entrySet()){
            System.out.println(stateMap.getValue());
        }
    }

    public static void main(String[] args){
        String path = "src/com/compiler/parser/parse.txt";
        readParseTXT(path);
        //生成grammar
        System.out.println("");
        for(Production production : productionList){
            System.out.println(production);
        }
        regularGrammarToNFA();
        printNFAState();
    }
}

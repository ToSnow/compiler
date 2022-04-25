package com.compiler.parser;

import com.compiler.model.Production;
import com.compiler.model.Symbol;
import com.compiler.model.Token;

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

    //存储读取到语句
    public final static List<String> sentences = new ArrayList<>();
    /**
     * 读取用户编写的程序，并存储到sentences中
     * @param path 用户编写的程序的路径
     * */
    public static void readProgramTXT(String path){
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String sentence = "";
            while(true){
                if((sentence = bufferedReader.readLine()) != null){
                    sentences.add(sentence);
                }
                else{
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final static List<Token> tokenList = new ArrayList<>();
    /**
     * 根据读取到的用户程序和获得的DFA，进行词法分析
     * 该词法分析会自动略过//和/*的注释符号
     * 词法分析的结果将放在tokenList中
     * @return  词法分析是否出错，false表示出错
     * */
    public static Boolean parseProgram(){
        boolean isLineComment = false;
        int row = 0;
        Token token = null;
        StringBuffer tokenContent = new StringBuffer();
        DFAState currentDFAState = DFAUtils.startDFA;    //当前匹配的DFA结点
        for(String sentence : sentences){
            int index = 0;
            while(index < sentence.length()){
                if(!isLineComment){
                    if(sentence.charAt(index) == '/' &&
                    index + 1 < sentence.length() && sentence.charAt(index + 1) == '*'){
                        //添加上一个token
                        if(token != null && tokenContent.length() != 0){
                            token.setContent(tokenContent.toString());
                            tokenList.add(token);
                            token = null;
                        }
                        //行注释开始
                        isLineComment = true;
                        index += 2;
                    }
                }
                else {
                    if(sentence.charAt(index) == '*' &&
                    index + 1 < sentence.length() && sentence.charAt(index + 1) == '/'){
                        //行注释结束
                        isLineComment = false;
                        index += 2;
                    }
                }
                if(sentence.charAt(index) == '/' &&
                index + 1 < sentence.length() && sentence.charAt(index + 1) == '/'){
                    //添加上一个token
                    if(token != null && tokenContent.length() != 0){
                        token.setContent(tokenContent.toString());
                        tokenList.add(token);
                        token = null;
                    }
                    //略过行注释
                    break;
                }
                //只有不处于注释状态时才有效
                if(!isLineComment){
                    //当token为空时初始化token
                    if(token == null || tokenContent.length() == 0){
                        token = new Token();
                        tokenContent = new StringBuffer();
                        //设置为DFA的开始结点
                        currentDFAState = DFAUtils.startDFA;
                    }
                    if(sentence.charAt(index) == ' '){
                        //略过空格
                        //空格还同时表示一个字符的结束
                        if(tokenContent.length() > 0){
                            //token不为空，则添加token
                            token.setContent(tokenContent.toString());
                            tokenList.add(token);
                            token = null;   //token置空
                        }
                    }
                    else{
                        if(tokenContent.length() == 0){
                            //设置当前token的开始行和列
                            token.setRow(row);
                            token.setCol(index);
                        }
                        //获取下一个可以转换到的DFA结点
                        boolean hasPath = false;
                        if(DFAUtils.DFAGraph.containsKey(currentDFAState) &&
                           DFAUtils.DFAGraph.get(currentDFAState).containsKey(String.valueOf(sentence.charAt(index))))
                            hasPath = true;
                        //boolean hasPath = DFAUtils.DFAGraph.get(currentDFAState).containsKey(String.valueOf(sentence.charAt(index)));
                        if(hasPath){
                            //如果存在转换路径
                            DFAState nextDFAState = DFAUtils.DFAGraph.get(currentDFAState).get(String.valueOf(sentence.charAt(index)));
                            tokenContent.append(sentence.charAt(index));
                            currentDFAState = nextDFAState;
                        }
                        else{
                            //如果不存在
                            //判断当前结点是否是终态
                            if(currentDFAState.getEnd()){
                                //当前符号读取结束
                                //添加token
                                if(tokenContent.length() != 0) {
                                    token.setContent(tokenContent.toString());
                                    tokenList.add(token);
                                    token = null;
                                }
                                --index;
                            }
                            else{
                                //非终态但不能转换说明出现了词法分析错误
                                System.out.println("Parse error at row " + (row + 1) + ", col " + (index + 1) + ", char:" + sentence.charAt(index) + "!");
                                return false;
                            }
                        }
                    }
                }
                ++index;
            }
            ++row;
        }
        //判断当前结点是否是终态
        if(currentDFAState.getEnd()){
            //添加token
            if(tokenContent.length() != 0) {
                token.setContent(tokenContent.toString());
                tokenList.add(token);
            }
        }
        else{
            //非终态但不能转换说明出现了词法分析错误
            int index = sentences.get(row).length() - 1;
            System.out.println("Parse error at row " + row + ", col " + sentences.get(row).charAt(index) + "!");
            return false;
        }
        //生成token对应的type
        for(Token t : tokenList){
            t.setType();
        }
        return true;
    }

    /**
     *  输出token列表
     * */
    public static void printTokenList(){
        for(Token token : tokenList){
            System.out.println(token);
        }
    }

    /**
     * 根据输入的正规文法，对用户程序进行分析
     * @param parsePath   正规文法的路径
     * @param programPath 用户程序的路径
     *
     * */
    public static void parse(String parsePath, String programPath){
        //读取正规文法
        readParseTXT(parsePath);
        //输出产生式
        System.out.println("");
        for(Production production : productionList){
            System.out.println(production);
        }
        //正规文法转NFA
        regularGrammarToNFA();
        printNFAState();
        //NFA转DFA
        DFAUtils.NFAToDFA(startNFA);
        DFAUtils.printDFAMap();
        //读取用户程序
        readProgramTXT(programPath);
        if(parseProgram()){
            printTokenList();
        }
    }

    public static void main(String[] args){
        String parsePath = "src/com/compiler/parser/parse.txt";
        String programPath = "src/com/compiler/parser/program.txt";
        parse(parsePath,programPath);
    }
}

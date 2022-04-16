package com.compiler.parser;

import com.compiler.model.Production;
import com.compiler.model.Symbol;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParserUtils {
    public static List<Production> productionList;      //产生式列表

    /**
     * 读取正规文法，最后得到List<Production>的列表
     * @param filePath  正规文法的路径
     * */
    public static void readParseTXT(String filePath){
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            InputStreamReader reader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String parse = "";
            productionList = new ArrayList<>();
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
                        if(vnString.length() != 0)
                            symbolList.add(vnSymbol);
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

    public static void main(String[] args){
        String path = "src/com/compiler/parser/parse.txt";
        readParseTXT(path);
        //生成grammar
        System.out.println("");
        for(Production production : productionList){
            System.out.println(production);
        }

    }
}

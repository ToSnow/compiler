package com.compiler;

import com.compiler.lr1.LR1Utils;
import com.compiler.model.Token;
import com.compiler.parser.ParserUtils;

import java.util.List;

public class Run {
    public static void main(String[] args){
        String parsePath = "src/com/compiler/parser/parse.txt";
        String programPath = "src/com/compiler/parser/program.txt";
        String productionPath = "src/com/compiler/lr1/production.txt";
        if(ParserUtils.parse(parsePath, programPath)){
            //获取token
            List<Token> tokenList = ParserUtils.tokenList;
            LR1Utils.startLR1(tokenList, productionPath);
        }
    }
}

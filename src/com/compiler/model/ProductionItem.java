package com.compiler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 文法的项目
 * 如：A->a•b,a/#
 * */
public class ProductionItem implements Comparable<ProductionItem>{
    public static final String DELIMITER = "•";     //分割符
    private final Production production;            //项目的产生式
    private final int delimiterPos;                 //分隔符的位置
    private final Symbol expect;                    //项目的展望符
    private final String content;                   //该文法项目的字符串形式

    public ProductionItem(Production production, int delimiterPos, Symbol expect, String content) {
        this.production = production;
        this.delimiterPos = delimiterPos;
        this.expect = expect;
        this.content = content;
    }

    /**
     * 生成项目的字符串形式，并创建文法的项目
     * @param production    产生式
     * @param expect        项目的展望符
     * @param delimiterPos  分隔符的位置
     * @return              文法的项目
     * */
    public static ProductionItem create(Production production, Symbol expect, int delimiterPos){
        List<Symbol> rightList = production.getRight();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(production.getLeft().getContent()).append("->");
        //将分隔符插入到项目的字符串中
        for(int i = 0; i < rightList.size(); ++i){
            Symbol symbol = rightList.get(i);
            if(i == delimiterPos){
                //插入分隔符
                stringBuilder.append(DELIMITER);
            }
            stringBuilder.append(symbol.getContent());
        }
        //分隔符在末尾的情况
        if(delimiterPos == rightList.size()){
            stringBuilder.append(DELIMITER);
        }
        //插入展望符
        stringBuilder.append(",").append(expect.getContent());
        //调用构造函数
        return new ProductionItem(production, delimiterPos, expect, stringBuilder.toString());
    }

    /**
     * 生成项目的字符串形式，并创建文法的项目，分割符默认为0
     * @param production    产生式
     * @param expect        项目的展望符
     * @return              文法的项目
     * */
    public static ProductionItem create(Production production,Symbol expect){
        return create(production, expect,0);
    }

    /**
     * 根据已有项目，创建该项目的下一个项目
     * @param productionItem 已有项目
     * @return               下一个项目
     * */
    public static ProductionItem create(ProductionItem productionItem){
        if(productionItem.delimiterPos >= productionItem.production.getRight().size()){
            //如果当前项目的分割符已经在项目末尾则创建失败
            throw new RuntimeException("项目的分隔符已在项目末尾，无法创建");
        }
        return create(productionItem.production,productionItem.expect,productionItem.delimiterPos + 1);
    }

    /**
     * 获取分隔符往后的第二个元素及展望符，用于闭包中的获取first集
     * @return 对产生式A->α•Bβ,a，获取FIRST(βa)中的βa
     * */
    public List<Symbol> getFirstSymbol(){
        List<Symbol> result = new ArrayList<>();
        List<Symbol> rightSymbols = production.getRight();
        for(int i = delimiterPos + 1; i < rightSymbols.size(); ++i){
            result.add(rightSymbols.get(i));
        }
        result.add(expect);
        return result;
    }

    public static String getDELIMITER() {
        return DELIMITER;
    }

    public Production getProduction() {
        return production;
    }

    public int getDelimiterPos() {
        return delimiterPos;
    }

    public Symbol getExpect() {
        return expect;
    }

    public String getContent() {
        return content;
    }

    /**
     * 根据content值比较是否相等，而不是直接比较对象
     * */
    @Override
    public boolean equals(Object obj){
        if(obj == null || getClass() != obj.getClass()) return false;
        if(this == obj) return true;
        ProductionItem productionItem = (ProductionItem) obj;
        return content.equals(((ProductionItem) obj).content);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(content);
    }

    @Override
    public int compareTo(ProductionItem productionItem){
        return content.compareTo(productionItem.content);
    }

    @Override
    public String toString() {
        return "ProductionItem{" + content + "}";
    }
}

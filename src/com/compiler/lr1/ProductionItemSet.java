package com.compiler.lr1;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * 项目集
 * */
public class ProductionItemSet implements Comparable<ProductionItemSet>, Cloneable{
    //所有项目集的集合，以项目集转换为字符串的内容为键
    public static final HashMap<String, ProductionItemSet> itemSets = new HashMap<>();
    private static int total = 0;                               //项目集总数
    private final Set<ProductionItem> productionItemSet;        //项目集
    private final String content;                               //项目集转换为字符串的内容
    private final int index;                                    //当前项目集的标号

    public ProductionItemSet(Set<ProductionItem> productionItemSet, String content, int index) {
        this.productionItemSet = productionItemSet;
        this.content = content;
        this.index = index;
        ++total;        //每执行一次构造函数，项目集总数+1
    }

    /**
     * 根据文法的项目的集合创建项目集（去除重复）
     * @param productionItemSet 文法项目的集合
     * @return                  项目集
     * */
    public static ProductionItemSet create(Set<ProductionItem> productionItemSet){
        StringBuilder stringBuilder = new StringBuilder();
        //排序，保证顺序不同但内容相同的set生成的stringBuilder是相同的
        productionItemSet.stream().sorted().forEach(
                productionItem -> stringBuilder.append(productionItem.getContent()).append(";")
        );
        String content = stringBuilder.toString();
        //通过content查找hashmap判断是否存在过
        if(itemSets.containsKey(content)){
            return itemSets.get(content);       //直接返回已有集合
        }
        else{
            ProductionItemSet itemSet = new ProductionItemSet(productionItemSet,content,total);
            itemSets.put(content,itemSet);
            return itemSet;
        }
    }

    public static HashMap<String, ProductionItemSet> getItemSets() {
        return itemSets;
    }

    public static int getTotal() {
        return total;
    }

    public Set<ProductionItem> getProductionItemSet() {
        return productionItemSet;
    }

    public String getContent() {
        return content;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        //根据content判断是否相等
        ProductionItemSet productionItemSet = (ProductionItemSet) obj;
        return Objects.equals(content,productionItemSet.content);
    }

    @Override
    public int compareTo(ProductionItemSet obj){
        Integer a = this.index;
        Integer b = obj.index;
        return a.compareTo(b);
    }

    @Override
    protected ProductionItemSet clone() throws CloneNotSupportedException {
        try {
            ProductionItemSet obj = (ProductionItemSet) super.clone();
            return obj;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        //ProductionItemSet{I0:S'->S,#}
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ProductionItemSet{I").append(index).append(":");
        for(ProductionItem productionItem : productionItemSet){
            stringBuilder.append(productionItem.getContent()).append("\t");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}

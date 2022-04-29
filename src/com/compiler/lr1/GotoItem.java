package com.compiler.lr1;

public class GotoItem {
    private final ProductionItemSet nextProductionItemSet;
    private final int number;       //项目集的序号

    public GotoItem(ProductionItemSet nextProductionItemSet){
        this.nextProductionItemSet = nextProductionItemSet;
        this.number = nextProductionItemSet.getIndex();
    }

    public ProductionItemSet getNextProductionItemSet() {
        return nextProductionItemSet;
    }

    public int getNumber() {
        return number;
    }
}

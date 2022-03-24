package com.compiler.lr1;

import com.compiler.model.Production;
import com.compiler.model.ProductionItemSet;

public class ActionItem {
    public static final String ACTION_S = "S";      //移进
    public static final String ACTION_R = "R";      //归约
    public static final String ACTION_ACC = "ACC";  //分析通过

    private final String actionType;                    //记录动作类型
    private final ProductionItemSet productionItemSet;  //移进状态时记录移入的项目集
    private final Production production;                //归约状态时记录归约用的产生式

    /**
     * 通过public的create方法创建对象
     */
    private ActionItem(String actionType, ProductionItemSet productionItemSet, Production production){
        this.actionType = actionType;
        this.productionItemSet = productionItemSet;
        this.production = production;
    }

    /**
     * 创建移进项目
     * @param productionItemSet 移进的项目集
     * @return                  actionItem对象
     * */
    public static ActionItem createActionS(ProductionItemSet productionItemSet){
        return new ActionItem(ACTION_S,productionItemSet,null);
    }

    /**
     * 创建归约项目
     * @param production 归约用的产生式
     * @return           actionItem对象
     * */
    public static ActionItem createActionR(Production production){
        return new ActionItem(ACTION_R,null,production);
    }

    /**
     * 创建结束项目
     * @return action对象
     * */
    public static ActionItem createActionACC(){
        return new ActionItem(ACTION_ACC,null,null);
    }

    public String getActionType() {
        return actionType;
    }

    public ProductionItemSet getProductionItemSet() {
        return productionItemSet;
    }

    public Production getProduction() {
        return production;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ActionItem{");
        switch(actionType){
            case ACTION_S:
                stringBuilder.append(ACTION_S).append(productionItemSet.getIndex());
                break;
            case ACTION_R:
                stringBuilder.append(ACTION_R).append(production.getIndex());
                break;
            case ACTION_ACC:
                stringBuilder.append(ACTION_ACC);
                break;
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}

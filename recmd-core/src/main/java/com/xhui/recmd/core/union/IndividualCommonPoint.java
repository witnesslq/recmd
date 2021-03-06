package com.xhui.recmd.core.union;

import org.json4s.jackson.Serialization;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

/**
 *
 * 个体兴趣组描述
 * Created by littlehui on 2016/9/11 0011.
 * -------------------------------------------------------------
 * columnGoals，为一个个体对某个事物的兴趣 组
 *                                            ------------------
 *                                              column      value
 *                                              commodity1 goal2
 *                                              commodity2 goal2
 *                                            ------------------
 * -------------------------------------------------------------
 */
public class IndividualCommonPoint extends HashMap<String, Double> implements Serializable {

    /**
     * 一般为rowkey
     */
    private String ip;

    /**
     * 与聚类中心距离
     */
    private Double classCoreDis;

/*
    用二维字符串数组的话，下标会很大。不利于存储
    private String[][] columnGoalsVs;*/

    private List<ColumnGoal> columnGoals;

   // private Map<String ,Object> columnGoalsMap;

    public String getIp() {
        return ip;
    }

    public Double getClassCoreDis() {
        return classCoreDis;
    }

    public void setClassCoreDis(Double classCoreDis) {
        this.classCoreDis = classCoreDis;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void updateColumnGoal(String column, Object oldValue, Object newValue) {
        ColumnGoal columnGoal = new ColumnGoal();
        columnGoal.setColumn(column);
        columnGoal.setGoal((Double) oldValue);
        columnGoals.remove(columnGoal);
        ColumnGoal columnGoalNew = new ColumnGoal();
        columnGoalNew.setColumn(column);
        columnGoalNew.setGoal((Double) newValue);
        columnGoals.add(columnGoalNew);
        //this.columnGoalsMap.put(column, newValue);
        this.put(column, (Double) newValue);
    }

    public void addColumnGoal(ColumnGoal columnGoal) {
        if (this.columnGoals == null) {
            this.columnGoals = new ArrayList<>();
            //this.columnGoalsMap = new HashMap<>();
        }
        this.columnGoals.add(columnGoal);
        //this.columnGoalsMap.put(columnGoal.getColumn(), columnGoal.getGoal());
        super.put(columnGoal.getColumn(), columnGoal.getGoal());
    }

    public List<ColumnGoal> getColumnGoals() {
        if (columnGoals != null) {
            Collections.sort(columnGoals);
        }
        return columnGoals;
    }

    public void setColumnGoals(List<ColumnGoal> columnGoals) {
        this.columnGoals = columnGoals;
        //this.columnGoalsMap = ObjectUtil.toMap(columnGoals);
        for (ColumnGoal columnGoal : columnGoals) {
            super.put(columnGoal.getColumn(), columnGoal.getGoal());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndividualCommonPoint that = (IndividualCommonPoint) o;
        Collections.sort(columnGoals);
        List<ColumnGoal> thatColumnGoals = that.getColumnGoals();
        Collections.sort(that.getColumnGoals());
        if (columnGoals != null) {
            for (int i=0; i<columnGoals.size(); i++) {
                if (!columnGoals.get(i).equals(thatColumnGoals.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "IndividualInterestPoint{" +
                "ip='" + ip + '\'' +
                ", classCoreDis=" + classCoreDis +
                ", columnGoals=" + columnGoals +
                '}';
    }

    @Override
    public int hashCode() {
        //int result = ip != null ? ip.hashCode() : 0;
        int result = (columnGoals != null ? columnGoals.hashCode() : 0);
        return result;
    }

    @Override
    public Double put(String column, Double value) {
        ColumnGoal columnGoal = new ColumnGoal(column, value);
        if (columnGoals == null) {
            columnGoals = new ArrayList<>();
        }
        this.addColumnGoal(columnGoal);
        return value;
    }

    public Double get(Object key) {
        Double value = super.get(key);
        if (value == null) {
            return 0d;
        }
        return value;
    }
}

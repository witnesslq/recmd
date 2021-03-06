package com.xhui.recmd.core.hao;

import com.xhui.recmd.core.union.ColumnGoal;
import com.xhui.recmd.core.union.IndividualCommonPoint;
import com.xhui.recmd.spark.utils.HbaseApi;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by littlehui on 2016/10/19.
 */
public class ClassifierHao {

    private static final String CLASS_TABLE = "classifier";

    private static final String CLASS_FAMILY = "ipClass";

    public void addToClassifier(String classifierKey, IndividualCommonPoint individualInterestPoint) {
        HbaseApi.addRow(CLASS_TABLE, classifierKey, CLASS_FAMILY, individualInterestPoint.getIp(), individualInterestPoint.getClassCoreDis() + "");
    }

    public void addAClassifier(List<IndividualCommonPoint> individualInterestPoints) {
        if (individualInterestPoints != null && individualInterestPoints.size() > 0) {
            for (IndividualCommonPoint individualInterestPoint : individualInterestPoints) {
                /**
                 * key          ip(10.5.32.97)  ip(1.2.32.32)   ....
                 * 121141       0.1             0.3             ....
                 */
                HbaseApi.addRow(CLASS_TABLE, individualInterestPoints.hashCode() + "", CLASS_FAMILY, individualInterestPoint.getIp(), individualInterestPoint.getClassCoreDis() + "");
            }
            //保存类别关联的所有Column值
        }
    }

    public IndividualCommonPoint getByClassfier(String rowKey) {
        //HbaseApi.getRow()
        Cell[] cells = HbaseApi.getRow(CLASS_TABLE, rowKey);
        IndividualCommonPoint individualInterestPoint = new IndividualCommonPoint();
        if (cells == null) {
            return individualInterestPoint;
        }
        for (Cell cell : cells) {
            String valueStr = new String(CellUtil.cloneValue(cell));
            Double value = new Double(valueStr);
            ColumnGoal columnGoal = new ColumnGoal(new String(CellUtil.cloneQualifier(cell)) + "", value);
            individualInterestPoint.addColumnGoal(columnGoal);
            individualInterestPoint.setIp(new String(CellUtil.cloneRow(cell)));
        }
        return individualInterestPoint;
    }

    public List<IndividualCommonPoint> queryAll() {
        ResultScanner resultScanner = HbaseApi.getAllRows(CLASS_TABLE);
        List<IndividualCommonPoint> individualInterestPoints = new ArrayList<>();
        for (Result result : resultScanner) {
            IndividualCommonPoint individualInterestPoint = new IndividualCommonPoint();
            for (Cell cell : result.rawCells()) {
                String valueStr = new String(CellUtil.cloneValue(cell));
                Double value = new Double(valueStr);
                ColumnGoal columnGoal = new ColumnGoal(new String(CellUtil.cloneQualifier(cell)) + "", value);
                individualInterestPoint.addColumnGoal(columnGoal);
                individualInterestPoint.setIp(new String(CellUtil.cloneRow(cell)));
            }
            individualInterestPoints.add(individualInterestPoint);
        }
        return individualInterestPoints;
    }

    public List<ColumnGoal> queryNearIps(String srcIp, Integer nearNumber) {
        Cell[] cells = HbaseApi.scanRowByColumn(CLASS_TABLE, srcIp);
        IndividualCommonPoint classifierPoint = new IndividualCommonPoint();
        for (Cell cell : cells) {
            String valueStr = new String(CellUtil.cloneValue(cell));
            Double value = new Double(valueStr);
     /*           if (value == 0) {
                    continue;
                }*/
            ColumnGoal columnGoal = new ColumnGoal(new String(CellUtil.cloneQualifier(cell)) + "", value);
            classifierPoint.addColumnGoal(columnGoal);
            classifierPoint.setIp(new String(CellUtil.cloneRow(cell)));
        }
        if (classifierPoint.size() < 1) {
            return null;
        }
        Double srcDis = classifierPoint.get(srcIp);
        List<ColumnGoal> ipDisAll = classifierPoint.getColumnGoals();
        Collections.sort(ipDisAll, new Comparator<ColumnGoal>() {
            @Override
            public int compare(ColumnGoal o1, ColumnGoal o2) {
                return o1.getGoal() - o2.getGoal() > 0 ? 1 : (o1.getGoal() - o2.getGoal() == 0 ? 0 : -1);
            }
        });
        //System.out.println(ipDisAll.size());
        int srcIndex = ipDisAll.indexOf(new ColumnGoal(srcIp, srcDis));
        if (srcIndex < 0) {
            return null;
        }
        List<ColumnGoal> result =  getNearColums(srcIndex, nearNumber, ipDisAll);
        return result;
    }

    public IndividualCommonPoint getClassifierByIp(String srcIp) {
        Cell[] cells = HbaseApi.scanRowByColumn(CLASS_TABLE, srcIp);
        IndividualCommonPoint classifierPoint = new IndividualCommonPoint();
        if (cells == null) {
            return classifierPoint;
        }
        for (Cell cell : cells) {
            String valueStr = new String(CellUtil.cloneValue(cell));
            Double value = new Double(valueStr);
     /*           if (value == 0) {
                    continue;
                }*/
            ColumnGoal columnGoal = new ColumnGoal(new String(CellUtil.cloneQualifier(cell)) + "", value);
            classifierPoint.addColumnGoal(columnGoal);
            classifierPoint.setIp(new String(CellUtil.cloneRow(cell)));
        }
        if (classifierPoint.size() < 1) {
            return null;
        }
        return classifierPoint;
    }

    public static void main(String[] args) {
        ClassifierHao classifierHao = new ClassifierHao();
        classifierHao.queryNearIps("111.85.209.135", 20);
    }


    private List<ColumnGoal> getNearColums(int srcIndex, int nearNum, List<ColumnGoal> srcColumGoals) {
        if (srcColumGoals.size() < 2) {
            return null;
        }
        if (srcIndex == 0) {
            //在第一个
            int resultStartIndex = srcIndex + 1;
            int resultEndIndex = resultStartIndex + nearNum;
            resultEndIndex = resultEndIndex > (srcColumGoals.size() - 1) ? srcColumGoals.size() : resultEndIndex;
            return srcColumGoals.subList(resultStartIndex, resultEndIndex);
        }
        if (srcIndex == (srcColumGoals.size() - 1)) {
            //在最后一个
            int resultEndIndex = srcIndex;
            int resultStartIndex = resultEndIndex - nearNum;
            resultStartIndex = resultStartIndex < 0 ? 0 : resultStartIndex;
            return srcColumGoals.subList(resultStartIndex, resultEndIndex);
        }
        //在中间
        if (srcIndex >= nearNum) {
            int resultStartIndex = (srcIndex - nearNum);
            int resultEndIndex = srcIndex;
            return srcColumGoals.subList(resultStartIndex, resultEndIndex);
        }
        if (srcIndex < nearNum) {
            int resultStartIndex = 0;
            int resultEndIndex = srcIndex;
            int tailStartIndex = srcIndex + 1;
            int tailEndIndex = srcIndex + 1;
            int remainCounts = nearNum - srcIndex;
            int srcToEnd = (srcColumGoals.size() - 1) - srcIndex;
            if (remainCounts >= srcToEnd) {
                tailEndIndex = srcColumGoals.size() - 1;
            } else {
                tailEndIndex = tailStartIndex + remainCounts;
            }
            List<ColumnGoal> startList = srcColumGoals.subList(resultStartIndex, resultEndIndex);
            List<ColumnGoal> endList = srcColumGoals.subList(tailStartIndex, tailEndIndex);
            startList.addAll(endList);
            return startList;
        }
        return null;
    }

    public String getClassifierIps(String ip) {

        return "";
    }
}

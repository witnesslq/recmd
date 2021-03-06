package com.xhui.recmd.zk.services;

import com.xhui.recmd.core.hao.ClassifierHao;
import com.xhui.recmd.core.union.ColumnGoal;
import com.xhui.recmd.core.union.IndividualCommonPoint;
import com.xhui.recmd.zk.bean.ClassifierVo;
import com.xhui.recmd.zk.bean.ResultPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by littlehui on 2016/11/20 0020.
 */
public class ClassifierService {

    private ClassifierHao classifierHao = new ClassifierHao();

    private static ClassifierService classifierService;

    public static ClassifierService getInstance () {
        if (classifierService != null) {
            return classifierService;
        } else {
            classifierService = new ClassifierService();
            return classifierService;
        }
    }

    private ClassifierService() {

    }

    public List<ClassifierVo> queryAllClassifiers() {
        List<IndividualCommonPoint> classifiers = classifierHao.queryAll();
        List<ClassifierVo> result = new ArrayList<>();
        if (classifiers != null && classifiers.size() > 0) {
            for (IndividualCommonPoint commonPoint : classifiers) {
                result.addAll(toClassfierVo(commonPoint));
            }
        }
        return result;
    }

    public ResultPage<ClassifierVo> queryByIp(String ip) {
        IndividualCommonPoint commonPoint = classifierHao.getClassifierByIp(ip);
        List<ClassifierVo> classifierVos = toClassfierVo(commonPoint);
        ClassifierVo resultVo = new ClassifierVo();
        resultVo.setClassifierIp(ip);
        resultVo.setClassifierKey(commonPoint.getIp());
        int indexOfResult = classifierVos.indexOf(resultVo);
        int resultPageNumber = (indexOfResult / 15);
        ResultPage<ClassifierVo> resultPage = new ResultPage<>();
        resultPage.setResultPageSize(15);
        resultPage.setResultPageNumber(resultPageNumber);
        resultPage.setResultList(classifierVos);
        resultPage.setSelectedIndex(indexOfResult);
        return resultPage;
    }

    private List<ClassifierVo> toClassfierVo(IndividualCommonPoint commonPoint) {
        List<ClassifierVo> result = new ArrayList<>();
        if (commonPoint != null && commonPoint.size() > 0) {
            for (ColumnGoal columnGoal : commonPoint.getColumnGoals()) {
                ClassifierVo classifierVo = new ClassifierVo();
                classifierVo.setClassifierKey(commonPoint.getIp());
                classifierVo.setClassifierIp(columnGoal.getColumn());
                classifierVo.setClassifierIpDis(columnGoal.getGoal());
                result.add(classifierVo);
            }
        }
        if (result.size() > 0) {
            Collections.sort(result, new Comparator<ClassifierVo>() {
                @Override
                public int compare(ClassifierVo o1, ClassifierVo o2) {
                    int result = o1.getClassifierIpDis() - o2.getClassifierIpDis() > 0 ? 1 : (o1.getClassifierIpDis() - o2.getClassifierIpDis() < 0 ? -1 : 0 );
                    return result;
                }
            });
        }
        return result;
    }

    public List<ClassifierVo> queryByRowKey(String value) {
        IndividualCommonPoint point = classifierHao.getByClassfier(value);
        return toClassfierVo(point);
    }
}

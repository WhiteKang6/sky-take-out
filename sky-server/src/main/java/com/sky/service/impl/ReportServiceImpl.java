package com.sky.service.impl;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    /**
     * 统计营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = getDateList(begin, end);
        //String dateListStr=dateList.stream().map(LocalDate::toString).collect(Collectors.joining(","));
        //String dateListStr= StringUtils.join(dateList,",");
        List<Double> turnoverList=new ArrayList<>();

        for (LocalDate date : dateList) {

            //起始时间
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            //终止时间
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);
            //起始时间，终止时间，状态为已完成
            Map map =new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sumByMap(map);
            turnover=turnover==null?0.0:turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }



    /**
     * 统计用户
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        List<Long> newUserList=new ArrayList<>();
        List<Long> totalUserList=new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);
            Long newUserCount=userMapper.countByTime(beginTime,endTime);
            newUserList.add(newUserCount);
            Long totalUserCount=userMapper.countByTime(null,endTime);
            totalUserList.add(totalUserCount);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();

    }

    /**
     * 统计订单
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        List<Integer> orderCountList=new ArrayList<>();
        List<Integer> validOrderCountList=new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);
            Map map =new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);

            Integer orderCount=orderMapper.countByMap(map);

            map.put("status", Orders.COMPLETED);
            Integer validOrderCount=orderMapper.countByMap(map);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        //订单总数
        Integer totalOrderCount=orderCountList.stream().mapToInt(Integer::intValue).sum();
        //有效订单数
        Integer validOrderCount=validOrderCountList.stream().mapToInt(Integer::intValue).sum();
        //订单完成率
        Double orderCompletionRate=0.0;
        if(totalOrderCount>0){
            orderCompletionRate=validOrderCount.doubleValue()/totalOrderCount.doubleValue();
        }
        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 统计销量Top10的菜品或套餐
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10Statistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime=LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime=LocalDateTime.of(end, LocalTime.MAX);
        List<OrderDetail> ordersDetailList=orderMapper.getSalesTop10Statistics(beginTime,endTime);
        /*List<String> nameList=new ArrayList<>();
        List<Integer> numberList=new ArrayList<>();
        for (OrderDetail ordersDetail : ordersDetailList) {
            nameList.add(ordersDetail.getName());
            numberList.add(ordersDetail.getNumber());
        }*/
        List<String> nameList=ordersDetailList.stream().map(OrderDetail::getName).collect(Collectors.toList());
        List<Integer> numberList=ordersDetailList.stream().map(OrderDetail::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

    /**
     * 获取日期列表
     * @param begin
     * @param end
     * @return
     */
    @NonNullDecl
    private static List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
}

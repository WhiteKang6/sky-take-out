package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    /**
     * 处理超时的未支付订单
     */
    @Scheduled(cron = "0 1 * * * ?")
    public void processTimeOutOrder(){
        log.info("处理超时的未支付订单");

        // 超过15分钟取消订单,下单时间<当前时间-15分钟
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        //select * from orders where status=? and order_time<time
        List<Orders> ordersList=orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        if(ordersList != null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                // 取消订单
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("超时未支付");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }


    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("处理一直处于派送中的订单");

        //每天0点处理一直处于派送中的订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        //select * from orders where status=? and order_time<time
        List<Orders> ordersList=orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if(ordersList != null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                // 已送达订单
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}

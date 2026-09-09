package com.macro.mall.service.impl;

import com.macro.mall.model.OmsOrderItem;
import com.macro.mall.dao.OmsOrderItemDao;
import com.macro.mall.service.OmsOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OmsOrderItemServiceImpl implements OmsOrderItemService {

    @Autowired
    private OmsOrderItemDao orderItemDao;

    @Override
    public List<OmsOrderItem> listAll() {
        return orderItemDao.listAll();
    }

    @Override
    public List<OmsOrderItem> listByOrderId(Long orderId) {
        return orderItemDao.listByOrderId(orderId);
    }

    @Override
    public OmsOrderItem getById(Long id) {
        return orderItemDao.getById(id);
    }

    @Override
    public int create(OmsOrderItem orderItem) {
        return orderItemDao.insert(orderItem);
    }

    @Override
    public int update(OmsOrderItem orderItem) {
        return orderItemDao.update(orderItem);
    }

    @Override
    public int delete(Long id) {
        return orderItemDao.delete(id);
    }
}
package com.macro.mall.service.impl;

import com.macro.mall.dto.OmsOrderQueryParam;
import com.macro.mall.model.OmsOrder;
import com.macro.mall.dao.OmsOrderDao;
import com.macro.mall.service.OmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OmsOrderServiceImpl implements OmsOrderService {

    @Autowired
    private OmsOrderDao orderDao;

    @Override
    public List<OmsOrder> list(OmsOrderQueryParam queryParam) {
        return orderDao.list(queryParam);
    }

    @Override
    public List<OmsOrder> listAll() {
        return orderDao.listAll();
    }

    @Override
    public OmsOrder getById(Long id) {
        return orderDao.getById(id);
    }

    @Override
    public int create(OmsOrder order) {
        return orderDao.insert(order);
    }

    @Override
    public int update(OmsOrder order) {
        return orderDao.update(order);
    }

    @Override
    public int delete(Long id) {
        return orderDao.delete(id);
    }
}
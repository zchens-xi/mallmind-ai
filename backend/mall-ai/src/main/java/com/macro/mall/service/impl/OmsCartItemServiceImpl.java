package com.macro.mall.service.impl;

import com.macro.mall.model.OmsCartItem;
import com.macro.mall.dao.OmsCartItemDao;
import com.macro.mall.service.OmsCartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OmsCartItemServiceImpl implements OmsCartItemService {

    @Autowired
    private OmsCartItemDao cartItemDao;

    @Override
    public List<OmsCartItem> listAll() {
        return cartItemDao.listAll();
    }

    @Override
    public List<OmsCartItem> listByMemberId(Long memberId) {
        return cartItemDao.listByMemberId(memberId);
    }

    @Override
    public OmsCartItem getById(Long id) {
        return cartItemDao.getById(id);
    }

    @Override
    public int create(OmsCartItem cartItem) {
        return cartItemDao.insert(cartItem);
    }

    @Override
    public int update(OmsCartItem cartItem) {
        return cartItemDao.update(cartItem);
    }

    @Override
    public int delete(Long id) {
        return cartItemDao.delete(id);
    }
}
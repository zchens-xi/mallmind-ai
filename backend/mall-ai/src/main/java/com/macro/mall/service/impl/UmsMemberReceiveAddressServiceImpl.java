package com.macro.mall.service.impl;

import com.macro.mall.model.UmsMemberReceiveAddress;
import com.macro.mall.dao.UmsMemberReceiveAddressDao;
import com.macro.mall.service.UmsMemberReceiveAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UmsMemberReceiveAddressServiceImpl implements UmsMemberReceiveAddressService {

    @Autowired
    private UmsMemberReceiveAddressDao addressDao;

    @Override
    public List<UmsMemberReceiveAddress> listAll() {
        return addressDao.listAll();
    }

    @Override
    public List<UmsMemberReceiveAddress> listByMemberId(Long memberId) {
        return addressDao.listByMemberId(memberId);
    }

    @Override
    public UmsMemberReceiveAddress getById(Long id) {
        return addressDao.getById(id);
    }

    @Override
    public int create(UmsMemberReceiveAddress address) {
        return addressDao.insert(address);
    }

    @Override
    public int update(UmsMemberReceiveAddress address) {
        return addressDao.update(address);
    }

    @Override
    public int delete(Long id) {
        return addressDao.delete(id);
    }
}
package com.macro.mall.service.impl;

import com.macro.mall.model.UmsMember;
import com.macro.mall.dao.UmsMemberDao;
import com.macro.mall.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UmsMemberServiceImpl implements UmsMemberService {

    @Autowired
    private UmsMemberDao memberDao;

    @Override
    public List<UmsMember> listAll() {
        return memberDao.listAll();
    }

    @Override
    public UmsMember getById(Long id) {
        return memberDao.getById(id);
    }

    @Override
    public int create(UmsMember member) {
        return memberDao.insert(member);
    }

    @Override
    public int update(UmsMember member) {
        return memberDao.update(member);
    }

    @Override
    public int delete(Long id) {
        return memberDao.delete(id);
    }
}
package com.macro.mall.service.impl;

import com.macro.mall.model.UmsMemberProfile;
import com.macro.mall.dao.UmsMemberProfileDao;
import com.macro.mall.service.UmsMemberProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UmsMemberProfileServiceImpl implements UmsMemberProfileService {

    @Autowired
    private UmsMemberProfileDao profileDao;

    @Override
    public List<UmsMemberProfile> listAll() {
        return profileDao.listAll();
    }

    @Override
    public UmsMemberProfile getByMemberId(Long memberId) {
        return profileDao.getByMemberId(memberId);
    }

    @Override
    public int create(UmsMemberProfile profile) {
        return profileDao.insert(profile);
    }

    @Override
    public int update(UmsMemberProfile profile) {
        return profileDao.update(profile);
    }

    @Override
    public int delete(Long memberId) {
        return profileDao.delete(memberId);
    }
}
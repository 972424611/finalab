package com.cslg.finalab.service.impl;

import com.cslg.finalab.beans.PageQuery;
import com.cslg.finalab.beans.PageResult;
import com.cslg.finalab.common.BeanValidator;
import com.cslg.finalab.common.FileOperation;
import com.cslg.finalab.dao.SysCollegeMapper;
import com.cslg.finalab.dao.SysDepartmentMapper;
import com.cslg.finalab.dao.SysLevelMapper;
import com.cslg.finalab.dao.SysMemberMapper;
import com.cslg.finalab.enums.FileEnum;
import com.cslg.finalab.enums.MemberEnum;
import com.cslg.finalab.exception.FileException;
import com.cslg.finalab.exception.MemberException;
import com.cslg.finalab.model.*;
import com.cslg.finalab.param.MemberParam;
import com.cslg.finalab.service.MemberService;
import com.cslg.finalab.vo.CouncilVo;
import com.cslg.finalab.vo.MemberVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    @Value("${server_address}")
    private String serverAddress;

    private final SysMemberMapper sysMemberMapper;

    private final SysLevelMapper sysLevelMapper;

    private final SysCollegeMapper sysCollegeMapper;

    private final SysDepartmentMapper sysDepartmentMapper;

    @Autowired
    public MemberServiceImpl(SysMemberMapper sysMemberMapper, SysLevelMapper sysLevelMapper,
                             SysCollegeMapper sysCollegeMapper, SysDepartmentMapper sysDepartmentMapper) {
        this.sysMemberMapper = sysMemberMapper;
        this.sysLevelMapper = sysLevelMapper;
        this.sysCollegeMapper = sysCollegeMapper;
        this.sysDepartmentMapper = sysDepartmentMapper;
    }

    @Override
    public List<CouncilVo> getCouncilList() {
        // TODO; 这里暂时只获取老师和总裁的信息
        List<SysLevel> sysLevelList = sysLevelMapper.selectLevelLimitGreaterMinister();
        List<CouncilVo> councilVoList = new ArrayList<>();
        for(SysLevel sysLevel : sysLevelList) {
            List<SysMember> sysMemberList = sysMemberMapper.selectByLevelId(sysLevel.getId());
            for(SysMember sysMember : sysMemberList) {
                CouncilVo councilVo = new CouncilVo();
                councilVo.setName(sysMember.getName());
                councilVo.setCatchphrase(sysMember.getCatchphrase());
                councilVo.setImage(serverAddress + sysMember.getHeadPortrait());
                councilVo.setLevelName(sysLevel.getName());
                councilVoList.add(councilVo);
            }
        }
        if(councilVoList.size() > 1) {
            // 把实验室导师放中间位置(一共只有三个３)
            Collections.swap(councilVoList, 1, 2);
        }
        return councilVoList;
    }

    private MemberVo sysToVo(SysMember sysMember) {
        MemberVo memberVo = new MemberVo();
        BeanUtils.copyProperties(sysMember, memberVo);
        memberVo.setGender(sysMember.getGender() == 0 ? "男" : "女");
        Integer levelId = sysMember.getLevelId();
        SysLevel sysLevel = sysLevelMapper.selectByPrimaryKey(levelId);
        memberVo.setLevelName(sysLevel.getName());
        Integer collegeId = sysMember.getCollege();
        SysCollege sysCollege = sysCollegeMapper.selectByPrimaryKey(collegeId);
        memberVo.setCollege(sysCollege.getCollegeName());
        SysDepartment sysDepartment = sysDepartmentMapper.selectByPrimaryKey(sysMember.getDepartmentId());
        memberVo.setDepartment(sysDepartment.getName());
        return memberVo;
    }

    @Override
    public PageResult<MemberVo> getMemberListByDepartmentId(Integer departmentId, PageQuery pageQuery) {
        if(departmentId == null) {
            return new PageResult<>();
        }
        int count = sysMemberMapper.countMemberByDepartmentId(departmentId);
        if(count < 1) {
            return new PageResult<>();
        }
        List<SysMember> sysMemberList = sysMemberMapper.selectByDepartmentId(departmentId, pageQuery);
        List<MemberVo> memberVoList = new ArrayList<>();
        for(SysMember sysMember : sysMemberList) {
            MemberVo memberVo = sysToVo(sysMember);
            memberVoList.add(memberVo);
        }
        PageResult<MemberVo> pageResult = new PageResult<>();
        pageResult.setList(memberVoList);
        pageResult.setTotal(count);
        return pageResult;
    }

    @Override
    public PageResult<MemberVo> getAllMemberList(PageQuery pageQuery) {
        int count = sysMemberMapper.countAllMember();
        if(count < 1) {
            return new PageResult<>();
        }
        List<SysMember> sysMemberList = sysMemberMapper.selectAllMember(pageQuery);
        List<MemberVo> memberVoList = new ArrayList<>();
        for(SysMember sysMember : sysMemberList) {
            MemberVo memberVo = sysToVo(sysMember);
            memberVoList.add(memberVo);
        }
        PageResult<MemberVo> pageResult = new PageResult<>();
        pageResult.setList(memberVoList);
        pageResult.setTotal(count);
        return pageResult;
    }

    private void checkMember(MemberParam memberParam) {
        BeanValidator.check(memberParam);
        if(memberParam == null) {
            throw new MemberException(MemberEnum.INFORMATION_CANNOT_BE_EMPTY);
        }
        // 检查学号是否重复
        String stuId = memberParam.getStuId();
        if(sysMemberMapper.countMemberByStuId(stuId) > 0) {
            throw new MemberException(MemberEnum.MEMBER_ALREADY_EXISTS);
        }
    }

    @Override
    public void saveMember(MemberParam memberParam) {
        // 检查参数
        checkMember(memberParam);
        SysMember sysMember = new SysMember();
        BeanUtils.copyProperties(memberParam, sysMember);
        // 检查学号是否重复
        if(sysMemberMapper.countMemberByStuId(memberParam.getStuId()) > 0) {
            throw new MemberException(MemberEnum.MEMBER_ALREADY_EXISTS);
        }
        // 查询学院
        SysCollege sysCollege = sysCollegeMapper.selectByCollegeName(memberParam.getCollege());
        sysMember.setCollege(sysCollege.getId());
        // 如果没有填写邮箱，则把邮箱设置为qq邮箱
        if (StringUtils.isBlank(memberParam.getEmail())) {
            sysMember.setEmail(memberParam.getQq() + "@qq.com");
        }
        // 查询部门
        SysDepartment department = sysDepartmentMapper.selectByDepartmentName(memberParam.getDepartment());
        if(department == null) {
            throw new MemberException(MemberEnum.DEPARTMENT_NOT_FOUND);
        }
        sysMember.setDepartmentId(department.getId());
        // 查询成员级别
        SysLevel sysLevel = sysLevelMapper.selectByLevelName(memberParam.getLevel());
        sysMember.setLevelId(sysLevel.getId());
        sysMemberMapper.insert(sysMember);
    }

    @Override
    public void deleteMemberById(Integer[] memberIds) {
        if(memberIds == null || memberIds.length == 0) {
            return;
        }
        // 删除该成员的照片
        List<SysMember> sysMemberList = sysMemberMapper.selectByPrimaryKeys(memberIds);
        for(SysMember sysMember : sysMemberList) {
            if(StringUtils.isNotBlank(sysMember.getHeadPortrait())) {
                FileOperation.deleteFile(sysMember.getHeadPortrait());
            }
        }
        sysMemberMapper.batchDeleteByPrimaryKey(memberIds);
    }

    @Override
    public void updateMemberById(MemberParam memberParam) {
        SysMember newSysMember = new SysMember();
        newSysMember.setId(memberParam.getId());
        BeanUtils.copyProperties(memberParam, newSysMember);
        if(StringUtils.isNotBlank(memberParam.getStuId())) {
            // 检查学号是否重复
            String stuId = memberParam.getStuId();
            if(sysMemberMapper.countMemberByStuId(stuId) > 0) {
                throw new MemberException(MemberEnum.MEMBER_ALREADY_EXISTS);
            }
            newSysMember.setStuId(memberParam.getStuId());
            SysMember oldSysMember = sysMemberMapper.selectByPrimaryKey(memberParam.getId());
            // 如果学号有变化，则相应的成员的图片名称需要修改，因为图片名称以学号命名
            String headPortrait = oldSysMember.getHeadPortrait();
            // 如果该成员原来就有照片
            if(StringUtils.isNotBlank(headPortrait)) {
                File oldFile = new File(headPortrait);
                String format = FileOperation.checkFileNameAndGetFormat(oldFile.getName());
                String newHeadPortrait = oldFile.getParent() + File.separator + stuId + format;
                File newFile = new File(newHeadPortrait);
                if(!oldFile.renameTo(newFile)) {
                    throw new FileException(FileEnum.FILE_RENAME_FAIL);
                }
                newSysMember.setHeadPortrait(newHeadPortrait);
            }
        }
        if(StringUtils.isNotBlank(memberParam.getCollege())) {
            // 查询学院
            SysCollege sysCollege = sysCollegeMapper.selectByCollegeName(memberParam.getCollege());
            newSysMember.setCollege(sysCollege.getId());
        }
        if(StringUtils.isNotBlank(memberParam.getDepartment())) {
            // 查询部门
            SysDepartment department = sysDepartmentMapper.selectByDepartmentName(memberParam.getDepartment());
            newSysMember.setDepartmentId(department.getId());
        }
        if(StringUtils.isNotBlank(memberParam.getLevel())) {
            // 查询成员级别
            SysLevel sysLevel = sysLevelMapper.selectByLevelName(memberParam.getLevel());
            newSysMember.setLevelId(sysLevel.getId());
        }
        // 默认把邮箱设置为qq邮箱
        if(StringUtils.isNotBlank(memberParam.getQq()) && StringUtils.isBlank(memberParam.getEmail())) {
            newSysMember.setEmail(memberParam.getQq() + "@qq.com");
        }
        sysMemberMapper.updateByPrimaryKeySelective(newSysMember);
    }

}

package com.cslg.finalab.service;

import com.cslg.finalab.param.MemberParam;
import com.cslg.finalab.vo.CouncilVo;
import com.cslg.finalab.vo.MemberVo;

import java.util.List;

public interface MemberService {

    /**
     * 获取理事会成员
     * @return List<CouncilVo>
     */
    List<CouncilVo> getCouncilList();

    /**
     * 获取部门成员，根据部门id
     * @param departmentId 部门id
     * @return List<MemberVo>
     */
    List<MemberVo> getMemberListByDepartmentId(Integer departmentId);

    /**
     * 保存成员
     * @param memberParam memberParam
     */
    void saveMember(MemberParam memberParam);
}
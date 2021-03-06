package com.cslg.finalab.dao;

import com.cslg.finalab.model.SysWinning;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysWinningMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysWinning record);

    int insertSelective(SysWinning record);

    SysWinning selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysWinning record);

    int updateByPrimaryKey(SysWinning record);

    // -------------------------------

    List<SysWinning> selectAll();

    int countWinningByName(String name);

    void insertSelectiveAndGetWinningId(SysWinning sysWinning);

    void updateAwardImageByPrimaryKey(@Param("awardImage") String awardImage, @Param("id") Integer winningId);

    void batchDeleteByPrimaryKey(@Param("winningIds") Integer[] winningIds);
}
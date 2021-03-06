package com.cslg.finalab.service;

import com.cslg.finalab.param.WinningParam;
import com.cslg.finalab.vo.WinningVo;

import java.util.List;

public interface WinningService {

    /**
     * 获取所有获奖信息
     * @return 获奖信息数组
     */
    List<WinningVo> getAllWinningList();

    /**
     * 保存获奖信息
     * @param winningParam 获奖信息对象
     * @return 新增加获奖信息的id
     */
    int saveWinning(WinningParam winningParam);

    /**
     * 删除获奖信息
     * @param winningIds 获奖id数组
     */
    void deleteWinningById(Integer[] winningIds);

    /**
     * 更新获奖信息
     * @param winningParam 更新信息
     */
    void updateWinningById(WinningParam winningParam);
}

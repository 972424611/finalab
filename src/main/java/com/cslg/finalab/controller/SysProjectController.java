package com.cslg.finalab.controller;

import com.cslg.finalab.common.JsonData;
import com.cslg.finalab.service.SysProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/sys/project")
public class SysProjectController {

    @Autowired
    private SysProjectService sysProjectService;

    @ResponseBody
    @RequestMapping(value = "/getAll")
    public JsonData getAll() {
        return JsonData.success(sysProjectService.getAllProjectList());
    }

    @ResponseBody
    @RequestMapping(value = "/detail")
    public JsonData detail(@RequestParam(value = "id") Integer id) {
        return JsonData.success(sysProjectService.getProjectDetailById(id));
    }
}

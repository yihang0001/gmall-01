package com.atguigu.gmallindex.controller;


import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import com.atguigu.gmallindex.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping("cates")
    public Resp<List<CategoryEntity>> queryLv1Category(){

        List<CategoryEntity> categoryEntities = this.indexService.queryLv1Category();

        return Resp.ok(categoryEntities);
    }

    @GetMapping("cates/{pid}")
    public Resp<List<CategoryVo>> queryLv2WithSubsByPid(@PathVariable("pid")Long pid){
        List<CategoryVo> categoryEntities = this.indexService.queryLv2WithSubsByPid(pid);

        return Resp.ok(categoryEntities);
    }

    @GetMapping("test/lock")
    public Resp<Object> testlock(){
        this.indexService.testlock();
        return Resp.ok(null);
    }

}

package io.renren.modules.k8s.controller;

import java.util.Arrays;
import java.util.Map;

import io.renren.common.validator.ValidatorUtils;
import io.renren.modules.k8s.entity.K8sPipelinesEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.renren.modules.k8s.entity.K8sPipelinesEntity;
import io.renren.modules.k8s.service.K8sPipelinesService;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;



/**
 * 
 *
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-10-20 19:11:33
 */
@RestController
@RequestMapping("k8s/k8spipelines")
public class K8sPipelinesController {
    @Autowired
    private K8sPipelinesService k8sPipelinesService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("k8s:k8spipelines:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = k8sPipelinesService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("k8s:k8spipelines:info")
    public R info(@PathVariable("id") Integer id){
        K8sPipelinesEntity k8sPipelines = k8sPipelinesService.getById(id);

        return R.ok().put("k8sPipelines", k8sPipelines);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("k8s:k8spipelines:save")
    public R save(@RequestBody K8sPipelinesEntity k8sPipelines){
        k8sPipelinesService.save(k8sPipelines);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("k8s:k8spipelines:update")
    public R update(@RequestBody K8sPipelinesEntity k8sPipelines){
        ValidatorUtils.validateEntity(k8sPipelines);
        k8sPipelinesService.updateById(k8sPipelines);
        
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("k8s:k8spipelines:delete")
    public R delete(@RequestBody Integer[] ids){
        k8sPipelinesService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

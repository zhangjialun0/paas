package io.renren.modules.sys.controller;

import java.util.Arrays;
import java.util.Map;

import io.renren.common.validator.ValidatorUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.renren.modules.sys.entity.K8sTemplateEntity;
import io.renren.modules.sys.service.K8sTemplateService;
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
@RequestMapping("sys/k8stemplate")
public class K8sTemplateController {
    @Autowired
    private K8sTemplateService k8sTemplateService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("sys:k8stemplate:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = k8sTemplateService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("sys:k8stemplate:info")
    public R info(@PathVariable("id") Integer id){
        K8sTemplateEntity k8sTemplate = k8sTemplateService.getById(id);

        return R.ok().put("k8sTemplate", k8sTemplate);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("sys:k8stemplate:save")
    public R save(@RequestBody K8sTemplateEntity k8sTemplate){
        k8sTemplateService.save(k8sTemplate);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("sys:k8stemplate:update")
    public R update(@RequestBody K8sTemplateEntity k8sTemplate){
        ValidatorUtils.validateEntity(k8sTemplate);
        k8sTemplateService.updateById(k8sTemplate);
        
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("sys:k8stemplate:delete")
    public R delete(@RequestBody Integer[] ids){
        k8sTemplateService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

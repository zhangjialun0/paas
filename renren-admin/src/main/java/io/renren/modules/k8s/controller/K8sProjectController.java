package io.renren.modules.k8s.controller;

import java.util.Arrays;
import java.util.Map;

import io.renren.common.validator.ValidatorUtils;
import io.renren.modules.sys.controller.AbstractController;
import io.renren.modules.sys.entity.SysUserEntity;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.renren.modules.k8s.entity.K8sProjectEntity;
import io.renren.modules.k8s.service.K8sProjectService;
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
@RequestMapping("k8s/k8sproject")
public class K8sProjectController extends AbstractController{
    @Autowired
    private K8sProjectService k8sProjectService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("k8s:k8sproject:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = k8sProjectService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("k8s:k8sproject:info")
    public R info(@PathVariable("id") Integer id){
        K8sProjectEntity k8sProject = k8sProjectService.getById(id);

        return R.ok().put("k8sProject", k8sProject);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("k8s:k8sproject:save")
    public R save(@RequestBody K8sProjectEntity k8sProject){
        SysUserEntity sysUserEntity =  this.getUser();
        k8sProjectService.save(k8sProject,sysUserEntity);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("k8s:k8sproject:update")
    public R update(@RequestBody K8sProjectEntity k8sProject){
        ValidatorUtils.validateEntity(k8sProject);
        k8sProjectService.updateById(k8sProject);
        
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("k8s:k8sproject:delete")
    public R delete(@RequestBody Integer[] ids){
        k8sProjectService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

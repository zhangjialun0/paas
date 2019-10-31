package io.renren.modules.k8s.controller;

import java.util.Arrays;
import java.util.Map;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.renren.common.validator.ValidatorUtils;
import io.renren.modules.k8s.entity.K8sPipelinesEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.renren.modules.k8s.entity.K8sPipelinesEntity;
import io.renren.modules.k8s.service.K8sPipelinesService;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;

import javax.validation.constraints.NotNull;


/**
 * 
 *
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-10-20 19:11:33
 */
@RestController
//@RequestMapping("k8s/k8spipelines/")
public class K8sPipelinesController {
    @Autowired
    private K8sPipelinesService k8sPipelinesService;

    /**
     * 列表
     */
    @RequestMapping("k8s/k8spipelines//list")
    @RequiresPermissions("k8s:k8spipelines:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = k8sPipelinesService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("k8s/k8spipelines/info/{id}")
    @RequiresPermissions("k8s:k8spipelines:info")
    public R info(@PathVariable("id") Integer id){
        K8sPipelinesEntity k8sPipelines = k8sPipelinesService.getById(id);

        return R.ok().put("k8sPipelines", k8sPipelines);
    }

    /**
     * 保存
     */
    @RequestMapping("k8s/k8spipelines/save")
    @RequiresPermissions("k8s:k8spipelines:save")
    public R save(@RequestBody K8sPipelinesEntity k8sPipelines){
        k8sPipelinesService.save(k8sPipelines);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("k8s/k8spipelines/update")
    @RequiresPermissions("k8s:k8spipelines:update")
    public R update(@RequestBody K8sPipelinesEntity k8sPipelines){
        ValidatorUtils.validateEntity(k8sPipelines);
        k8sPipelinesService.updateById(k8sPipelines);
        
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("k8s/k8spipelines/delete")
    @RequiresPermissions("k8s:k8spipelines:delete")
    public R delete(@RequestBody Integer[] ids){
        k8sPipelinesService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }




    /**
     * rancher 部署部分
     * fzl
     * 10-25
     */

    /**
     *
     * 测试
     *
     */
    @RequestMapping("k8s/k8sRancherAction/test_rancher")
    public R test_rancher(){
        KubernetesClient client = k8sPipelinesService.test_rancher();
        System.out.println(client);
        return R.ok();
//        if (client != null )
//            return true;
//        return false;
    }

    /**
     * 创建命名空间
     * @param name
     * @return
     */
    @RequestMapping("k8s/k8sRancherAction/createNamespace")
//    @RequiresPermissions("k8s:k8sRancherAction:createNamespace")
    public R createNamespace(@RequestParam String name){
        Namespace namespace = k8sPipelinesService.createNamespace(name);
        return R.ok().put("namespace",namespace);
    }

    /**
     * 列出所有命名空间
     * @return
     */
    @RequestMapping("k8s/k8sRancherAction/listNamespace")
//    @RequiresPermissions("k8s:k8sRancherAction:listNamespace")
    public R listNamespace(){
        NamespaceList namespaceList = k8sPipelinesService.listNamespace();
        return R.ok().put("namespaceList",namespaceList);
    }

    /**
     * 创建pod
     * @param namespace
     * @param podName
     * @return
     */
    @RequestMapping("k8s/k8sRancherAction/createNamespacePod")
//    @RequiresPermissions("k8s:k8sRancherAction:createNamespacePod")
    public R createNamespacePod(@RequestParam String namespace ,@RequestParam String podName){
        Pod pod = k8sPipelinesService.CreatePod(namespace,podName);
        return R.ok().put("pod",pod);
    }

    /**
     * 列出pod
     * @param namespace
     * @return
     */
    @RequestMapping("k8s/k8sRancherAction/listNamespacePod")
//    @RequiresPermissions("k8s:k8sRancherAction:listNamespacePod")
    public R listNamespacePod(@RequestParam String namespace){
        PodList podList = k8sPipelinesService.ListPod(namespace);
        return R.ok().put("podList",podList);
    }

    /**
     * 创建命名空间下的服务
     * @param namespace
     * @param serviceName
     * @param pipelinesId
     * @return
     */
    @RequestMapping("k8s/k8sRancherAction/createNamespaceService")
//    @RequiresPermissions("k8s:k8sRancherAction:createNamespaceService")
    public R createNamespaceService(@RequestParam String namespace , @RequestParam String serviceName, @RequestParam String pipelinesId) throws Exception {
        Service service = k8sPipelinesService.CreateNamespaceService(namespace,serviceName,pipelinesId);
        return R.ok().put("service",service);
    }

    /**
     * 列出服务
     * @param namespace
     * @return
     */
    @RequestMapping("k8s/k8sRancherAction/listNamespaceService")
//    @RequiresPermissions("k8s:k8sRancherAction:listNamespaceService")
    public R listNamespaceService(@RequestParam String namespace){
        ServiceList serviceList=k8sPipelinesService.ListServise(namespace);
        return R.ok().put("listNamespaceService",serviceList);
    }

    /**
     * 创建部署
     * @param namespace
     * @param deploymentName
     * @param orgName
     * @param projectGroupName
     * @param projectName
     * @param branche
     * @param pipelinesId
     * @return
     */
    @RequestMapping("k8s/k8sRancherAction/createNamespaceDeployment")
//    @RequiresPermissions("k8s:k8sRancherAction:createNamespaceDeployment")
    public R createNamespaceDeployment(@RequestParam String namespace, @RequestParam String deploymentName , @RequestParam String orgName,@RequestParam String projectGroupName, @RequestParam String projectName , @RequestParam String branche ,  @RequestParam String pipelinesId ) throws Exception {
        Deployment deployment=k8sPipelinesService.CreateNamespaceDeployment(namespace,deploymentName,orgName,projectGroupName,projectName,branche,pipelinesId);
        return R.ok().put("deployment",deployment);
    }

    /**
     * 列出命名空间下的部署
     * @param namespace
     * @return
     */
    @RequestMapping("k8s/k8sRancherAction/listNamespaceDeployment")
//    @RequiresPermissions("k8s:k8sRancherAction:listNamespaceDeployment")
    public R listNamespaceDeployment(@RequestParam String namespace ){
        DeploymentList deploymentList=k8sPipelinesService.ListDeployment(namespace);
        return R.ok().put("deploymentList",deploymentList);
    }

    /**
     * 将所创建的命名空间移到集群项目中
     * @param namespace
     */
    @RequestMapping("k8s/k8sRancherAction/moveNamespaceToProject")
//    @RequiresPermissions("k8s:k8sRancherAction:moveNamespaceToProject")
    public void moveNamespaceToProject(@RequestParam String namespace ){
        k8sPipelinesService.moveNamespaceToProject(namespace);
    }

    /**
     * 自动化部署
     * @param orgName
     * @param proGroupName
     * @param projectName
     * @param branches
     * @param projectGroupId
     * @param pipelineId
     * @return
     */
    @RequestMapping("k8s/k8sRancherAction/runProjectGroupAndAutoConfiguration")
//    @RequiresPermissions("k8s:k8sRancherAction:runProjectGroupAndAutoConfiguration")
    public R runProjectGroupAndAutoConfiguration(@RequestParam String orgName , @RequestParam String proGroupName , @RequestParam String projectName , @RequestParam String branches , @RequestParam String projectGroupId , @RequestParam String pipelineId) throws Exception {
        String name = k8sPipelinesService.runProjectGroupAndAutoConfiguration(orgName, proGroupName, projectName, branches, projectGroupId, pipelineId);
        return R.ok().put("name",name);
    }

    /**
     * 重新部署
     * @param namespace
     * @param deploymentName
     * @param orgName
     * @param projectGroupName
     * @param projectName
     * @param branches
     * @param pipelinesId
     * @return
     */

    @RequestMapping("k8s/k8sRancherAction/repeatDeployment")
//    @RequiresPermissions("k8s:k8sRancherAction:repeatDeployment")
    public R repeatDeployment( @RequestParam String namespace,  @RequestParam String deploymentName , @RequestParam String orgName, @RequestParam String projectGroupName,  @RequestParam String projectName , @RequestParam String branches , @RequestParam String pipelinesId ) throws Exception {
        Deployment deployment=k8sPipelinesService.repeatDeployment(namespace,deploymentName,orgName,projectGroupName,projectName,branches,pipelinesId);
        return R.ok().put("成功重新部署",deployment);
    }

}

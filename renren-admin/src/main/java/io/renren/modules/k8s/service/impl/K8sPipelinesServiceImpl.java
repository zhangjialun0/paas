package io.renren.modules.k8s.service.impl;

import com.baomidou.mybatisplus.extension.service.additional.query.impl.QueryChainWrapper;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.renren.common.utils.ChineseChangeToEnglish;
import io.renren.common.utils.K8sFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;

import io.renren.modules.k8s.dao.K8sPipelinesDao;
import io.renren.modules.k8s.entity.K8sPipelinesEntity;
import io.renren.modules.k8s.service.K8sPipelinesService;
import sun.util.locale.LocaleExtensions;
import sun.util.locale.LocaleSyntaxException;


@Service("k8sPipelinesService")
public class K8sPipelinesServiceImpl extends ServiceImpl<K8sPipelinesDao, K8sPipelinesEntity> implements K8sPipelinesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<K8sPipelinesEntity> page = this.page(
                new Query<K8sPipelinesEntity>().getPage(params),
                new QueryWrapper<K8sPipelinesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public Namespace createNamespace(String name) {
        try(final KubernetesClient client = K8sFactory.getKubernetesClient()){
            Namespace namespace = new NamespaceBuilder().withApiVersion("v1")
                    .withKind("Namespace")
                    .withNewMetadata()
//                    .withNamespace(name)
                    .withName(name)
                    .addToLabels("name",name)
                    .withClusterName("cloudcarex")
                    .endMetadata()
                    .build();

            namespace = K8sFactory.CreateNamespace(K8sFactory.getKubernetesClient(),namespace);

            //创建部署前需要先创建镜像凭证
            client.secrets().inNamespace(name).create(new SecretBuilder()
                    .withApiVersion("v1")
                    .withKind("Secret")
                    .withNewMetadata()
                    .addToLabels("name",name)
                    .withNamespace(name)
                    .withName(name)
                    .endMetadata()
                    .withNewType("kubernetes.io/dockerconfigjson")
                    .addToData(".dockerconfigjson","eyJhdXRocyI6eyIxNzIuMTYuMTY1LjEwODo1MDAwIjp7InVzZXJuYW1lIjoiYWRtaW4iLCJwYXNzd29yZCI6InNsaWFuMTIzIiwiYXV0aCI6IllXUnRhVzQ2YzJ4cFlXNHhNak09In19fQ==")
                    .build());

            return namespace;
        }
    }

    /**
     * 修改api后的列出命名空间  f'z'l
     * @return
     */
    @Override
    public NamespaceList listNamespace() {
        final KubernetesClient client = K8sFactory.getKubernetesClient();
        return K8sFactory.ListNamespace(client);
    }

    /**
     * 修改api后的创建pod fzl
     * @param namespace
     * @param podName
     * @return
     */
    @Override
    public Pod CreatePod(String namespace, String podName) {
        try(final KubernetesClient client = K8sFactory.getKubernetesClient()) {
            Pod pod = new PodBuilder().withApiVersion("v1")
                    .withKind("Pod")
                    .withNewMetadata()
                    .withName(podName)
                    .addToLabels("name",podName)
                    .addToLabels("namespace",namespace)
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withName(podName)
                    .withImage("172.16.165.108:5000/library/gitlab/gitlab-runner:latest")
                    .endContainer()
                    .endSpec()
                    .build();
            return K8sFactory.CreatePod(K8sFactory.getKubernetesClient(),pod,namespace);
        }


    }

    /**
     * 列出pod fzl
     * @param namespace
     * @return
     */
    @Override
    public PodList ListPod(String namespace) {
        return K8sFactory.ListPod(K8sFactory.getKubernetesClient(),namespace);
    }

    /**
     * 修改api后的创建服务
     * @param namespace
     * @param serviceName
     * @param pipelinesId
     * @return
     */
    @Override
    public io.fabric8.kubernetes.api.model.Service CreateNamespaceService(String namespace, String serviceName , String pipelinesId ) throws Exception {
//        Pipelines pipelines = pipelinesRepository.findOneByModel(Filter.condition()
//                .equal("id",pipelinesId)
//                .equal("available",true));
        K8sPipelinesEntity pipelines = baseMapper.selectById(pipelinesId);
        if(pipelines!=null){
            try (final KubernetesClient client = K8sFactory.getKubernetesClient()){
                io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder().withApiVersion("v1").withKind("Service").withNewSpec()
                        .withSelector(Collections.singletonMap("app", serviceName))
                        .withType("NodePort")
                        .addNewPort()
                        .withName("port")
                        .withProtocol("TCP")
                        .withPort(Integer.valueOf(pipelines.getPort()))
                        .withTargetPort(new IntOrString(Integer.valueOf(pipelines.getPort())))
                        .withNodePort((int)(Math.random()*20000)+10000)
                        .endPort()
                        .endSpec()
                        .withNewMetadata()
                        .withName(serviceName)
                        .withNamespace(namespace)
                        .addToLabels(Collections.singletonMap("app", serviceName))
                        .endMetadata()
                        .build();
                return K8sFactory.CreateNamespaceService(client,service,namespace);
            }
        }
        else{
            throw new Exception("创建服务过程中查找流水线出错");
        }

    }

    /**
     *  修改api后的列出服务  fzl
     * @param namespace
     * @return
     */

    @Override
    public ServiceList ListServise(String namespace) {
        return K8sFactory.ListService(K8sFactory.getKubernetesClient(),namespace);
    }

    /**
     * 修改api后的创建部署  fzl
     * @param namespace
     * @param deploymentName
     * @param pipelinesId
     * @return
     */
    @Override
    public Deployment CreateNamespaceDeployment(String namespace, String deploymentName , String orgName, String projectGroupName, String projectName , String branches , String pipelinesId ) throws Exception {
//        Pipelines pipelines = pipelinesRepository.findOneByModel(Filter.condition()
//                .equal("id",pipelinesId)
//                .equal("available",true));
        K8sPipelinesEntity pipelines = baseMapper.selectById(pipelinesId);
        if(pipelines!=null){
            try (final KubernetesClient client = K8sFactory.getKubernetesClient()) {
                //创建部署
                Deployment deployment = new DeploymentBuilder().withApiVersion("apps/v1beta2")
                        .withKind("Deployment")
                        .withNewMetadata()
                        .withName(deploymentName)
                        .withNamespace(namespace)
                        .endMetadata()
                        .withNewSpec()
                        .withReplicas(1)
                        .withNewTemplate()
                        .withNewMetadata()
                        .addToLabels("app", deploymentName)
                        .endMetadata()
                        .withNewSpec()
                        .addNewContainer()
                        .withName(deploymentName)
//            docker pull 172.16.165.108:5000/cloudcarex_user_registry/projectpath:master
//            docker pull 172.16.165.108:5000/cloudcarex_user_registry/w/g4/p4:master
                        .withImage("172.16.165.108:5000/cloudcarex_user_registry/"+orgName+"/"+projectGroupName+"/"+projectName+":"+branches)
                        .withImagePullPolicy("Always")
                        .withPorts(new ContainerPort(Integer.valueOf(pipelines.getPort()),null,null,"public","TCP"))
                        .endContainer()
                        .withRestartPolicy("Always")
                        .withImagePullSecrets(new LocalObjectReference(namespace))
                        .endSpec()
                        .endTemplate()
                        .withNewSelector()
                        .addToMatchLabels("app", deploymentName)
                        .endSelector()
                        .endSpec()
                        .build();
                return K8sFactory.CreateNamespaceDeployment(client,deployment,namespace);
            }
        }
        else{
            throw new Exception("创建部署过程中查找流水线出错");
        }
    }

    /**
     * 修改api后的列出部署  fzl
     * @param namespace
     * @return
     */
    @Override
    public DeploymentList ListDeployment(String namespace) {
        return K8sFactory.ListDeployment(K8sFactory.getKubernetesClient(),namespace);
    }

    /**
     * 将namespace移动到project中
     * @param namespace
     */
    @Override
    public void moveNamespaceToProject(String namespace){
        K8sFactory.moveNamespaceToProject("token-zrhhr","9p994nphv94c7pjlgfnzdvshz2z5lnp7b7rlll9xf95zdcnrfmcrmc",namespace);
    }

    /**
     * 启动项目的自动部署接口 fzl
     * @param orgName
     * @param proGroupName
     * @param projectName
     * @param branches
     * @param projectGroupId
     * @param pipelineId
     */
    @Override
    public String runProjectGroupAndAutoConfiguration(String orgName, String proGroupName, String projectName ,String branches , String projectGroupId ,String pipelineId) throws Exception {
        // 设置操作标志位
        int count = 0x00;
        // 部署回滚标志设定
        final int step1= 0x01;
        final int step2= 0x02;
        final int step3= 0x04;
        final int step4= 0x08;
        final int step5= 0x10;
        final int step6= 0x20;
        // 局部存储命名空间id,命名空间名
        String namespaceId = null;
        String name = null;
        final KubernetesClient client = K8sFactory.getKubernetesClient();
        //若为中文，转化为英文,建立命名空间
        String orgNameEnglish = ChineseChangeToEnglish.getFullSpell(orgName);
        String proGroupNameEnglish = ChineseChangeToEnglish.getFullSpell(proGroupName);
        String projectNameEnglish = ChineseChangeToEnglish.getFullSpell(projectName);
        try {
            //判断namespace数据库中是否存在该项目组id
//            com.cloudcare.cloudcarex.devops.data.model.Namespace namespace = namespaceRepository.findOneByModel(Filter.condition()
//                    .equal("projectGroupId",projectGroupId)
//                    .equal("available",true));

//            if(namespace != null){
//                //如果存在，使用该命名空间创建部署和服务
//                name = namespace.getName();
//            }
//            else{
                //不存在 则重新创建
                //判断rancher是否已经存在该命名空间,如果存在，重新创建
            name = orgNameEnglish + '-' + proGroupNameEnglish + '-' + (int)(Math.random() * 10000);
            while(client.namespaces().withName(name).get() != null )
                name = orgNameEnglish + '-' + proGroupNameEnglish + '-' + (int)(Math.random() * 10000);
            //创建命名空间
            Namespace namespace1 = createNamespace(name);
            if( namespace1 == null ){
                throw new Exception("创建命名空间出现异常");
            }

            // 检测移动命名空间是否成功
            try {
                //移动到rancher-project中
                moveNamespaceToProject(name);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("将命名空间移动到rancher过程中出错");
            }

//            }
            // 第一步：创建命名空间成功
            count^=step1;

            //添加到数据库namespace中
//            com.cloudcare.cloudcarex.devops.data.model.Namespace NameSpace = new com.cloudcare.cloudcarex.devops.data.model.Namespace();
//            NameSpace.setName(name);
//            NameSpace.setAvailable(true);
//            NameSpace.setProjectGroupId(projectGroupId);

            //判断数据库中是否已经存在该命名空间，如果存在就更新
//            com.cloudcare.cloudcarex.devops.data.model.Namespace n = namespaceRepository.findOneByModel(
//                    Filter.condition()
//                            .equal("name",name)
//                            .equal("available",true));
//            if(n != null ){
//                //赋值给更新变量NameSpace
//                NameSpace.setId(n.getId());
//                NameSpace.setRunner(n.getRunner());
//                NameSpace.setCreateTime(n.getCreateTime());
//                // 赋值给namespaceId标志位
//                namespaceId = n.getId();
//                namespaceRepository.updateModel(NameSpace);
//            }
//            else{
//                try {
//                    namespaceRepository.createModel(NameSpace);
//                    // 赋值给namespaceId标志位
//                    com.cloudcare.cloudcarex.devops.data.model.Namespace nn = namespaceRepository.findOneByModel(
//                            Filter.condition()
//                                    .equal("name",name)
//                                    .equal("available",true));
//                    namespaceId = nn.getId();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    throw new LocaleBizServiceException("namespace数据添加失败");
//                }
//            }
            // 第二步：添加到数据库namespace成功
//            count^=step2;

            //判断部署是否已经存在
            Deployment dpms = client.apps().deployments().inNamespace(name).withName(projectNameEnglish).get();
            if( dpms != null ){
//                throw new LocaleBizServiceException("已经存在该部署");
                //删除该部署 重新创建
                if(client.apps().deployments().inNamespace(name).withName(projectNameEnglish).delete()){
                    //重新创建
                    Deployment deployment = CreateNamespaceDeployment(name, projectNameEnglish, orgNameEnglish, proGroupNameEnglish, projectNameEnglish, branches,pipelineId);
                    if(deployment == null ){
                        throw new Exception("创建部署出现异常");
                    }
                }
                else{
                    throw new Exception("删除部署失败");
                }

            }
            else{
                Deployment deployment = CreateNamespaceDeployment(name, projectNameEnglish, orgNameEnglish, proGroupNameEnglish, projectNameEnglish, branches,pipelineId);
                if(deployment == null ){
                    throw new Exception("创建部署出现异常");
                }
            }
            // 第三步： 创建服务成功
//            count^=step3;
            count^=step2;

            //判断服务是否已经存在
            io.fabric8.kubernetes.api.model.Service svc = client.services().inNamespace(name).withName(projectNameEnglish).get();
            if(svc != null ){

                if(client.services().inNamespace(name).withName(projectNameEnglish).delete()){
                    io.fabric8.kubernetes.api.model.Service service = CreateNamespaceService(name,projectNameEnglish,pipelineId);
                    if(service == null){
                        throw new Exception("创建服务出现异常");
                    }
                }
                else{
                    throw new Exception("删除服务失败");
                }
            }
            else{
                io.fabric8.kubernetes.api.model.Service service = CreateNamespaceService(name,projectNameEnglish,pipelineId);
                if(service == null){
                    throw new Exception("创建服务出现异常");
                }
            }
            // 第四步：创建服务成功
//            count^=step4;
            count^=step3;
            //添加到数据库container中
            //再次检测是否创建成功
//            com.cloudcare.cloudcarex.devops.data.model.Namespace namespace1 = namespaceRepository.findOneByModel(Filter.condition()
//                    .equal("name",name)
//                    .equal("available",true));
//            if(namespace1 == null){
//                throw new LocaleBizServiceException("命名空间不存在，由于上一步添加到命名空间数据库失败");
//            }
//
//            com.cloudcare.cloudcarex.devops.data.model.Container container = new com.cloudcare.cloudcarex.devops.data.model.Container();
//            container.setAvailable(true);
//            container.setNamespace(namespace1);
//
//            com.cloudcare.cloudcarex.devops.data.model.Container c = containerRepository.findOneByModel(
//                    Filter.condition()
//                            .equal("namespaceId",namespaceId)
//                            .equal("available",true));
//            //判断container数据库中是否已经存在该数据
//            if(c!= null ){
//                container.setId(c.getId());
//                container.setCreateTime(c.getCreateTime());
//                containerRepository.updateModel(container);
//            }
//            else{
//                try{
//                    containerRepository.createModel(container);
//                    count^=step5;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    throw new LocaleBizServiceException("container数据库添加数据失败");
//                }
//            }
//
//            //添加到application数据库中
//            Application application = applicationRepository.findOneByModel(Filter.condition()
//                    .equal("pipelinesId",pipelineId)
//                    .equal("available",true));
//
//            if( application == null ){
//                throw new LocaleBizServiceException("不存在该application");
//            }
//            else{
//                application.setAvailable(true);
//                List<ServicePort> servicePorts = client.services().inNamespace(name).withName(projectName).get().getSpec().getPorts();
//                for(ServicePort servicePort : servicePorts ){
//                    application.setUrl(Configs.getString("deployment.applicationUrl")+servicePort.getNodePort());
//                    break;
//                }
//                try {
//                    applicationRepository.updateModel(application);
//                    // 第六步
//                    count^=step6;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    throw new LocaleBizServiceException("更新application失败");
//                }
//            }

            return name;
        }catch (Exception e){
            // 部署回滚操作
//            count <<= 1;
//
//            // 第六步操作 回滚application数据库改动
//            if((count&0x20)!=0){
//                Application application = applicationRepository.findOneByModel(Filter.condition()
//                        .equal("pipelinesId",pipelineId)
//                        .equal("available",true));
//                if(application == null ){
//                    throw new LocaleBizServiceException("回滚application不存在");
//                }
//                else{
//                    try {
//                        // 重新更新
//                        application.setUrl(null);
//                        applicationRepository.updateModel(application);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                        throw new LocaleBizServiceException("回滚更新数据库application数据错误");
//                    }
//                }
//            }
//
//            count <<= 1;
//            // 第五步操作 回滚数据库container
//            if((count&0x20)!=0){
//                com.cloudcare.cloudcarex.devops.data.model.Container cc = containerRepository.findOneByModel(
//                        Filter.condition()
//                                .equal("namespaceId",namespaceId)
//                                .equal("available",true));
//                if(cc == null){
//                    throw new LocaleBizServiceException("回滚container不存在");
//                }
//                else {
//                    try{
//                        // 逻辑删除
//                        cc.setAvailable(false);
//                        containerRepository.updateModel(cc);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                        throw new LocaleBizServiceException("回滚删除数据库container数据错误");
//                    }
//
//                }
//            }

            count <<= 1;
            // 第四步操作 回滚删除服务
            if((count&0x04)!=0){
                try{
                    client.services().inNamespace(name).withName(proGroupNameEnglish).delete();
                }catch (Exception e1){
                    e1.printStackTrace();
                    throw new Exception("回滚删除服务错误");
                }
            }

            count<<=1;
            // 第三步操作：回滚删除部署
            if((count&0x04)!=0){
                try{
                    client.apps().deployments().inNamespace(name).withName(projectNameEnglish).delete();
                }catch (Exception e1){
                    e1.printStackTrace();
                    throw new Exception("回滚删除部署错误");
                }
            }

//            count<<=1;
//            // 第二步操作：回滚删除namespace数据库数据
//            if((count&0x20)!=0){
//                com.cloudcare.cloudcarex.devops.data.model.Namespace namespace2 = namespaceRepository.findOneByModel(Filter.condition()
//                        .equal("id",namespaceId)
//                        .equal("available",true));
//                if(namespace2!=null){
//                    try {
//                        // 逻辑删除
//                        namespace2.setAvailable(false);
//                        namespaceRepository.updateModel(namespace2);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                        throw new LocaleBizServiceException("回滚删除命名空间数据库数据错误");
//                    }
//                }
//                else{
//                    throw new LocaleBizServiceException("回滚过程无法查到namespace");
//                }
//
//            }

            count<<=1;
            // 第一步操作：回滚删除namespace
            if((count&0x04)!=0){
                try {
                    client.namespaces().withName(name).delete();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new Exception("回滚删除命名空间数据库数据错误");
                }
            }

            e.printStackTrace();
            throw new Exception("Deploy false");
        }
    }


    /**
     * 重新部署 fzl
     * @param namespace
     * @param deploymentName
     * @param orgName
     * @param projectGroupName
     * @param projectName
     * @param branches
     * @param pipelinesId
     * @return
     */
    @Override
    public Deployment repeatDeployment(String namespace, String deploymentName, String orgName, String projectGroupName, String projectName, String branches, String pipelinesId) throws Exception {
//        Pipelines pipelines = pipelinesRepository.findOneByModel(Filter.condition()
//                .equal("id",pipelinesId)
//                .equal("available",true));
        K8sPipelinesEntity pipelines = baseMapper.selectById(pipelinesId);
        if(pipelines!=null){
            try (final KubernetesClient client = K8sFactory.getKubernetesClient()) {
                //若为中文，转化为英文,建立命名空间
                String orgNameEnglish = ChineseChangeToEnglish.getFullSpell(orgName);
                String proGroupNameEnglish = ChineseChangeToEnglish.getFullSpell(projectGroupName);
                String projectNameEnglish = ChineseChangeToEnglish.getFullSpell(projectName);

                // 删除该命名空间中的部署
                client.apps().deployments().inNamespace(namespace).delete();

                //重新创建部署
                Deployment deployment = new DeploymentBuilder().withApiVersion("apps/v1beta2")
                        .withKind("Deployment")
                        .withNewMetadata()
                        .withName(deploymentName)
                        .withNamespace(namespace)
                        .endMetadata()
                        .withNewSpec()
                        .withReplicas(1)
                        .withNewTemplate()
                        .withNewMetadata()
                        .addToLabels("app", deploymentName)
                        .endMetadata()
                        .withNewSpec()
                        .addNewContainer()
                        .withName(deploymentName)
//            docker pull 172.16.165.108:5000/cloudcarex_user_registry/projectpath:master
//            docker pull 172.16.165.108:5000/cloudcarex_user_registry/w/g4/p4:master
                        .withImage("172.16.165.108:5000/cloudcarex_user_registry/"+orgNameEnglish+"/"+proGroupNameEnglish+"/"+projectNameEnglish+":"+branches)
                        .withImagePullPolicy("Always")
                        .withPorts(new ContainerPort(Integer.valueOf(pipelines.getPort()),null,null,"public","TCP"))
                        .endContainer()
                        .withRestartPolicy("Always")
                        .withImagePullSecrets(new LocalObjectReference(namespace))
                        .endSpec()
                        .endTemplate()
                        .withNewSelector()
                        .addToMatchLabels("app", deploymentName)
                        .endSelector()
                        .endSpec()
                        .build();
                return K8sFactory.CreateNamespaceDeployment(client,deployment,namespace);
            }
        }
        else{
            throw new Exception("重新创建部署过程中查找流水线出错");
        }
    }

}

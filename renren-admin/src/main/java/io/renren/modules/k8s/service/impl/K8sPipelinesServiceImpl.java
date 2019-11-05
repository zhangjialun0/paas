package io.renren.modules.k8s.service.impl;

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
    public KubernetesClient test_rancher() {
        return K8sFactory.getKubernetesClient();
    }

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
        try( final KubernetesClient client = K8sFactory.getKubernetesClient() ){
            Namespace namespace = new NamespaceBuilder().withApiVersion("v1")
                    .withKind("Namespace")
                    .withNewMetadata()
//                    .withNamespace("cloudcarex")
                    .withName(name)
                    .addToLabels("name",name)
                    .withClusterName("cloudcarex")
                    .endMetadata()
                    .build();

//            Namespace namespace1 = K8sFactory.CreateNamespace(K8sFactory.getKubernetesClient(),namespace);
            Namespace namespace1 = client.namespaces().create(namespace);

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

            return namespace1;
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
     * @return
     */
    @Override
    public io.fabric8.kubernetes.api.model.Service CreateNamespaceService(String namespace, String serviceName ){

            try (final KubernetesClient client = K8sFactory.getKubernetesClient()){
                io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder().withApiVersion("v1").withKind("Service").withNewSpec()
                        .withSelector(Collections.singletonMap("app", serviceName))
                        .withType("NodePort")
                        .addNewPort()
                        .withName("port")
                        .withProtocol("TCP")
                        .withPort(8080)
//                        Integer.valueOf(pipelines.getPort())
                        .withTargetPort(new IntOrString(8080))
//                        new IntOrString(Integer.valueOf(pipelines.getPort()))
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
     * @return
     */
    @Override
    public Deployment CreateNamespaceDeployment(String namespace, String deploymentName , String projectName , String branches ) {

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
                        .withImage("172.16.165.108:5000/cloudcarex_user_registry/"+"paasproject"+"/"+projectName+":"+branches)
                        .withImagePullPolicy("Always")
                        .withPorts(new ContainerPort(8080,null,null,"public","TCP"))
//                        Integer.valueOf(pipelines.getPort()
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
     * @param projectName
     * @param branches
     */
    @Override
    public String runProjectGroupAndAutoConfiguration( String projectName ,String branches ) throws Exception {
        // 设置操作标志位
        int count = 0x00;
        // 部署回滚标志设定
        final int step1= 0x01;
        final int step2= 0x02;
        final int step3= 0x04;
        final int step4= 0x08;
        final int step5= 0x10;
        final int step6= 0x20;
        String name = null;
        final KubernetesClient client = K8sFactory.getKubernetesClient();
        //若为中文，转化为英文,建立命名空间
//        String orgNameEnglish = ChineseChangeToEnglish.getFullSpell(orgName);
//        String proGroupNameEnglish = ChineseChangeToEnglish.getFullSpell(proGroupName);
        String proGroupNameEnglish = "paasproject";
        String projectNameEnglish = ChineseChangeToEnglish.getFullSpell(projectName);
        try {
            //判断rancher是否已经存在该命名空间,如果存在，重新创建
            name =  proGroupNameEnglish + '-' + projectNameEnglish+(int)(Math.random() * 10000);
            if(client.namespaces().withName(name).get() != null )
                name = proGroupNameEnglish + '-' + projectNameEnglish + (int)(Math.random() * 10000);
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

            //判断部署是否已经存在
            Deployment dpms = client.apps().deployments().inNamespace(name).withName(projectNameEnglish).get();
            if( dpms != null ){
//                throw new LocaleBizServiceException("已经存在该部署");
                //删除该部署 重新创建
                if(client.apps().deployments().inNamespace(name).withName(projectNameEnglish).delete()){
                    //重新创建
                    Deployment deployment = CreateNamespaceDeployment(name, projectNameEnglish,projectNameEnglish, branches);
                    if(deployment == null ){
                        throw new Exception("创建部署出现异常");
                    }
                }
                else{
                    throw new Exception("删除部署失败");
                }

            }
            else{
                Deployment deployment = CreateNamespaceDeployment(name, projectNameEnglish,projectNameEnglish, branches);
                if(deployment == null ){
                    throw new Exception("创建部署出现异常");
                }
            }
            // 第二步： 创建服务成功
//            count^=step3;
            count^=step2;

            //判断服务是否已经存在
            io.fabric8.kubernetes.api.model.Service svc = client.services().inNamespace(name).withName(projectNameEnglish).get();
            if(svc != null ){

                if(client.services().inNamespace(name).withName(projectNameEnglish).delete()){
                    io.fabric8.kubernetes.api.model.Service service = CreateNamespaceService(name,projectNameEnglish);
                    if(service == null){
                        throw new Exception("创建服务出现异常");
                    }
                }
                else{
                    throw new Exception("删除服务失败");
                }
            }
            else{
                io.fabric8.kubernetes.api.model.Service service = CreateNamespaceService(name,projectNameEnglish);
                if(service == null){
                    throw new Exception("创建服务出现异常");
                }
            }
            // 第三步：创建服务成功
//            count^=step4;
            count^=step3;

            return name;
        }catch (Exception e){
            // 部署回滚操作

            count <<= 1;
            // 第三步操作 回滚删除服务
            if((count&0x04)!=0){
                try{
                    client.services().inNamespace(name).withName(proGroupNameEnglish).delete();
                }catch (Exception e1){
                    e1.printStackTrace();
                    throw new Exception("回滚删除服务错误");
                }
            }

            count<<=1;
            // 第二步操作：回滚删除部署
            if((count&0x04)!=0){
                try{
                    client.apps().deployments().inNamespace(name).withName(projectNameEnglish).delete();
                }catch (Exception e1){
                    e1.printStackTrace();
                    throw new Exception("回滚删除部署错误");
                }
            }

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
     * @param projectName
     * @param branches
     * @return
     */
    @Override
    public Deployment repeatDeployment(String namespace, String deploymentName, String projectName, String branches){

            try (final KubernetesClient client = K8sFactory.getKubernetesClient()) {
                //若为中文，转化为英文,建立命名空间
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
                        .withImage("172.16.165.108:5000/cloudcarex_user_registry/"+"paasproject"+"/"+projectNameEnglish+":"+branches)
                        .withImagePullPolicy("Always")
                        .withPorts(new ContainerPort(8080,null,null,"public","TCP"))
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

}

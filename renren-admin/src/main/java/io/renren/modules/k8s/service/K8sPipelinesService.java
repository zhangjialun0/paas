package io.renren.modules.k8s.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.renren.common.utils.PageUtils;
import io.renren.modules.k8s.entity.K8sPipelinesEntity;

import java.util.Map;

/**
 * 
 *
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-10-20 19:11:33
 */
public interface K8sPipelinesService extends IService<K8sPipelinesEntity> {

    KubernetesClient test_rancher();

    PageUtils queryPage(Map<String, Object> params);

    Namespace createNamespace(String name);

    NamespaceList listNamespace();

    Pod CreatePod(String namespace, String podName);

    PodList ListPod(String namespace);

    Service CreateNamespaceService(String namespace, String serviceName, String pipelinesId) throws Exception;

    ServiceList ListServise(String namespace);

    Deployment CreateNamespaceDeployment(String namespace, String deploymentName, String orgName, String projectGroupName, String projectName, String branche, String pipelinesId) throws Exception;

    DeploymentList ListDeployment(String namespace);

    void moveNamespaceToProject(String namespace);

    String runProjectGroupAndAutoConfiguration(String orgName, String proGroupName, String projectName, String branches, String projectGroupId, String pipelineId) throws Exception;

    Deployment repeatDeployment(String namespace, String deploymentName, String orgName, String projectGroupName, String projectName, String branches, String pipelinesId) throws Exception;
}


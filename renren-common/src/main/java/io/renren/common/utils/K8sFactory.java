package io.renren.common.utils;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 关于K8s的所有操作
 * @author fzl
 * @date 2019-10-25
 */
public class K8sFactory {

    /**
     * 获取客户端
     * @return
     */
    public static KubernetesClient getKubernetesClient() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        InputStream resourceAsStream = contextClassLoader.getResourceAsStream("conf/kubeConfig");

        InputStreamReader reader = new InputStreamReader(resourceAsStream);
        int tempchar;
        String content = null;
        Config config = null;
        try {
            while ((tempchar = reader.read()) != -1){
                if (((char) tempchar) != '\r') {
                    content += (char) tempchar;
                }
            }

            config = Config.fromKubeconfig(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        URL kubeConfig = contextClassLoader.getResource("./conf/kubeConfig");
//        String kubeConfigPath = kubeConfig.getPath();

//        File file = new File(kubeConfigPath);
//        content = FileUtils.readFileToString(file);


        KubernetesClient client = new DefaultKubernetesClient(config);

        return client;
    }

    /**
     * 创建命名空间
     * @param client
     * @param namespace
     * @return
     */
    public static Namespace CreateNamespace(KubernetesClient client , Namespace namespace){
        return client.namespaces().create(namespace);
    }

    /**
     * 列出命名空间
     * @param client
     * @return
     */
    public static NamespaceList ListNamespace(KubernetesClient client ){
        return client.namespaces().list();
    }

    /**
     * 创建pod
     * @param client
     * @param pod
     * @return
     */
    public static Pod CreatePod(KubernetesClient client , Pod pod , String namespace){
        return client.pods().inNamespace(namespace).create(pod);
    }

    /**
     * 列出pod
     * @param client
     * @param namespace
     * @return
     */
    public static PodList ListPod(KubernetesClient client, String namespace){
        return client.pods().inNamespace(namespace).list();
    }

    /**
     * 创建节点
     * @param client
     * @param node
     * @return
     */
    public static Node CreateNode(KubernetesClient client , Node node){
        return client.nodes().create(node);
    }

    /**
     * 列出节点
     * @param client
     * @return
     */
    public static NodeList ListNode(KubernetesClient client){
        return client.nodes().list();
    }

    /**
     * 创建服务
     * @param client
     * @param service
     * @param namespace
     * @return Service
     * @author fzl
     */
    public static Service CreateNamespaceService(KubernetesClient client,Service service,String namespace){
        return client.services().inNamespace(namespace).create(service);
    }

    /**
     * 列出服务
     * @param client
     * @param namespace
     * @return
     */
    public static ServiceList ListService(KubernetesClient client,String namespace){
        return client.services().inNamespace(namespace).list();
    }

    /**
     * 创建部署
     * @author fzl
     * @param client
     * @param deployment
     * @param namespace
     * @return Deployment
     */
    public static Deployment CreateNamespaceDeployment(KubernetesClient client,Deployment deployment,String namespace){
        return client.apps().deployments().inNamespace(namespace).create(deployment);
    }

    /**
     * 列出部署
     * @param client
     * @param namespace
     * @return
     */
    public static DeploymentList ListDeployment(KubernetesClient client,String namespace){
        return client.apps().deployments().inNamespace(namespace).list();
    }

    /**
     * 移动namespace到project
     * @param accessKey
     * @param secretKey
     * @param namespace
     */
    public static void moveNamespaceToProject(String accessKey,String secretKey,String namespace){
        String[] curl = {"curl","-u",accessKey+":"+secretKey,"-X","POST","-H","Accept: application/json","-H","Content-Type: application/json","-d","{\"projectId\":\"c-8cx8c:p-d4zfk\"}","https://121.199.8.245/v3/cluster/c-8cx8c/namespaces/"+namespace+"?action=move","-k"};

        ProcessBuilder pb = new ProcessBuilder(curl);
        pb.redirectErrorStream(true);
        Process p = null;
        try {
            p = pb.start();
            BufferedReader br = null;
            String line = null;

            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while((line = br.readLine()) != null){
                System.out.println("\t" + line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            p.destroy();
        }
    }
}

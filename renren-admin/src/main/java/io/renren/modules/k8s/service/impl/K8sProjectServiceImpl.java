package io.renren.modules.k8s.service.impl;

import io.renren.modules.sys.controller.AbstractController;
import io.renren.modules.sys.dao.SysUserDao;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.service.SysUserService;
import io.renren.modules.sys.service.impl.SysUserServiceImpl;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;

import io.renren.modules.k8s.dao.K8sProjectDao;
import io.renren.modules.k8s.entity.K8sProjectEntity;
import io.renren.modules.k8s.service.K8sProjectService;

import static org.gitlab4j.api.models.Visibility.PRIVATE;


@Service("k8sProjectService")
public class K8sProjectServiceImpl extends ServiceImpl<K8sProjectDao, K8sProjectEntity>  implements K8sProjectService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<K8sProjectEntity> page = this.page(
                new Query<K8sProjectEntity>().getPage(params),
                new QueryWrapper<K8sProjectEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public boolean save(K8sProjectEntity k8sProject,SysUserEntity userEntity) {
        try {
            //gitlab操作

//        SysUserService sysUserService = new SysUserServiceImpl();
//        SysUserEntity userEntity = sysUserService.getById(userid);
            GitLabApi gitLabApi = new GitLabApi("http://121.199.40.157","HuBrDB4623XFxZ-DmHdf");
            gitLabApi.sudo(userEntity.getUsername());
            Project project =new Project();
            project.setName(k8sProject.getName());
            project.setPath(k8sProject.getPath());

            project.setCreatorId(userEntity.getGitlabId());
            project.setDescription(k8sProject.getDescription());
            project.setVisibility(PRIVATE);
            Owner own = new Owner();
            own.setId(userEntity.getGitlabId());
            project.setOwner(own);

            project = gitLabApi.getProjectApi().createProject(project);

            List<Project> projects = new ArrayList<>();

            projects.add(project);



            List<Group> group = gitLabApi.getGroupApi().getGroups();

            Group group1 = group.get(0);

            List<Project> group2 = group1.getProjects();

            group1.setProjects(projects);
            gitLabApi.unsudo();
            gitLabApi.getGroupApi().transferProject(group.get(0).getId(),project.getId());

            k8sProject.setGitlabProjectId(project.getId());
            k8sProject.setDefaultBranch(project.getDefaultBranch());
            k8sProject.setHttpUrlToRepo(project.getHttpUrlToRepo());
            k8sProject.setPathWithNamespace(project.getPathWithNamespace());
            k8sProject.setSshUrlToRepo(project.getSshUrlToRepo());

            this.save(k8sProject);
        } catch (GitLabApiException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void createTwoFiles(K8sProjectEntity k8sProjectEntity,SysUserEntity userEntity){
        GitLabApi gitLabApi = new GitLabApi("http://121.199.40.157","HuBrDB4623XFxZ-DmHdf");
        Project project = null;
        try {
            project = gitLabApi.getProjectApi().getProject(k8sProjectEntity.getGitlabProjectId());
        String dockerfile = "FROM tomcat\n" +
                "RUN rm -rf /usr/local/tomcat/webapps/* \n" +
                "ADD ./target /usr/local/tomcat/webapps/ \n" +
                "ENV JAVA_OPTIONS \"-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.country=CN -Duser.timezone=UTC\" \n" +
                "EXPOSE 8080";
        String gitlabYMl ="job_build:\n" +
                "  image: maven:latest\n" +
                "  stage: build\n" +
                "  script:\n" +
                "    - mvn clean install -e -U\n" +
                "  artifacts:\n" +
                "    name: "+project.getName()+"\n" +
                "    untracked: true\n" +
                "    expire_in: 60 mins\n" +
                "    paths:\n" +
                "      - ./\n" +
                "  tags:\n" +
                "    - maven\n" +
                "\n" +
                "job_release:\n" +
                "  stage: release\n" +
                "  services:\n" +
                "    - docker:dind\n" +
                "  before_script:\n" +
                "    - docker login -u \"$CI_REGISTRY_USER\" -p \"$CI_REGISTRY_PASSWORD\" $CI_REGISTRY\n" +
                "  script:\n" +
                "    - docker build -t $CI_REGISTRY_PATH/$CI_PROJECT_NAME:$CI_COMMIT_REF_SLUG .\n" +
                "    - docker push $CI_REGISTRY_PATH/$CI_PROJECT_NAME:$CI_COMMIT_REF_SLUG\n" +
                "  tags:\n" +
                "    - maven";

            CommitAction commitAction1 =createFile(project,userEntity,"/Dockerfile",dockerfile);
            CommitAction commitAction2 =createFile(project,userEntity,"/.gitlab-ci.yml",gitlabYMl);
            List<CommitAction> commitActions = new ArrayList<>();
            commitActions.add(commitAction1);
            commitActions.add(commitAction2);
//            GitLabApi gitLabApi = new GitLabApi("http://121.199.40.157","HuBrDB4623XFxZ-DmHdf");
            gitLabApi.getCommitsApi().createCommit(project.getId(),"master","File",null,null,null,commitActions);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private CommitAction createFile(Project project,SysUserEntity userEntity,String filename,String s)throws Exception{
        try {
            GitLabApi gitLabApi = new GitLabApi("http://121.199.40.157","HuBrDB4623XFxZ-DmHdf");
            gitLabApi.sudo(userEntity.getUsername());
            CommitAction action = new CommitAction();
            action.setAction(CommitAction.Action.UPDATE);

            RepositoryFile repositoryFile = null;
            try{
                repositoryFile = gitLabApi.getRepositoryFileApi().getFile(project.getId(),filename,"master");
            }catch (GitLabApiException e){
                if (e.getHttpStatus() == 404){
                    action.setAction(CommitAction.Action.CREATE);
                }
            }

            action.setFilePath(filename);
            action.setContent(s);
            return action;
            //  gitLabApi.getCommitsApi().createCommit(projectId,"master","File",null,null,null,action);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}
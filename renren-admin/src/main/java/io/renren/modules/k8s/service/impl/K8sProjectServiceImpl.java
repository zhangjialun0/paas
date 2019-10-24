package io.renren.modules.k8s.service.impl;

import io.renren.modules.sys.controller.AbstractController;
import io.renren.modules.sys.dao.SysUserDao;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.service.SysUserService;
import io.renren.modules.sys.service.impl.SysUserServiceImpl;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.Owner;
import org.gitlab4j.api.models.Project;
import org.springframework.stereotype.Service;

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

        gitLabApi.sudo(userEntity.getUsername());

        List<Group> group = gitLabApi.getGroupApi().getGroups();

        group.get(0).getProjects().add(project);

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

}

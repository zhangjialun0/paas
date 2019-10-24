package io.renren.modules.sys.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;

import io.renren.modules.sys.dao.K8sTemplateDao;
import io.renren.modules.sys.entity.K8sTemplateEntity;
import io.renren.modules.sys.service.K8sTemplateService;


@Service("k8sTemplateService")
public class K8sTemplateServiceImpl extends ServiceImpl<K8sTemplateDao, K8sTemplateEntity> implements K8sTemplateService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<K8sTemplateEntity> page = this.page(
                new Query<K8sTemplateEntity>().getPage(params),
                new QueryWrapper<K8sTemplateEntity>()
        );

        return new PageUtils(page);
    }

}

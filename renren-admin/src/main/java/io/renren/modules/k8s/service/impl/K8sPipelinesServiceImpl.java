package io.renren.modules.k8s.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;

import io.renren.modules.k8s.dao.K8sPipelinesDao;
import io.renren.modules.k8s.entity.K8sPipelinesEntity;
import io.renren.modules.k8s.service.K8sPipelinesService;


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

}

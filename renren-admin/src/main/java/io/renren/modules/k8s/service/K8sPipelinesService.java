package io.renren.modules.k8s.service;

import com.baomidou.mybatisplus.extension.service.IService;
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

    PageUtils queryPage(Map<String, Object> params);
}


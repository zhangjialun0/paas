package io.renren.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.k8s.entity.K8sProjectEntity;
import io.renren.modules.sys.entity.K8sTemplateEntity;

import java.util.Map;

/**
 * 
 *
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-10-20 19:11:33
 */
public interface K8sTemplateService extends IService<K8sTemplateEntity> {

    PageUtils queryPage(Map<String, Object> params);


}


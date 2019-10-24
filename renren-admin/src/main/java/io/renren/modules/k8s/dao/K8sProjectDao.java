package io.renren.modules.k8s.dao;

import io.renren.modules.k8s.entity.K8sProjectEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-10-20 19:11:33
 */
@Mapper
public interface K8sProjectDao extends BaseMapper<K8sProjectEntity> {
	
}

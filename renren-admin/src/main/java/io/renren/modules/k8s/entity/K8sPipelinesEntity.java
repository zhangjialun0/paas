package io.renren.modules.k8s.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-10-20 19:11:33
 */
@Data
@TableName("k8s_pipelines")
public class K8sPipelinesEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Integer id;
	/**
	 * 
	 */
	private Integer gitlabPipelinesId;
	/**
	 * 名称
	 */
	private String name;
	/**
	 * 
	 */
	private Integer projectId;
	/**
	 * 编程语言
	 */
	private String language;
	/**
	 * 构建方式
	 */
	private String buildType;
	/**
	 * 
	 */
	private String port;
	/**
	 * 分支名称
	 */
	private String branch;
	/**
	 * 
	 */
	private String buildOrder;
	/**
	 * 
	 */
	private String dockerfileOrder;

}

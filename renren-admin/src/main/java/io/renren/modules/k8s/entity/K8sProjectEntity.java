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
@TableName("k8s_project")
public class K8sProjectEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Integer id;
	/**
	 * 路径命名空间
	 */
	private String pathWithNamespace;
	/**
	 * 
	 */
	private Integer gitlabProjectId;
	/**
	 * 
	 */
	private String description;
	/**
	 * 默认分支
	 */
	private String defaultBranch;
	/**
	 * ssh地址
	 */
	private String sshUrlToRepo;
	/**
	 * http地址
	 */
	private String httpUrlToRepo;
	/**
	 * 项目名
	 */
	private String name;
	/**
	 * 
	 */
	private String path;

}

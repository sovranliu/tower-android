package com.qcast.tower.business.user;

import java.io.Serializable;

import com.slfuture.carrie.base.json.JSONVisitor;

/**
 * 注册好友
 */
public class Friend extends User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 解析数据生成用户对象
	 * 
	 * @param visitor 数据
	 * @return 解析结果
	 */
	public boolean parse(JSONVisitor visitor) {
		if(!super.parse(visitor)) {
			return false;
		}
		return true;
	}
}

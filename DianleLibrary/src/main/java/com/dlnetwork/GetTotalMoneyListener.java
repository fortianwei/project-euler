package com.dlnetwork;

/**
 * @Description: 获取虚拟货币余额的接口。成功则返回虚拟货币的名称，余额，失败时返回失败的信息
 * @author: Jiangtao.Cai
 * @date: 2011-9-25
 */
public interface GetTotalMoneyListener {
	/**
	 * @Description: 成功则返回虚拟货币的名称，余额
	 * @author: Jiangtao.Cai
	 * @date: 2011-9-25
	 */
	public void getTotalMoneySuccessed(String name, long amount);

	/**
	 * @Description: 失败时返回失败的信息
	 * @author: Jiangtao.Cai
	 * @date: 2011-9-25
	 */
	public void getTotalMoneyFailed(String error);

}

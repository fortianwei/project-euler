package com.dlnetwork;

/**
 * 
 * @Description:赠送 虚拟币的接口， 赠送成功则返回用户的余额,失败则返回失败信息。
 * @author: Jiangtao.Cai
 * @date: 2011-9-25
 */
public interface GiveMoneyListener {
	/**
	 * @author: Jiangtao.Cai
	 * @date: 2011-9-25
	 * @Description: 返回用户的余额
	 * @param amount用户的余额
	 */
	public void giveMoneySuccess(long amount);

	/**
	 * @param error失败信息
	 * @author: Jiangtao.Cai
	 * @date: 2011-9-25
	 * @Description: 失败则返回失败信息
	 */
	public void giveMoneyFailed(String error);
}

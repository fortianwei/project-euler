package com.dlnetwork;

/**
 * @Description: 扣除虚拟货币的接口。扣除成功则返回余额，失败时返回失败的信息
 * @author: Jiangtao.Cai
 * @date: 2011-9-25
 */
public interface SpendMoneyListener {
	/**
	 * 
	 * @param amount
	 *            虚拟货币的余额
	 */
	public void spendMoneySuccess(long amount);

	/**
	 * fasdjp
	 * 
	 * @param error
	 *            失败时返回失败的信息
	 */
	public void spendMoneyFailed(String error);
}

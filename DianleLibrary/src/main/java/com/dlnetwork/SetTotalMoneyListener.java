package com.dlnetwork;
/**
 * @Description: 设置用户虚拟货币总额的接口。成功则返回虚拟货币的名称，余额，失败时返回失败的信息
 * @author: Jiangtao.Cai
 * @date: 2011-9-25
 */
public interface SetTotalMoneyListener {

		/**
		 * @Description: 设置用户虚拟货币总额成功则返回虚拟货币的名称，余额
		 * @author: Jiangtao.Cai
		 * @date: 2011-9-25
		 */
		public void setTotalMoneySuccessed(String name, long amount);

		/**
		 * @Description: 失败时返回失败的信息
		 * @author: Jiangtao.Cai
		 * @date: 2011-9-25
		 */
		public void setTotalMoneyFailed(String error);

}

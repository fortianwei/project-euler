package com.dlnetwork;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wanggang
 * Date: 12-12-5
 * Time: 下午6:08
 * To change this template use File | Settings | File Templates.
 */
public interface GetAdListListener {
    /*
    *  if the server returns the ad-list successfully
    * */
    public void getAdListSucceeded(List adList);

    /*
    *  if it failed to get the ad-list
    * */
    public void getAdListFailed(String error);
}

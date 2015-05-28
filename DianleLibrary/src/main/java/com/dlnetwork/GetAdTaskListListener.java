package com.dlnetwork;

import java.util.List;

public interface GetAdTaskListListener {
	  /*
	    *  if the server returns the ad-list successfully
	    * */
	    public void getAdListSucceeded(List adList);

	    /*
	    *  if it failed to get the ad-list
	    * */
	    public void getAdListFailed(String error);
}

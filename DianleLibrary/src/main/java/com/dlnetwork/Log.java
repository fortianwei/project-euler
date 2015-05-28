package com.dlnetwork;

/**
 * Created by tianwei on 15/5/28.
 */

/**
 * 封装LOG，可变参数，如果给出一个参数，那么默认使用默认tag
 * 如果使用2个及以上的参数，第一个是tag，第二个是message，后面的抛弃
 */
public class Log {

    private static final boolean LOG_ENABLED = false;
    private static final String DEFAULT_TAG = "Di"+"".trim()+"an"+"".trim()+"Joy";

    public static void v(String... strs){
        if(LOG_ENABLED ){
            if(strs.length > 1) {
                android.util.Log.v(strs[0], strs[1]);
            }else if(strs.length == 1){
                android.util.Log.v(DEFAULT_TAG,strs[0]);
            }
        }
    }

    public static void d(String... strs){
        if(LOG_ENABLED ){
            if(strs.length > 1) {
                android.util.Log.d(strs[0], strs[1]);
            }else if(strs.length == 1){
                android.util.Log.v(DEFAULT_TAG,strs[0]);
            }
        }
    }

    public static void i(String... strs){
        if(LOG_ENABLED ){
            if(strs.length > 1) {
                android.util.Log.i(strs[0], strs[1]);
            }else if(strs.length == 1){
                android.util.Log.v(DEFAULT_TAG,strs[0]);
            }
        }
    }

    public static void w(String... strs){
        if(LOG_ENABLED ){
            if(strs.length > 1) {
                android.util.Log.w(strs[0], strs[1]);
            }else if(strs.length == 1){
                android.util.Log.v(DEFAULT_TAG,strs[0]);
            }
        }
    }

    public static void e(String... strs){
        if(LOG_ENABLED ){
            if(strs.length > 1) {
                android.util.Log.e(strs[0], strs[1]);
            }else if(strs.length == 1){
                android.util.Log.v(DEFAULT_TAG,strs[0]);
            }
        }
    }
}

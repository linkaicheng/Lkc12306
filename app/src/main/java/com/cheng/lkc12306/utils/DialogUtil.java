package com.cheng.lkc12306.utils;

import android.content.DialogInterface;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2016/8/16 0016.
 */
public class DialogUtil {
  public static void dialogClose(DialogInterface dialog,boolean flag){
        try {
            Field field=dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog,flag);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }
}

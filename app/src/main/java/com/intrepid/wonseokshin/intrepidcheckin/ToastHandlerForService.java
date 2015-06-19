package com.intrepid.wonseokshin.intrepidcheckin;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class ToastHandlerForService extends Handler{

    Context context;
    public String toastMessage = "";
    private static ToastHandlerForService instance = null;

    protected ToastHandlerForService() {
        // Exists only to defeat instantiation.
    }

    public static ToastHandlerForService getInstance(Context context) {
        if(instance == null) {
            instance = new ToastHandlerForService();
        }

        instance.context = context;
        return instance;
    }

    public void setToastMessage(String message){
        toastMessage = message;
    }

    @Override
    public void handleMessage(Message msg)
    {
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
    }
}

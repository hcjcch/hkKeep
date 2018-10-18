package com.example.hubaoyu.threebody.http;

import android.util.Log;

import com.example.hubaoyu.threebody.MainActivity;
import com.example.hubaoyu.threebody.MainThreadUtils;
import com.example.hubaoyu.threebody.model.SquatModel;
import com.example.hubaoyu.threebody.model.SquatModelResponse;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rx.functions.Action1;

/**
 * SquatHttp
 *
 * @author huangchen
 */
public class SquatHttp {

    private static OkHttpClient mOkHttpClient = new OkHttpClient();
    private static Gson gson = new Gson();
    private Action1<SquatModel> dataCall;
    private ExecutorService executorService;
    private boolean stop;

    public void start() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new MyRunable());
    }

    public void stop() {
        stop = true;
        executorService.shutdownNow();
    }

    private void pollingData() {
        while (true) {
            if (stop) {
                break;
            }
            long lastRequestTime = System.currentTimeMillis();
            getData();
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRequestTime > 50) {
                getData();
            } else {
                try {
                    Log.d("huangchen", String.valueOf(50 - (currentTime - lastRequestTime)));
                    Thread.sleep(50 - (currentTime - lastRequestTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getData();
            }
        }


    }

    private void getData() {
        final Request request = new Request.Builder().url("http://" + MainActivity.IP + ":8100/hackthon/user").build();
        try {
            Response response = mOkHttpClient.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                final SquatModelResponse squatModelResponse = gson.fromJson(responseBody.string(), SquatModelResponse.class);
                if (squatModelResponse.isOk() && squatModelResponse.getData() != null) {
                    MainThreadUtils.post(new Runnable() {
                        @Override
                        public void run() {
                            dataCall.call(squatModelResponse.getData());
                        }
                    });
                    Log.d("huangchen", squatModelResponse.getData().toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyRunable implements Runnable {
        @Override
        public void run() {
            pollingData();
        }
    }

    public void setDataCall(Action1<SquatModel> dataCall) {
        this.dataCall = dataCall;
    }
}
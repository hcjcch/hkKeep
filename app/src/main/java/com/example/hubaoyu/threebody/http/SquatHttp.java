package com.example.hubaoyu.threebody.http;

import android.util.Log;

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
        if (stop) {
            return;
        }
        long lastRequestTime = System.currentTimeMillis();
        final Request request = new Request.Builder().url("http://10.2.25.238:8100/hackthon/user").build();
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
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime > 100) {
            pollingData();
        } else {
            try {
                Thread.sleep(100 - (currentTime - lastRequestTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pollingData();
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
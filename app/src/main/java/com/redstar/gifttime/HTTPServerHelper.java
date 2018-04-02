package com.redstar.gifttime;


import android.net.Proxy;
import android.net.wifi.hotspot2.pps.Credential;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Challenge;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class HTTPServerHelper {

    private OkHttpClient client;

    private static final String SERVER_IP = "80.93.182.129:3000";
    private static final String PROTOCOL = "http";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public HTTPServerHelper(final String email, final String password) {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {

                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());

                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .addInterceptor(new BasicAuthInterceptor(email, password))
                .build();
    }

    public HTTPServerHelper() {
        client = new OkHttpClient();
        Log.w("Gift Time", "Created an empty client!");
    }

    private String buildURL(String sURL) {
        Log.w("Gift Time", "Request URL: " + PROTOCOL + "://" + SERVER_IP + sURL);
        return PROTOCOL + "://" + SERVER_IP + sURL;
    }

    public HTTPAnswer doPostQuery(String sURL, String query) {
        try {
            RequestBody body = RequestBody.create(JSON, query.getBytes());
            Request request = new Request.Builder()
                    .url(buildURL(sURL))
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            return new HTTPAnswer(response.body().string(), response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HTTPAnswer doPutQuery(String sURL, String query) {
        try {
            RequestBody body = RequestBody.create(JSON, query.getBytes());
            Request request = new Request.Builder()
                    .url(buildURL(sURL))
                    .put(body)
                    .build();
            Response response = client.newCall(request).execute();

            return new HTTPAnswer(response.body().string(), response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HTTPAnswer doGetQuery(String sURL) {
        try {
            Request request = new Request.Builder()
                    .url(buildURL(sURL))
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            return new HTTPAnswer(response.body().string(), response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HTTPAnswer doDeleteQuery(String sURL) {
        try {
            Request request = new Request.Builder()
                    .url(buildURL(sURL))
                    .delete()
                    .build();

            Response response = client.newCall(request).execute();
            return new HTTPAnswer(null, response.code());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    class HTTPAnswer {
        private String answer = null;
        private int responseCode;

        public HTTPAnswer(String answer, int responseCode) {
            this.answer = answer;
            this.responseCode = responseCode;
        }


        public String getAnswerBody() {
            return answer;
        }


        public int getResponseCode() {
            return responseCode;
        }
    }

    public class BasicAuthInterceptor implements Interceptor {

        private String credentials;

        public BasicAuthInterceptor(String user, String password) {
            this.credentials = Credentials.basic(user, password);
            Log.w("Gift Time", "User: " + user + " " + password);
            Log.w("Gift Time", "Credential: " + credentials);
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Log.w("Gift Time", "Intercept!");
            Request request = chain.request();
            Log.w("Gift Time", "Headers num: " + request.headers().size());
            Request authenticatedRequest = request.newBuilder()
                    .addHeader("Authorization", credentials).build();
            Log.w("Gift Time", "Headers num: " + authenticatedRequest.headers().size());
            Log.w("Gift Time", authenticatedRequest.headers().name(0));
            return chain.proceed(authenticatedRequest);
        }

    }
}


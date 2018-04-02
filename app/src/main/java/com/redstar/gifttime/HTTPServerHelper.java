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

    /// Client object
    private OkHttpClient client;

    /// Host IP adress
    private static final String SERVER_IP = "80.93.182.129:3000";

    /// Requests protocol
    private static final String PROTOCOL = "http";

    /// Requests content type
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Constructor with email and password. Modifies all of requests with cookies, auth data and
     * another params.
     * @param email user's email
     * @param password user's password
     */
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
    }

    /**
     * Builds a full request URL
     * @param method Request method
     * @return full request URL
     */
    private String buildURL(String method) {
        return PROTOCOL + "://" + SERVER_IP + method;
    }

    /**
     * Does POST query to server.
     *
     * @param sURL Request URL
     * @param query Request body
     * @return {@link HTTPAnswer HTTPAnswer} object with response data from server
     */
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

    /**
     * Does POST query to server.
     *
     * @param sURL Request URL
     * @param query Request body
     * @return {@link HTTPAnswer HTTPAnswer} object with response data from server
     */
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

    /**
     * Does GET query to server.
     *
     * @param sURL Request URL
     * @return {@link HTTPAnswer HTTPAnswer} object with response data from server
     */
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

    /**
     * Does DELETE query to server.
     *
     * @param sURL Request URL
     * @return {@link HTTPAnswer HTTPAnswer} object with response data from server
     */
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

    /**
     * Answer from the server. Contains string with answer body and responce code,
     */
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

    /**
     * Auth Interceptor for modifying requests with user email and password.
     * Sets the authorization header to each request.
     */
    public class BasicAuthInterceptor implements Interceptor {

        private String credentials;

        public BasicAuthInterceptor(String user, String password) {
            this.credentials = Credentials.basic(user, password);
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Request authenticatedRequest = request.newBuilder()
                    .addHeader("Authorization", credentials).build();
            return chain.proceed(authenticatedRequest);
        }

    }
}


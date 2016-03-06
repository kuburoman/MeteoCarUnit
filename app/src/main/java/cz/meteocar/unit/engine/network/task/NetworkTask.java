package cz.meteocar.unit.engine.network.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.dto.ErrorResponse;

/**
 * Created by Nell on 6.3.2016.
 */
public abstract class NetworkTask<IN, OUT> extends AsyncTask<IN, String, OUT> {
    private Gson gson = new Gson();

    protected Context context;
    private String baseURL;
    private String unitName;
    private String secretKey;

    protected ErrorResponse errorResponse;

    abstract protected String getDataURL();

    abstract protected Class<IN> getClassIN();

    abstract protected Class<OUT> getClassOUT();

    public NetworkTask(Context context, String baseURL, String unitName, String secretKey) {
        this.context = context;
        this.baseURL = baseURL;
        this.unitName = unitName;
        this.secretKey = secretKey;
    }

    protected OUT post(IN request) {
        HttpPost post = new HttpPost(baseURL + "/boardUnit/" + unitName + "/" + getDataURL());
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Secret-Key", secretKey);
        if (request != null) {
            try {
                post.setEntity(new StringEntity(gson.toJson(request, getClassIN()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(AppLog.LOG_TAG_NETWORK, e.getMessage());
            }
        }
        return getData(post);
    }

    protected OUT get(IN request) throws UnsupportedEncodingException {
        HttpGet post = new HttpGet(baseURL + "/" + unitName + "/" + getDataURL());
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Secret-Key", secretKey);
        return getData(post);
    }

    public OUT getData(HttpRequestBase requestBase) {

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        try {
            response = client.execute(requestBase);

            String body = EntityUtils.toString(response.getEntity());

            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    return gson.fromJson(body, getClassOUT());
                case HttpStatus.SC_BAD_REQUEST:
                case HttpStatus.SC_FORBIDDEN:
                    errorResponse = gson.fromJson(body, ErrorResponse.class);
                    return null;
                default:
                    errorResponse = new ErrorResponse("ERROR", body);
                    return null;
            }
        } catch (Exception e) {
            Log.e("Network", e.getMessage());
            return null;
        }
    }

}

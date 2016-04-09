package cz.meteocar.unit.engine.network.task;

import android.net.Uri;
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
import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.network.NetworkException;
import cz.meteocar.unit.engine.network.dto.ErrorResponse;
import cz.meteocar.unit.engine.storage.DB;

/**
 * Network connector to rest.
 */
public class NetworkConnector<IN, OUT> {
    private Gson gson = new Gson();

    private String baseURL;
    private String unitName;
    private String secretKey;
    private Class<IN> inClass;
    private Class<OUT> outClass;
    private String dataUrl;

    public NetworkConnector(Class<IN> inClass, Class<OUT> outClass, String dataUrl) {
        this.inClass = inClass;
        this.outClass = outClass;
        this.dataUrl = dataUrl;
    }

    public OUT post(IN request) throws NetworkException {

        this.baseURL = DB.getNetworkAddress();
        this.unitName = DB.getBoardUnitName();
        this.secretKey = DB.getBoardUnitSecretKey();

        HttpPost post = new HttpPost(baseURL + "/boardUnit/" + unitName + "/" + dataUrl);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Secret-Key", secretKey);

        try {
            if (!inClass.equals(Void.class)) {
                if (inClass.equals(String.class)) {
                    post.setEntity(new StringEntity((String) request, "UTF-8"));
                } else {
                    post.setEntity(new StringEntity(gson.toJson(request, inClass), "UTF-8"));
                }
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(AppLog.LOG_TAG_NETWORK, e.getMessage(), e);
        }
        return getData(post);
    }

    public OUT get() throws NetworkException {
        return get(new ArrayList<QueryParameter>());
    }

    public OUT get(List<QueryParameter> params) throws NetworkException {

        this.baseURL = DB.getNetworkAddress();
        this.unitName = DB.getBoardUnitName();
        this.secretKey = DB.getBoardUnitSecretKey();

        Uri.Builder b = Uri.parse(baseURL + "/boardUnit/" + unitName + "/" + dataUrl).buildUpon();

        for (QueryParameter param : params) {
            b.appendQueryParameter(param.getKey(), param.getValue());
        }

        HttpGet post = new HttpGet(b.build().toString());

        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Secret-Key", secretKey);

        return getData(post);
    }

    protected OUT getData(HttpRequestBase requestBase) throws NetworkException {

        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(requestBase);


            String body = EntityUtils.toString(response.getEntity());

            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    if (!outClass.equals(Void.class)) {
                        return gson.fromJson(body, outClass);
                    }
                    return null;
                case HttpStatus.SC_BAD_REQUEST:
                case HttpStatus.SC_FORBIDDEN:
                    throw new NetworkException(gson.fromJson(body, ErrorResponse.class));
                default:
                    throw new NetworkException(new ErrorResponse("ERROR", "Response code: " + response.getStatusLine().getStatusCode()));
            }

        } catch (IOException | IllegalArgumentException e) {
            Log.e(AppLog.LOG_TAG_NETWORK, e.getMessage(), e);
            throw new NetworkException(new ErrorResponse("ERROR", e.getMessage()));
        }
    }
}

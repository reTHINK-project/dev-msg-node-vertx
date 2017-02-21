package eu.rethink.mn.util;

import com.google.gson.Gson;
import io.vertx.core.Vertx;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private SSLContext sslContext;
    private SSLConnectionSocketFactory sslsf;
    private CloseableHttpClient httpClient;
    private final Gson GSON;

    public HttpRequest(String trustStore, String trustPass, String keyStore, String keyPass, String keyPassphrase) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException {

        System.out.println(trustPass);
        System.out.println(keyPass);
        System.out.println(keyPassphrase);

        GSON = new Gson();

        sslContext = SSLContexts.custom()
                .loadTrustMaterial(new File(trustStore), trustPass.toCharArray())
                .loadKeyMaterial(new File(keyStore), keyPass.toCharArray(), keyPassphrase.toCharArray())
                .build();

        sslsf = new SSLConnectionSocketFactory(
                sslContext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
    }

    public HttpRequest() {

        GSON = new Gson();

        httpClient = HttpClients.createDefault();
    }

    public String get(String url) throws IOException {

        HttpGet httpGet = new HttpGet(url);

        CloseableHttpResponse response = httpClient.execute(httpGet);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            String body = IOUtils.toString(entity.getContent());

            return buildResponse(statusCode, body);

        } finally {
            response.close();
        }

    }

    public String put(String url, String data) throws IOException {

        HttpPut httpPut = new HttpPut(url);

        StringEntity params = new StringEntity(data, "UTF-8");
        params.setContentType("application/json");

        httpPut.addHeader("content-type", "application/json");
        httpPut.setEntity(params);


        CloseableHttpResponse response = httpClient.execute(httpPut);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            String body = IOUtils.toString(entity.getContent());

            String json = buildResponse(statusCode, body);

            return json;

        } finally {
            response.close();
        }

    }

    public String del(String url) throws IOException {

        HttpDelete httpDel = new HttpDelete(url);

        CloseableHttpResponse response = httpClient.execute(httpDel);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            String body = IOUtils.toString(entity.getContent());

            return buildResponse(statusCode, body);

        } finally {
            response.close();
        }

    }

    private String buildResponse(int code, String data) {
        Map<String, Object> results = new HashMap<>();
        results.put("code", code);
        results.put("data", data);

        return GSON.toJson(results);
    }

}

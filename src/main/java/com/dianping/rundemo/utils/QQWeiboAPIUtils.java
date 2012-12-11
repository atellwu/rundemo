package com.dianping.rundemo.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;

public class QQWeiboAPIUtils {

    private static final String APP_KEY = "801285448";
    private static final String SECRET_KEY = "cf7c25e5d0a258ad0a10112192383903";

    public static Map<String, String> getAccessToken(String code) throws ClientProtocolException, IOException {
        String url = "https://open.t.qq.com/cgi-bin/oauth2/access_token";
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
        nvps.add(new BasicNameValuePair("client_id", APP_KEY));
        nvps.add(new BasicNameValuePair("client_secret", SECRET_KEY));
        nvps.add(new BasicNameValuePair("redirect_uri", "http://dianping3216.com:8080/rundemo/logined"));
        nvps.add(new BasicNameValuePair("code", code));
        String resultStr = HttpClientUtil.post(url, nvps);
        System.out.println(resultStr);
        //access_token=534d2297463e4de0f283947dd6295a7b&expires_in=604800&refresh_token=eb9a80978771dade4b1269fac86474e7&openid=dcbd7d599307b9418dc62d75c18cee53&name=atell_wu&nick=Atell&state=
        String[] pairs = StringUtils.split(resultStr, '&');
        Map<String, String> map = new HashMap<String, String>();
        for (String pair : pairs) {
            String[] keyvalue = StringUtils.split(pair, '=');
            if (keyvalue.length > 1) {
                String key = keyvalue[0];
                String value = keyvalue[1];
                map.put(key, value);
            }
        }
        return map;
    }
}

package com.caoutch.transnet;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
public class GsonRequest<T> extends Request<T> {

    GsonBuilder gsonBuilder=new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private  Gson gson;
    private final Class<T> clazz;
    private final Map<String, String> params;
    private final Response.Listener<T> listener;


    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     * @param params Map of request post or get parameters
     */
    public GsonRequest(int method,String url, Class<T> clazz, Map<String, String> params,
                       Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        gson=gsonBuilder.create();
        String version = "A"+BuildConfig.VERSION_CODE;
        String lang= Locale.getDefault().getLanguage();
        params.put("ver", version);
        params.put("lang", lang);

        SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.ENGLISH);
        String time=dateFormatter.format(new Date());

        params.put("time", time);
        /*String keyString =time+version+lang;
        if(params.containsKey("lat")){
            keyString=keyString+params.get("lat");
        }
        if(params.containsKey("lng")){
            keyString=keyString+params.get("lng");
        }
        if(params.containsKey("type")){
            keyString=keyString+params.get("type");
        }
        if(params.containsKey("userId")){
            keyString=keyString+params.get("userId");
        }
        if(params.containsKey("lastMessageId")){
            keyString=keyString+params.get("lastMessageId");
        }
        if(params.containsKey("user_id")){
            keyString=keyString+params.get("user_id");
        }
        if(params.containsKey("_id")){
            keyString=keyString+params.get("_id");
        }
        if(params.containsKey("id")){
            keyString=keyString+params.get("id");
        }
        if(params.containsKey("driverRate")){
            keyString=keyString+params.get("driverRate");
        }
        if(params.containsKey("carRate")){
            keyString=keyString+params.get("carRate");
        }
        if(params.containsKey("clientId")){
            keyString=keyString+params.get("clientId");
        }
        if(params.containsKey("clientRate")){
            keyString=keyString+params.get("clientRate");
        }
        if(params.containsKey("driverId")){
            keyString=keyString+params.get("driverId");
        }
        if(params.containsKey("firstName")){
            keyString=keyString+params.get("firstName");
        }
        if(params.containsKey("password")){
            keyString=keyString+params.get("password");
        }
        if(params.containsKey("status")){
            keyString=keyString+params.get("status");
        }
        if(params.containsKey("mobile")){
            keyString=keyString+params.get("mobile");
        }
        if(params.containsKey("email")){
            keyString=keyString+params.get("email");
        }
        if(params.containsKey("lastName")){
            keyString=keyString+params.get("lastName");
        }
        if(params.containsKey("userName")){
            keyString=keyString+params.get("userName");
        }
        if(params.containsKey("carColor")){
            keyString=keyString+params.get("carColor");
        }
        if(params.containsKey("carLicenseNumber")){
            keyString=keyString+params.get("carLicenseNumber");
        }
        if(params.containsKey("carMadeYear")){
            keyString=keyString+params.get("carMadeYear");
        }
        if(params.containsKey("carManufacturer")){
            keyString=keyString+params.get("carManufacturer");
        }
        if(params.containsKey("carModel")){
            keyString=keyString+params.get("carModel");
        }
        if(params.containsKey("carNumber")){
            keyString=keyString+params.get("carNumber");
        }
        if(params.containsKey("carType")){
            keyString=keyString+params.get("carType");
        }
        if(params.containsKey("driverLicenseNumber")){
            keyString=keyString+params.get("driverLicenseNumber");
        }
        if(params.containsKey("idNumber")){
            keyString=keyString+params.get("idNumber");
        }

        params.put("key", Constants.getKey(keyString));*/

        this.params = params;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getParams() throws AuthFailureError {
        return params != null ? params : super.getParams();
    }



    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}

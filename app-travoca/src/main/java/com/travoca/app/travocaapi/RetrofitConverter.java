package com.travoca.app.travocaapi;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit.GsonConverterFactory;

/**
 * @author ortal
 * @date 2015-11-03
 */
public class RetrofitConverter {

    public static Object getBodyAs(ResponseBody body, Type type) throws IOException {

        return GsonConverterFactory.create().fromResponseBody(type, null).convert(body);

    }
}

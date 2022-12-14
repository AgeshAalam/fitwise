package com.fitwise.utils;



import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The builder class that obtains an instance of Retrofit library in order to make REST calls
 * to other servers.
 *
 * @author Ameex_BOT
 *
 */
@Component
public class APIBuilder {
	
	private APIBuilder() {
		
	}

    /**
     * The builder method the returns the main retrofit object.
     * @param baseUrl The base url for api.
     *
     * @return The retrofit global instance.
     */
    public static APIService builder(final String apiUrl) {
        return new Retrofit.Builder()
                .baseUrl(apiUrl)
                .client(UnSafeClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(APIService.class);

    }

    /**
     * An instance of OKHttp client with a default timeout and logging interceptor.
     *
     * @return The OkHttpClient global instance.
     */
    private static OkHttpClient getClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient()
                .newBuilder()
                .addInterceptor(logging)
                .connectTimeout(5, TimeUnit.MINUTES).build();
    }

}
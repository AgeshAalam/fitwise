package com.fitwise.interceptors;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class VimeoServiceInterceptors implements ClientHttpRequestInterceptor {

    String vimeoToken = "";

    public VimeoServiceInterceptors(String vimeoToken) {
        this.vimeoToken = vimeoToken;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set("Authorization", "bearer " + this.vimeoToken);
        return execution.execute(request, body);
    }

}
package com.fitwise.configuration;

import com.fitwise.exception.ApplicationException;
import com.fitwise.view.ResponseModel;
import com.google.gson.Gson;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class RestTemplateExceptionHandlerConfiguration extends DefaultResponseErrorHandler {

    @Override
    public void handleError(final ClientHttpResponse clientResponse) {
        throw new ApplicationException(getMessage(clientResponse).getMessage());
    }

    private ResponseModel getMessage(final ClientHttpResponse clientResponse) {
        ResponseModel response = new ResponseModel();
        try {
            String line;
            BufferedReader rd = new BufferedReader(new InputStreamReader(clientResponse.getBody()));
            while ((line = rd.readLine()) != null) {
                response = new Gson().fromJson(line, ResponseModel.class);
            }
        } catch (IOException ex) {
            throw new ApplicationException("Unable to read buffered data", ex);
        }
        return response;
    }
}
package com.fitwise.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;

/**
 * The Class WebServiceUtils.
 */
public class WebServiceUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceUtils.class);

	/** The server url with endpoint */
	private String url;

	/** The query params for url */
	private Map<String, String> queryParams;

	/** The url/path params for url */
	private Map<String, String> urlParams;

	/** The request headers */
	private HttpHeaders httpHeaders;

	/**
	 * no args constructor
	 */
	public WebServiceUtils() {
		super();
	}

	/**
	 * Constructor with server url
	 * 
	 * @param url
	 *            The server url with endpoint
	 */
	public WebServiceUtils(final String url) {
		this.url = url;
	}

	/**
	 * 
	 * @param url
	 *            the url with endpoint
	 * @param queryParams
	 *            The query params
	 * @param urlParams
	 *            the url params
	 * @param headers
	 *            the request headers
	 */
	public WebServiceUtils(final String url, final Map<String, String> queryParams, final Map<String, String> urlParams,
			final Map<String, String> headers) {
		super();
		this.url = url;
		this.queryParams = queryParams;
		this.urlParams = urlParams;
		if (headers != null) {
			this.httpHeaders = addHeaders(headers);			
		}

	}

	/**
	 * HTTP POST method
	 * 
	 * @param <T>
	 *            The request object type
	 * @param <V>
	 *            The response object type
	 * @param payload
	 *            the request details
	 * @return the V type response
	 */
	public <T, V> V post(final T payload, final Class<V> responseType, HttpHeaders headers) {
		
		
		RestTemplate template = getRestTemplate();
		return template.postForObject(buildUrl(), new HttpEntity<T>(payload, headers), responseType);
	}

	/**
	 * HTTP POST method
	 * 
	 * @param <T>
	 *            The request object type
	 * @param <V>
	 *            The response object type
	 * @param payload
	 *            the request details
	 * @return the V type response
	 */
	public Object postWithHeaderEncodedObject(final MultiValueMap<String, String> payload, HttpHeaders httpHeaders){

		RestTemplate restTemplate = new RestTemplate();

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(payload, httpHeaders);

		ResponseEntity<Object> response =
				restTemplate.exchange(buildUrl(),
                          HttpMethod.POST,
                          entity,
                          Object.class);
		
		System.out.println(new Gson().toJson(response.getBody()));
		
		return response.getBody();
		
	}
	
	/**
	 * HTTP POST method
	 * 
	 * @param <T>
	 *            The request object type
	 * @param <V>
	 *            The response object type
	 * @param payload
	 *            the request details
	 * @return the V type response
	 */
	public <T, V> V postWithHeader(final T payload, final HttpHeaders httpHeaders, final Class<V> responseType) {
		
		RestTemplate template = getRestTemplate();
		return template.postForObject(buildUrl(), new HttpEntity<T>(payload, httpHeaders), responseType);
	}


	/**
	 * HTTP PUT request with path and query params
	 * 
	 * @param responseType
	 *            The API response type
	 * @return The API response
	 */
	public Object put(final Object payload, final Object responseType) {

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		RestTemplate template = getRestTemplate();

		HttpEntity<Object> entityReq = new HttpEntity<Object>(payload, httpHeaders);

		LOGGER.info("payload --> " + new Gson().toJson(payload));
		return template.exchange(buildUrl(), HttpMethod.PUT, entityReq, Object.class).getBody();
	}

	/**
	 * HTTP PUT request with path and query params
	 * 
	 * @param responseType
	 *            The API response type
	 * @return The API response
	 */
	public Object putWithHeaders(final Object payload, final Object responseType, HttpHeaders httpHeaders) {

		RestTemplate template = getRestTemplate();
		
		HttpEntity<Object> entityReq = new HttpEntity<Object>(payload, httpHeaders);

		LOGGER.info("payload --> " + new Gson().toJson(payload));
		return template.exchange(buildUrl(), HttpMethod.PUT, entityReq, Object.class).getBody();
	}

	/**
	 * HTTP DELETE request with path and query params
	 * 
	 * @param responseType
	 *            The API response type
	 * @return The API response
	 */
	public Object delete() {

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		RestTemplate template = getRestTemplate();
		HttpEntity<Object> entityReq = new HttpEntity<Object>(null, httpHeaders);

		return template.exchange(buildUrl(), HttpMethod.DELETE, entityReq, Object.class).getBody();

	}

	/**
	 * HTTP DELETE request with path and query params
	 * 
	 * @param responseType
	 *            The API response type
	 * @return The API response
	 */
	public Object deleteWithHeader(HttpHeaders httpHeaders) {

		RestTemplate template = getRestTemplate();
		HttpEntity<Object> entityReq = new HttpEntity<Object>(null, httpHeaders);

		return template.exchange(buildUrl(), HttpMethod.DELETE, entityReq, Object.class).getBody();

	}


	/**
	 * HTTP GET request with path and query params
	 * 
	 * @param responseType
	 *            The API response type
	 * @return The API response
	 */
	public <V> V get(final Class<V> responseType) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		RestTemplate template = getRestTemplate();

		return template.exchange(buildUrl(), HttpMethod.GET, new HttpEntity<Object>(httpHeaders), responseType)
				.getBody();
	}

	/**
	 * HTTP GET request with path and query params
	 * 
	 * @param responseType
	 *            The API response type
	 * @return The API response
	 */
	public <V> V getWithHeader(final Class<V> responseType, HttpHeaders httpHeaders) {

		LOGGER.info("Get with headers ... ");
		RestTemplate template = getRestTemplate();

		return template.exchange(buildUrl(), HttpMethod.GET, new HttpEntity<Object>(httpHeaders), responseType)
				.getBody();
	}

	/**
	 * Build server url with params
	 * 
	 * @return The final url
	 */
	private String buildUrl() {

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
		if (queryParams == null || queryParams.isEmpty()) {
			url = builder.toUriString();
		} else {
			for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				builder.queryParam(entry.getKey(), entry.getValue());
			}
		}
		if (urlParams == null || urlParams.isEmpty()) {
			url = builder.toUriString();
		} else {
			url = builder.buildAndExpand(urlParams).toUriString();
		}

		return url;
	}

	private HttpHeaders addHeaders(final Map<String, String> headers) {

		HttpHeaders httpHeaders = new HttpHeaders();
		for (Map.Entry<String, String> header : headers.entrySet()) {
			httpHeaders.add(header.getKey(), header.getValue());
		}
		return httpHeaders;
	}

	public RestTemplate getRestTemplate() {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
		messageConverters.add(converter);

		RestTemplate template = new RestTemplate();
		template.setMessageConverters(messageConverters);

		return template;
	}
}
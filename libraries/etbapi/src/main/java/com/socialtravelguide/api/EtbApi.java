package com.socialtravelguide.api;

import android.support.v4.util.ArrayMap;

import com.socialtravelguide.api.mock.ResultsMockClient;
import com.socialtravelguide.api.model.CancelRequest;
import com.socialtravelguide.api.model.CancelResponse;
import com.socialtravelguide.api.model.DetailsResponse;
import com.socialtravelguide.api.model.HotelRequest;
import com.socialtravelguide.api.model.OrderRequest;
import com.socialtravelguide.api.model.OrderResponse;
import com.socialtravelguide.api.model.ResultsResponse;
import com.socialtravelguide.api.model.SearchRequest;
import com.socialtravelguide.api.model.search.ListType;
import com.socialtravelguide.api.model.search.Type;
import com.socialtravelguide.api.utils.RequestUtils;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.QueryMap;

/**
 * @author alex
 * @date 2015-04-19
 */
public class EtbApi {
//    public static final String PATH_ACCOMMODATIONS = "/v1/accommodations";
//    public static final String PATH_SEARCH = PATH_ACCOMMODATIONS + "/results";
//    public static final String PATH_ORDERS = "/v1/orders";
public static final String PATH_ACCOMMODATIONS ="/etbstatic/checkRoomAvailability.json" ;
    public static final String PATH_SEARCH = "/etbstatic/searchingAccommodations.json" ;
    public static final String PATH_ORDERS = "/etbstatic/placeAnOrder.json";
    public static final int LIMIT = 15;
    private OkHttpClient mHttpClient;

    private EtbApiConfig mConfig;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private Interceptor mRequestInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            HttpUrl url = request.httpUrl().newBuilder()
                    .addQueryParameter("apiKey", mConfig.getApiKey())
                    .addQueryParameter("campaignId", String.valueOf(mConfig.getCampaignId()))
                    .build();


            return chain.proceed(request.newBuilder().url(url).build());
        }
    };

    public EtbApi(String apiKey, int campaignId) {
        this(new EtbApiConfig(apiKey, campaignId), null);
    }

    public EtbApi(EtbApiConfig config) {
        this(config, null);
    }

    public EtbApi(EtbApiConfig config, OkHttpClient httpClient) {
        mConfig = config;
        mHttpClient = httpClient == null ? new OkHttpClient() : httpClient;
        mHttpClient.interceptors().add(0, mRequestInterceptor);
        mHttpClient.interceptors().add(new ResultsMockClient());
    }

    public EtbApiConfig getConfig() {
        return mConfig;
    }

    private Service create(boolean isSecure) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .client(mHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(isSecure ? mConfig.getSecureEndpoint() : mConfig.getEndpoint());

        Retrofit restAdapter = builder.build();

        return restAdapter.create(Service.class);
    }

    public Call<ResultsResponse> results(SearchRequest searchRequest) throws InvalidParameterException {
        return results(searchRequest, 0);
    }

    public Call<ResultsResponse> results(SearchRequest searchRequest, int offset) throws InvalidParameterException {

        Service service = create(false);

        ArrayMap<String, String> query = new ArrayMap<>();

        // Location
        Type loc = searchRequest.getType();
        if (loc == null) {
            throw new InvalidParameterException();
        }
        query.put("type", loc.getType());
        query.put("context", loc.getContext());

        query.put("currency", searchRequest.getCurrency());
        query.put("language", searchRequest.getLanguage());

        // Availability
        if (searchRequest.getNumberOfPersons() != 0 && searchRequest.getNumberOfRooms() != 0) {
            query.put("capacity", RequestUtils.capacity(searchRequest.getNumberOfPersons(), searchRequest.getNumberOfRooms()));
        }

        // Limit
        if (searchRequest.getType() instanceof ListType) {
            query.put("limit", String.valueOf(999));
            query.put("offset", String.valueOf(0));
        } else {
            query.put("limit", String.valueOf(LIMIT));
            query.put("offset", String.valueOf(offset));
        }

        // Sort
        SearchRequest.Sort sort = searchRequest.getSort();
        if (sort != null) {
            String[] sortStr = sort.type.split("_");
            query.put("orderBy", sortStr[0]);
            if (sortStr.length > 1) {
                query.put("order", sortStr[1]);
            }
        }

        query.put("metaFields", "all"); // full = This also includes the field filterNrs

        query.put("customerCountryCode", searchRequest.getCustomerCountryCode());

        return service.results(query);
    }

    public Call<DetailsResponse> details(int id, HotelRequest hotelRequest) {
        Service service = create(false);
        ArrayMap<String, String> query = new ArrayMap<>();
        query.put("capacity", RequestUtils.capacity(hotelRequest.getNumberOfPersons(), hotelRequest.getNumberOfRooms()));

        query.put("currency", hotelRequest.getCurrency());
        query.put("language", hotelRequest.getLanguage());

        query.put("customerCountryCode", hotelRequest.getCustomerCountryCode());

        return service.details( query);
//        return service.details(id, query);
    }

    public Call<OrderResponse> order(OrderRequest request) {
        Service service = create(true);
        return service.order(request);
    }

    public Call<CancelResponse> cancel(CancelRequest request, String orderId, String rateId) {
        Service service = create(true);
//        return service.cancel(request, orderId, rateId);
        return service.cancel(request);
    }

    public Call<OrderResponse> retrieve(String orderId, String password) {
        Service service = create(true);
        Map<String, String> query = new HashMap<>();
        if (password != null) {
            query.put("password", password);
        }
//        return service.retrieve(orderId, query);
        return service.retrieve( query);
    }


    public interface Service {

        @GET(PATH_SEARCH)
        Call<ResultsResponse> results(@QueryMap Map<String, String> query);

//        @GET(PATH_ACCOMMODATIONS + "/{id}")
//Call<DetailsResponse> details(@Path("id") int id, @QueryMap Map<String, String> query);

        @GET(PATH_ACCOMMODATIONS )
        Call<DetailsResponse> details( @QueryMap Map<String, String> query);

        @POST(PATH_ORDERS )
        Call<OrderResponse> order(@Body OrderRequest request);

//        @POST(PATH_ORDERS + "/{orderId}/rates/{rateId}/cancel")
//Call<CancelResponse> cancel(@Body CancelRequest request, @Path("orderId") String orderId, @Path("rateId") String rateId);
        @POST(PATH_ORDERS )
        Call<CancelResponse> cancel(@Body CancelRequest request);

        //        @GET(PATH_ORDERS + "/{orderId}")
//        Call<OrderResponse> retrieve(@Path("orderId") String orderId, @QueryMap Map<String, String> query);
        @GET(PATH_ORDERS )
        Call<OrderResponse> retrieve( @QueryMap Map<String, String> query);
    }

}

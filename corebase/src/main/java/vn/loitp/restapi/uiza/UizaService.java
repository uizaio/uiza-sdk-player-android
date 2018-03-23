package vn.loitp.restapi.uiza;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import vn.loitp.restapi.uiza.model.tracking.UizaTracking;
import vn.loitp.restapi.uiza.model.v1.getlinkplay.GetLinkPlay;
import vn.loitp.restapi.uiza.model.v1.listallmetadata.ListAllMetadata;
import vn.loitp.restapi.uiza.model.v2.auth.Auth;
import vn.loitp.restapi.uiza.model.v2.getdetailentity.GetDetailEntity;
import vn.loitp.restapi.uiza.model.v2.getplayerinfo.PlayerConfig;
import vn.loitp.restapi.uiza.model.v2.listallentity.JsonBody;
import vn.loitp.restapi.uiza.model.v2.listallentity.ListAllEntity;
import vn.loitp.restapi.uiza.model.v2.listallentityrelation.ListAllEntityRelation;
import vn.loitp.restapi.uiza.model.v2.search.Search;

/**
 * @author loitp
 */

public interface UizaService {
    //=====================================================Sample
    /*@GET("v1/app/poster")
    Observable<GetPoster[]> getPoster(@Query("number") int number);

    @GET("api/public/v1/media/entity/get-link-play")
    Observable<GetLinkPlay> getLinkPlayV1(@Query("entityId") String entityId);

    @FormUrlEncoded
    @PUT("v1/room/follow")
    Observable<Object> followIdol(@Field("roomId") String roomId);

    @FormUrlEncoded
    @POST("/public/v1/auth/login")
    Observable<Object> login(@Field("username") String username, @Field("password") String password);

    @Headers("Content-Type: application/vnd.api+json")
    @GET("/api/data/v1/metadata/list")
    Observable<Object> getListAllMetadata();

    @GET("/api/data/v1/entity/list")
    Observable<GetAll> getAll(@Query("limit") int limit, @Query("page") int page);

    @GET("/api/data/v1/entity/list")
    Observable<Object> testGetAll();*/
    //=====================================================End Sample


    //=====================================================v2 dev-api.uiza.io/resource/index.html
    @FormUrlEncoded
    @POST("/api/resource/v1/auth/credentical")
    Observable<Auth> auth(@Field("accessKeyId") String accessKeyId, @Field("secretKeyId") String secretKeyId);

    //@FormUrlEncoded
    @POST("/api/resource/v1/auth/check-token")
    Observable<Auth> checkToken();

    @FormUrlEncoded
    @POST("/api/resource/v1/media/metadata/list")
    Observable<vn.loitp.restapi.uiza.model.v2.listallmetadata.ListAllMetadata> listAllMetadataV2(@Field("limit") int limit, @Field("orderBy") String orderBy, @Field("orderType") String orderType);

    @Headers("Content-Type: application/json")
    @POST("/api/resource/v1/media/entity/list")
    Observable<ListAllEntity> listAllEntityV2(@Body JsonBody jsonBody);

    @FormUrlEncoded
    @POST("/api/resource/v1/media/entity/detail")
    Observable<GetDetailEntity> getDetailEntityV2(@Field("id") String id);

    @GET("/api/public/v2/media/entity/get-link-play")
    Observable<vn.loitp.restapi.uiza.model.v2.getlinkplay.GetLinkPlay> getLinkPlayV2(@Query("entityId") String entityId, @Query("appId") String appId);

    @FormUrlEncoded
    @POST("/api/resource/v1/media/entity/related")
    Observable<ListAllEntityRelation> getListAllEntityRalationV2(@Field("id") String id);

    @FormUrlEncoded
    @POST("/api/resource/v1/media/search")
    Observable<Search> searchEntityV2(@Field("keyword") String keyword, @Field("limit") int limit, @Field("page") int page);

    //=====================================================end v2 dev-api.uiza.io/resource/index.html


    //=====================================================v1 http://dev-api.uiza.io/data/index.html
    @FormUrlEncoded
    @POST("/api/data/v1/metadata/list")
    Observable<ListAllMetadata> listAllMetadataV1(@Field("limit") int limit, @Field("orderBy") String orderBy, @Field("orderType") String orderType);

    @Headers("Content-Type: application/json")
    @POST("/api/data/v1/entity/list")
    Observable<vn.loitp.restapi.uiza.model.v1.listallentity.ListAllEntity> listAllEntityV1(@Body JsonBody jsonBody);

    @GET("/api/public/v1/media/entity/get-link-play")
    Observable<GetLinkPlay> getLinkPlayV1(@Query("entityId") String entityId, @Query("appId") String appId);

    //getPlayerConfig
    @GET("/api/public/v1/player/info/{id}")
    Observable<PlayerConfig> getPlayerInfo(@Path("id") String playerId);

    @FormUrlEncoded
    @POST("/api/data/v1/entity/detail")
    Observable<vn.loitp.restapi.uiza.model.v1.getdetailentity.GetDetailEntity> getDetailEntityV1(@Field("id") String id);

    @FormUrlEncoded
    @POST("/api/data/v1/entity/related")
    Observable<vn.loitp.restapi.uiza.model.v1.listallentityrelation.ListAllEntityRelation> getListAllEntityRalationV1(@Field("id") String id);

    @FormUrlEncoded
    @POST("/api/data/v1/search")
    Observable<vn.loitp.restapi.uiza.model.v1.search.Search> searchEntityV1(@Field("keyword") String keyword, @Field("limit") int limit, @Field("page") int page);

    //=====================================================end v1 http://dev-api.uiza.io/data/index.html

    //=====================================================tracking
    @Headers("Content-Type: application/json")
    @POST("v1/tracking/mobile")
    Observable<Object> track(@Body UizaTracking uizaTracking);
    //end =====================================================tracking
}

# Welcome to UizaSDK

# Importing the Library
**Step 1. Add the JitPack repository to your build file**  

    allprojects {  
          repositories {  
             ...  
             maven { url 'https://jitpack.io' }  
          }   }
**Step 2. Add the dependency**  

    defaultConfig {  
      ...  
      multiDexEnabled  true  
    }
    ...
    dependencies {  
      compile 'com.github.tplloi:basemaster:1.0.3'  
      compile 'com.android.support:multidex:1.0.3'  
    }

# How to call API?:
**Step1: You must extend your activity/fragment like this**  

    public class YourActivity extends BaseActivity{
    ...
    }

or

    public class YourFragment extends BaseFragment{
    }

**Step 2: Create interface**  

    public interface APIServices {  
        @GET("/2.2/questions?order=desc&sort=votes&site=stackoverflow&tagged=android&filter=withbody")  
        Observable<Object> test(); 
    }
**Step3: Call api by using this function** 

    private void testAPI() {  
        RestClient.init("https://api.stackexchange.com");  
        //RestClient.init("https://api.stackexchange.com", "token");  
        APIServices service = RestClient.createService(APIServices.class);  
        subscribe(service.test(), new ApiSubscriber<Object>() {  
            @Override  
            public void onSuccess(Object result) {  
                LLog.d(TAG, "testAPI onSuccess " \+ LSApplication.getInstance().getGson().toJson(result));  
            }  
      
            @Override  
            public void onFail(Throwable e) {  
                handleException(e);  
            }  
        });  
    }
**More infomation: Expand interface**

    public interface APIServices {  
        @GET("/2.2/questions?order=desc&sort=votes&site=stackoverflow&tagged=android&filter=withbody")  
        Observable<Object> test();  
      
        @FormUrlEncoded  
	    @POST("/api/public/v1/auth/credentical")  
        Observable<Object> auth(@Field("accessKeyId") String accessKeyId, @Field("secretKeyId") String secretKeyId);  
      
        @POST("/api/data/v1/metadata/list")  
        Observable<Object> getMetadatList();  
      
        @GET("v1/app/poster")  
        Observable<GetPoster\[\]>  
        getPoster(@Query("number") int number);  
      
        @FormUrlEncoded  
        @PUT("v1/room/follow")  
        Observable<FollowIdol> followIdol(@Field("roomId") String roomId);  
      
        @DELETE("v1/room/schedule")  
        Observable<Void> deleteSchedule(@Query("scheduleId") String id);  
      
        @Multipart  
        @POST("v1/room/banner")  
        Observable<Room> updateBanner(@Part MultipartBody.Part file);  
    }
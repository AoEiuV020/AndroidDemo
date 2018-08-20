package cc.aoeiuv020.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaiduMapHelper extends MapHelper {
    private static final String TAG = "BaiduMapHelper";
    @SuppressLint("StaticFieldLeak")
    private static BaiduMapHelper INSTANCE;
    private Context context;
    private LocationClient locationClient;
    private BDAbstractLocationListener locationListener;

    private BaiduMapHelper(Context context) {
        this.context = context;
        locationClient = new LocationClient(context);
    }

    public static BaiduMapHelper getInstance(Context context) {
        // 单例用懒加载，为了传入context,
        if (INSTANCE == null) {
            synchronized (BaiduMapHelper.class) {
                if (INSTANCE == null) {
                    SDKInitializer.initialize(context);
                    INSTANCE = new BaiduMapHelper(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private void requestLocationOnce(final OnSuccessListener<BDLocation> onSuccessListener, final OnErrorListener onErrorListener) {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);           // 设置定位模式
        option.setCoorType("bd09ll");                                  // 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(5000);                                      // 设置发起定位请求的间隔时间为10s
        option.setIsNeedAddress(true);
        option.setNeedDeviceDirect(false);
        locationClient.setLocOption(option);
        locationListener = new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                // 只定位一次就停止，
                locationClient.unRegisterLocationListener(this);
                locationClient.stop();
                int resultCode;
                if (location == null) {
                    if (onErrorListener != null) {
                        onErrorListener.onError(new RuntimeException("百度定位失败: location is null,"));
                    }
                    return;
                }
                resultCode = location.getLocType();
                // 百度定位失败
                if (resultCode != BDLocation.TypeGpsLocation && resultCode != BDLocation.TypeCacheLocation
                        && resultCode != BDLocation.TypeOffLineLocation && resultCode != BDLocation.TypeNetWorkLocation) {
                    Log.d(TAG, "百度定位失败");
                    if (onErrorListener != null) {
                        onErrorListener.onError(new RuntimeException("百度定位失败: " + location.getLocTypeDescription()));
                    }
                    return;
                }

                // 百度定位成功
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(location);
                }
            }

        };
        locationClient.registerLocationListener(locationListener);
        locationClient.start();
    }

    @Override
    public void requestLatLng(final OnSuccessListener<LatLng> onSuccessListener, final OnErrorListener onErrorListener) {
        requestLocationOnce(new OnSuccessListener<BDLocation>() {
            @Override
            public void onSuccess(BDLocation location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                onSuccessListener.onSuccess(latLng);
            }
        }, onErrorListener);
    }

    private void requestPoiList(LatLng latLng,
                                final OnSuccessListener<List<PoiInfo>> onSuccessListener,
                                final OnErrorListener onErrorListener) {
        GeoCoder geoSearch = GeoCoder.newInstance();
        geoSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
            }

            /**
             * 反地理编码，由地点经纬度获取地点名称
             */
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                List<PoiInfo> poiList = result.getPoiList();
                if (poiList == null || poiList.isEmpty()) {
                    if (onErrorListener != null) {
                        onErrorListener.onError(new RuntimeException("百度获取周边位置失败，"));
                    }
                    return;
                }
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(poiList);
                }
            }
        });
        /* 加载定位数据 */
        com.baidu.mapapi.model.LatLng bdLatLng = new com.baidu.mapapi.model.LatLng(latLng.getLatitude(), latLng.getLongitude());
        ReverseGeoCodeOption reverseGeoCodeOption = new ReverseGeoCodeOption();
        reverseGeoCodeOption.location(bdLatLng);
        geoSearch.reverseGeoCode(reverseGeoCodeOption);
    }

    @Override
    public void requestPlaceList(LatLng latLng,
                                 final OnSuccessListener<List<Place>> onSuccessListener,
                                 final OnErrorListener onErrorListener) {
        requestPoiList(latLng, new OnSuccessListener<List<PoiInfo>>() {
            @Override
            public void onSuccess(List<PoiInfo> poiList) {
                List<Place> placeList = new ArrayList<>(poiList.size());
                for (PoiInfo poi : poiList) {
                    // 以防万一避免空指针，
                    String name = "" + poi.name;
                    String address = "" + poi.address;
                    LatLng placeLatLng = new LatLng(poi.location.latitude, poi.location.longitude);
                    Place place = new Place(name, address, placeLatLng);
                    placeList.add(place);
                }
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(placeList);
                }

            }
        }, onErrorListener);
    }

    @Override
    public void requestCityName(final LatLng latLng,
                                final OnSuccessListener<String> onSuccessListener,
                                final OnErrorListener onErrorListener) {
        requestPoiList(latLng, new OnSuccessListener<List<PoiInfo>>() {
            @Override
            public void onSuccess(List<PoiInfo> poiList) {
                String city = null;
                for (PoiInfo poi : poiList) {
                    city = poi.city;
                    if (city != null) {
                        break;
                    }
                }
                if (city == null) {
                    if (onErrorListener != null) {
                        onErrorListener.onError(new RuntimeException(
                                String.format(Locale.CHINA, "地址<%f, %f>找不到城市名，",
                                        latLng.getLatitude(), latLng.getLongitude())));
                    }
                    return;
                }
                onSuccessListener.onSuccess(city);
            }
        }, onErrorListener);
    }

    @Override
    public Picker getPicker(Context context) {
        return new BaiduMapPicker(context);
    }

    private class BaiduMapPicker extends Picker {
        private MapView mapView;
        private Context context;

        private BaiduMapPicker(Context context) {
            this.context = context;
        }

        private void createMapView() {
            if (mapView == null) {
                mapView = new MapView(context);
                mapView.setClickable(true);
                mapView.setFocusable(true);
            }
        }

        @Override
        public void attack(FrameLayout container) {
            Log.d(TAG, "attack: ");
            createMapView();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(mapView, params);
        }

        @Override
        void create() {
            super.create();
            createMapView();
        }

        @Override
        void resume() {
            super.resume();
            mapView.onResume();
        }

        @Override
        void pause() {
            super.pause();
            mapView.onPause();
        }

        @Override
        void destroy() {
            super.destroy();
            mapView.onDestroy();
        }
    }
}

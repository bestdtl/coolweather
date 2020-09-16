package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forcast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titileCity;
    private TextView titleUpdateTime;
    private TextView txtDegree;
    private TextView txtWeatherInfo;
    private LinearLayout layoutForecast;
    private TextView txtApi;
    private TextView txtPM25;
    private TextView txtComfort;
    private TextView txtCarWash;
    private TextView txtSport;

    private ImageView imgBingPic;

    public SwipeRefreshLayout swipeRefreshLayout;
    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        if(Build.VERSION.SDK_INT>=21)
        {
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        }
        initView();
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=preferences.getString("weather",null);
        if (weatherString!=null)
        {
            //有缓存直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }
        else
        {
            //无缓存时去服务器查询天气数据
            mWeatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        String bingPic=preferences.getString("bing_Pic",null);
        if(bingPic!=null)
        {
            Glide.with(this).load(bingPic).into(imgBingPic);
        }
        else
        {
            loadBingPic();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

    }

    private void initView() {
        weatherLayout = (ScrollView) findViewById(R.id.layoutWeather);
        titileCity = (TextView) findViewById(R.id.txtCity);
        titleUpdateTime = (TextView) findViewById(R.id.txtUpdateTime);
        txtDegree = (TextView) findViewById(R.id.txtDegree);
        txtWeatherInfo = (TextView) findViewById(R.id.txtWeatherInfo);
        txtApi = (TextView) findViewById(R.id.txtApi);
        txtPM25 = (TextView) findViewById(R.id.txtPM25);
        txtComfort = (TextView) findViewById(R.id.txtComfort);
        txtCarWash = (TextView) findViewById(R.id.txtCarWash);
        txtSport = (TextView) findViewById(R.id.txtSport);
        layoutForecast = (LinearLayout) findViewById(R.id.layoutForecast);
        imgBingPic=(ImageView)findViewById(R.id.imgBingPic);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
    }

    public void requestWeather(final String weatherId)
    {
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=d7f54bb2753645efb3e3e90361c9289a";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null && "ok".equals(weather.status))
                        {
                            SharedPreferences.Editor editor=PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId=weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }
                        else
                        {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        loadBingPic();
    }

    /**
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather)
    {
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updatTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        titileCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        txtDegree.setText(degree);
        txtWeatherInfo.setText(weatherInfo);
        layoutForecast.removeAllViews();
        for (Forcast forcast:weather.forcastList)
        {
            View view= LayoutInflater.from(this).inflate(R.layout.layout_forecast_item,layoutForecast,false);
            TextView txtDate=(TextView)view.findViewById(R.id.txtDate);
            TextView txtInfo=(TextView)view.findViewById(R.id.txtInfo);
            TextView txtMax=(TextView)view.findViewById(R.id.txtMax);
            TextView txtMin=(TextView)view.findViewById(R.id.txtMin);
            txtDate.setText(forcast.date);
            txtInfo.setText(forcast.more.info);
            txtMax.setText(forcast.temperature.max);
            txtMin.setText(forcast.temperature.min);
            layoutForecast.addView(view);
        }
        if(weather.aqi!=null)
        {
            txtApi.setText(weather.aqi.aqiCity.aqi);
            txtPM25.setText(weather.aqi.aqiCity.pm25);
        }
        String comfort="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String sport="运动建议："+weather.suggestion.sport.info;
        txtComfort.setText(comfort);
        txtCarWash.setText(carWash);
        txtSport.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

    }

    private void loadBingPic()
    {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(imgBingPic);
                    }
                });
            }
        });
    }
}

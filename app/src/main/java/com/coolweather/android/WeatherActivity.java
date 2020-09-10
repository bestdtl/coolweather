package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initView();
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=preferences.getString("weather",null);
        if (weatherString!=null)
        {
            //有缓存直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }
        else
        {
            //无缓存时去服务器查询天气数据
            String weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

    }

    private void initView() {
        weatherLayout = (ScrollView) findViewById(R.id.layoutWeather);
        titileCity = (TextView) findViewById(R.id.txtCity);
        txtDegree = (TextView) findViewById(R.id.txtDegree);
        txtWeatherInfo = (TextView) findViewById(R.id.txtWeatherInfo);
        txtApi = (TextView) findViewById(R.id.txtApi);
        txtPM25 = (TextView) findViewById(R.id.txtPM25);
        txtComfort = (TextView) findViewById(R.id.txtComfort);
        txtCarWash = (TextView) findViewById(R.id.txtCarWash);
        txtSport = (TextView) findViewById(R.id.txtSport);
        layoutForecast = (LinearLayout) findViewById(R.id.layoutForecast);
    }

    public void requestWeather(final String weatherId)
    {
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"d7f54bb2753645efb3e3e90361c9289a";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
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
                            showWeatherInfo(weather);
                        }
                        else
                        {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

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
            //
        }
    }
}

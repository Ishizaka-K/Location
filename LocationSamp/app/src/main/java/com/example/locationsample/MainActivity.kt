package com.example.locationsample

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    // FusedLocationProviderClientのインスタンスを作成
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val TAG = "MainActivity"
    private lateinit var locationAdapter: LocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // RecyclerView と Adapter の設定
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        locationAdapter = LocationAdapter(mutableListOf())
        recyclerView.adapter = locationAdapter

        // FusedLocationProviderClientの取得
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 位置情報の更新を開始するメソッドを呼び出し
        startUpdatingLocation()

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), 0)
        }

        // コルーチンを使って非同期に最後に取得した位置情報を取得する
        lifecycleScope.launch {
            launch {
                while (true) {
                    delay(3000)  // 3秒待機
                    getLastKnownLocation()
                }// 非同期関数を呼び出す
            }
        }
    }

    // 最後に取得した位置情報を非同期で取得するサスペンド関数
    private suspend fun getLastKnownLocation() {
        try {
            // awaitLastLocation()を使って最後の位置情報を取得
            val lastLocation = fusedLocationClient.awaitLastLocation()

            // 取得した位置情報をテキストビューに表示（FORMAT_MINUTESフォーマットで表示）
            updateLocationList(lastLocation.asString(Location.FORMAT_MINUTES))
            Log.d(TAG, lastLocation.toString())  // 取得した位置情報をログに出力
        } catch (e: Exception) {
            // 位置情報取得が失敗した場合、エラーメッセージを表示
            updateLocationList("Unable to get location.")
            Log.d(TAG, "Unable to get location", e)
        }
    }

    // 位置情報の更新をFlowで受け取るためのメソッド
    private fun startUpdatingLocation() {
        fusedLocationClient.locationFlow()  // 位置情報をFlowで受け取る
            .conflate()  // Flowの値を処理する際に、最新の値だけを処理するように最適化
            .catch { e ->  // エラー処理
                // 位置情報取得が失敗した場合、エラーメッセージを表示
                updateLocationList("Unable to get location.")
                Log.d(TAG, "Unable to get location", e)
            }
            .asLiveData()  // FlowをLiveDataに変換する
            .observe(this, Observer { location ->  // LiveDataを監視してUIを更新
                // 位置情報が取得できた場合、その情報をテキストビューに表示
                updateLocationList(location.asString(Location.FORMAT_MINUTES))
                Log.d("startUpdatingLocation", location.toString())  // 取得した位置情報をログに出力
            })
    }

    // 位置情報をリストに追加
    private fun updateLocationList(location: String) {
        locationAdapter.addLocation(location) // リストに位置情報を追加
    }


    // 権限リクエストの結果を受け取るメソッド
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // ユーザーが権限を許可した場合、アクティビティを再作成して位置情報取得を再開
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate()  // 再作成して、権限が付与された後の処理を再度行う
        }
    }
}


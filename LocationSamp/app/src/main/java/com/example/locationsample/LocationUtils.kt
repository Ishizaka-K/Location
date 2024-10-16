package com.example.locationsample

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume

// LocationRequestの生成。更新間隔と精度の設定を行う
fun createLocationRequest() = LocationRequest.Builder(3000) // 更新間隔を3秒に設定
    //setinterval
    //setIntervalMillis = 3000,
    //setFastestInterval
    .setMinUpdateIntervalMillis(2000)  // 最短の更新間隔を2秒に設定
    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)  // 高精度の位置情報を要求
    .setWaitForAccurateLocation(false)  // 正確な位置情報を待つ
    .build()

// LocationCallbackの実装。位置情報の更新があった場合に呼び出される
fun locationCallback() = object : LocationCallback() {
    override fun onLocationResult(p0: LocationResult) {
        p0 ?: return // 位置情報がnullでない場合に処理を続ける
        for (location in p0.locations) {
            // 取得した位置情報をログに出力
            Log.d("Location Update", "Lat: ${location.latitude}, Lng: ${location.longitude}")
        }
    }
}

// Locationクラスに拡張関数を追加して、位置情報を文字列形式に変換するメソッド
fun Location.asString(format: Int = Location.FORMAT_DEGREES): String {
    val latitude = Location.convert(latitude, format)  // 緯度を指定フォーマットで文字列化
    val longitude = Location.convert(longitude, format)  // 経度を指定フォーマットで文字列化
    return "Location is: $latitude, $longitude"  // 位置情報を文字列で返す
}

// 最後に取得した位置情報を非同期に取得するサスペンド関数
@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.awaitLastLocation(): Location =
    suspendCancellableCoroutine<Location> { continuation ->
        // lastLocationを非同期に取得し、成功時に結果をcontinuationに渡す
        lastLocation.addOnSuccessListener { location ->
            continuation.resume(location)
        }.addOnFailureListener { e ->
            // 失敗した場合、例外をcontinuationに渡す
            continuation.resumeWithException(e)
        }
    }

// コルーチンフローを使って位置情報をリアルタイムに取得する関数
@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.locationFlow() = callbackFlow<Location> {
    // 位置情報の更新をリクエストし、コールバックで位置情報をFlowに流す
    requestLocationUpdates(
        createLocationRequest(),  // 先ほど作成したLocationRequestを使用
        locationCallback(),  // 位置情報が更新されるたびに呼び出されるコールバック
        Looper.getMainLooper()  // メインスレッドのLooperを指定
    ).addOnFailureListener { e ->
        // 位置情報のリクエストが失敗した場合に、例外をキャッチしてFlowを閉じる
        close(e) // 例外発生時にFlowを終了
    }

    awaitClose {
        // Flowのコレクションが終了したときに位置情報の更新を停止
        removeLocationUpdates(locationCallback()) // リソースを解放
    }
}
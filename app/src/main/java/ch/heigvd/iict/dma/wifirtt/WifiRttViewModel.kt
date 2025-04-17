package ch.heigvd.iict.dma.wifirtt

import android.net.wifi.rtt.RangingResult
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import ch.heigvd.iict.dma.wifirtt.config.MapConfig
import ch.heigvd.iict.dma.wifirtt.config.MapConfigs
import ch.heigvd.iict.dma.wifirtt.models.RangedAccessPoint
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver
import com.lemmingapex.trilateration.TrilaterationFunction
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer

class WifiRttViewModel : ViewModel() {

    // PERMISSIONS MANAGEMENT
    private val _wifiRttPermissionsGranted = MutableLiveData<Boolean>(null)
    val wifiRttPermissionsGranted : LiveData<Boolean> get() = _wifiRttPermissionsGranted

    fun wifiRttPermissionsGrantedUpdate(granted : Boolean) {
        _wifiRttPermissionsGranted.postValue(granted)
    }

    // WIFI RTT AVAILABILITY MANAGEMENT
    private val _wifiRttEnabled = MutableLiveData<Boolean>(null)
    val wifiRttEnabled : LiveData<Boolean> get() = _wifiRttEnabled

    fun wifiRttEnabledUpdate(enabled : Boolean) {
        _wifiRttEnabled.postValue(enabled)
    }

    // WIFI RTT MEASURES MANAGEMENT
    private val _rangedAccessPoints = MutableLiveData(emptyList<RangedAccessPoint>())
    val rangedAccessPoints : LiveData<List<RangedAccessPoint>> = _rangedAccessPoints.map { l -> l.toList().map { el -> el.copy() } }

    // CONFIGURATION MANAGEMENT
    private val _mapConfig = MutableLiveData(MapConfigs.b30)
    val mapConfig : LiveData<MapConfig> get() = _mapConfig

    fun onNewRangingResults(newResults : List<RangingResult>) {
        val updatedList = _rangedAccessPoints.value?.toMutableList() ?: mutableListOf()

        for (result in newResults) {
            if (result.status != RangingResult.STATUS_SUCCESS) continue

            val existing = updatedList.find { it.bssid == result.macAddress.toString() }
            if (existing != null) {
                existing.update(result)
            } else {
                val newAp = RangedAccessPoint.newInstance(result)
                newAp.update(result)
                updatedList.add(newAp)
            }
        }

        val now = System.currentTimeMillis()
        val freshList = updatedList.filter { now - it.age <= 15_000 }

        _rangedAccessPoints.postValue(freshList)

        estimateLocation()
    }

    // WIFI RTT ACCESS POINT LOCATIONS

    private val _estimatedPosition = MutableLiveData<DoubleArray>(null)
    val estimatedPosition : LiveData<DoubleArray> get() = _estimatedPosition

    private val _estimatedDistances = MutableLiveData<MutableMap<String, Double>>(mutableMapOf())
    val estimatedDistances : LiveData<Map<String, Double>> = _estimatedDistances.map { m -> m.toMap() }

    private val _debug = MutableLiveData(false)
    val debug : LiveData<Boolean> get() = _debug

    fun debugMode(debug: Boolean) {
        _debug.postValue(debug)
    }

    private fun estimateLocation() {
        val apLocations = mapConfig.value?.accessPointKnownLocations ?: return
        val apList = _rangedAccessPoints.value ?: return

        // get filtered APs
        val selectedAps = apList.filter { apLocations.containsKey(it.bssid) }

        // Choose between 2d or 3d trilateration
        val use3D = apLocations.values.any { it.heightMm != 0 }

        val minApCount = if (use3D) 4 else 3
        if (selectedAps.size < minApCount) return

        val positions = mutableListOf<DoubleArray>()
        val distances = mutableListOf<Double>()

        // Get distances from the selected APs
        for (ap in selectedAps) {
            val loc = apLocations[ap.bssid] ?: continue
            positions.add(
                if (use3D)
                    doubleArrayOf(loc.xMm.toDouble(), loc.yMm.toDouble(), loc.heightMm.toDouble())
                else
                    doubleArrayOf(loc.xMm.toDouble(), loc.yMm.toDouble())
            )
            distances.add(ap.distanceMm)
        }

        try {
            // Solve the x, y and z components with the trilateration solver
            val solver = NonLinearLeastSquaresSolver(
                TrilaterationFunction(positions.toTypedArray(), distances.toDoubleArray()),
                LevenbergMarquardtOptimizer()
            )
            val solution = solver.solve()
            val point = solution.point.toArray()

            val estimatedX = point[0]
            val estimatedY = point[1]
            val estimatedZ = if (use3D && point.size > 2) point[2] else 0.0

            _estimatedPosition.postValue(doubleArrayOf(estimatedX, estimatedY, estimatedZ))


            val distMap = selectedAps.associate { it.bssid to it.distanceMm }.toMutableMap()
            _estimatedDistances.postValue(distMap)

        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la trilatération", e)
        }
    }

    fun setMapConfig(config: MapConfig) {
        _mapConfig.postValue(config)
    }

    companion object {
        private val TAG = WifiRttViewModel::class.simpleName
    }

}
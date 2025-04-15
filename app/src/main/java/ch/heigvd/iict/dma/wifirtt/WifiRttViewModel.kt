package ch.heigvd.iict.dma.wifirtt

import android.net.wifi.rtt.RangingResult
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

//    private fun estimateLocation() {
//        // TODO we need to compute the estimated location by trilateration
//        // the library https://github.com/lemmingapex/trilateration
//        // will certainly helps you for the maths
//
//        // you should post the coordinates [x, y, height] of the estimated position in _estimatedPosition
//        // in the second experiment, you can hardcode the height as 0.0
//        _estimatedPosition.postValue(doubleArrayOf(2500.0, 8500.0, 0.0))
//
//        //as well as the distances with each access point as a MutableMap<String, Double>
//        val estimatedDistances = mutableMapOf(
//            "bc:df:58:f2:f7:b4" to 4500.0,
//            "24:e5:0f:08:17:a9" to 2650.0,
//            "24:e5:0f:08:5c:19" to 6400.0
//        )
//        _estimatedDistances.postValue(estimatedDistances)
//    }

    private fun estimateLocation() {
        val apLocations = mapConfig.value?.accessPointKnownLocations ?: return
        val apList = _rangedAccessPoints.value ?: return

        // On garde seulement les 3 AP définis dans le plan
        val selectedAps = apList.filter { apLocations.containsKey(it.bssid) }

        if (selectedAps.size < 3) return // Pas assez de points pour trilatération

        // On construit les données de trilatération
        val positions = mutableListOf<DoubleArray>()
        val distances = mutableListOf<Double>()

        for (ap in selectedAps) {
            val loc = apLocations[ap.bssid] ?: continue
            positions.add(doubleArrayOf(loc.xMm.toDouble(), loc.yMm.toDouble()))
            distances.add(ap.distanceMm)
        }

        // Si on a toujours au moins 3 AP valides
        if (positions.size >= 3) {
            try {
                val solver = NonLinearLeastSquaresSolver(
                    TrilaterationFunction(positions.toTypedArray(), distances.toDoubleArray()),
                    LevenbergMarquardtOptimizer()
                )
                val solution = solver.solve()

                val x = solution.point.getEntry(0)
                val y = solution.point.getEntry(1)
                val z = 0.0 // hauteur ignorée dans B30

                _estimatedPosition.postValue(doubleArrayOf(x, y, z))

                // Poster les distances actuelles pour affichage debug
                val distMap = selectedAps.associate { it.bssid to it.distanceMm }.toMutableMap()
                _estimatedDistances.postValue(distMap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    companion object {
        private val TAG = WifiRttViewModel::class.simpleName
    }

}
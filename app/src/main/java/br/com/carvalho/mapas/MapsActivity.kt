package br.com.carvalho.mapas

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.content.ContextCompat


import android.util.Log
import com.google.android.gms.location.LocationListener


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
        //adicionar as classes abaixo
        LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    override fun onLocationChanged(p0: Location?) {
        Log.d("TAG", "Firing onLocationChanged..............................................")
        mCurrentLocation = location
        mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
        updateUI()
    }
   var mCurrentLocation:Location? = null
   var LocationRequest mLocationRequest? = null
   var mLastUpdateTime:String? = null

    val REQUEST_GPS = 0


    override fun onConnectionSuspended(p0: Int) {
        Log.i("TAG","Suspenso")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("TAG","Erro de conexão")
    }

    override fun onConnected(p0: Bundle?) {
        checkPermission()

        val minhaLocalizacao = LocationServices
                .FusedLocationApi.
                getLastLocation(mGoogleApiClient)

        if (minhaLocalizacao != null) {
            adicionarMarcador(minhaLocalizacao.latitude,
                    minhaLocalizacao.longitude,
                    "oi eu aqui")
        }
    }

    private fun checkPermission() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                val builder = AlertDialog.Builder(this)

                builder.setMessage("Necessário a permissão para GPS")
                        .setTitle("Permissão Requerida")

                builder.setPositiveButton("OK") { dialog, id ->

                    requestPermission()
                }

                val dialog = builder.create()
                dialog.show()

            } else {
                requestPermission()
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_GPS)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_GPS -> {
                if (grantResults.size == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("TAG", "Permissão negada pelo usuário")
                } else {
                    Log.i("TAG", "Permissão concedida pelo usuário")
                }
                return
            }
        }
    }


    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient

   @Synchronized  fun callConnection() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                //Para adicionar o locationServices e necessario no Build.GRadle adicionar location
                .addApi(LocationServices.API)
                .build()

        mGoogleApiClient.connect()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btPesquisar.setOnClickListener {
            //para limpar os marcadores do mapa
            mMap.clear()

            val geocoder = Geocoder(this)
            var address: List<Address>?

            address = geocoder.getFromLocationName(etEndereco.text.toString(), 1)

            if (address.isNotEmpty()) {
                val location = address[0]
                adicionarMarcador(location.latitude, location.longitude, "Endereco pesquisado")
            } else {
                var alert = AlertDialog.Builder(this).create()
                alert.setTitle("Ops!! Deu ruim!!!!")
                alert.setMessage("Endereço não encontrado!")

                alert.setCancelable(false)

                alert.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", { dialogInterface, Inteiro ->
                    alert.dismiss()
                })

                alert.show()
            }

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    fun adicionarMarcador(latitude: Double, longitude: Double, title: String) {
        val sydney = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions()
                .position(sydney)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round)))



        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16f))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        callConnection()
        // Add a marker in Sydney and move the camera

    }


    private fun updateUI() {
        Log.d("TAG", "UI update initiated .............")
        if (null != mCurrentLocation) {
            val lat = String.valueOf(mCurrentLocation.getLatitude())
            val lng = String.valueOf(mCurrentLocation.getLongitude())
            tvLocation.setText("At Time: " + mLastUpdateTime + "\n" +
                    "Latitude: " + lat + "\n" +
                    "Longitude: " + lng + "\n" +
                    "Accuracy: " + mCurrentLocation.getAccuracy() + "\n" +
                    "Provider: " + mCurrentLocation.getProvider())
        } else {
            Log.d(FragmentActivity.TAG, "location is null ...............")
        }
    }
}

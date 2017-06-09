package ar.com.lichtmaier.antenas;

import android.arch.lifecycle.*;
import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.location.LocationRequest;

public class MapaActivity extends AppCompatActivity implements LocationClientCompat.Callback, LifecycleRegistryOwner
{
	private long comienzoUsoPantalla;
	@Nullable private LocationClientCompat locationClient;

	private AyudanteDePagos ayudanteDePagos;
	private MenuItem opciónPagar;
	private MapaFragment mapaFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Antena.applicationContext = getApplicationContext();
		super.onCreate(savedInstanceState);
		Antena.applicationContext = null;
		setContentView(R.layout.activity_mapa);
		Toolbar tb = (Toolbar)findViewById(R.id.toolbar);
		if(tb != null)
		{
			setSupportActionBar(tb);
			//noinspection ConstantConditions
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		ayudanteDePagos = AyudanteDePagos.dameInstancia(this);
		ayudanteDePagos.observe(this, (pro) -> {
			if(pro == null)
				return;
			if(opciónPagar != null)
				opciónPagar.setVisible(!pro);
		});

		if(savedInstanceState == null)
		{
			mapaFragment = new MapaFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, mapaFragment)
					.commit();
			if(getIntent().getExtras() == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
				getSystemService(ShortcutManager.class).reportShortcutUsed("mapa");
		} else
		{
			mapaFragment = (MapaFragment)getSupportFragmentManager().findFragmentById(R.id.container);
		}

		if(mapaFragment == null)
			throw new NullPointerException("mapaFragment es null, activity = " + this);

		locationClient = LocationClientCompat.create(this, LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(200)
				.setFastestInterval(200)
				.setSmallestDisplacement(1), this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		comienzoUsoPantalla = System.currentTimeMillis();
	}

	@Override
	protected void onPause()
	{
		if(System.currentTimeMillis() - comienzoUsoPantalla > 1000 * 10)
			Calificame.registráQueMiróElMapa(this);
		super.onPause();
	}

	@Override
	protected void onStop()
	{
		if(locationClient != null)
			locationClient.stop();
		super.onStop();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if(locationClient != null)
			locationClient.start();
	}

	@Override
	public void onBackPressed()
	{
		// Si fuimos llamados sólo para mostrar una antena, al primer "back" nos vamos.
		if(((MapaFragment)getSupportFragmentManager().findFragmentById(R.id.container)).modoMostrarAntena)
		{
			finish();
			return;
		}

		FragmentManager fm = getSupportFragmentManager();
		if(fm.getBackStackEntryCount() == 0)
		{
			super.onBackPressed();
			return;
		}
		fm.popBackStack("canales", FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.mapa, menu);
		opciónPagar = menu.findItem(R.id.action_pagar);
		Boolean pro = ayudanteDePagos.getValue();
		if(pro != null)
			opciónPagar.setVisible(!pro);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		switch(id)
		{
			case R.id.action_pagar:
				ayudanteDePagos.pagar(this);
				return true;
			case R.id.action_settings:
				Intent i = new Intent(this, PreferenciasActivity.class);
				startActivity(i);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void canalSeleccionado(Antena antena, Canal canal)
	{
		MapaFragment mfr = (MapaFragment)getSupportFragmentManager().findFragmentById(R.id.container);
		mfr.canalSeleccionado(antena, canal);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(locationClient != null && locationClient.onActivityResult(requestCode, resultCode, data))
			return;
		if(ayudanteDePagos.onActivityResult(requestCode, resultCode, data))
			return;
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onConnectionFailed()
	{
		Log.e("antenas", "No se pudo inicializar Play Services: El mapa no accederá a la ubicación actual.");
	}

	@Override
	public void onLocationChanged(Location location)
	{
		if(mapaFragment != null)
			mapaFragment.onLocationChanged(location);
	}

	LiveData<Location> getLocation()
	{
		return locationClient == null ? null : locationClient.getLocation();
	}

	LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

	@Override
	public LifecycleRegistry getLifecycle()
	{
		return lifecycleRegistry;
	}
}

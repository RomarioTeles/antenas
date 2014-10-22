package ar.com.lichtmaier.antenas;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.*;
import android.widget.TextView;

public class UnaAntenaActivity extends AntenaActivity
{
	private Antena antena;
	private int orientaciónOriginal;
	private int flechaOriginalY;
	private int flechaOriginalX;
	private int flechaOriginalAncho;
	private int flechaOriginalAlto;
	private float escalaAncho;
	private float escalaAlto;
	private int mLeftDelta;
	private int mTopDelta;
	private double ángulo;
	private FlechaView flecha;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		asignarLayout();
		Bundle bundle = getIntent().getExtras();
		antena = Antena.dameAntena(this, País.valueOf(bundle.getString("ar.com.lichtmaier.antenas.antenaPaís")), bundle.getInt("ar.com.lichtmaier.antenas.antenaIndex"));
		((TextView)findViewById(R.id.antena_desc)).setText(antena.descripción);
		nuevaUbicación(); // para que se configure la distancia

		flechaOriginalY = bundle.getInt(PACKAGE + ".top");
		flechaOriginalX = bundle.getInt(PACKAGE + ".left");
		flechaOriginalAncho = bundle.getInt(PACKAGE + ".width");
		flechaOriginalAlto = bundle.getInt(PACKAGE + ".height");
		orientaciónOriginal = bundle.getInt(PACKAGE + ".orientation");
		final double ángulo = bundle.getDouble(PACKAGE + ".ángulo");

		flecha = (FlechaView)findViewById(R.id.flecha);

		if(savedInstanceState == null)
		{
			flecha.setÁngulo(ángulo);
			flecha.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
			{
				@Override
				public boolean onPreDraw()
				{
					flecha.getViewTreeObserver().removeOnPreDrawListener(this);

					calcularDeltas();

					AnimationSet anim = new AnimationSet(true);
					anim.addAnimation(new ScaleAnimation(escalaAncho, 1, escalaAlto, 1, 0, 0));
					anim.addAnimation(new TranslateAnimation(mLeftDelta, 0, mTopDelta, 0));
					anim.setInterpolator(new AccelerateDecelerateInterpolator());
					anim.setDuration(500);
					flecha.startAnimation(anim);

					AlphaAnimation aa = new AlphaAnimation(0, 1);
					aa.setDuration(600);
					aa.setInterpolator(new AccelerateInterpolator());
					findViewById(R.id.fondo).startAnimation(aa);

					TextView antenaDesc = (TextView) findViewById(R.id.antena_desc);
					int[] screenLocation = new int[2];
					antenaDesc.getLocationOnScreen(screenLocation);
					int d = getWindow().getDecorView().getBottom() - screenLocation[1];
					TranslateAnimation ta = new TranslateAnimation(0, 0, d, 0);
					ta.setInterpolator(new DecelerateInterpolator());
					ta.setDuration(500);
					ta.setStartOffset(200);
					antenaDesc.startAnimation(ta);
					TextView antenaDist = (TextView) findViewById(R.id.antena_dist);
					ta = new TranslateAnimation(0, 0, d, 0);
					ta.setInterpolator(new DecelerateInterpolator());
					ta.setDuration(500);
					ta.setStartOffset(200);
					antenaDist.startAnimation(ta);

					if(AntenaActivity.flechaADesaparecer != null)
					{
						anim.setAnimationListener(new Animation.AnimationListener()
						{
							@Override
							public void onAnimationStart(Animation animation)
							{
								flechaADesaparecer.setVisibility(View.INVISIBLE);
								/*
								flechaADesaparecer.postDelayed(new Runnable()
								{
									@Override
									public void run()
									{
										flechaADesaparecer.setVisibility(View.INVISIBLE);
									}
								}, 200);*/
							}

							@Override
							public void onAnimationEnd(Animation animation) { }

							@Override
							public void onAnimationRepeat(Animation animation) { }
						});

						aa.setAnimationListener(new Animation.AnimationListener()
						{
							@Override
							public void onAnimationStart(Animation animation) { }

							@Override
							public void onAnimationEnd(Animation animation)
							{
								AntenaActivity.flechaADesaparecer.setVisibility(View.VISIBLE);
							}

							@Override
							public void onAnimationRepeat(Animation animation) { }
						});
					}

					/*
					f.setPivotX(0);
					f.setPivotY(0);
					f.setScaleX(mWidthScale);
					f.setScaleY(mHeightScale);
					f.setTranslationX(mLeftDelta);
					f.setTranslationY(mTopDelta);
					f.animate().setDuration(3000).scaleX(1).scaleY(1).translationX(0).translationY(0);
					*/
					return true;
				}
			});
		}
	}

	private void calcularDeltas()
	{
		int[] screenLocation = new int[2];
		flecha.getLocationOnScreen(screenLocation);
		mLeftDelta = flechaOriginalX - screenLocation[0];
		mTopDelta = flechaOriginalY - screenLocation[1];

		escalaAncho = (float) flechaOriginalAncho / flecha.getWidth();
		escalaAlto = (float) flechaOriginalAlto / flecha.getHeight();
	}

	@Override
	public void onBackPressed()
	{
		if (getResources().getConfiguration().orientation != orientaciónOriginal)
		{
			super.onBackPressed();
			return;
		}
		if(escalaAlto == 0)
			calcularDeltas();
		if(AntenaActivity.flechaADesaparecer != null)
			AntenaActivity.flechaADesaparecer.setVisibility(View.INVISIBLE);
		AlphaAnimation aa = new AlphaAnimation(1, 0);
		aa.setDuration(500);
		aa.setInterpolator(new AccelerateInterpolator());
		aa.setFillAfter(true);
		findViewById(R.id.fondo).startAnimation(aa);
		aa.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation) { }
			@Override
			public void onAnimationEnd(Animation animation)
			{
				finish();
				if(AntenaActivity.flechaADesaparecer != null)
				{
					AntenaActivity.flechaADesaparecer.setÁngulo(ángulo);
					AntenaActivity.flechaADesaparecer.setVisibility(View.VISIBLE);
				}
			}
			@Override
			public void onAnimationRepeat(Animation animation) { }
		});

		AnimationSet anim = new AnimationSet(true);
		anim.addAnimation(new ScaleAnimation(1, escalaAncho, 1, escalaAlto, 0, 0));
		anim.addAnimation(new TranslateAnimation(0, mLeftDelta, 0, mTopDelta));
		anim.setInterpolator(new AccelerateDecelerateInterpolator());
		anim.setDuration(500);
		anim.setFillAfter(true);
		findViewById(R.id.flecha).startAnimation(anim);

		TextView antenaDesc = (TextView) findViewById(R.id.antena_desc);
		int[] screenLocation = new int[2];
		antenaDesc.getLocationOnScreen(screenLocation);
		int d = getWindow().getDecorView().getBottom() - screenLocation[1];
		TranslateAnimation ta = new TranslateAnimation(0, 0, 0, d);
		ta.setInterpolator(new AccelerateInterpolator());
		ta.setDuration(500);
		ta.setFillAfter(true);
		antenaDesc.startAnimation(ta);
		TextView antenaDist = (TextView) findViewById(R.id.antena_dist);
		ta = new TranslateAnimation(0, 0, 0, d);
		ta.setInterpolator(new AccelerateInterpolator());
		ta.setDuration(500);
		ta.setFillAfter(true);
		antenaDist.startAnimation(ta);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		((Aplicacion)getApplication()).reportActivityStart(this);
	}

	@Override
	protected void onStop()
	{
		((Aplicacion)getApplication()).reportActivityStop(this);
		super.onStop();
	}

	@Override
	public void finish()
	{
		super.finish();
		if (getResources().getConfiguration().orientation == orientaciónOriginal)
			overridePendingTransition(0, 0);
	}

	@Override
	protected void asignarLayout()
	{
		setContentView(R.layout.activity_una_antena);
	}

	@Override
	protected void nuevaUbicación()
	{
		if(antena != null)
			ponéDistancia(antena, (TextView) findViewById(R.id.antena_dist));
	}

	@Override
	void nuevaOrientación(double brújula)
	{
		double rumbo = antena.rumboDesde(coordsUsuario);
		FlechaView f = (FlechaView)findViewById(R.id.flecha);
		ángulo = rumbo - brújula;
		f.setÁngulo(ángulo);
	}
}

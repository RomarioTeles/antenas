package ar.com.lichtmaier.util;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class AsyncLiveData<T> extends LiveData<T>
{
	private Future<T> future;
	private boolean loaded = false;

	public AsyncLiveData(boolean loadImmediately)
	{
		if(loadImmediately)
			load();
	}

	public AsyncLiveData()
	{
		this(true);
	}

	private void load()
	{
		future = ((ExecutorService)AsyncTask.THREAD_POOL_EXECUTOR).submit(() -> {
			T r = loadInBackground();
			postValue(r);
			synchronized(AsyncLiveData.this) {
				future = null;
				loaded = true;
			}
			return r;
		});
	}

	@Override
	protected synchronized void onInactive()
	{
		if(future != null)
		{
			future.cancel(false);
			future = null;
		}
	}

	@Override
	protected synchronized void onActive()
	{
		if(future == null && !loaded)
			load();
	}

	@WorkerThread
	protected abstract T loadInBackground();
}
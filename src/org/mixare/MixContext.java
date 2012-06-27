/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.render.Matrix;
import org.mixare.mgr.datasource.DataSourceManager;
import org.mixare.mgr.datasource.DataSourceManagerFactory;
import org.mixare.mgr.downloader.DownloadManager;
import org.mixare.mgr.downloader.DownloadManagerFactory;
import org.mixare.mgr.location.LocationFinder;
import org.mixare.mgr.location.LocationFinderFactory;
import org.mixare.mgr.notification.NotificationManager;
import org.mixare.mgr.notification.NotificationManagerFactory;
import org.mixare.mgr.webcontent.WebContentManager;
import org.mixare.mgr.webcontent.WebContentManagerFactory;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;

/**
 * MixView Context wrapper that provide access to {@link DownloadManager downloadManager}, 
 * {@link LocationFinder LocationFinder}, {@link DataSourceManager DataSourceManager}, 
 * {@link WebContentManager WebContentManager}, {@link NotificationManager NotificationManager}
 * and Application context
 * 
 * 
 */
public class MixContext extends ContextWrapper implements MixContextInterface {

	/** TAG for logging */
	public static final String TAG = "Mixare";

	private MixView mixView;
	
	/* TODO MixContext#rotationM move to DataView or AugView*/
	private Matrix rotationM = new Matrix();

	/** Responsible for all download */
	private DownloadManager downloadManager;

	/** Responsible for all location tasks */
	private LocationFinder locationFinder;

	/** Responsible for data Source Management */
	private DataSourceManager dataSourceManager;

	/** Responsible for Web Content */
	private WebContentManager webContentManager;
	
	/** Responsible for Notification logging */
	private NotificationManager notificationManager;

	/**
	 * Constructor - currently only accepts MixView activity.
	 * Future plans - allow (Activity appCtx)
	 * 
	 * @param appCtx MixView instance
	 */
	public MixContext(MixView appCtx) {
		super(appCtx);
		mixView = appCtx;

		getDataSourceManager().refreshDataSources();

		if (!getDataSourceManager().isAtLeastOneDatasourceSelected()) {
			rotationM.toIdentity();
		}
		getLocationFinder().switchOn();
		getLocationFinder().findLocation();
	}

	/**
	 * Returns intent's data
	 * @return intentData or empty string
	 */
	public String getStartUrl() {
		Intent intent = ((Activity) getActualMixView()).getIntent();
		if (intent.getAction() != null
				&& intent.getAction().equals(Intent.ACTION_VIEW)) {
			return intent.getData().toString();
		} else {
			return "";
		}
	}

	/**
	 * synchronized rotation setter!
	 * TODO {@link MixContext#getRM(Matrix) Validate method}
	 * @param dest
	 */
	public void getRM(Matrix dest) {
		synchronized (rotationM) {
			dest.set(rotationM);
		}
	}

	/**
	 * Shows a webpage with the given url when clicked on a marker.
	 * 
	 * @param url String url link
	 */
	public void loadMixViewWebPage(String url) throws Exception {
		// TODO: CHECK INTERFACE METHOD
		getWebContentManager().loadWebPage(url, getActualMixView());
	}

	/**
	 * Keep context view consistent with current view -
	 * It sets MixView.
	 * 
	 * @param mixView
	 */
	public void doResume(MixView mixView) {
		setActualMixView(mixView);
	}

	/**
	 * synchronized method to smooth rotation View.
	 * TODO {@link MixContext#updateSmoothRotation(Matrix) validate method}
	 * 
	 * @param smoothR
	 */
	public void updateSmoothRotation(Matrix smoothR) {
		synchronized (rotationM) {
			rotationM.set(smoothR);
		}
	}

	/**
	 * Returns {@link DataSourceManager DataSourceManager}.
	 * 
	 * @return dataSourceManager
	 */
	public DataSourceManager getDataSourceManager() {
		if (this.dataSourceManager == null) {
			dataSourceManager = DataSourceManagerFactory
					.makeDataSourceManager(this);
		}
		return dataSourceManager;
	}

	/**
	 * Returns {@link LocationFinder LocationFinder}.
	 * 
	 * @return locationFinder
	 */
	public LocationFinder getLocationFinder() {
		if (this.locationFinder == null) {
			locationFinder = LocationFinderFactory.makeLocationFinder(this);
		}
		return locationFinder;
	}

	/**
	 * Returns {@link DownLoadManager DownloadManager}.
	 * 
	 * @return downloadMng
	 */
	public DownloadManager getDownloadManager() {
		if (this.downloadManager == null) {
			downloadManager = DownloadManagerFactory.makeDownloadManager(this);
			getLocationFinder().setDownloadManager(downloadManager);
		}
		return downloadManager;
	}

	/**
	 * Returns {@link WebContentManager WebContentManager}.
	 * 
	 * @return webContntMng
	 */
	public WebContentManager getWebContentManager() {
		if (this.webContentManager == null) {
			webContentManager = WebContentManagerFactory
					.makeWebContentManager(this);
		}
		return webContentManager;
	}

	/**
	 * Returns {@link NotificationManager NotificationManager}.
	 * 
	 * @return notificationMng
	 */
	public NotificationManager getNotificationManager() {
		if (this.notificationManager == null) {
			notificationManager = NotificationManagerFactory
					.makeNotificationManager(this);
		}
		return notificationManager;
	}
	
	/**
	 * Returns current {@link MixView MixView}.
	 * 
	 * @return mixView
	 */
	public MixView getActualMixView() {
		synchronized (mixView) {
			return this.mixView;
		}
	}

	/*
	 * Private method that sets MixView
	 * @see MixContext#doResume(MixView mv)
	 */
	private void setActualMixView(MixView mv) {
		synchronized (mixView) {
			this.mixView = mv;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ContentResolver getContentResolver() {
		ContentResolver out = super.getContentResolver();
		if (super.getContentResolver() == null) {
			out = getActualMixView().getContentResolver();
		}
		return out;
	}
	
	/**
	 * Toast POPUP notification
	 * 
	 * @param string message
	 */
	public void doPopUp(final String string){
		getNotificationManager().addNotification(string);
	}

	/**
	 * Toast POPUP notification
	 * 
	 * @param connectionGpsDialogText
	 */
	public void doPopUp(int RidOfString) {
        doPopUp(this.getString(RidOfString));
	}
}

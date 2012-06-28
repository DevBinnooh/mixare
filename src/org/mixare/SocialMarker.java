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

import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.TextObj;

import android.location.Location;

/**
 * The SocialMarker class represents a marker, which contains data from
 * sources like twitter etc. Social markers appear at the top of the screen
 * and show a small logo of the source.
 * 
 * @author hannes
 *
 */
public class SocialMarker extends LocalMarker {
	
	public static final int MAX_OBJECTS=15;

	/**
	 * SocialMarker constructor with the given params.
	 * 
	 * @param id String Marker's id
	 * @param title String Marker's title
	 * @param latitude double latitude
	 * @param longitude double longitude
	 * @param altitude double altitude
	 * @param url String link when clicked
	 * @param type int Datasource type
	 * @param Color int Color representation {@link android.graphics.Color Color}
	 * @see LocalMarker
	 */
	public SocialMarker(String id, String title, double latitude, double longitude,
			double altitude, String URL, int type, int color) {
		super(id, title, latitude, longitude, altitude, URL, type, color);
	}

	/**
	 * Updates NavigationMarker location
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	public void update(Location curGPSFix) {

		//0.35 radians ~= 20 degree
		//0.85 radians ~= 45 degree
		//minAltitude = sin(0.35)
		//maxAltitude = sin(0.85)
		
		// we want the social markers to be on the upper part of
		// your surrounding sphere 
		double altitude = curGPSFix.getAltitude()+Math.sin(0.35)*distance+Math.sin(0.4)*(distance/(MixView.getDataView().getRadius()*1000f/distance));
		getmGeoLoc().setAltitude(altitude);
		super.update(curGPSFix);

	}

	/**
	 * Draw specification of Navigation Marker
	 */
	@Override
	public void draw(PaintScreen dw) {
		//This is The Ghost marker
		//drawTitle(dw);

		if (isVisible) {
			drawTitle(dw);
			float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
			//Bitmap bitmap = BitmapFactory.decodeResource(MixContext.getResources(), DataSource.getDataSourceIcon());
//			if(bitmap!=null) {
//				dw.paintBitmap(bitmap, cMarker.x - maxHeight/1.5f, cMarker.y - maxHeight/1.5f);
//			}
//			else {
				dw.setStrokeWidth(maxHeight / 10f);
				dw.setFill(false);
				//dw.setColor(DataSource.getColor(type));
				dw.paintCircle(cMarker.x, cMarker.y, maxHeight / 1.5f);
			//}
		}
	}

	/**
	 * Draw a title for SocialMarker. It displays full title if title's length is less
	 * than <b>20</b> chars, otherwise, it displays the first 20 chars and concatenate
	 * three dots "..."
	 * 
	 * @param dw PaintScreen View Screen that title screen will be drawn into
	 */
	private void drawTitle(final PaintScreen dw) {
		if (isVisible) {
			final float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
			String textStr = MixUtils.shortenTitle(title,distance,20);
			textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1, 250,
					dw, underline);
			 dw.setColor(this.getColour());
			final float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
					getSignMarker().x, getSignMarker().y);
			txtLab.prepare(textBlock);
			dw.setStrokeWidth(1f);
			dw.setFill(true);
			dw.paintObj(txtLab, getSignMarker().x - txtLab.getWidth() / 2,
					getSignMarker().y + maxHeight, currentAngle + 90, 1);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxObjects() {
		return MAX_OBJECTS;
	}

	
}

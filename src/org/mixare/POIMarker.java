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

import java.text.DecimalFormat;

import org.mixare.LocalMarker;
import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.TextObj;

import android.graphics.Color;
import android.graphics.Path;
import android.location.Location;

/**
 * This markers represent the points of interest.
 * On the screen they appear as circles, since this
 * class inherits the draw method of the Marker.
 * 
 * @author hannes
 * 
 */
public class POIMarker extends LocalMarker {

	public static final int MAX_OBJECTS = 20;
	public static final int OSM_URL_MAX_OBJECTS = 5;

	public POIMarker(String id, String title, double latitude, double longitude,
			double altitude, String URL, int type, int color) {
		super(id, title, latitude, longitude, altitude, URL, type, color);

	}

	@Override
	public void update(Location curGPSFix) {
		super.update(curGPSFix);
	}

	@Override
	public int getMaxObjects() {
		return MAX_OBJECTS;
	}

	@Override
	public void drawCircle(PaintScreen dw) {
		if (isVisible) {
			float maxHeight = dw.getHeight();
			dw.setStrokeWidth(maxHeight / 100f);
			dw.setFill(false);

				dw.setColor(getColour());
			
			// draw circle with radius depending on distance
			// 0.44 is approx. vertical fov in radians
			double angle = 2.0 * Math.atan2(10, distance);
			double radius = Math.max(
					Math.min(angle / 0.44 * maxHeight, maxHeight),
					maxHeight / 25f);

			/*
			 * distance 100 is the threshold to convert from circle to another
			 * shape
			 */
			if (distance < 100.0)
				otherShape(dw);
			else
				dw.paintCircle(cMarker.x, cMarker.y, (float) radius);

		}
	}

	/**
	 * Draw a title for POIMarker. It displays full title if title's length is less
	 * than <b>15</b> chars, otherwise, it displays the first 10 chars and concatenate
	 * three dots "..."
	 * 
	 * @param dw PaintScreen View Screen that title screen will be drawn into
	 */
	public void drawTitle(PaintScreen dw) {
		if (isVisible) {
			final float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
			String textStr = MixUtils.shortenTitle(title,distance,15);
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

	public void otherShape(PaintScreen dw) {
		// This is to draw new shape, triangle
		float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
				getSignMarker().x, getSignMarker().y);
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

		dw.setColor(getColour());
		float radius = maxHeight / 1.5f;
		dw.setStrokeWidth(dw.getHeight() / 100f);
		dw.setFill(false);

		Path tri = new Path();
		float x = 0;
		float y = 0;
		tri.moveTo(x, y);
		tri.lineTo(x - radius, y - radius);
		tri.lineTo(x + radius, y - radius);

		tri.close();
		dw.paintPath(tri, cMarker.x, cMarker.y, radius * 2, radius * 2,
				currentAngle + 90, 1);
	}

	@Override
	public void draw(PaintScreen dw) {
		drawCircle(dw);
		drawTitle(dw);
		
	}

}

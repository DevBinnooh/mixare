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
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.MixUtils;
import org.mixare.lib.render.Matrix;
import org.mixare.lib.render.MixVector;

/**
 * This class calculates the bearing and <strike> pitch out </strike>of the angles,
 * it also saves the state of downloads.
 */
public class MixState implements MixStateInterface{

	/** Not started state identifier */
	public final static int NOT_STARTED = 0;
	/** Processing state identifier */
	public final static int PROCESSING = 1; 
	/** Ready state identifier */
	public final static int READY = 2;
	/** Done state identifier */
	public final static int DONE = 3; 

	/** Next state identifier */
	public int nextLStatus = MixState.NOT_STARTED;
	//String downloadId;

	private float curBearing;
	/** @deprecated not in used */
	private float curPitch;

	private boolean detailsView;

	/**
	 * Handles click events and launches web pages if its webpage event.
	 *  
	 */
	public boolean handleEvent(MixContextInterface ctx, String onPress) {
		if (onPress != null && onPress.startsWith("webpage")) {
			try {
				String webpage = MixUtils.parseAction(onPress);
				this.detailsView = true;
				ctx.loadMixViewWebPage(webpage);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} 
		return true;
	}

	/**
	 * Returns current bearing direction.
	 * @return curBearing
	 * @see #calcPitchBearing(Matrix)
	 */
	public float getCurBearing() {
		return curBearing;
	}

	/**
	 * @deprecated not in use
	 * @return
	 */
	public float getCurPitch() {
		return curPitch;
	}
	
	/**
	 * Returns detail view state
	 * @return detailsView
	 * @see #setDetailsView(boolean)
	 */
	public boolean isDetailsView() {
		return detailsView;
	}
	
	/**
	 * Sets detail view state
	 * @param detailsView
	 * @see #isDetailsView()
	 */
	public void setDetailsView(boolean detailsView) {
		this.detailsView = detailsView;
	}

	/**
	 * A method that calculate bearing direction and <strike> pitch angle </strike>.
	 * The passed matrix is 3x3 matrix that contains the current device rotation.
	 * 
	 * @param rotationM
	 * @see http://en.wikipedia.org/wiki/Bearing_(navigation)
	 * @see http://www.movable-type.co.uk/scripts/latlong.html
	 */
	public void calcPitchBearing(Matrix rotationM) {
		MixVector looking = new MixVector();
		rotationM.transpose();
		looking.set(1, 0, 0);
		looking.prod(rotationM);
		this.curBearing = (int) (MixUtils.getAngle(0, 0, looking.x, looking.z)  + 360 ) % 360 ;

		//The next comming lines are not in use
		rotationM.transpose();
		looking.set(0, 1, 0);
		looking.prod(rotationM);
		this.curPitch = -MixUtils.getAngle(0, 0, looking.y, looking.z);
	}
}

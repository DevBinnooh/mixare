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

import java.net.URLDecoder;
import java.text.DecimalFormat;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenLine;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.marker.draw.ParcelableProperty;
import org.mixare.lib.marker.draw.PrimitiveProperty;
import org.mixare.lib.reality.PhysicalPlace;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;

import android.graphics.Bitmap;
import android.location.Location;

/**
 * The class represents a marker and contains its information.
 * It draws the marker itself and the corresponding label.
 * All markers are specific markers like SocialMarkers or
 * NavigationMarkers, since this class is abstract
 */
public abstract class LocalMarker implements Marker {

	private String ID;
	/** Marker's title to be shown */
	protected String title;
	/** Boolean if title link to http page */
	protected boolean underline = false;
	private String URL;
	protected PhysicalPlace mGeoLoc;
	/** distance from user to mGeoLoc in meters */
	protected double distance;
	/** Marker's color */
	private int colour;
	
	private boolean active;

	// Draw properties
	/** Marker's Visibility to user */
	protected boolean isVisible;
//	private boolean isLookingAt;
//	private boolean isNear;
//	private float deltaCenter;
	/** Current Marker view position */
	public MixVector cMarker = new MixVector();
	
	/** projected Marker position SignMarker */
	private MixVector signMarker = new MixVector();

	/** Marker location */
	protected MixVector locationVector = new MixVector();
	
	/** Origin view points 
	 * @deprecated please don't use this
	 */
	private MixVector origin = new MixVector(0, 0, 0);
	
	/** @deprecated Please don't use this vector, Scope has been lowered */
	private MixVector upV = new MixVector(0, 1, 0);
	
	/** @deprecated Please don't use this, Scope has been lowered */
	private ScreenLine pPt = new ScreenLine();

	/** Title Label, can be override to set final touches on setting the title 
	 * TODO {@link LocalMarker#txtLab txtLabel} localize scope
	 */ 
	public Label txtLab = new Label();
	
	/** Title Box, can be override to set final touches on setting the title 
	 * TODO {@link LocalMarker#textBlock textBlock} localize scope
	 */ 
	protected TextObj textBlock;

	/**
	 * LocalMarker - Constructor
	 * Sub-Classes <b>need to call this constructor</b>
	 * @param id String marker's id (will be reset to id+type+title) {@link #hashCode() use for reference}
	 * @param title String marker's title
	 * @param latitude double marker's latitude
	 * @param longitude double marker's longitude
	 * @param altitude double marker's altitude (optional)
	 * @param link String link page when marker clicked
	 * @param type int {@link org.mixare.data.DataSource#TYPE data source type}
	 * @param colour int Color representation {@link android.graphics.Color Color}
	 */
	public LocalMarker(final String id,  String title, final double latitude,
			 double longitude, final double altitude,final String link,
			int type, final int colour) {
		super();

		this.active = false;
		this.title = title;
		this.mGeoLoc = (new PhysicalPlace(latitude,longitude,altitude));
		if (link != null && link.length() > 0) {
			this.URL = ("webpage:" + URLDecoder.decode(link));
			this.underline = true;
		}
		this.colour = colour;
		this.ID = id + "##" + type + "##" + title;
	}


	/**
	 * Computes rotation and marker's new position 
	 * TODO remove {@link LocalMarker#cCMarker(MixVector, Camera, float, float) originalPoints} to local scope
	 * @param originalPoint (0,0,0)
	 * @param viewCam {@link Camera camera} class
	 * @param addX float x position to be moved to
	 * @param addY float y position to be moved to
	 */
	private void cCMarker(MixVector originalPoint, Camera viewCam, final float addX, final float addY) {

		// Temp properties
		final MixVector tmpa = new MixVector(originalPoint);
		final MixVector tmpc = new MixVector(0,1,0);
		tmpa.add(locationVector); //3 
		tmpc.add(locationVector); //3
		tmpa.sub(viewCam.lco); //4
		tmpc.sub(viewCam.lco); //4
		tmpa.prod(viewCam.transform); //5
		tmpc.prod(viewCam.transform); //5

		final MixVector tmpb = new MixVector();
		viewCam.projectPoint(tmpa, tmpb, addX, addY); //6
		cMarker.set(tmpb); //7
		viewCam.projectPoint(tmpc, tmpb, addX, addY); //6
		getSignMarker().set(tmpb); //7
	}

	/**
	 * Checks if Marker is within Z angle of Camera.
	 * It sets the visibility upon that.
	 */
	private void calcV() {
		isVisible = false;
//		isLookingAt = false;
//		deltaCenter = Float.MAX_VALUE;

		if (cMarker.z < -1f) {
			isVisible = true;
		}
	}

	/**
	 * Updates marker's based on the current location
	 */
	public void update(Location curGPSFix) {
		// An elevation of 0.0 probably means that the elevation of the
		// POI is not known and should be set to the users GPS height
		// Note: this could be improved with calls to 
		// http://www.geonames.org/export/web-services.html#astergdem 
		// to estimate the correct height with DEM models like SRTM, AGDEM or GTOPO30
		if(getmGeoLoc().getAltitude()==0.0)
			getmGeoLoc().setAltitude(curGPSFix.getAltitude());

		// compute the relative position vector from user position to POI location
		PhysicalPlace.convLocToVec(curGPSFix, getmGeoLoc(), locationVector);
	}

	/**
	 * A must call function to rotate marker to the current position,
	 * which provide the smooth aumented view.
	 * <b>LocalMarkers sub-classes:</b> Don't need to call this method,
	 * this method is being called by view before calling {@link #draw(PaintScreen)}
	 * 
	 * If the caller activity froze the view, this function will not be called.
	 * <b>Overriding this method is not allowed</b>
	 * 
	 */
	final public void calcPaint(Camera viewCam, float addX, float addY) {
		cCMarker(new MixVector(0,0,0), viewCam, addX, addY);
		calcV();
	}

//	private void calcPaint(Camera viewCam) {
//		cCMarker(origin, viewCam, 0, 0);
//	}

	/**
	 * Checks if click event is within marker's bounderies
	 * 
	 * TODO adapt the following to the variable radius!
	 * TODO LocalMarker#isClickValid lower computing overhead
	 * @param x float x clicked position
	 * @param y float y clicked position
	 * @return boolean true if clicked, false otherwise
	 */
	private boolean isClickValid(float x, float y) {
		
		//if the marker is not active (i.e. not shown in AR view) we don't have to check it for clicks
		if (!isActive() && !this.isVisible)
			return false;

		final float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
				getSignMarker().x, getSignMarker().y);
		//TODO adapt the following to the variable radius!
		final ScreenLine screenBounderies = new ScreenLine();
		screenBounderies.x = x - getSignMarker().x;
		screenBounderies.y = y - getSignMarker().y;
		screenBounderies.rotate((float) Math.toRadians(-(currentAngle + 90)));
		screenBounderies.x += txtLab.getX();
		screenBounderies.y += txtLab.getY();

		final float objX = txtLab.getX() - txtLab.getWidth() / 2;
		float objY = txtLab.getY() - txtLab.getHeight() / 2;
		float objW = txtLab.getWidth();
		float objH = txtLab.getHeight();

		//TODO LocalMarker#isClickValid lower computing overhead
		if (screenBounderies.x > objX && screenBounderies.x < objX + objW && screenBounderies.y > objY
				&& screenBounderies.y < objY + objH) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Local marker specific drawing instruction.
	 * <b>LocalMarkers Sub-classes Must call this to be drawn</b>
	 * default implementation, for example, is drawing "object"/"circle" and 
	 * text block
	 * @param dw PaintScreen to be drawn into
	 */
	abstract public void draw(PaintScreen dw);
//	{
//		drawCircle(dw);
//		drawTextBlock(dw);
//	}

	/**
	 * @deprecated please use your custom drawing
	 * @param dw PaintScreen
	 * @see #draw(PaintScreen)
	 */
	public void drawCircle(PaintScreen dw) {

		if (isVisible) {
			//float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
			float maxHeight = dw.getHeight();
			dw.setStrokeWidth(maxHeight / 100f);
			dw.setFill(false);
			//dw.setColor(DataSource.getColor(type));

			//draw circle with radius depending on distance
			//0.44 is approx. vertical fov in radians 
			double angle = 2.0*Math.atan2(10,distance);
			double radius = Math.max(Math.min(angle/0.44 * maxHeight, maxHeight),maxHeight/25f);
			//double radius = angle/0.44d * (double)maxHeight;

			dw.paintCircle(cMarker.x, cMarker.y, (float)radius);
		}
	}

	/**
	 * @deprecated Please use your custom drawing
	 * @param dw PaintScreen
	 * @see #draw(PaintScreen)
	 */
	public void drawTextBlock(PaintScreen dw) {
		//TODO: grandezza cerchi e trasparenza
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

		//TODO: change textblock only when distance changes
		String textStr="";

		double d = distance;
		DecimalFormat df = new DecimalFormat("@#");
		if(d<1000.0) {
			textStr = getTitle() + " ("+ df.format(d) + "m)";			
		}
		else {
			d=d/1000.0;
			textStr = getTitle() + " (" + df.format(d) + "km)";
		}

		textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1,
				250, dw, isUnderline());

		if (isVisible) {

			//dw.setColor(DataSource.getColor(type));

			float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y, getSignMarker().x, getSignMarker().y);

			txtLab.prepare(textBlock);

			dw.setStrokeWidth(1f);
			dw.setFill(true);
			dw.paintObj(txtLab, getSignMarker().x - txtLab.getWidth()
					/ 2, getSignMarker().y + maxHeight, currentAngle + 90, 1);
		}

	}

	/**
	 * Handles Marker's click event.
	 * if Click was made within marker's boundaries, then click event is registered 
	 * to current view state.
	 * @return boolean true if matches boundaries, false otherwise 
	 */
	public boolean fClick(float x, float y, MixContextInterface ctx, MixStateInterface state) {
		boolean evtHandled = false;

		if (isClickValid(x, y)) {
			evtHandled = state.handleEvent(ctx, getURL());
		}
		return evtHandled;
	}

	/* ****** Getters / setters **********/
	
	/**
	 * Returns registered title (Marker title)
	 * @return String Marker's title
	 */
	public String getTitle(){
		return title;
	}

	/**
	 * Returns Marker's link url
	 * @return String Marker's URL
	 */
	public String getURL(){
		return URL;
	}

	/**
	 * Returns Marker's Latitude
	 * @return double Marker's Latitude
	 */
	public double getLatitude() {
		return getmGeoLoc().getLatitude();
	}

	/**
	 * Returns Marker's Longitude
	 * @return double Marker's Longitude
	 */
	public double getLongitude() {
		return getmGeoLoc().getLongitude();
	}

	/**
	 * Returns Marker's Altitude
	 * @return double Altitude
	 */
	public double getAltitude() {
		return getmGeoLoc().getAltitude();
	}

	/**
	 * Returns vector of user's location (and point of View)
	 * in term of x,y,z 
	 * @return MixVector location vector
	 */
	public MixVector getLocationVector() {
		return locationVector;
	}
	
	/**
	 * Get computed distance between user and marker
	 * @return double distance
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * Sets the distance between user and marker
	 * (In KM)
	 * @param distance double
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}

	/**
	 * Marker's id 
	 * @return String marker's id
	 */
	public String getID() {
		return ID;
	}
	
	/**
	 * Protected method that set's marker's id
	 * @param iD String
	 */
	protected void setID(String iD) {
		ID = iD;
	}

	/**
	 * Public method that compare the distance between markers.
	 * Comparsion is done in double, return in int
	 * @return int difference between marker's distance
	 */
	public int compareTo(Marker another) {

		Marker leftPm = this;
		Marker rightPm = another;

		return Double.compare(leftPm.getDistance(), rightPm.getDistance());

	}

	/**
	 * Checks if marker's are the same
	 * @return boolean true if marker's the same, false otherwise
	 */
	@Override
	public boolean equals (Object marker) {
		return this.ID.equals(((Marker) marker).getID());
	}
	
	/**
	 * Marker's id hashcode
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return this.ID.hashCode();
	}

	/**
	 * Returns true if marker was enabled, false otherwise
	 * @return boolean
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Set's Marker to be enabled
	 * @param active boolean
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Marker's Maximum displayed objects
	 */
	abstract public int getMaxObjects();
	
	/**
	 * @deprecated Local Marker's set their own drawings
	 * @param image Bitmap
	 */
	protected void setImage(Bitmap image){
	}
	
	/**
	 * @deprecated Local Marker's should handle their own drawing
	 * @return null
	 */
	protected Bitmap getImage(){
		return null;
	}

	/**
	 * Returns int color representation {@link android.graphics.Color#Color() android Colors}
	 * @return int color
	 */
	public int getColour() {
		return colour;
	}

	/**
	 * Set's text label of marker
	 * @param txtLab marker's text label
	 */
	@Override
	public void setTxtLab(Label txtLab) {
		this.txtLab = txtLab;
	}

	/**
	 * Get's marker label
	 * @return Label
	 */
	@Override
	public Label getTxtLab() {
		return txtLab;
	}
	
	//why not setExtras?
	public void setExtras(String name, PrimitiveProperty primitiveProperty){
		//nothing to add
	}

	public void setExtras(String name, ParcelableProperty parcelableProperty){
		//nothing to add
	}


	/**
	 * @param title String to set
	 */
	protected void setTitle(String title) {
		this.title = title;
	}


	/**
	 * @return boolean the underline
	 */
	protected boolean isUnderline() {
		return underline;
	}


	/**
	 * @param underline to set (boolean)
	 */
	protected void setUnderline(boolean underline) {
		this.underline = underline;
	}


	/**
	 * @param the uRL to set
	 */
	protected void setURL(String uRL) {
		URL = uRL;
	}


	/**
	 * @return the mGeoLoc
	 */
	protected PhysicalPlace getmGeoLoc() {
		return mGeoLoc;
	}


	/**
	 * @see PhysicalPlace
	 * @param the mGeoLoc to set (PhysicalPlace)
	 */
	protected void setmGeoLoc(PhysicalPlace mGeoLoc) {
		this.mGeoLoc = mGeoLoc;
	}


	/**
	 * @return the signMarker
	 */
	protected MixVector getSignMarker() {
		return signMarker;
	}


	/**
	 * @param signMarker the signMarker to set
	 */
	protected void setSignMarker(MixVector signMarker) {
		this.signMarker = signMarker;
	}
}

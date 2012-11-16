package com.onionuml.visplugin.ui.graphics.graphicalmodels;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import com.onionuml.visplugin.core.UmlRelationshipElement;

/**
 * Represents the model of a uml diagram relationship between two elements to
 * be rendered with the Eclipse Graphical Editing Framework (GEF).
 */
public class RelationshipElementGraphicalModel implements
		IElementGraphicalModel {
	
	private Point mPosition;
	private Dimension mSize;
	private UmlRelationshipElement mRelationship;
	private List<Point> mPoints = new ArrayList<Point>();
	
	/**
	 * Constructs a new RelationshipElementGraphicalModel from the given
	 * UmlRelationshipElement object.
	 * @param rel the UmlRelationshipElement object representing the relationship 
	 */
	public RelationshipElementGraphicalModel(UmlRelationshipElement rel){
		mRelationship = rel;
		mPosition = new Point();
		mSize = new Dimension();
	}
	
	/**
	 * Gets a reference to the underlying UmlRelationshipElement object.
	 */
	public UmlRelationshipElement getRelationshipElement(){
		return mRelationship;
	}
	
	/**
	 * Adds the specified point as a bend point in the relationship line.
	 */
	public void addBendPoint(Point p){
		mPoints.add(p);
	}
	
	/**
	 * Returns the list of bend points.
	 */
	public List<Point> getBendPoints(){
		return mPoints;
	}
	
	@Override
	public Point getPosition() {
		return mPosition;
	}

	@Override
	public Dimension getSize() {
		return mSize;
	}

	@Override
	public void setPosition(Point position) {
		mPosition = position;
	}

	@Override
	public void setSize(Dimension size) {
		mSize = size;
	}
}

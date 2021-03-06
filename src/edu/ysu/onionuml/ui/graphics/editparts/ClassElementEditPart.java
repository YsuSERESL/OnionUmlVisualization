package edu.ysu.onionuml.ui.graphics.editparts;

import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;

import edu.ysu.onionuml.Activator;
import edu.ysu.onionuml.core.RelationshipType;
import edu.ysu.onionuml.core.UmlAttribute;
import edu.ysu.onionuml.core.UmlClassElement;
import edu.ysu.onionuml.core.UmlOperation;
import edu.ysu.onionuml.preferences.PreferenceConstants;
import edu.ysu.onionuml.ui.graphics.figures.ClassFigure;
import edu.ysu.onionuml.ui.graphics.graphicalmodels.ClassElementGraphicalModel;

/**
 * Represents the view/controller of a uml class to be rendered with the Eclipse
 * Graphical Editing Framework (GEF).
 */
public class ClassElementEditPart extends AbstractGraphicalEditPart 
		implements MouseListener, MouseMotionListener{
	
	//private static final Color CLASS_COLOR = new Color(null, 255, 255, 206);
	private static final Font NAME_FONT = new Font(null, "Arial", 12, SWT.BOLD);
	private static final Font STEREOTYPE_FONT = new Font(null, "Arial", 10, SWT.NORMAL);
	private static final Font NAME_ABSTRACT_FONT = new Font(null, "Arial", 12, SWT.BOLD | SWT.ITALIC);
	private static final Font NORMAL_FONT = new Font(null, "Arial", 12, SWT.NORMAL);
	private static final Font ABSTRACT_FONT = new Font(null, "Arial", 12, SWT.ITALIC);
	private static final Font STATIC_FONT = new Font(null, "Arial", 12, SWT.UNDERLINE_SINGLE);
	
	
	private Point mDragLocation = null;
	
	@Override
	public void activate(){
		super.activate();
		addEditPartListener(new EditPartListener.Stub(){
			@Override
			public void selectedStateChanged(EditPart part){
				ClassElementEditPart c = (ClassElementEditPart)part;
				if(c.getSelected() != EditPart.SELECTED_NONE){
					((ClassDiagramEditPart)c.getParent()).addToSelection(c);
				}
				else{
					((ClassDiagramEditPart)c.getParent()).removeFromSelection(c);
				}
			}
		});
	}
	
	@Override
	protected IFigure createFigure() {		
		ClassElementGraphicalModel model = (ClassElementGraphicalModel) getModel();
		UmlClassElement classElement = model.getClassElement();
		String stereotype = classElement.getStereotype();
		Color classColor = getClassColorFromPreferences(stereotype);
		IFigure f = new ClassFigure(classColor, NAME_FONT, NORMAL_FONT, STEREOTYPE_FONT);
		f.addMouseListener(this);
		f.addMouseMotionListener(this);
		return f;
	}
	
	@Override
	public DragTracker getDragTracker(Request request){
		return super.getDragTracker(request);
	}
	
	@Override
	protected void createEditPolicies() {
	}
	
	@Override
	protected void refreshVisuals() {
		ClassElementGraphicalModel model = (ClassElementGraphicalModel) getModel();
		ClassFigure figure = (ClassFigure) getFigure();
		UmlClassElement classElement = model.getClassElement();
		
		if(!model.isVisible()){
			figure.setVisible(false);
		}
		else{
			figure.setVisible(true);
			
			if(model.isCompacted() && !model.getChildRelationships().isEmpty()){
				figure.setIsOnion(true);
				figure.clear();
				if(!model.getChildRelationships().isEmpty()){
					for(RelationshipType relationship : model.getChildRelationships()){
						figure.addOnionRelationship(relationship);
					}
				}
				else{
					figure.addOnionRelationship(null);
				}
			}
			else{
				figure.setIsOnion(false);
				figure.clear();
				
				if(classElement.getIsAbstract()){
					figure.setNameFont(NAME_ABSTRACT_FONT);
				}
				
				figure.setNameString(classElement.getName());
				
				String stereotype = classElement.getStereotype();
				//if the sterotype is set
				if(stereotype != null && stereotype.length() > 0){
					//check user preferences for stereotype display and set the stereotype
					IPreferenceStore store = Activator.getDefault().getPreferenceStore();				
					if (store.getBoolean(PreferenceConstants.P_SHOW_STEREOTYPES)) {
						figure.setStereotypeString("<<" + stereotype + ">>");
					}
				}
				
				// set the background color
				Color classColor = getClassColorFromPreferences(stereotype);
				figure.setBackgroundColor(classColor);
				
				IPreferenceStore store = Activator.getDefault().getPreferenceStore();               
				
				// setup properties (fields)
				if(classElement.getAttributes().size() == 0){
					figure.addProperty("", null, null);
				}
				
				else if (!store.getBoolean(PreferenceConstants.P_SHOW_FIELDS))
				{
					figure.addProperty("...", null, null);
				}
				
				else{
					for(UmlAttribute a : classElement.getAttributes()){
						figure.addProperty(a.toString(), null, null);
					}
				}
				
				// setup operations (methods)
				if(classElement.getOperations().size() == 0){
					figure.addOperation("", null, null);
				}
				
				else if (!store.getBoolean(PreferenceConstants.P_SHOW_METHODS))
				{
					figure.addOperation("...", null, null);
				}
				
				else{
					for(UmlOperation o : classElement.getOperations()){
						
						if(o.isAbstract){
							figure.addOperation(o.toString(), null, ABSTRACT_FONT);
						}
						else if(o.isStatic){
							figure.addOperation(o.toString(), null, STATIC_FONT);
						}
						else{
							figure.addOperation(o.toString(), null, null);
						}
					}
				}
			}
		}
		
		model.setSize(figure.getPreferredSize());
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, figure,
				new Rectangle(model.getPosition(), new Dimension(-1,-1)));
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		if(mDragLocation != null){
			Point location = me.getLocation();
			Dimension offset = location.getDifference(mDragLocation);
			if (offset.width != 0 || offset.height != 0){
				mDragLocation = location;
				
				ClassDiagramEditPart parent = ((ClassDiagramEditPart)getParent());
				Set<ClassElementEditPart> selected = parent.getSelectedClasses();
				
				// if multiple classes are selected, move entire selected at once
				if(selected.size() > 0){
					for(ClassElementEditPart c : selected){
						IFigure f = c.getFigure();
						Rectangle bounds = f.getBounds().getCopy();
						UpdateManager updateManager = f.getUpdateManager();
						LayoutManager layoutManager = f.getParent().getLayoutManager();
						updateManager.addDirtyRegion(f.getParent(), bounds);
						bounds.translate(offset.width, offset.height);
						f.translate(offset.width, offset.height);
						layoutManager.setConstraint(f, bounds);
						updateManager.addDirtyRegion(f.getParent(), bounds);
						ClassElementGraphicalModel model = (ClassElementGraphicalModel)c.getModel();
						model.setPosition(bounds.getLocation());
					}
				}
				// otherwise move only the selected class
				else{
					IFigure f = getFigure();
					Rectangle bounds = f.getBounds().getCopy();
					UpdateManager updateManager = f.getUpdateManager();
					LayoutManager layoutManager = f.getParent().getLayoutManager();
					updateManager.addDirtyRegion(f.getParent(), bounds);
					bounds.translate(offset.width, offset.height);
					f.translate(offset.width, offset.height);
					layoutManager.setConstraint(f, bounds);
					updateManager.addDirtyRegion(f.getParent(), bounds);
					ClassElementGraphicalModel model = (ClassElementGraphicalModel)getModel();
					model.setPosition(bounds.getLocation());
				}
				me.consume();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent me) {}

	@Override
	public void mouseExited(MouseEvent me) {}

	@Override
	public void mouseHover(MouseEvent me) {}

	@Override
	public void mouseMoved(MouseEvent me) {}

	@Override
	public void mousePressed(MouseEvent me) {
		mDragLocation = me.getLocation();
		me.consume();
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		if(mDragLocation != null){
			me.consume();
			mDragLocation = null;
		}
	}

	@Override
	public void mouseDoubleClicked(MouseEvent me) {}
	
	
	/* Private Methods **************************************************/
	
	/**
	 * Returns the appropriate class background color based on user preferences.
	 * 
	 * @param stereotype	the stereotype of the class
	 * @return				the background color for the class
	 */
	private Color getClassColorFromPreferences(String stereotype) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		RGB classRGB = new RGB(255, 255, 206);
		//if using stereotype colors, gets appropriate color for stereotype
		//returns default class color if stereotype isn't recognized
		if (store.getBoolean(PreferenceConstants.P_USE_STEREOTYPE_COLORS)) {
			if (stereotype.equalsIgnoreCase("boundary")) {
				classRGB = PreferenceConverter.getColor(store, PreferenceConstants.P_BOUNDARY_CLASS_COLOR);
			} else if (stereotype.equalsIgnoreCase("control")){
				classRGB = PreferenceConverter.getColor(store, PreferenceConstants.P_CONTROL_CLASS_COLOR);
			} else if (stereotype.equalsIgnoreCase("entity")){
				classRGB = PreferenceConverter.getColor(store, PreferenceConstants.P_ENTITY_CLASS_COLOR);
			} else {
				classRGB = PreferenceConverter.getColor(store, PreferenceConstants.P_CLASS_COLOR);
			}
		}
		//if not using stereotype colors, returns default class color for every class
		else {
			classRGB = PreferenceConverter.getColor(store, PreferenceConstants.P_CLASS_COLOR);
		}
		return new Color(null, classRGB);
	}
	
	
}

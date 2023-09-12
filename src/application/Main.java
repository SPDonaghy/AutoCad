package application;
/*
 * NOTES
 * 	
 */
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.util.*;
import javafx.scene.input.*;


public class Main extends Application {

	//Final Values
	private static final int HEIGHT = 700;
	private static final int WIDTH = 1000;
	private static final int STROKE_WIDTH = 2;
	private final int SNAP = 10;
	private static final Color BACKGROUND_COLOR = Color.BLACK;
	private static final Color LINE_COLOR = Color.WHITE;
	private static final Color SELECTED_COLOR = Color.LIGHTBLUE;


	//Elements
	private Scene scene;
	private VBox topPane;
	private HBox toolSelectionRow;
	private RadioButton line,box,circle,arc,select;
	private ToggleGroup toolSelection;
	private Pane drawingBoard;
	private Rectangle background;
	private Line vertical,horizontal;
	private BorderPane root;
	private Text dimension;
	
	//Object Lists
	private ArrayList<Shape> shapes;
	private ArrayList<DraftLine> lines;
	private ArrayList<Point> points;

	//Event Handlers
	private MousePressEventHandler mpeh = new MousePressEventHandler();
	private MouseDragEventHandler mdeh = new MouseDragEventHandler();
	private MouseReleaseEventHandler mreh = new MouseReleaseEventHandler();

	
	@Override
	public void start(Stage primaryStage) {
		
		try {
			
			//Tool selection pane
			Text toolsTitle = new Text("Tools");
			toolSelection = new ToggleGroup();
			
			//initialize tool selection radio buttons
			line = new RadioButton("Line");
			box = new RadioButton("Box");
			circle = new RadioButton("Circle");
			arc = new RadioButton("Arc");
			select = new RadioButton("Select");
			
			RadioButton[] tools = {line,box,circle,arc,select};
			
			for(RadioButton rB:tools) {
				
				rB.setToggleGroup(toolSelection);
				//add set on action later
			}
			//line is the selected tool by default
			line.setSelected(true);
			
			//collect together the tool selection radio buttons
			toolSelectionRow = new HBox(20,line,box,circle,arc,select);
			
			//format the tool selection radio buttons
			toolSelectionRow.setPadding(new Insets(10));
			toolSelectionRow.setAlignment(Pos.CENTER);
			
			//collect together the title and the tool selection buttons
			topPane = new VBox(toolsTitle,toolSelectionRow);
			
			//format the top pane
			topPane.setPadding(new Insets(10));
			topPane.setAlignment(Pos.CENTER);

			
			//DrawingBoard
			drawingBoard = new Pane();
			background = new Rectangle(0,0,WIDTH,HEIGHT);
			background.setFill(BACKGROUND_COLOR);

			//Axis gridlines
			vertical = new Line(WIDTH/2,0,WIDTH/2,HEIGHT);
			vertical.setStroke(Color.GRAY);
			horizontal = new Line(0,HEIGHT/2,WIDTH,HEIGHT/2);
			horizontal.setStroke(Color.GRAY);
			
			drawingBoard.getChildren().addAll(background,vertical,horizontal);

			//Event Handlers

			//Cursor will change from crosshair to deafault when the mouse moves into the top pane
			topPane.hoverProperty().addListener((observable) -> {
                
				scene.setCursor(Cursor.DEFAULT);
				
				//Restore any hovered over shapes to default stroke width
				for(Shape shape:shapes){
					if(shape.getStrokeWidth()>STROKE_WIDTH)
						shape.setStrokeWidth(STROKE_WIDTH);
				}

        	});

			//When the cursor moves off of a shape and onto the background, all highlighted lines should be unhighlighted
			//and the cursor restores to crosshair
			background.hoverProperty().addListener((observable) -> {
                
				scene.setCursor(Cursor.CROSSHAIR);
				
				//Restore any hovered over shapes to default stroke width
				for(Shape shape:shapes){
					if(shape.getStrokeWidth()>STROKE_WIDTH)
						shape.setStrokeWidth(STROKE_WIDTH);
				}

        	});
			
			drawingBoard.setOnMousePressed(mpeh);
			drawingBoard.setOnMouseDragged(mdeh);
			drawingBoard.setOnMouseReleased(mreh);
			
			//initialize the shapes and lines arrays
			shapes = new ArrayList<Shape>();
			lines = new ArrayList<DraftLine>();
			points = new ArrayList<Point>();
			
			root = new BorderPane();
			root.setTop(topPane);
			root.setCenter(drawingBoard);
		
			scene = new Scene(root,WIDTH,HEIGHT);
			
			//set cursor to crosshair
			//cursor will be a crosshair when mouse is on the drawing board (black section)
			scene.setCursor(Cursor.CROSSHAIR);

			primaryStage.setTitle("RadCAD");
			primaryStage.setScene(scene);
			primaryStage.show();
		} 
		
		catch(Exception e) {
			
			e.printStackTrace();
		}
	}
	//Inner Interfaces
	interface Highlightable{

		public abstract void highlight();

	}
	//Inner Classes
	/**
	 * The points class describe points in the drawing
	 * keeping track of the points is useful for snapping to them when drawing
	 * 
	 * a point has x and y coordinates
	 * getDistance() returns the distance between two points
	 * 
	 * @author Sean Donaghy
	 *
	 */
	public class Point extends Shape implements Highlightable{
		
		private static final int RADIUS = 2;
		private Circle pointCircle;
		private boolean isSelected;
		private double x,y;
		
		public Point(double x,double y) {
			
			this.x = x;
			this.y = y;
			
		}
		@Override
		public void highlight(){

		}
		public boolean isSelected() {
			return isSelected;
		}
		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
			if (isSelected) {
				this.pointCircle.setStroke(SELECTED_COLOR);
			}
			else{
				this.pointCircle.setStroke(LINE_COLOR);
			}
		}
		public double getY() {
			return this.y;
		}
		public void setY(double y) {
			this.y = y;
		}
		public double getX() {
			return this.x;
		}
		public void setX(double x) {
			this.x = x;
		}
		public void setCircle(double x,double y) {
			
			this.pointCircle = new Circle(x,y,RADIUS);
			this.pointCircle.setFill(LINE_COLOR);

		}
		public Circle getCircle() {
			return this.pointCircle;
		}
		public double getDistance(Point other){
			
			return Math.sqrt(Math.pow(this.x-other.getX(),2)+
						     Math.pow(this.y-other.getY(),2));
		}
	}
	public class DraftLine extends Line implements Highlightable{
		
		private Point start,end,mid;
		private double angle;
		private boolean isSelected;
		private Color color;

		public DraftLine(double startX, double startY, double endX, double endY){
			super(startX,startY,endX,endY);

			this.start = new Point(startX,startY);
			this.end = new Point(endX,endY);
			this.isSelected = false;
			this.setStroke(LINE_COLOR);
			this.setStrokeWidth(STROKE_WIDTH);
		}

		@Override
		public void highlight(){

		}

		public Point getStart() {
			return this.start;
		}

		public void setStart(Point start) {
			this.start = start;
		}

		public Point getEnd() {
			return this.end;
		}

		public void setEnd(Point end) {
			this.end = end;
		}

		public Point getMid() {
			return this.mid;
		}

		public void setMid(Point mid) {
			this.mid = mid;
		}

		public double getAngle() {
			return angle;
		}

		public void setAngle(double angle) {
			this.angle = angle;
		}

		public boolean isSelected() {
			return isSelected;
		}

		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
			if (isSelected) {
				this.setStroke(SELECTED_COLOR);
			}
			else{
				this.setStroke(LINE_COLOR);
			}
		}

		public Color getColor() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}
	}
	//Methods

	/**
	 * This method is used to add any shapes to the Array List shapes
	 * When a shape is added, an event listener is also added to the shape to check if the mouse is hovering over the shape
	 * 
	 * If the mouse is hovering over the shape, while in select mode, the cursor will change to a hand
	 * @param shapeArgs
	 */
	public void addToShapes(Shape ...shapeArgs){
		
		for(Shape shape:shapeArgs){

			shapes.add(shape);
			shape.hoverProperty().addListener((observable) -> {
                
				if(select.isSelected()){
					scene.setCursor(Cursor.HAND);
					shape.setStrokeWidth(STROKE_WIDTH*2);
				}	
        	});
		}
	}
	/**
	 * This method will round a double to the nearest 100th
	 * @param num
	 * @return
	 */
	public static double round(double num) {
		
		double temp = num;
		temp *= 100;
		temp = (int)temp;
		temp /= 100;
		
		return temp;
	}
	/**
	 * This method will deselect all drawing entities
	 */
	private void deselectAll(){

		//once more entities are implemented they should all be stored in a single array list of type Shape
		//deselect all Draft Lines
		for(DraftLine line:lines) {
			if(line.isSelected())
				line.setSelected(false);
		}
		//Deselect all points
		for(Point point:points){
			if(point.isSelected())
				point.setSelected(false);
		}
	}
	/**
	 * The MousePressEventHandler will excute multiple possible actions, depending on the tool selected
	 */
	public class MousePressEventHandler implements EventHandler<MouseEvent>{
		
		private DraftLine selectedLine;
		private Point selectedPoint;
		
		@Override
		public void handle(MouseEvent e) {
			
			boolean nearPoint = false;
			Point mousePos = new Point(e.getX(),e.getY());
			
			//by deafault the starting point of any shape being drawn should be the location of the mouse
			//when the Mouse Pressed Event is created
			Point start = mousePos;
			
			//if the cursor is close enough to a point on the drawing
			//the start point of the shape being drawn will snap to that point
			//the cursor should specifically snap to the point CLOSEST to the cursor AND within SNAP tolerance

			double pointDist = 1_000_000_000;
			
			for(int i=0;i<points.size();i++) {
				
				if(mousePos.getDistance(points.get(i))<SNAP&&(mousePos.getDistance(points.get(i)) < pointDist)) {
					
					start = points.get(i);
					//comparing to the pointDist variable ensures the line snaps to the nearest point within snapping distance
					pointDist = mousePos.getDistance(points.get(i));
					nearPoint = true;
				}

			//if no points are close enough to snap to
		    //but the cursor is near an axis
			//the cursor should snap to the axis
			
			//x-axis snap
			if(mousePos.getDistance(new Point (mousePos.getX(),HEIGHT/2))<SNAP&&!nearPoint) {
				
				start = new Point(mousePos.getX(),HEIGHT/2);
			}
			//Y-axis snap
			if(mousePos.getDistance(new Point (WIDTH/2,mousePos.getY()))<SNAP&&!nearPoint) {
							
				start = new Point(WIDTH/2,mousePos.getY());
			}
			
					
			}
			
			//Line Creation Tool
			if(line.isSelected()) {
				
				//this will create a line where both the start and end points are intitially where the mouse
				//click occurred or if the mouse was close enough to a point, the line will start at that point
				DraftLine draftLine = new DraftLine(start.getX(),start.getY(),start.getX(),start.getY());
				draftLine.setStart(start);
				
				//all lines are added to an Array list so that they can be changed
				lines.add(draftLine);

				addToShapes(draftLine);
				
				//Add a circle for the start point
				draftLine.getStart().setCircle(start.getX(),start.getY());
				addToShapes(draftLine.getStart().getCircle());
				
				//the user should be able to start or finish a line on top of a point(point circle)
				draftLine.getStart().getCircle().setOnMousePressed(mpeh);
				draftLine.getStart().getCircle().setOnMouseDragged(mdeh);
				draftLine.getStart().getCircle().setOnMouseReleased(mreh);
				
				//add the start point to the Array List points to keep track of all points
				points.add(draftLine.getStart());
				

				drawingBoard.getChildren().add(draftLine.getStart().getCircle());
			
				//add the line to the drawing
				drawingBoard.getChildren().add(draftLine);
				
				//add the line dimension to the drawing
				dimension = new Text("");
				dimension.setStroke(LINE_COLOR);
				drawingBoard.getChildren().add(dimension);
				
				//the user should also be able to start a new line when they have clicked on a previously drawn line
				draftLine.setOnMousePressed(mpeh);
				draftLine.setOnMouseDragged(mdeh);
				
				for(Shape s:shapes){
					s.setOnMousePressed(mpeh);
					s.setOnMouseDragged(mdeh);
					s.setOnMouseReleased(mreh);
					
				}
			}
			
			//Entity Selection Tool
			if(select.isSelected()) {
				
				if(e.getSource() instanceof DraftLine) {
					
					selectedLine = (DraftLine) e.getSource();
					selectedLine.setSelected(true);
					e.consume();
				}
				else if(e.getSource() instanceof Point){
					selectedPoint = (Point) e.getSource();
					selectedPoint.setSelected(true);
					e.consume();
				}


				else{
					deselectAll();
				}
				
			}

			else{
				deselectAll();
			}

		}
		
	}
	/**
	 * Mouse Dragged Event Handler
	 * This event handler class changes the dimensions of the shape being drawn
	 * It also facilitates snapping
	 * 
	 * Snapping Details:
	 * Line: snaps to vertical or horizontal depending on SNAP sensitivity value
	 * @author Sean Donaghy
	 *
	 */
	public class MouseDragEventHandler implements EventHandler<MouseEvent>{
		
		private static int dimPad = 15;
		private Line currentLine;
		private Point start,end;
		private double dimVal;
		@Override
		public void handle(MouseEvent e) {
			
			boolean nearPoint = false;
			if(line.isSelected()) {
				
				//the most recent line added to the drawing
				currentLine = lines.get(lines.size()-1);
				
				//start and end points for the line being drawn
				start = new Point(currentLine.getStartX(),currentLine.getStartY());
				
				//this code block will change the endpoint of the most recently added line to the line Array List
				
				//this line will check if the line being drawn is close enough to being vertical to snap to vertical
				//VERTICAL SNAPPING
				if(Math.abs(e.getX()-currentLine.getStartX())>SNAP) {
					
					//if the line is too far from being vertical, do not snap
					currentLine.setEndX(e.getX());
					
				}
				
				else {
					
					//if the line is close enough to vertical, snap to vertical
					currentLine.setEndX(currentLine.getStartX());
					
				}
				
				//HORIZONTAL SNAPPING
				if(Math.abs(e.getY()-currentLine.getStartY())>SNAP){
					
					currentLine.setEndY(e.getY());
				}
				
				else {
					
					//if the line is close enough to horizontal, snap to horizontal
					currentLine.setEndY(currentLine.getStartY());
				}
				
				end = new Point(currentLine.getEndX(),currentLine.getEndY());
				
				//This block of code is to check if the cursor is near any point that we can snap to
				double pointDist = 1_000_000_000;
				
				for(int i=0;i<points.size();i++) {
					
					if(end.getDistance(points.get(i))<SNAP&&(end.getDistance(points.get(i)) < pointDist)) {
						
						currentLine.setEndX(points.get(i).getX());
						currentLine.setEndY(points.get(i).getY());
						nearPoint = true;
			
					}
						
				}
				
				//this block should make the end point snap to an axis if no snap points are nearby
				//but it unintentionally causes the line to snap to an axis when it should be snapping to a point
				//x-axis snap
				if(end.getDistance(new Point (end.getX(),HEIGHT/2))<SNAP&&!nearPoint) {
					
					 currentLine.setEndY(HEIGHT/2);
				}
				//Y-axis snap
				if(end.getDistance(new Point (WIDTH/2,end.getY()))<SNAP&&!nearPoint) {
								
					currentLine.setEndX(WIDTH/2);
				}
				
				//declare the end point again in case it was changed
				end = new Point(currentLine.getEndX(),currentLine.getEndY());
				
				dimVal = start.getDistance(end);
				dimVal = round(dimVal);
				
				//set the position of the dimension value halfway along the line
				dimension.setLayoutX((start.getX()+end.getX())/2+dimPad);
				dimension.setLayoutY((start.getY()+end.getY())/2+dimPad);
				dimension.setText(""+dimVal);
				
				
			}
			
			
		}
		

			
		
	}
	/**
	 * Mouse Release Event Handler
	 * On mouse release drawn shape details are finalized
	 * @author Sean Donaghy
	 *
	 */
	public class MouseReleaseEventHandler implements EventHandler<MouseEvent>{
		
		private Point endPoint;
		private Circle endPointCircle;
		@Override
		public void handle(MouseEvent e) {
			
			if(line.isSelected()) {
				
				//we only want to add a point to the points Array once the mouse is 
				//released and the line drawing is complete
				Line currentLine = lines.get(lines.size()-1);
				
				endPoint = new Point(currentLine.getEndX(),currentLine.getEndY());
				endPoint.setCircle(endPoint.getX(),endPoint.getY());
				
				endPoint.getCircle().setOnMousePressed(mpeh);
				endPoint.getCircle().setOnMouseDragged(mdeh);
				endPoint.getCircle().setOnMouseReleased(mreh);
				
				points.add(endPoint);
				
				addToShapes(endPoint.getCircle());

				drawingBoard.getChildren().add(endPoint.getCircle());
				e.consume();
			}
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

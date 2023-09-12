package application;
/*
 * NOTES
 * the next step for this project is a complete restructure how everything works
 * I need to make a draftLine class that extends Line and each draft line will have 
 * 2 points, isSelected boolean, angle, length, and color	
 */
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.geometry.Insets;
import java.util.*;
import javafx.scene.input.*;


public class Main extends Application {
	private static int HEIGHT = 700;
	private static int WIDTH = 1000;
	
	
	private VBox topPane;
	private HBox toolSelectionRow;
	private RadioButton line,box,circle,arc,select;
	private ToggleGroup toolSelection;
	private Pane drawingBoard;
	private Rectangle background;
	private Line vertical,horizontal;
	private static Color backgroundColor = Color.BLACK;
	private BorderPane root;
	private Text dimension;
	private int SNAP = 10;
	
	private ArrayList<Shape> shapes;
	private ArrayList<Line> lines;
	private ArrayList<Point> points;
	
	@Override
	public void start(Stage primaryStage) {
		
		try {
			
			//build the tool selection pane
			Text toolsTitle = new Text("Tools");
			
			//create a toggle group for the tool select buttons
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
			
			//build the drawingBoard
			drawingBoard = new Pane();
			background = new Rectangle(0,0,WIDTH,HEIGHT);
			background.setFill(backgroundColor);
			
			drawingBoard.setOnMousePressed(new MousePressEventHandler());
			drawingBoard.setOnMouseDragged(new MouseDragEventHandler());
			drawingBoard.setOnMouseReleased(new MouseReleaseEventHandler());
			
			//create axis gridlines
			vertical = new Line(WIDTH/2,0,WIDTH/2,HEIGHT);
			vertical.setStroke(Color.GRAY);
			horizontal = new Line(0,HEIGHT/2,WIDTH,HEIGHT/2);
			horizontal.setStroke(Color.GRAY);
			
			drawingBoard.getChildren().addAll(background,vertical,horizontal);
			
			//initialize the shapes and lines arrays
			shapes = new ArrayList<Shape>();
			lines = new ArrayList<Line>();
			points = new ArrayList<Point>();
			
			root = new BorderPane();
			root.setTop(topPane);
			root.setCenter(drawingBoard);
		
			Scene scene = new Scene(root,WIDTH,HEIGHT);
			primaryStage.setTitle("RadCAD");
			primaryStage.setScene(scene);
			primaryStage.show();
		} 
		
		catch(Exception e) {
			
			e.printStackTrace();
		}
	}

	public class DraftLine extends Line {
		
		/*
		public DraftLine(double start, double end){

		}*/
	}
	/**
	 * The MousePressEventHandler will excute multiple possible actions, depending on the tool selected
	 */
	public class MousePressEventHandler implements EventHandler<MouseEvent>{
		
		private Line selectedLine;
		
		@Override
		public void handle(MouseEvent e) {
			
			//wherever the mouse is pressed, all lines should return to not selected
			//this really highlights the fact that a draftLine class is needed
			for(Line l:lines) {
			
			l.setFill(Color.WHITE);
			
			}
			
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
				Line drawLine = new Line(start.getX(),start.getY(),start.getX(),start.getY());
				drawLine.setStrokeWidth(2);
				drawLine.setStroke(Color.WHITE);
				//all lines are added to an Array list so that they can be changed
				lines.add(drawLine);
				
				//Add a circle for the start point
				start.setCircle(start.getX(),start.getY());
				
				//the user should be able to start or finish a line on top of a point(point circle)
				start.getCircle().setOnMousePressed(new MousePressEventHandler());
				start.getCircle().setOnMouseDragged(new MouseDragEventHandler());
				start.getCircle().setOnMouseReleased(new MouseReleaseEventHandler());
				
				//add the start point to the Array List points to keep track of all points
				points.add(start);
				drawingBoard.getChildren().add(start.getCircle());
			
				//add the line to the drawing
				drawingBoard.getChildren().add(drawLine);
				
				//add the line dimension to the drawing
				dimension = new Text("");
				dimension.setStroke(Color.WHITE);
				drawingBoard.getChildren().add(dimension);
				
				//the user should also be able to start a new line when they have clicked on a previously drawn line
				drawLine.setOnMousePressed(new MousePressEventHandler());
				drawLine.setOnMouseDragged(new MouseDragEventHandler());
				//drawLine.setOnMouseReleased(new MouseReleaseEventHandler());
			}
			
			//Entity Selection Tool
			//This doesn't work yet
			if(select.isSelected()) {
				
				if(e.getSource() instanceof Line) {
					
					selectedLine = (Line)e.getSource();
					selectedLine.setFill(Color.LIGHTBLUE);
					
				}
				
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
		
		public static double round(double num) {
			
			double temp = num;
			temp *= 100;
			temp = (int)temp;
			temp /= 100;
			
			return temp;
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
				
				endPoint.getCircle().setOnMousePressed(new MousePressEventHandler());
				endPoint.getCircle().setOnMouseDragged(new MouseDragEventHandler());
				endPoint.getCircle().setOnMouseReleased(new MouseReleaseEventHandler());
				
				points.add(endPoint);
				
				drawingBoard.getChildren().add(endPoint.getCircle());
				e.consume();
			}
		}
	}
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
	public class Point extends Shape{
		
		private static int radius = 2;
		private Circle pointCircle;
		private double x,y;
		
		public Point(double x,double y) {
			
			this.x = x;
			this.y = y;
			
		
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
			
			this.pointCircle = new Circle(x,y,radius);
			this.pointCircle.setFill(Color.WHITE);
		}
		public Circle getCircle() {
			return this.pointCircle;
		}
		public double getDistance(Point other){
			
			return Math.sqrt(Math.pow(this.x-other.getX(),2)+
						     Math.pow(this.y-other.getY(),2));
		}
		
		
	}
	public static void main(String[] args) {
		launch(args);
	}
}

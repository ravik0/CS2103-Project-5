package main;
import javafx.application.Application;
import java.util.*;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class ExpressionEditor extends Application {
	public static void main (String[] args) {
		launch(args);
	}

	/**
	 * Mouse event handler for the entire pane that constitutes the ExpressionEditor
	 */
	private static class MouseEventHandler implements EventHandler<MouseEvent> {
		final private Pane pane;
		private Node root;
		private ParsedExpression node;
		
		private ParsedExpression deepCopy;
		private Node deepCopyNode;
		
		private double _startSceneX;
		private double _startSceneY;
		
		private Map<Integer, Expression> otherPossibleConfigurations;
		private Map<Integer, Double> configPositions;
		
		private ParsedExpression originalExpression;
		
		private Expression nearest;
		MouseEventHandler (Pane pane_, CompoundExpression rootExpression_) {
			pane = pane_;
			root = rootExpression_.getNode();
			node = (ParsedExpression)rootExpression_;
			otherPossibleConfigurations = new HashMap<Integer, Expression>();
			configPositions = new HashMap<Integer, Double>();
			originalExpression = (ParsedExpression) rootExpression_;
		}

		public void handle (MouseEvent event) {
			if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				_startSceneX = event.getSceneX();
				_startSceneY = event.getSceneY();
				final List<Expression> children = node.getChildren();
				final Point2D mousePos = root.sceneToLocal(new Point2D(_startSceneX, _startSceneY));
				if(children.size() == 0) { //if no children and mouse pressed, reset focus
					root.setStyle("");
					root = originalExpression.getNode();
					node = originalExpression; 
				}
				
				for(int i = 0; i < children.size(); i++) {
					if(children.get(i).getNode().getBoundsInParent().contains(mousePos)) {
						//focus section, reset current focus style and refocus on the child.
						root.setStyle("");
						root = children.get(i).getNode();
						node = (ParsedExpression) children.get(i);
						root.setStyle("-fx-border-color: red;");	
						node.setExpressionColor(Paint.valueOf("gray"));
						
						//deep copy section, make a deepcopy, set its position, and show it
						deepCopy = (ParsedExpression) node.deepCopy();
						deepCopyNode = deepCopy.getNode();
						deepCopyNode.setLayoutX(root.localToScene(0,0).getX());
						deepCopyNode.setLayoutY(root.localToScene(0,0).getY()-25);
						pane.getChildren().add(deepCopyNode);
						
						//make the other possible configurations
						otherPossibleConfigurations = node.getOtherPossibleConfigurations();
						for(int a = 0; a < ((ParsedExpression)node.getParent()).getChildren().size(); a++) {
							otherPossibleConfigurations.get(a).getNode().setLayoutY(5000);
							otherPossibleConfigurations.get(a).getNode().setLayoutX(WINDOW_WIDTH/4);
							//add the other configs very far offscreen but in the correct x position such that we know the x position of the focuses.
							pane.getChildren().add(otherPossibleConfigurations.get(a).getNode());
							//this was necessary, attempting to do it in ParsedExpression lead to 3 hours of work with nothing accomplished
							//the positions did not seem to get initialized for rootExpression until a mouse click so this was the best comprimise
							//could've attempted to emulate this inside of parsedExpression but that is just so much more work.
						}
						
						break;
					}
					else if (i == children.size()-1) {
						root.setStyle("");
						root = originalExpression.getNode();
						node = originalExpression;
						//if we don't find it, root is now the originalExpression
					}
				}
			} 
			else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && !node.equals(originalExpression)) { //if we drag and node is NOT the original expression
				//move the deepcopy
				root.setStyle("");
				deepCopyNode.setTranslateX(event.getSceneX()-_startSceneX);
				deepCopyNode.setTranslateY(event.getSceneY()-_startSceneY);
				
				//if we don't have positions for all other configurations, we make that
				if(configPositions.isEmpty()) {
					for(int i = 0; i < ((ParsedExpression)node.getParent()).getChildren().size(); i++) {
						final Bounds theBounds = ((ParsedExpression)otherPossibleConfigurations.get(i)).getChildren().get(i).getNode().getBoundsInParent();
						configPositions.put(i, theBounds.getWidth()/2+theBounds.getMinX());
					}
				}
				
				//we find the x position we're closed to
				final int index = findNearestX(node.getParent().getNode().sceneToLocal(event.getSceneX(), event.getSceneY()).getX());
				pane.getChildren().remove(originalExpression.getNode());
				nearest = otherPossibleConfigurations.get(index);
				if(((ParsedExpression) node.getParent()).hasParent()) { //if the parent has a parent (i.e the parent is not the original expression
					final List<Integer> path = find(originalExpression, (ParsedExpression) node, new ArrayList<Integer>()); 
					final ParsedExpression temp = goDownList(originalExpression, path);
					((ParsedExpression)temp).convertTo((ParsedExpression) nearest);
					//we traverse down the tree until we find the node we are selecting
					//we then find the index of the other config we want to use
					//we then convert the current node to that new configuration and reform the originalexpression node.
					originalExpression.convertTo(ParsedExpression.getOriginal(temp));
				}
				else {
					originalExpression.convertTo((ParsedExpression) nearest);
					//otherwise nearest is just what we want so we do that and reform the node
				}
				originalExpression.reformNode();
				originalExpression.getNode().setLayoutX(WINDOW_WIDTH/4); //reset the layout
				originalExpression.getNode().setLayoutY(WINDOW_HEIGHT/2);
				pane.getChildren().add(originalExpression.getNode());
			} 
			else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
				pane.getChildren().remove(deepCopyNode);
				node.setExpressionColor(Paint.valueOf("black"));
				configPositions.clear();
				System.out.println(originalExpression.convertToString(0));
			}
		}
		
		/**
		 * Helper function to find the nearest x position to our mouseX in the other possible configurations
		 * @param mouseX the current mouse position
		 * @return the index of the nearest other possible configuration
		 */
		private int findNearestX(double mouseX) {
			int nearest = 0;
			for(int i = 0; i < configPositions.size(); i++) {
				if(Math.abs(mouseX-configPositions.get(i)) < Math.abs(mouseX-configPositions.get(nearest))) {
					nearest = i;
				}
				otherPossibleConfigurations.get(i).getNode().setLayoutY(5000);
			}
			return nearest;
		}
		
		/**
		 * An implementation of a tree-search using depth-first search. It finds the index-path to follow to find the target
		 * @param toSearch the expression to search
		 * @param target what we want to find
		 * @param path the current path
		 * @return the path if we found it, null otherwise
		 */
		private List<Integer> find(ParsedExpression toSearch, ParsedExpression target, List<Integer> path) {
			if(toSearch.equals(target)) {
				return path;
			}
			for(int i = 0; i < toSearch.getChildren().size(); i++) {
				List<Integer> newPath = listCopy(path);
				newPath.add(i);
				List<Integer> possiblePath = find((ParsedExpression)toSearch.getChildren().get(i), target, newPath);
				if(possiblePath != null) {
					return possiblePath;
				}
			}
			return null;
		}
		
		/**
		 * A function that will go down a given index-path list (gotten from the find function) and will return the parent of the expression we wanted to find
		 * @param target the thing we are going down on
		 * @param path the path we are going to follow
		 * @return the parent of what we are searching for, such that what we are searching for is in the children
		 */
		private ParsedExpression goDownList(ParsedExpression target, List<Integer> path) {
			if(path.isEmpty()) return (ParsedExpression) target.getParent();
			List<Integer> newPath = listCopy(path);
			newPath.remove(0);
			return (goDownList((ParsedExpression) target.getChildren().get(path.get(0)), newPath));
		}
		
		/**
		 * A function to copy a list of integers into a new list of integers, keeping the same object pointers within but with a new list pointer
		 * @param toCopy the list to copy
		 * @return a new list containing the same integers in the same order but with a new list pointer.
		 */
		private List<Integer> listCopy(List<Integer> toCopy) {
			List<Integer> ret = new ArrayList<Integer>();
			for(int i = 0; i < toCopy.size(); i++) {
				ret.add(toCopy.get(i));
			}
			return ret;
		}
	}

	/**
	 * Size of the GUI
	 */
	private static final int WINDOW_WIDTH = 500, WINDOW_HEIGHT = 250;

	/**
	 * Initial expression shown in the textbox
	 */
	private static final String EXAMPLE_EXPRESSION = "2*x+3*y+4*z+(7+6*z)";

	/**
	 * Parser used for parsing expressions.
	 */
	private final ExpressionParser expressionParser = new SimpleExpressionParser();

	@Override
	public void start (Stage primaryStage) {
		primaryStage.setTitle("Expression Editor");

		// Add the textbox and Parser button
		final Pane queryPane = new HBox();
		final TextField textField = new TextField(EXAMPLE_EXPRESSION);
		final Button button = new Button("Parse");
		queryPane.getChildren().add(textField);

		final Pane expressionPane = new Pane();
		// Add the callback to handle when the Parse button is pressed	
		button.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle (MouseEvent e) {
				// Try to parse the expression
				try {
					// Success! Add the expression's Node to the expressionPane
					final Expression expression = expressionParser.parse(textField.getText(), true);
					System.out.println(expression.convertToString(0));
					expressionPane.getChildren().clear();
					expressionPane.getChildren().add(expression.getNode());
					expression.getNode().setLayoutX(WINDOW_WIDTH/4);
					expression.getNode().setLayoutY(WINDOW_HEIGHT/2);
					expressionPane.setStyle("-fx-font: 15 \"Comic Sans MS\";"); //set font to best font

					// If the parsed expression is a CompoundExpression, then register some callbacks
					if (!((ParsedExpression)expression).isLiteral()) {
						((Pane) expression.getNode()).setBorder(Expression.NO_BORDER);
						final MouseEventHandler eventHandler = new MouseEventHandler(expressionPane, (CompoundExpression) expression);
						expressionPane.setOnMousePressed(eventHandler);
						expressionPane.setOnMouseDragged(eventHandler);
						expressionPane.setOnMouseReleased(eventHandler);
					}
				} catch (ExpressionParseException epe) {
					// If we can't parse the expression, then mark it in red
					textField.setStyle("-fx-text-fill: red");
				}
			}
		});
		queryPane.getChildren().add(button);

		// Reset the color to black whenever the user presses a key
		textField.setOnKeyPressed(e -> textField.setStyle("-fx-text-fill: black"));
		
		final BorderPane root = new BorderPane();
		root.setTop(queryPane);
		root.setCenter(expressionPane);

		primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
		primaryStage.show();
	}
}

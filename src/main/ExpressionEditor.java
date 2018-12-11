package main;
import javafx.application.Application;
import java.util.*;

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
	private static ParsedExpression originalExpression;
	public static void main (String[] args) {
		launch(args);
	}

	/**
	 * Mouse event handler for the entire pane that constitutes the ExpressionEditor
	 */
	private static class MouseEventHandler implements EventHandler<MouseEvent> {
		private Pane pane;
		private Node root;
		private ParsedExpression node;
		
		private ParsedExpression deepCopy;
		private Node deepCopyNode;
		
		private double _startSceneX;
		private double _startSceneY;
		MouseEventHandler (Pane pane_, CompoundExpression rootExpression_) {
			pane = pane_;
			root = rootExpression_.getNode();
			node = (ParsedExpression)rootExpression_;
		}

		public void handle (MouseEvent event) {
			System.out.println(node.getNode().getBoundsInParent());
			System.out.println(((ParsedExpression) node).getChildren().get(1).getNode().getBoundsInParent());
			if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				_startSceneX = event.getSceneX();
				_startSceneY = event.getSceneY();
				List<Expression> children = node.getChildren();
				Point2D mousePos = root.sceneToLocal(new Point2D(_startSceneX, _startSceneY));
				if(children.size() == 0) {
					root.setStyle("");
					root = originalExpression.getNode();
					node = originalExpression;
				}
				for(int i = 0; i < children.size(); i++) {
					if(children.get(i).getNode().getBoundsInParent().contains(mousePos)) {
						root.setStyle("");
						root = children.get(i).getNode();
						node = (ParsedExpression) children.get(i);
						root.setStyle("-fx-border-color: red;");	
						node.setExpressionColor(Paint.valueOf("gray"));
						node.findXPositions(root.getLayoutX(), root.getTranslateX(), root, pane);
						deepCopy = (ParsedExpression) node.deepCopy();
						deepCopyNode = deepCopy.getNode();
						deepCopyNode.setLayoutX(root.localToScene(0,0).getX());
						deepCopyNode.setLayoutY(root.localToScene(0,0).getY()-25);
						pane.getChildren().add(deepCopyNode);
						
						break;
					}
					else if (i == children.size()-1) {
						root.setStyle("");
						root = originalExpression.getNode();
						node = originalExpression;
					}
				}
			} 
			else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && node.hasParent()) {
				root.setStyle("");
				deepCopyNode.setTranslateX(event.getSceneX()-_startSceneX);
				deepCopyNode.setTranslateY(event.getSceneY()-_startSceneY);
			} 
			else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
				root.setLayoutX(root.getLayoutX() + root.getTranslateX());
				root.setLayoutY(root.getLayoutY() + root.getTranslateY());
				root.setTranslateX(0);
				root.setTranslateY(0);
				pane.getChildren().remove(deepCopyNode);
				node.setExpressionColor(Paint.valueOf("black"));
				//System.out.println(originalExpression.convertToString(0));
			}
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
					originalExpression = (ParsedExpression) expression;
					expressionPane.getChildren().clear();
					expressionPane.getChildren().add(expression.getNode());
					expression.getNode().setLayoutX(WINDOW_WIDTH/4);
					expression.getNode().setLayoutY(WINDOW_HEIGHT/2);

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

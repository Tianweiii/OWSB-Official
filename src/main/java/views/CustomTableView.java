package views;

import controllers.CustomTableViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import models.ModelInitializable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class CustomTableView implements View {
	protected static CustomTableViewController controller;
	private final AnchorPane rootPane;
	private static String[] columns;
	private static Class<? extends ModelInitializable> model;
	private static final ArrayList<Class<? extends ModelInitializable>> joins = new ArrayList<>();
	private static final ArrayList<String> joinKeys = new ArrayList<>();

	public CustomTableView(String[] columns, Class<? extends ModelInitializable> model, Class<? extends ModelInitializable>[] joins, String[] joinKey) throws IOException {
		CustomTableView.columns = columns;
		CustomTableView.model = model;
		for (Class<? extends ModelInitializable> join : joins) {
			if (join != null && joinKey != null) {
				CustomTableView.joins.add(join);
				CustomTableView.joinKeys.add(joinKey[CustomTableView.joins.indexOf(join)]);
			}
		}

		FXMLLoader loader = new FXMLLoader(new URL("file:src/main/resources/Components/CustomTableView.fxml"));
		this.rootPane = loader.load();

		CustomTableView.controller = loader.getController();
	}

	public static void setCommand(Command command) {
		CustomTableViewController.setCommand(command);
	}

	public static void setJoins(Class<? extends ModelInitializable> join, String joinKey) {
		CustomTableView.joins.add(join);
		CustomTableView.joinKeys.add(joinKey);
	}

	public static ArrayList<Class<? extends ModelInitializable>> getJoins() {
		return CustomTableView.joins;
	}

	public static ArrayList<String> getJoinKeys() {
		return CustomTableView.joinKeys;
	}

	public static String[] getColumns() {
		return CustomTableView.columns;
	}

	public static Class<? extends ModelInitializable> getModel() {
		return CustomTableView.model;
	}

	@Override
	public Parent getView() {
		return this.rootPane;
	}
}

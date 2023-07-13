package UWF.ACPProject5;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MusicClient extends Application {

	private static final String titleTxt = "Musical Instrument Lookup";

	public static void main(String[] args) {

		Application.launch(args);
	}

	ChoiceBox<String> instruments;
	ChoiceBox<String> instBrand;
	ChoiceBox<String> wareHouse;
	TextField cost;

	@Override
	public void start(Stage primaryStage) {

		primaryStage.setTitle(titleTxt);

		// Window label
		instruments = new ChoiceBox<String>();
		Label instLabel = new Label("Instrument Type: ");
		instruments.getItems().addAll("all", "guitar", "bass", "keyboard", "drums");
		instruments.getSelectionModel().select(0);
		HBox instrumentsHb = new HBox();
		instrumentsHb.setAlignment(Pos.CENTER);
		instrumentsHb.getChildren().addAll(instLabel, instruments);

		instruments.setOnAction(e -> instrumentSelect(instruments));

		instBrand = new ChoiceBox<String>();
		Label brandLabel = new Label("Instrument Brand: ");
		instBrand.getItems().addAll("all", "gibson", "fender", "roland", "alesis", "ludwig", "pearl", "yamaha");
		instBrand.getSelectionModel().select(0);
		HBox brandHb = new HBox();
		brandHb.setAlignment(Pos.CENTER);
		brandHb.getChildren().addAll(brandLabel, instBrand);

		cost = new TextField();
		cost.setFont(Font.font("Calibri", FontWeight.NORMAL, 12));
		Label costLabel = new Label("Maximum cost: ");
		HBox costHb = new HBox();
		costHb.setAlignment(Pos.CENTER);
		costHb.getChildren().addAll(costLabel, cost);

		wareHouse = new ChoiceBox<String>();
		Label wareLabel = new Label("Warehouse Location: ");
		wareHouse.getItems().addAll("all", "PNS", "CLT", "DFW");
		wareHouse.getSelectionModel().select(0);
		HBox warehouseHb = new HBox();
		warehouseHb.setAlignment(Pos.CENTER);
		warehouseHb.getChildren().addAll(wareLabel, wareHouse);

		// Button
		Button textbtn = new Button("Submit Request");
		textbtn.setOnAction(new TextButtonListener());
		HBox buttonHb = new HBox(10);
		buttonHb.setAlignment(Pos.CENTER);

		Button clear = new Button("Clear Results");
		EventHandler<ActionEvent> event2 = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				instruments.getSelectionModel().clearAndSelect(0);
				instBrand.getSelectionModel().clearAndSelect(0);
				wareHouse.getSelectionModel().clearAndSelect(0);;
				;
				cost.clear();
			}
		};
		clear.setOnAction(event2);

		buttonHb.getChildren().addAll(textbtn, clear);

		// Vbox
		VBox vbox = new VBox(30);
		vbox.setPadding(new Insets(25, 25, 25, 25));
		vbox.getChildren().addAll(instrumentsHb, brandHb, costHb, warehouseHb, buttonHb);

		// Scene
		Scene scene = new Scene(vbox, 500, 350); // w x h
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public void instrumentSelect(ChoiceBox<String> i) {
		System.out.println("instrument selected");
		setBrands();
	}

	public void setBrands() {
		instBrand.getItems().clear();
		String st = instruments.getValue();
		System.out.println("brand selected. st = " + st);
		if (st != null) {
			if (st.equals("guitar"))
				instBrand.getItems().addAll("all", "yamaha", "gibson");
			else if (st.equals("keyboard"))
				instBrand.getItems().addAll("all", "roland", "alesis");
			else if (st.equals("bass"))
				instBrand.getItems().addAll("all", "fender");
			else if (st.equals("drums"))
				instBrand.getItems().addAll("all", "ludwig", "pearl");
			else {
				System.out.println("else of brand selected");
				instBrand.getItems().addAll("all", "gibson", "fender", "roland", "alesis", "ludwig", "pearl", "yamaha");
			}
		}
		instBrand.getSelectionModel().select(0);
	}

	private void sendRequest() {

		String type = instruments.getValue();
		String brand = instBrand.getValue();
		String theCost = cost.getText();
		if (theCost.length() == 0)
			theCost = "0";
		String ware = wareHouse.getValue();
		String request = type + " " + brand + " " + theCost + " " + ware + "\n";
		System.out.println("[" + request + "]");
		Socket s = null;
		Scanner in = null;
		try {

			s = new Socket("localhost", 8889);
			InputStream instream = s.getInputStream();
			OutputStream outstream = s.getOutputStream();
			in = new Scanner(instream);
			PrintWriter out = new PrintWriter(outstream);

			out.print(request);
			out.flush();
			String response = "";
			int lines = 0;
			while (in.hasNext()) {
				response += in.nextLine();
				response += "\n";
				lines++;
			}
			System.out.println(response);

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle(titleTxt);
			alert.setResizable(true);
			alert.getDialogPane().setPrefSize(500, 150 + 20 * lines);
			alert.setHeaderText("Results");
			alert.setContentText(response);
			alert.show();

		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
		finally {
			if(in != null) {
				in.close();
			}
		}
	}

	private class TextButtonListener implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {
			sendRequest();
		}
	}
}
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages creating, updating, reading, and deleting a database table
 * 
 * @author Developer
 * @version 2.0
 */
public class DatabaseManager {
	/**
	 * url of the database
	 */
	private static String url;
	/**
	 * username for the database
	 */
	private static String username;
	/**
	 * password for the database
	 */
	private static String password;
	/**
	 * connection for the database
	 */
	private static Connection conn;

	/**
	 * path to the database configuration file
	 */
	private static final String CONFIG_FILE = "database.properties";

	/**
	 * Enumeration of different possible statuses for database events
	 */
	private static enum Statuses {
		PENDING, SUCCESS, FAILURE
	}

	/**
	 * @throws SQLException
	 */
	public DatabaseManager() throws SQLException {
		init(CONFIG_FILE);
		
		openConnection();
		dropTable("INSTRUMENTS");
		openConnection();
		dropTable("LOCATIONS");
		openConnection();
		dropTable("INVENTORY");
		closeConnection();
		
		openConnection();
		Statement stat = null;
		try {
			stat = conn.createStatement();
		} catch (SQLException e) {
			System.out.println(e.getLocalizedMessage() + "ERROR" + Statuses.FAILURE.toString());
		}
		try {
			createInstruments(stat);
			createInventory(stat);
			createLocations(stat);

			queryDatabase("all", "all", "500", "PNS");

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage() + "ERROR" + Statuses.FAILURE.toString());
		}

	}

	/**
	 * Initializes the data source.
	 * 
	 * @param fileName the name of the property file that contains the database
	 *                 driver, URL, username, and password
	 * @throws SQLException
	 */
	public void init(String fileName) {
		Properties props = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			System.out.println(e.getLocalizedMessage() + "ERROR" + Statuses.FAILURE.toString());
		}
		try {
			props.load(in);
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage() + "ERROR" + Statuses.FAILURE.toString());
		}

		String driver = props.getProperty("jdbc.driver");
		url = props.getProperty("jdbc.url");
		username = props.getProperty("jdbc.username");
		if (username == null)
			username = "";
		password = props.getProperty("jdbc.password");
		if (password == null)
			password = "";
		if (driver != null)
			try {
				Class.forName(driver);
			} catch (ClassNotFoundException e) {
				System.out.println(e.getLocalizedMessage() + "ERROR" + Statuses.FAILURE.toString());
			}
	}

	/**
	 * opens the connection to the database
	 */
	public void openConnection() {
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			System.out.println(e.getLocalizedMessage() + "ERROR" + Statuses.FAILURE.toString());
		}
	}

	/**
	 * closes the database connection
	 */
	public void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getLocalizedMessage() + "ERROR" + Statuses.FAILURE.toString());
		}
	}

	public void createInstruments(Statement stat) {

		try {
			stat.execute("CREATE TABLE Instruments (instName CHAR(12),instNumber INTEGER,cost DOUBLE,brand CHAR(20))");
			stat.execute("INSERT INTO Instruments VALUES ('guitar',1,100.0,'yamaha')");
			stat.execute("INSERT INTO Instruments VALUES ('guitar',2,500.0,'gibson')");
			stat.execute("INSERT INTO Instruments VALUES ('bass',3,250.0,'fender')");
			stat.execute("INSERT INTO Instruments VALUES ('keyboard',4,600.0,'roland')");
			stat.execute("INSERT INTO Instruments VALUES ('keyboard',5,500.0,'alesis')");
			stat.execute("INSERT INTO Instruments VALUES ('drums',6,1500.0,'ludwig')");
			stat.execute("INSERT INTO Instruments VALUES ('drums',7,400.0,'yamaha')");

		} catch (SQLException e) {
			System.out.println(e.getLocalizedMessage() + "ERROR" + Statuses.FAILURE.toString());
		}

	}

	public void createLocations(Statement stat) throws Exception {
		stat.execute("CREATE TABLE Locations (locName CHAR(12),locNumber INTEGER,address CHAR(50))");
		stat.execute("INSERT INTO Locations VALUES ('PNS',1,'Pensacola Florida')");
		stat.execute("INSERT INTO Locations VALUES ('CLT',2,'Charlotte North Carolina')");
		stat.execute("INSERT INTO Locations VALUES ('DFW',3,'Dallas Fort Worth Texas')");

	}

	public void createInventory(Statement stat) throws Exception {
		stat.execute("CREATE TABLE Inventory (iNumber INTEGER,lNumber INTEGER,quantity INTEGER)");
		stat.execute("INSERT INTO Inventory VALUES (1,1,15)"); // how many ? 15, where? 1, what instrument? 1
		stat.execute("INSERT INTO Inventory VALUES (1,2,27)");
		stat.execute("INSERT INTO Inventory VALUES (1,3,20)");
		stat.execute("INSERT INTO Inventory VALUES (2,1,10)");
		stat.execute("INSERT INTO Inventory VALUES (2,2,10)");
		stat.execute("INSERT INTO Inventory VALUES (2,3,35)");
		stat.execute("INSERT INTO Inventory VALUES (3,1,45)");
		stat.execute("INSERT INTO Inventory VALUES (3,2,10)");
		stat.execute("INSERT INTO Inventory VALUES (3,3,17)");
		stat.execute("INSERT INTO Inventory VALUES (4,1,28)");
		stat.execute("INSERT INTO Inventory VALUES (4,2,10)");
		stat.execute("INSERT INTO Inventory VALUES (4,3,16)");
		stat.execute("INSERT INTO Inventory VALUES (5,1,28)");
		stat.execute("INSERT INTO Inventory VALUES (5,2,10)");
		stat.execute("INSERT INTO Inventory VALUES (5,3,1)");
		stat.execute("INSERT INTO Inventory VALUES (6,1,2)");
		stat.execute("INSERT INTO Inventory VALUES (6,2,10)");
		stat.execute("INSERT INTO Inventory VALUES (6,3,16)");
		stat.execute("INSERT INTO Inventory VALUES (7,1,16)");
		stat.execute("INSERT INTO Inventory VALUES (7,2,4)");
		stat.execute("INSERT INTO Inventory VALUES (7,3,12)");

	}

	public String queryDatabase(String instrumentName, String brand, String cost, String location) {
		String results = "";
		// if loc is all, return results from all locations
		// if cost is 0, return results with all price ranges
		// if brand is all, return all brands
		// if instrument is all, return all instrument types

		try {
			String queryString = "SELECT ins.instName, ins.brand, ins.cost, inv.quantity, loc.address FROM instruments ins, inventory inv, locations loc WHERE ins.instNumber = inv.iNumber AND inv.lNumber = loc.locNumber";
			if (!instrumentName.equalsIgnoreCase("all") && instrumentName != null) {
				queryString += " AND ins.instName = " + "'" + instrumentName + "'";
			}

			if (!brand.equalsIgnoreCase("all") && brand != null) {
				queryString += " AND ins.brand = " + "'" + brand + "'";
			}

			if (!cost.equalsIgnoreCase("0") && cost != null) {
				queryString += " AND ins.cost < " + cost;
			}

			if (!location.equalsIgnoreCase("all") && location != null) {
				queryString += " AND loc.locName = " + "'" + location + "'";
			}
			openConnection();
			PreparedStatement queryStatement = conn.prepareStatement(queryString);

			ResultSet rs = queryStatement.executeQuery();
			results = parseQueryResults(rs);
		} catch (SQLException e) {
			System.out.println(e.getLocalizedMessage() + "ERROR" + Statuses.FAILURE.toString());
		}
		closeConnection();
		return results;

	}

	/**
	 * Drop the table from the database
	 * 
	 * @throws SQLException
	 */
	public void dropTable(String tableName) throws SQLException {
		if (tableExists(tableName)) {
			String deleteCommand = String.format("DROP TABLE %s", tableName);
			try {
				openConnection();
				PreparedStatement deleteStatement = conn.prepareStatement(deleteCommand);
				deleteStatement.execute();
			} catch (SQLException e) {
				System.err.println(e.getMessage() + "DROP TABLE" + Statuses.FAILURE.toString());
			} finally {
				closeConnection();
			}
		}
	}

	/**
	 * check if the table has been created yet
	 * 
	 * @return true - if the table exists <br />
	 *         false - if the table does not exits
	 */
	public boolean tableExists(String tableName) throws SQLException {
		ResultSet tables = null;
		tables = conn.getMetaData().getTables(null, null, tableName, null);
		return tables.next();
	}

	public String parseQueryResults(ResultSet queryResults) {
		String resultString = "";
		try {
			if (queryResults != null) {
				while (queryResults.next()) {
					ResultSetMetaData rsm = queryResults.getMetaData();
					int cols = rsm.getColumnCount();
					for (int i = 1; i <= cols; i++)
						resultString += String.format("%-12s", queryResults.getString(i));
					resultString += "\n";
				}
			}
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage() + "ERROR" + Statuses.FAILURE.toString());
		}
		return resultString;
	}
}

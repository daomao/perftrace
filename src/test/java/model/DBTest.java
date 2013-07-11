package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Ignore;

@Ignore
public class DBTest {

	private String user = "jmonitor";
	private String pwd = "jmonitor";
	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@10.6.36.194:1521:ora10g";

	private Connection conn;
	private Statement stmt;

	public void getConnection(String sql) throws ClassNotFoundException,
			SQLException {
		Class.forName(driver);
		conn = DriverManager.getConnection(url, user, pwd);
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next()) {
			String a = rs.getString(1);
			String b = rs.getString(2);
			String c = rs.getString(3);
			System.out.println(a);
		}
		conn.close();
		stmt.close();
	}

	public void getConnection2() throws ClassNotFoundException, SQLException {
		Class.forName(driver);
		conn = DriverManager.getConnection(url, user, pwd);
		PreparedStatement ps = conn
		.prepareStatement("select * from t_jmonitor_perf_except_raw_log t where t.id = ?");
		ps.setInt(1, 1101);
		ps.executeQuery();
	
		conn.close();
		ps.close();
	}

	public static void main(String[] args) throws Exception, SQLException {
		String sql = "select * from t_jmonitor_perf_except_raw_log t";
		DBTest ts = new DBTest();
		ts.getConnection(sql);
		ts.getConnection2();
	}
}

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.sql.Statement;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import oracle.sql.STRUCT;
import oracle.spatial.geometry.JGeometry;

public class ExtraCredit extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static JCheckBox checkbox;
	private Container contentPane;
	private JPanel panel;
	private Paint draw;
	private boolean ifCheck = false;
	private ConnectDatabaseAndQuery con;
	private ArrayList<Lion> lions;
	private ArrayList<Pond> ponds;
	private ArrayList<Region> regions;
	

	/*
	 * Construction function.
	 *
	 * This function implement the panel initiation and repaint.
	 * @checkbox.addItemListener(): listen if the user select the choose.
	 * @addMouseListener(): listen if the user click the region.
	 * @checkbox.addActionListener(): if the checkbox is not selected, then repaint the initial elements and colors.
	*/

	public ExtraCredit(String str) {
		super(str);
		contentPane = getContentPane();
		checkbox = new JCheckBox("show lions and ponds in selected region");
		panel = new JPanel();
		panel.add(checkbox);
		con = new ConnectDatabaseAndQuery();
		lions = con.getLions();
		ponds = con.getPonds();
		regions = con.getRegions();
		draw = new Paint(lions, ponds, regions);
		checkbox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				ifCheck = e.getStateChange() == ItemEvent.SELECTED;
			}
		});
		
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (ifCheck) {
					Point point = e.getPoint();
					for (Region r : regions) {
						if (r.ifContain(point)) {
							String region_id = r.getId();
							lions = con.getLionsInRegion(region_id, draw);
							ponds = con.getPondsInRegion(region_id, draw);
							draw.paint(lions, ponds, regions);
						}
					}
				}
			}
		});
		checkbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!checkbox.isSelected()) {
					lions = con.getLions();
					ponds = con.getPonds();
					regions = con.getRegions();
					draw.paint(lions, ponds, regions);
				}
			}
		});
		contentPane.add(panel, BorderLayout.NORTH);
		contentPane.add(draw);
		pack(); 
		setVisible(true);
	}
	
	public static void main(String[] args) {
		ExtraCredit application = new ExtraCredit("MY CSCI585 HW5");
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
	public Dimension getPreferredSize() {
		return new Dimension(540, 580);
	}
}


/*
 * This is the class implements connect to the database and query the elemennts.
 * Method:
 * @getLions(): return all lions in color green.
 * @getPonds(): return all ponds in color blue.
 * @getRegions(): return all regions in color white.  
 * @getPondsInRegion(): return the ponds in selected region in color red.
 * @getLionsInRegion(): return the lions in selected region in color red.
 *
*/

class ConnectDatabaseAndQuery {
	public ConnectDatabaseAndQuery() {
		
	}
	
	public Connection connectToDatabase() {
		try {
			String driver = "oracle.jdbc.driver.OracleDriver";
			Class.forName(driver);
			String url = "jdbc:oracle:thin:@localhost:1521:XE";
			String user = "SYSTEM";
			String password = "2239838133";
			return DriverManager.getConnection(url, user, password);
		}
		catch (ClassNotFoundException e){
			System.out.println("Class Not Found");
			return null;
		}
		catch (SQLException e) {
			System.out.println("Conncetion Failed");
			return null;
		}
	}
	
	/*
	 * This part return the result of lions.
	*/
	
	public ArrayList<Lion> getLions() {
		try {
			Connection conn = connectToDatabase();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM lions");
			ArrayList<Lion> lions = new ArrayList<>();
			while (rs.next()) {
				String lion_id = rs.getString(1);
		    	STRUCT st = (STRUCT) rs.getObject(2);
		    	JGeometry location = JGeometry.load(st);
				lions.add(new Lion((int)location.getFirstPoint()[0],(int)location.getFirstPoint()[1], lion_id));
			}
			closeConnection(conn);
			return lions;
		}
		catch (SQLException e) {
			System.out.println("Conncetion Failed");
			return null;
		}
	}
	
	public ArrayList<Pond> getPonds() {
		try {
			Connection conn = connectToDatabase();
			Statement stmt = conn.createStatement();
			String sql = "select * from ponds";
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<Pond> ponds = new ArrayList<>();
			while (rs.next()) {
				STRUCT object = (STRUCT) rs.getObject(2);
				JGeometry shape = JGeometry.load(object);
				String pond_id = rs.getString(1);
				ponds.add(new Pond((int)((double[])shape.getOrdinatesOfElements()[0])[0],
						            (int)((double[])shape.getOrdinatesOfElements()[0])[1],
						            (int)((double[])shape.getOrdinatesOfElements()[0])[2],
						            (int)((double[])shape.getOrdinatesOfElements()[0])[3],
									pond_id));
			}
			closeConnection(conn);
			return ponds;
		}
		catch (SQLException e) {
			System.out.println("Conncetion Failed");
			return null;
		}
	}
	
	public ArrayList<Region> getRegions() {
		try {
			Connection conn = connectToDatabase();
			Statement stmt = conn.createStatement();
			String sql = "select * from regions";
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<Region> regions = new ArrayList<>();
			while (rs.next()) {
				STRUCT object = (STRUCT) rs.getObject(2);
				JGeometry shape = JGeometry.load(object);
				String region_id = rs.getString(1);
				regions.add(new Region((int)((double[])shape.getOrdinatesOfElements()[0])[0],
						            (int)((double[])shape.getOrdinatesOfElements()[0])[1],
						            (int)((double[])shape.getOrdinatesOfElements()[0])[2],
						            (int)((double[])shape.getOrdinatesOfElements()[0])[3],
						            (int)((double[])shape.getOrdinatesOfElements()[0])[4],
						            (int)((double[])shape.getOrdinatesOfElements()[0])[5],
						            (int)((double[])shape.getOrdinatesOfElements()[0])[6],
						            (int)((double[])shape.getOrdinatesOfElements()[0])[7],				            
									region_id));
			}
			closeConnection(conn);
			return regions;
		}
		catch (SQLException e) {
			System.out.println("Conncetion Failed");
			return null;
		}
	}
	
	public ArrayList<Pond> getPondsInRegion(String region_id, Paint temp) {
		try {
			Connection conn = connectToDatabase();
			Statement stmt = conn.createStatement();
			String sql = "SELECT p.pond_id, p.shape FROM ponds p, regions r " +
				     "WHERE r.region_id = '"+ region_id +"' And sdo_inside(p.shape, r.shape) = 'TRUE'";
	
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<Pond> ponds = temp.getPonds();
			HashSet<String> pondId = new HashSet<>();
			while (rs.next()) {
				String pond_id = rs.getString(1);
		    	pondId.add(pond_id);
			}
			for (Pond p : ponds) {
				if (pondId.contains(p.getId())) {
					p.setColor(Color.RED);
				}
				else {
					p.setColor(Color.BLUE);
				}
			}
			closeConnection(conn);
			return ponds;
		}
		catch (SQLException e) {
			System.out.println("Conncetion Failed");
			return null;
		}
	}
	
	public ArrayList<Lion> getLionsInRegion(String region_id, Paint temp) {
		try {
			Connection conn = connectToDatabase();
			Statement stmt = conn.createStatement();
			String sql = "SELECT l.lion_id, l.location FROM lions l, regions r " +
				     "WHERE r.region_id = '"+ region_id +"' And sdo_inside(l.location, r.shape) = 'TRUE'";
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<Lion> lions = temp.getLions();
			HashSet<String> lionId = new HashSet<>();
			while (rs.next()) {
				String lion_id = rs.getString(1);
		    	lionId.add(lion_id);
			}
			for (Lion l : lions) {
				if (lionId.contains(l.getId())) {
					l.setColor(Color.RED);
				}
				else {
					l.setColor(Color.GREEN);
				}
			}
			closeConnection(conn);
			return lions;
		}
		catch (SQLException e) {
			System.out.println("Conncetion Failed");
			return null;
		}
	}
	
	public static void closeConnection (Connection conn) {
		try {
			conn.close();
		}
		catch (Exception e) {
			System.out.println("Connection close error");
		}
	}
}

/*
 * This is the class Lion.
 * Parameter: x, y is the coordinate of the lion.
 * lion_id is the PK of lion.
 * lion_color shows the color of lion.
 */

class Lion {
	private int x, y;
	private String lion_id;
	private boolean ifSelect;
	private Color lion_color;
	public Lion (int x, int y, String lion_id) {
		this.x = x;
		this.y = 500 - y;
		this.lion_id = lion_id;
		ifSelect = false;
		lion_color = Color.GREEN;
	}

	public String getId() {
		return lion_id;
	}
	
	public void setSelect(boolean flag) {
		ifSelect = flag;
	}
	
	public boolean getSelect() {
		return ifSelect;
	}
	
	public void setColor(Color r) {
		lion_color = r;
	}
	public void draw_lion(Graphics g) {
		g.setColor(lion_color);
		g.fillOval(x,  y,  10, 10);
	}
	
	// this is for single test.
	public Color getColor() {
		return lion_color;
	}
}

class Pond {
	private int x, y, r;
	private String pond_id;
	private boolean ifSelect;
	private Color pond_color;
	public Pond(int x1, int y1, int x2, int y2, String pond_id) {
		x = (x1 + x2) / 2;
		y = 500 - y1;
		r = (x1 > x2) ? (x1 - x2) / 2 : (x2 - x1) / 2;
		this.pond_id = pond_id;
		pond_color = Color.BLUE;
	}

	public String getId() {
		return pond_id;
	}
	
	public void setSelect(boolean flag) {
		ifSelect = flag;
	}
	
	public boolean getSelect() {
		return ifSelect;
	}
	
	public void setColor(Color r) {
		pond_color = r;
	}
	
	public void draw_pond(Graphics g) {
		g.setColor(Color.BLACK);
		g.drawArc(x, y, r * 2, r * 2, 0, 360);
		g.setColor(pond_color);
		g.fillOval(x,  y,  r * 2, r * 2);
	}
}

class Region {
	private int[] x = new int[4];
	private int[] y = new int[4];
	private String region_id;
	private boolean ifSelect;
	public Region(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, String region_id) {
		x[0] = x1;
		x[1] = x2;
		x[2] = x3;
		x[3] = x4;
		y[0] = 500 - y1;
		y[1] = 500 - y2;
		y[2] = 500 - y3;
		y[3] = 500 - y4;
		this.region_id = region_id;
	}
	
	public void draw_region (Graphics g) {
		g.setColor(Color.black);
		g.drawPolyline(x, y, 4);
	}
	
	public boolean ifContain(Point p) {
		int Px = (int)p.getX();
		int Py = (int)p.getY();
		Polygon temp = new Polygon(x, y, 4);
		return temp.contains(Px, Py);
	}
	
	public String getId() {
		return region_id;
	}
	
	public void setSelect(boolean flag) {
		ifSelect = flag;
	}
	
	public boolean getSelect() {
		return ifSelect;
	}
	
}

class Paint extends JPanel {
	public ArrayList<Lion> lions;
	private ArrayList<Pond> ponds;
	private ArrayList<Region> regions;
	private Color lion_color;
	private Color pond_color;
	private Color region_color;
	
	public Paint(ArrayList<Lion> lions, ArrayList<Pond> ponds, ArrayList<Region> regions) {
		this.lions = lions;
		this.ponds = ponds;
		this.regions =regions;
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		for (Lion l : lions) {
			l.draw_lion(g);
		}
		
		for (Pond p : ponds) {
			p.draw_pond(g);
		}
		
		for (Region r : regions) {
			r.draw_region(g);
		}
	}
	
	public ArrayList<Lion> getLions() {
		return lions;
	}
	
	public ArrayList<Pond> getPonds() {
		return ponds;
	}
	
	public void paint(ArrayList<Lion> lions, ArrayList<Pond> ponds, ArrayList<Region> regions) {
		this.lions = lions;
		this.ponds = ponds;
		this.regions = regions;
		repaint();
	}
}
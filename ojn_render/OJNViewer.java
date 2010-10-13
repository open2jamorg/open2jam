import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.event.*;
import java.awt.event.*;

import java.util.List;
import java.util.TreeMap;

import org.open2jam.parser.*;

public class OJNViewer implements ListSelectionListener
{
	JFrame frame;
	ChartHeader h;

	JList list;
	String selected_file;

	JLabel parent_dir;
	JLabel title;
	JLabel songid;
	JLabel genre;
	JLabel bpm;
	JLabel level;
	JLabel note_count;
	JLabel artist;
	JLabel noter;
	JLabel ojm_file;
	JLabel time;
	JLabel note_offset;
	JLabel cover_size;
	JLabel signature;
	JLabel package_count;
	JLabel cover_image;

	JLabel label_list[];

	ChartParser cp = new OJNParser();

	public OJNViewer(String dir)
	{
		parent_dir = new JLabel(dir);
		frame = new JFrame("OJNViewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel infopanel = new JPanel();
		infopanel.setLayout(new BoxLayout(infopanel,BoxLayout.Y_AXIS));
		
		
		list = new JList();
		setOJNList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);

		frame.getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,new JScrollPane(list),infopanel));

		infopanel.setPreferredSize(new Dimension(500,400));
		Font mono = new Font(Font.MONOSPACED,Font.PLAIN,14);

		songid = new JLabel("SONGID:");
		title = new JLabel("TITLE:");
		genre = new JLabel("GENRE:");
		bpm = new JLabel("BPM:");
		level = new JLabel("LEVEL:");
		note_count = new JLabel("NOTE COUNT:");
		artist = new JLabel("ARTIST:");
		noter = new JLabel("NOTER:");
		ojm_file = new JLabel("OJM FILE:");
		time = new JLabel("TIME:");
		note_offset = new JLabel("NOTE OFFSET:");
		cover_size = new JLabel("COVER SIZE:");
		signature = new JLabel("SIGNATURE:");
		package_count = new JLabel("PACKAGE_COUNT:");
		cover_image = new JLabel();

		JButton chdir = new JButton("Change DIR");
		chdir.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){changeDir();}
		});

// 		JButton echart = new JButton("Ex"), 
// 			nchart = new JButton("Nx"),
// 			hchart = new JButton("Hx");
// 		echart.setActionCommand("e");
// 		nchart.setActionCommand("n");
// 		hchart.setActionCommand("h");
// 		ActionListener mkchart = new ActionListener(){
// 			public void actionPerformed(ActionEvent e){showChart(e.getActionCommand());}
// 		};
// 		echart.addActionListener(mkchart);
// 		nchart.addActionListener(mkchart);
// 		hchart.addActionListener(mkchart);

		parent_dir.setFont(mono);
		title.setFont(mono);
		songid.setFont(mono);
		genre.setFont(mono);
		bpm.setFont(mono);
		level.setFont(mono);
		note_count.setFont(mono);
		artist.setFont(mono);
		noter.setFont(mono);
		ojm_file.setFont(mono);
		time.setFont(mono);
		note_offset.setFont(mono);
		cover_size.setFont(mono);
		signature.setFont(mono);
		package_count.setFont(mono);


		infopanel.add(parent_dir);
		infopanel.add(chdir);
		infopanel.add(songid);
		infopanel.add(title);
		infopanel.add(genre);
		infopanel.add(bpm);
		infopanel.add(level);
		infopanel.add(note_count);
		infopanel.add(artist);
		infopanel.add(noter);
		infopanel.add(ojm_file);
		infopanel.add(time);
		infopanel.add(note_offset);
		infopanel.add(cover_size);
		infopanel.add(signature);
		infopanel.add(package_count);
		infopanel.add(cover_image);

		cover_image.addMouseListener( new MouseAdapter(){
			public void mouseClicked(MouseEvent e){coverDialog();}
		});

		frame.setSize(600,400);
		frame.setVisible(true);
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if(selected_file!=null && selected_file.equals(list.getSelectedValue()))return;
		selected_file = list.getSelectedValue().toString();
		updateInfo();
	}

	private void updateInfo()
	{
		h = cp.parseFileHeader(parent_dir.getText()+File.separatorChar+selected_file,0);

		title.setText(        "TITLE:        "+h.title);
		genre.setText(        "GENRE:        "+h.genre);
		bpm.setText(          "BPM:          "+h.bpm);
		level.setText(        "LEVEL:        "+h.level);
		note_count.setText(   "NOTE COUNT:   "+h.noteCount);
		artist.setText(       "ARTIST:       "+h.artist);
		time.setText(         "TIME:         "+h.duration);
		cover_image.setIcon(new ImageIcon(h.cover.getScaledInstance(200,200,java.awt.Image.SCALE_SMOOTH)));
	}


	public void coverDialog()
	{
		JOptionPane.showMessageDialog(frame,null,null,JOptionPane.INFORMATION_MESSAGE,new ImageIcon(h.cover));
	}
	public void changeDir()
	{
		String dir = chooseDir();
		if(dir == null)return;

		parent_dir.setText(dir);
		setOJNList();
	}
	public String chooseDir()
	{
		JFileChooser chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new File(parent_dir.getText()));
		chooser.setDialogTitle("Select a directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if(chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile().getAbsolutePath();
		else
			return null;
	}
// 	private void showChart(String c)
// 	{
// 		int lvl = -1;
// 		if(c.equals("e"))lvl = 0;
// 		else if(c.equals("n"))lvl = 1;
// 		else if(c.equals("h"))lvl = 2;
// 		JDialog jd = new JDialog(frame,true);
// 		JLabel chart = new JLabel();
// 		chart.setIcon(new ImageIcon(h.cover));
// 		jd.getContentPane().add(new JScrollPane(chart));
// 		jd.setSize(600,400);
// 		jd.setVisible(true);
// 	}

	private static FilenameFilter ojnfilter = new FilenameFilter(){
				public boolean accept(File dir, String name){
					return name.toLowerCase().endsWith(".ojn");
			}};
	private void setOJNList()
	{
		String files[] = new File(parent_dir.getText()).list(ojnfilter);
		String[] sf = sortOJNFiles(files);
		list.setListData(sf);
	}

	private String[] sortOJNFiles(String[] files)
	{
		TreeMap<Integer,String> h = new TreeMap<Integer,String>();
		for(int i=0;i<files.length;i++)
		{
			int key = Integer.parseInt("0"+files[i].replaceAll("\\D",""));
			h.put(key,files[i]);
		}
		return h.values().toArray(new String[0]);
	}

	public static void main(String args[])
	{
		try {
		for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				UIManager.setLookAndFeel(info.getClassName());
				break;
			}
		}
		} catch (Exception e) {}
		new OJNViewer(System.getProperty("user.dir"));
	}
}
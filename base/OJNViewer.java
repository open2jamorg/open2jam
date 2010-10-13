import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.util.List;
import java.util.TreeMap;

import org.open2jam.parser.*;

public class OJNViewer implements ListSelectionListener
{
	final JFrame frame;
	ChartHeader h;

	JList list;
	String selected_file;

	JLabel parent_dir;
	JLabel title;
	JLabel genre;
	JLabel bpm;
	JLabel level;
	JLabel note_count;
	JLabel artist;
	JLabel time;
	JLabel cover_image;

	JLabel label_list[];

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

		title = new JLabel("TITLE:");
		genre = new JLabel("GENRE:");
		bpm = new JLabel("BPM:");
		level = new JLabel("LEVEL:");
		note_count = new JLabel("NOTE COUNT:");
		artist = new JLabel("ARTIST:");
		time = new JLabel("TIME:");
		cover_image = new JLabel();

		JButton chdir = new JButton("Change DIR");
		chdir.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){changeDir();}
		});

		JButton chart = new JButton("Make Chart");

		ActionListener mkchart = new ActionListener(){
			public void actionPerformed(ActionEvent e){saveChart();}
		};
		chart.addActionListener(mkchart);

		parent_dir.setFont(mono);
		title.setFont(mono);
		genre.setFont(mono);
		bpm.setFont(mono);
		level.setFont(mono);
		note_count.setFont(mono);
		artist.setFont(mono);
		time.setFont(mono);


		infopanel.add(parent_dir);
		infopanel.add(chdir);
		infopanel.add(title);
		infopanel.add(genre);
		infopanel.add(bpm);
		infopanel.add(level);
		infopanel.add(note_count);
		infopanel.add(artist);
		infopanel.add(time);
		infopanel.add(chart);
		infopanel.add(cover_image);

		cover_image.addMouseListener( new MouseAdapter(){
			public void mouseClicked(MouseEvent e){coverDialog();}
		});

		frame.setSize(600,400);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				frame.setVisible(true);
			}
		});
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if(selected_file!=null && selected_file.equals(list.getSelectedValue()))return;
		selected_file = list.getSelectedValue().toString();
		updateInfo();
	}

	private void updateInfo()
	{
		h = ChartParser.parseFileHeader(parent_dir.getText()+File.separatorChar+selected_file,2);

		title.setText(        "TITLE:        "+h.getTitle());
		genre.setText(        "GENRE:        "+h.getGenre());
		bpm.setText(          "BPM:          "+h.getBPM());
		level.setText(        "LEVEL:        "+h.getLevel());
		note_count.setText(   "NOTE COUNT:   "+h.getNoteCount());
		artist.setText(       "ARTIST:       "+h.getArtist());
		time.setText(         "TIME:         "+h.getDuration());
		cover_image.setIcon(new ImageIcon(h.getCover().getScaledInstance(200,200,java.awt.Image.SCALE_SMOOTH)));
	}

	private void saveChart()
	{
		java.awt.image.BufferedImage bi = ImageRender.renderChart(ChartParser.parseFile(h), 1, 4);
		String s = parent_dir.getText()+File.separator+selected_file+".png";
		boolean ok = false;
		try{
			ok = javax.imageio.ImageIO.write(bi, "png", new File(s));
		}catch(Exception e){
			e.printStackTrace();
		}
		if(ok)JOptionPane.showMessageDialog(frame,"OK. Chart saved in "+s);
		else JOptionPane.showMessageDialog(frame,"Error");
	}

	public void coverDialog()
	{
		JOptionPane.showMessageDialog(frame,null,null,
			JOptionPane.INFORMATION_MESSAGE,new ImageIcon(h.getCover()));
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
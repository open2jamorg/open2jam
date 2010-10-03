import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.HashMap;

public class OJNViewer implements ListSelectionListener
{
	JFrame frame;

	JList list;
	String selected_file;
	OJN ojn;

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

		JButton echart = new JButton("Ex"), 
			nchart = new JButton("Nx"),
			hchart = new JButton("Hx");
		echart.setActionCommand("e");
		nchart.setActionCommand("n");
		hchart.setActionCommand("h");
		ActionListener mkchart = new ActionListener(){
			public void actionPerformed(ActionEvent e){showChart(e.getActionCommand());}
		};
		echart.addActionListener(mkchart);
		nchart.addActionListener(mkchart);
		hchart.addActionListener(mkchart);

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

// int unk_num[];
// char unk_zero[];
// int unk_time[];
// short unk_id[];
// char unk_oldgenre[];
// char unk_zero2[];
// boolean unk_bool;
// char unk_k[];
// char unk_zero3[];
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
		infopanel.add(echart);
		infopanel.add(nchart);
		infopanel.add(hchart);

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
		ojn = new OJN(parent_dir.getText()+File.separatorChar+selected_file);

		songid.setText(       "SONGID:       "+ojn.songid);
		title.setText(        "TITLE:        "+ojn.getTitle());
		genre.setText(        "GENRE:        "+ojn.getGenre());
		bpm.setText(          "BPM:          "+ojn.bpm);
		level.setText(        "LEVEL:        "+ojn.level[0]+", "+ojn.level[1]+", "+ojn.level[2]);
		note_count.setText(   "NOTE COUNT:   "+ojn.note_count[0]+", "+ojn.note_count[1]+", "+ojn.note_count[2]);
		artist.setText(       "ARTIST:       "+ojn.getArtist());
		noter.setText(        "NOTER:        "+ojn.getNoter());
		ojm_file.setText(     "OJM FILE:     "+ojn.getOJM());
		time.setText(         "TIME:         "+ojn.getTime(0)+", "+ojn.getTime(1)+", "+ojn.getTime(2));
		note_offset.setText(  "NOTE OFFSET:  "+ojn.note_offset[0]+", "+ojn.note_offset[1]+", "+ojn.note_offset[2]+", "+ojn.note_offset[3]);
		cover_size.setText(   "COVER SIZE:   "+ojn.cover_size+" bytes");
		signature.setText(    "SIGNATURE:    "+ojn.getSignature());
		package_count.setText("PACKAGE COUNT:"+ojn.package_count[0]+", "+ojn.package_count[1]+", "+ojn.package_count[2]);
		cover_image.setIcon(ojn.getMiniCover());
	}


	public void coverDialog()
	{
		JOptionPane.showMessageDialog(frame,null,null,JOptionPane.INFORMATION_MESSAGE,ojn.getCover());
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
	private void showChart(String c)
	{
		int lvl = -1;
		if(c.equals("e"))lvl = 0;
		else if(c.equals("n"))lvl = 1;
		else if(c.equals("h"))lvl = 2;
		JDialog jd = new JDialog(frame,true);
		JLabel chart = new JLabel();
		chart.setIcon(ojn.getChart(lvl));
		jd.getContentPane().add(new JScrollPane(chart));
		jd.setSize(600,400);
		jd.setVisible(true);
	}

	private static FilenameFilter ojnfilter = new FilenameFilter(){
				public boolean accept(File dir, String name){
					return name.toLowerCase().endsWith(".ojn");
			}};
	private void setOJNList()
	{
		String files[] = new File(parent_dir.getText()).list(ojnfilter);
		sortOJNFiles(files);
		list.setListData(files);
	}

	private void sortOJNFiles(String[] files)
	{
		int i = 0;
		HashMap<Integer,String> h = new HashMap<Integer,String>();
		int keys[] = new int[files.length];
		for(i=0;i<files.length;i++)
		{
			int key = Integer.parseInt("0"+files[i].replaceAll("\\D",""));
			h.put(key,files[i]);
			keys[i] = key;	
		}
		java.util.Arrays.sort(keys);
		for(int k : keys)
		{
			files[k] = h.get(k);
		}
	}

	public static void main(String args[])
	{
		new OJNViewer(System.getProperty("user.dir"));
	}
}
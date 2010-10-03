import java.util.Vector;

public class RawNotes extends Vector<Note>
{
	private int maxbeat;
	private int notelevel;

	public RawNotes(int notelevel)
	{
		super();
		this.notelevel = notelevel;
	}
	public void setMaxBeat(int mb){maxbeat=mb;}
	public int  getMaxBeat(){return maxbeat;}
}
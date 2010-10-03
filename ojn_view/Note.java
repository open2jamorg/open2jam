
public class Note
{
	int channel;
	int beat;
	float bpm;
	short value;
	char flag1;
	char flag2;
	boolean longnote;

	public Note(int c, int b, short v, char f1, char f2)
	{
		channel=c;beat=b;value=v;flag1=f1;flag2=f2;
		longnote = (f2 & 2)!=0;
	}
	public Note(int c,int b, float bp)
	{
		channel=c;beat=b;bpm=bp;
	}
}
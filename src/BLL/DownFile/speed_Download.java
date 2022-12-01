package BLL.DownFile;

public class speed_Download{
	private  int total_Size_1s;
	private boolean check;
	public speed_Download() {
		total_Size_1s=0;
		check = false;
	}

	public synchronized void plus_Size_DownLoad_1s(int dungluong)
	{
		total_Size_1s+=dungluong;
	}
	public int Get_Seze_1s()
	{
		return total_Size_1s;
	}
	public void set_Size_Download()
	{
		this.total_Size_1s=0;
	}
	public void set_Check(boolean check)
	{
		this.check=check;

	}
	public boolean get_Check()
	{
		return this.check;
	}
}
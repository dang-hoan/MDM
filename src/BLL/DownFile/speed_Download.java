package BLL.DownFile;

import BLL.Values;

public class speed_Download{
	private  long total_Size_1s;
	private int check;
	public speed_Download() {
		total_Size_1s=0;
		check = Values.READY;
	}

	public synchronized void plus_Size_DownLoad_1s(long dungluong)
	{
		total_Size_1s+=dungluong;
	}
	public long Get_Seze_1s()
	{
		return total_Size_1s;
	}
	public void set_Size_Download()
	{
		this.total_Size_1s=0;
	}
	public void set_Check(int check)
	{
		this.check=check;

	}
	public int get_Check()
	{
		return this.check;
	}
}
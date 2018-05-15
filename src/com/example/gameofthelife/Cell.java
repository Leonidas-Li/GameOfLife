package com.example.gameofthelife;

public class Cell {
	public int Survival = 0;// 细胞存活与否 0代表死亡，1代表存活
	public int Coordinate_X = 0;// 细胞的横坐标
	public int Coordinate_Y = 0;// 细胞的纵坐标
	public int Color = 0x00000000;// 用颜色表名细胞的不同(就像每个人一样，每个细胞个体是不同的)，可以用随机数来对齐进行相应操作，默认为完全透明的(表示细胞是死亡的)
	public int NeighbourCount = 0;// 存储邻居数量

	public int ScanNeighbourCount(int X, int Y, int[][] Matrix) {
		NeighbourCount = Utils.gainNeighbourCount(X, Y, Matrix);
		return NeighbourCount;
	}// 方法结束
}

package com.example.gameofthelife;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class Utils {
	static public List<Cell> getCellFrom_ColAndInitState(int col,
			int initStateArr[]) {
		List<Cell> ListCell = new ArrayList<Cell>();// 存储通过API数据创建的Cell对象

		int[][] twoDimensionalMatrix = new int[initStateArr.length][col];
		// 初始化数组，全部赋值为0,其实也可以不用写此语句块，java中int型数组的值默认为0，为了方便理解才写此语句块
		for (int i = 0; i < initStateArr.length; i++) {
			for (int j = 0; j < col; j++) {
				twoDimensionalMatrix[i][j] = 0;
			}
		}
		// 填写二维数组---得到的结果是正确的，不过与图形是相反的，暂时无所谓。
		for (int i = 0; i < twoDimensionalMatrix.length; i++) {
			for (int j = col - 1; initStateArr[i] != 0; j--) {
				twoDimensionalMatrix[i][j] = initStateArr[i] % 2;
				// Log.i("initStateArr" + i, initStateArr[i] + "");
				/*
				 * Log.i("testMatrix:" + i + ":" + j,
				 * twoDimensionalMatrix[i][j]+ "");
				 */
				initStateArr[i] = initStateArr[i] / 2;
				// Log.i("testQuotient", initStateArr[i] + "");
			}

		}
		// test
		/*
		 * for (int i = 0; i < initStateArr.length; i++) { for (int j = 0; j <
		 * col; j++) { Log.i("testMatrix:" + i + ":" + j,
		 * twoDimensionalMatrix[i][j] + ""); } }
		 */
		// 通过二维数组创建Cell
		for (int i = 0; i < twoDimensionalMatrix.length; i++) {// 2
			for (int j = 0; j < twoDimensionalMatrix[i].length; j++) {// 3
				Cell cell = new Cell();
				cell.Survival = twoDimensionalMatrix[i][j];
				cell.Coordinate_X = j;// 横坐标---数组的第二个下标--最大值为3
				cell.Coordinate_Y = i;// 纵坐标---数组的第一个下标--最大值为2
				cell.NeighbourCount = gainNeighbourCount(j, i,
						twoDimensionalMatrix);
				// test

				Log.i("testMatrix:" + i + ":" + j, twoDimensionalMatrix[i][j]
						+ "");

				Log.i("test_cell.Survival", cell.Survival + "");
				// Log.i("test_cell.Coordinate_X", cell.Coordinate_X + "");
				// Log.i("test_cell.Coordinate_Y", cell.Coordinate_Y + "");
				Log.i("test_cell.NeighbourCount", cell.NeighbourCount + "");
				Log.i("test------------------------------",
						"----------------------------------");
				ListCell.add(cell);
			}
		}
		// test
		/*
		 * for (Cell cell : ListCell) { Log.i("test_cell的邻居个数",
		 * cell.NeighbourCount + ""); }
		 */
		return ListCell;
	}

	// 获取Cell的邻居数量
	static public int gainNeighbourCount(int X, int Y, int[][] MyMatrix) {
		int NeighbourCount = 0;// 邻居的数量
		// MAX(Y)=2， MAX(X)=3 ）(X,Y)指的是原点
		for (int y = Y - 1; y <= Y + 1; y++) {
			for (int x = X - 1; x <= X + 1; x++) {
				if (y >= 0 && x >= 0) {
					if (x == X && y == Y) {
					} else {
						// 防止数组越界---被这个语句坑惨了/(ㄒoㄒ)/~~，“找了两天”，终于找到错哪里了，大家以后一定要注意越界的问题啊，不然会很浪费时间的
						if (y < MyMatrix.length && x < MyMatrix[0].length) {
							NeighbourCount += MyMatrix[y][x];
						}
					}
				}
			}
		}
		// Log.i("testgainNeighbourCount", NeighbourCount + "");
		return NeighbourCount;
	}// 方法结束

	static public Cell createCellFrom_X_and_Y(int x, int y,
			int TheAreaExistCell[][]) {
		// 如果这个区域不存在细胞，则创建细胞
		if (TheAreaExistCell[y][x] != 1) {
			Cell cell = new Cell();
			cell.Survival = 1;
			cell.Coordinate_X = x;
			cell.Coordinate_Y = y;
			// 通过游戏区域进行初始设置的时候不能设置不要获取邻居数量，因为获取到的邻居数量是错误的---等初始设置结束后在同一进行邻居数量的设置
			// cell.ScanNeighbourCount(x, y, Matrix);
			return cell;
		}
		return null;
	}

	static public int JudgeSurvivalResources(int Survival, int NeighbourCount) {// 判断生存生存条件---返回1或0---1代表生存条件满足；0代表生存条件不满足

		if (NeighbourCount == 3) {
			return 1;
		} else if ((Survival == 1) && (NeighbourCount == 2)) {
			return 1;
		} else {
			return 0;
		}
	}
}

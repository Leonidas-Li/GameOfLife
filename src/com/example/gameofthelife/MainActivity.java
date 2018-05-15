package com.example.gameofthelife;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	Thread T_cellVolution;// 用于进行细胞演化的线程
	static private Button bt_CellAutomaticGeneration;// 自动生成细胞按钮
	static private Button bt_Rule;// 规则按钮
	static private Button bt_Save;// 存档按钮
	static private Button bt_Load;// 读档按钮
	static private Button bt_Start;// 开始按钮
	static private Button bt_Stop;// 暂停按钮
	static private Button bt_End;// 终止按钮
	static private Button bt_speedEvolution;// 加速演化按钮
	static private Button bt_decelerateEvolution;// 减速演化按钮
	static private Button bt_reset;// 清除所有细胞按钮
	static private EditText et_FileName;// 用于输入需要“存档”或“加载”的文件的文件名
	static private int[][] TheAreaExistCell = new int[20][20];// 用于存储某个区域是否存在细胞---1代表此区域存在细胞
	static private ImageView iv_GameBG;// 中间的游戏区域
	static private JSONObject JO;// 从服务器获取到的JSON对象
	static List<Cell> listCell = new ArrayList<Cell>();// 用于存储整个游戏区域(细胞生存的区域)的细胞
	static List<Cell> CellImage_List;// 用于存储细胞图对象---也就是存储每次刷新UI时的有关bitmap的相关信息
	static Canvas canvas;// 画板
	static Paint paint;// 画笔
	static Bitmap bm_GameBG_copy;// 背景图的副本---可读可写
	static protected int evolution = 0;// 演化变量，有三个值---分别是1，-1,0---分别代表“进行演化”-“暂停演化”-“终止演化”
	static private int evolutionTime = 1000;// 演化一次的时间---以毫秒计算---初始为1秒

	// 通过API创建细胞
	public void CreateCellFromAPI() {
		Thread T = new Thread() {
			public void run() {
				super.run();
				String path = "http://139.199.16.21/api/initState";
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");// 设置get方式读取
					conn.setConnectTimeout(6000);// 设置连接超时
					conn.setReadTimeout(6000);// 设置读取超时
					conn.connect();// 发送请求，与服务器建立连接
					// Log.i("test", "1");
					if (conn.getResponseCode() == 200) {
						// 获取数据
						BufferedReader br = new BufferedReader(
								new InputStreamReader(conn.getInputStream()));
						StringBuffer sb = new StringBuffer();
						String str = "";
						while ((str = br.readLine()) != null) {
							sb.append(str);
						}
						JO = new JSONObject(sb.toString());
						int col = JO.getInt("col");
						int initStateArr[];
						Log.i("testJO", JO.toString());
						Log.i("testCOl", col + "");
						JSONArray initState = JO.getJSONArray("initState");
						Log.i("testInitState", initState.toString());
						initStateArr = new int[initState.length()];

						for (int i = 0; i < initState.length(); i++) {
							initStateArr[i] = initState.getInt(i);
						}
						// 通过服务器返回的col和initstate参数创建细胞，得到一个封装了获取到的细胞信息的list集合
						List<Cell> listCell = Utils
								.getCellFrom_ColAndInitState(col, initStateArr);// 将获得的细胞封装到List集合中
						// 将listCell集合放到消息中,并通过轮询器对象将其发送到消息队列中,然后由消息处理器对象handler对消息进行处理，刷新UI
						Message msg = handler.obtainMessage();
						msg.obj = listCell;
						msg.what = 0;
						handler.sendMessage(msg);
						// test
						/*
						 * for (Cell cell : listCell) {
						 * Log.i("test第一次_cell的邻居个数", cell.NeighbourCount + "");
						 * }
						 */
					} else {
						Log.i("test", "网络请求失败");
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		T.start();
	}// CreateCellFromAPI()方法结束

	static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				paint.setColor(0XFF000000);
				List<Cell> listCell_msg = (List<Cell>) msg.obj;
				for (Cell cell : listCell_msg) {
					if (cell.Survival == 1) {
						int Y = cell.Coordinate_Y * 20;// 20*20=400个像素作为一个细胞
						int X = cell.Coordinate_X * 20;
						canvas.drawRect(X + 8 * 20, Y + 8 * 20,
								X + 20 + 8 * 20, Y + 20 + 8 * 20, paint);
						TheAreaExistCell[cell.Coordinate_Y + 8][cell.Coordinate_X + 8] = 1;// 生成细胞之后-将此区域设为有活细胞的状态
					} else {
						TheAreaExistCell[cell.Coordinate_Y + 8][cell.Coordinate_X + 8] = 0;
					}
				}
				iv_GameBG.setImageBitmap(bm_GameBG_copy);
				break;
			case 1:
				Bitmap bm = (Bitmap) msg.obj;
				iv_GameBG.setImageBitmap(bm);
			case 2:

			default:
				break;
			}

		};// handleMessage方法结束
	};// handler

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// -------------
		bt_CellAutomaticGeneration = (Button) findViewById(R.id.bt_cellAutomaticGeneration);
		bt_Rule = (Button) findViewById(R.id.bt_rule);
		bt_Save = (Button) findViewById(R.id.bt_save);
		bt_Load = (Button) findViewById(R.id.bt_load);
		bt_Start = (Button) findViewById(R.id.bt_start);
		bt_Stop = (Button) findViewById(R.id.bt_stop);
		bt_End = (Button) findViewById(R.id.bt_end);
		bt_speedEvolution = (Button) findViewById(R.id.bt_speedEvolution);
		bt_decelerateEvolution = (Button) findViewById(R.id.bt_decelerateEvolution);
		bt_reset = (Button) findViewById(R.id.bt_reset);
		et_FileName = (EditText) findViewById(R.id.et_FileName);
		iv_GameBG = (ImageView) findViewById(R.id.iv_background);
		// -------------
		Resources rs = this.getResources();
		InputStream is = rs.openRawResource(R.drawable.bg);
		Bitmap bm_GameBG = BitmapFactory.decodeStream(is);
		// 创建图片副本
		// 1.在内存中创建一个与原图一模一样大小的bitmap对象，创建与原图大小一致的的白纸
		bm_GameBG_copy = Bitmap.createBitmap(bm_GameBG.getWidth(),
				bm_GameBG.getHeight(), bm_GameBG.getConfig());
		// 2.创建画笔对象
		paint = new Paint();
		paint.setColor(0XFF000000);// 将画笔设置为黑色
		// 3.创建画板对象，把白纸铺在画板上
		canvas = new Canvas(bm_GameBG_copy);
		// 4.开始作画，把原图的内容绘制在白纸上
		/*
		 * Matrix Mat = new Matrix();// 创建一个矩阵用于对图片进行放大 Mat.setScale(20, 20,
		 * bm_GameBG_copy.getWidth() / 2, bm_GameBG_copy.getHeight() / 2);//
		 * 将图片的x和y都放大64倍---从中心点开始
		 */canvas.drawBitmap(bm_GameBG, new Matrix(), paint);
		// canvas.drawRect(0, 0, 10, 10, paint);
		iv_GameBG.setImageBitmap(bm_GameBG_copy);
		// -------------
		// 设置游戏区域的触摸监听
		iv_GameBG.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				int action = event.getAction();// 获取action
				if (evolution == 1) {// 如果evolution = 1则不能对游戏区域进行操作
					switch (action) {
					case MotionEvent.ACTION_DOWN:
						Toast.makeText(MainActivity.this,
								"正在进行演化，不能手动生成细胞---如果需要生成新的细胞，请暂停后在进行操作",
								Toast.LENGTH_SHORT).show();
						break;
					}

				} else {
					evolution = -1;

					int x = 0;// 触摸到的区域的x(横)坐标
					int y = 0;// 触摸到的区域的y(纵)坐标
					Cell cell;// 用于存储创建的细胞
					paint.setColor(0XFF000000);
					switch (action) {
					// 用户手指触摸屏幕
					case MotionEvent.ACTION_DOWN:
						x = (int) event.getX();// 代表矩阵的横坐标---列---图形的像素坐标
						y = (int) event.getY();// 代表矩阵的纵坐标---行---图形的像素坐标
						if (x < bm_GameBG_copy.getWidth()
								&& y < bm_GameBG_copy.getHeight() && x >= 0
								&& y >= 0) {// 防止越界
							// 获取细胞的x坐标
							while (x % 20 != 0) {
								x--;// 如果x(横)坐标不是20的整数倍则自减1
							}
							// 获取细胞的y坐标
							while (y % 20 != 0) {
								y--;// 如果y(纵)坐标不是20的整数倍则自减1
							}
							if (TheAreaExistCell[y / 20][x / 20] != 1) {
								cell = Utils.createCellFrom_X_and_Y(x / 20,
										y / 20, TheAreaExistCell);
								TheAreaExistCell[y / 20][x / 20] = 1;// 生成细胞之后-将此区域设为有活细胞的状态
								listCell.add(cell);
								canvas.drawRect(x, y, x + 20, y + 20, paint);// 将图形画出来
								iv_GameBG.setImageBitmap(bm_GameBG_copy);
							}
						}
						// Log.i("test", "用户手指触摸屏幕");
						break;
					// 用户手指在滑动
					case MotionEvent.ACTION_MOVE:
						// Log.i("test", "用户手指在滑动");
						x = (int) event.getX();// 代表矩阵的横坐标---列---图形的像素坐标
						y = (int) event.getY();// 代表矩阵的纵坐标---行---图形的像素坐标
						if (x < bm_GameBG_copy.getWidth()
								&& y < bm_GameBG_copy.getHeight() && x >= 0
								&& y >= 0) {// 防止越界
							// 获取细胞的x坐标
							while (x % 20 != 0) {
								x--;// 如果x(横)坐标不是20的整数倍则自减1
							}
							// 获取细胞的y坐标
							while (y % 20 != 0) {
								y--;// 如果y(纵)坐标不是20的整数倍则自减1
							}
							if (TheAreaExistCell[y / 20][x / 20] != 1) {
								cell = Utils.createCellFrom_X_and_Y(x / 20,
										y / 20, TheAreaExistCell);
								TheAreaExistCell[y / 20][x / 20] = 1;// 生成细胞之后-将此区域设为有活细胞的状态
								listCell.add(cell);
								canvas.drawRect(x, y, x + 20, y + 20, paint);// 将图形画出来
								iv_GameBG.setImageBitmap(bm_GameBG_copy);
							}

							// test
							Log.i("testArea" + x / 20 + ";" + y / 20 + " = ",
									"" + TheAreaExistCell[y / 20][x / 20]);
							Log.i("testListSize", listCell.size() + "");
							/*
							 * Log.i("test创建对了细胞", "细胞的坐标为：" +
							 * cell.Coordinate_X+ " -- " + cell.Coordinate_Y);
							 */
						}
						break;
					// 用户手指离开屏幕
					case MotionEvent.ACTION_UP:
						// Log.i("test", "用户手指离开屏幕");
						x = (int) event.getX();// 代表矩阵的横坐标---列---图形的像素坐标
						y = (int) event.getY();// 代表矩阵的纵坐标---行---图形的像素坐标
						if (x < bm_GameBG_copy.getWidth()
								&& y < bm_GameBG_copy.getHeight() && x >= 0
								&& y >= 0) {// 防止越界
							// 获取细胞的x坐标
							while (x % 20 != 0) {
								x--;// 如果x(横)坐标不是20的整数倍则自减1
							}
							// 获取细胞的y坐标
							while (y % 20 != 0) {
								y--;// 如果y(纵)坐标不是20的整数倍则自减1
							}
							if (TheAreaExistCell[y / 20][x / 20] != 1) {
								cell = Utils.createCellFrom_X_and_Y(x / 20,
										y / 20, TheAreaExistCell);
								TheAreaExistCell[y / 20][x / 20] = 1;// 生成细胞之后-将此区域设为有活细胞的状态
								listCell.add(cell);
								canvas.drawRect(x, y, x + 20, y + 20, paint);// 将图形画出来
								iv_GameBG.setImageBitmap(bm_GameBG_copy);
							}
						}
						break;
					}
				}
				// true: 告诉系统，这个触摸事件由我来处理---也就是只要产生了触摸事件都由此监听方法进行处理
				// false:
				// 告诉系统，这个触摸事件我不处理，这时系统会把后续的触摸事件传递给此控件的父节点---也就是此监听方法只一次触摸事件，后续的触摸事件就不处理了
				return true;
			}
		});
		// 设置暂停按钮的监听
		bt_Stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (evolution != 0) {
					Log.i("test", "暂停按钮被点击了");
					Thread.yield();// 暂停UI前线程
					evolution = -1;
				} else {
					Toast.makeText(MainActivity.this, "细胞已终止演化",
							Toast.LENGTH_SHORT).show();
				}
			}
		});// 暂停按钮监听结束

		// 设置终止按钮的监听
		bt_End.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				evolution = 0;
				T_cellVolution.interrupt();
				Toast.makeText(MainActivity.this, "细胞已终止演化", Toast.LENGTH_SHORT)
						.show();
			}
		});// 终止按钮监听结束

		// 设置自动生成细胞按钮的监听
		bt_CellAutomaticGeneration.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				evolution = -1;
				CreateCellFromAPI();// 通过API创建细胞
			}
		});
		// 设置开始按钮的监听
		bt_Start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (evolution != 0) {
					Log.i("test", "开始按钮被点击了");
					// TODO Auto-generated method stub
					T_cellVolution = new Thread() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							super.run();
							if (evolution != 1) {// 防止用户多次点击开始按钮
								evolution = 1;
								// --------------绘制-以及-获取下一张细胞图--的区域
								while (evolution == 1) {
									long start = System.currentTimeMillis();
									CellImage cellImage = new CellImage(
											TheAreaExistCell);// 创建一张细胞图对象
									CellImage_List = cellImage
											.getCellImage_List();// 获得细胞图中的装有所有细胞信息的list集合
									for (Cell cellFromCellImage : CellImage_List) {// 开始遍历并进行画图和生成下一次的细胞图
										// 画图---如果细胞是或者的这将其变为黑色
										if (cellFromCellImage.Survival == 1) {
											int Y = cellFromCellImage.Coordinate_Y * 20;// 20*20=400个像素作为一个细胞
											int X = cellFromCellImage.Coordinate_X * 20;
											paint.setColor(0XFF000000);
											canvas.drawRect(X, Y, X + 20,
													Y + 20, paint);
											TheAreaExistCell[cellFromCellImage.Coordinate_Y][cellFromCellImage.Coordinate_X] = 1;
										} else {// 如果细胞为死亡则将其置为白色
											int Y = cellFromCellImage.Coordinate_Y * 20;// 20*20=400个像素作为一个细胞
											int X = cellFromCellImage.Coordinate_X * 20;
											paint.setColor(Color.WHITE);
											canvas.drawRect(X, Y, X + 20,
													Y + 20, paint);
											TheAreaExistCell[cellFromCellImage.Coordinate_Y][cellFromCellImage.Coordinate_X] = 0;
										}// 画图结束

										// 生成新的TheAreaExistCell矩阵---其实只是在原有矩阵上通过生命游戏的规则对矩阵进行更改
										TheAreaExistCell[cellFromCellImage.Coordinate_Y][cellFromCellImage.Coordinate_X] = Utils
												.JudgeSurvivalResources(
														cellFromCellImage.Survival,
														cellFromCellImage.NeighbourCount);
									}// 增强for循环结束
										// iv_GameBG.setImageBitmap(bm_GameBG_copy);
									long end = System.currentTimeMillis();
									// --------------绘制-以及-获取下一张细胞图--的区域
									if (end - start < evolutionTime) {// 设置evolutionTime毫秒演化1次
										try {
											Thread.sleep(evolutionTime
													- (end - start));
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
									// 发送消息
									Message msg = Message.obtain();
									msg.obj = bm_GameBG_copy;
									msg.what = 1;
									handler.sendMessage(msg);
								}// 用于绘制细胞图的while循环结束
							}// if语句结束
						}
					};
					T_cellVolution.start();
				} else {
					Toast.makeText(MainActivity.this, "细胞已终止演化",
							Toast.LENGTH_SHORT).show();
				}
			}// 监听函数结束
		});// 开始按钮监听结束

		// 设置存档按钮监听
		bt_Save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.i("test", "点击了保存存档按钮");
				String SaveFileName = et_FileName.getText().toString();// 获取文件名
				Log.i("test文件名", SaveFileName);
				// TODO Auto-generated method stub
				File file = new File("sdcard/" + SaveFileName + ".txt");
				try {
					file.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Log.i("test", "文件没有创建");
				}
				try {
					FileOutputStream fos = new FileOutputStream(file);
					// 将TheAreaExistCell数组按照int类型一个一个的存入文件中
					for (int y = 0; y < TheAreaExistCell.length; y++) {
						for (int x = 0; x < TheAreaExistCell[y].length; x++) {
							fos.write(TheAreaExistCell[y][x]);
						}
					}
					Toast.makeText(
							MainActivity.this,
							"存档成功(～￣▽￣)～  --- 保存路径为：" + "sdcard/GameOfTheLife/"
									+ SaveFileName + ".txt", Toast.LENGTH_SHORT)
							.show();
					Log.i("test", "存档成功");
					fos.close();
					// test
					/*
					 * for (int y = 0; y < TheAreaExistCell.length; y++) { for
					 * (int x = 0; x < TheAreaExistCell[y].length; x++) {
					 * Log.i("testSave" + y + ";" + x, TheAreaExistCell[y][x] +
					 * ""); } }
					 */
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(MainActivity.this,
							"存档失败╮(╯﹏╰)╭   ---请检查你的路径是否错误!", Toast.LENGTH_LONG)
							.show();
					Log.i("test", "存档失败");
				}
			}
		});// 存档按钮监听结束

		// 设置读档按钮监听
		bt_Load.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String LoadFileName = et_FileName.getText().toString();// 获取文件名
				try {
					FileInputStream fis = new FileInputStream("sdcard/"
							+ LoadFileName + ".txt");
					int D;
					int y = 0;// TheAreaExistCell的行
					int x = 0;// TheAreaExistCell的列
					while ((D = fis.read()) != -1) {
						// 如果x(列数)到达最大值则行数自加1
						if (x == TheAreaExistCell[y].length) {
							x = 0;
							y++;
						}
						TheAreaExistCell[y][x] = D;
						x++;
					}
					fis.close();
					// test
					for (int y1 = 0; y1 < TheAreaExistCell.length; y1++) {
						for (int x1 = 0; x1 < TheAreaExistCell[y1].length; x1++) {
							Log.i("testLoad" + y1 + ";" + x1,
									TheAreaExistCell[y1][x1] + "");
						}
					}

					for (int y1 = 0; y1 < TheAreaExistCell.length * 20; y1++) {
						for (int x1 = 0; x1 < TheAreaExistCell[0].length * 20; x1++) {
							bm_GameBG_copy.setPixel(y1, x1, Color.WHITE);
						}
					}
					iv_GameBG.setImageBitmap(bm_GameBG_copy);
					// 通过TheAreaExistCell矩阵将图形画到游戏区域上
					// ---------------------------------注意：此操作会阻塞UI线程，需要放到子线程中
					for (int Y = 0; Y < TheAreaExistCell.length; Y++) {
						for (int X = 0; X < TheAreaExistCell[Y].length; X++) {
							if (TheAreaExistCell[Y][X] == 1) {
								paint.setColor(Color.BLACK);
								canvas.drawRect(Y * 20, X * 20, Y * 20 + 20,
										X * 20 + 20, paint);
								iv_GameBG.setImageBitmap(bm_GameBG_copy);
							}
						}
					}
					Toast.makeText(MainActivity.this, "读档完成~\\(≧▽≦)/~",
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(MainActivity.this,
							"读档失败╮(╯﹏╰)╭   ---请检查你的路径是否错误!", Toast.LENGTH_SHORT)
							.show();
				}
				// 将游戏区域初始化（也就是重新将其变成白色）
			}
		});// 读档按钮监听结束

		// 设置规则按钮监听
		bt_Rule.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent gotoRule = new Intent(MainActivity.this,
						Activity_Rule.class);
				startActivity(gotoRule);
			}
		});
		// 设置加速演化按钮监听
		bt_speedEvolution.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (evolution != 0) {
					if (evolutionTime > 100) {
						evolutionTime -= 100;
					} else {
						Toast.makeText(MainActivity.this,
								"速度已经最大了， 已经比刘翔还快了···", Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					Toast.makeText(MainActivity.this, "细胞已终止演化",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		// 设置减速演化按钮监听
		bt_decelerateEvolution.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (evolution != 0) {
					if ((evolutionTime - 3000) < 0) {
						evolutionTime += 100;
					} else {
						Toast.makeText(MainActivity.this,
								"速度已经最慢了，蜗牛都没这么慢呐···", Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					Toast.makeText(MainActivity.this, "细胞已终止演化",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		// 设置清除所有细胞按钮的监听
		bt_reset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				evolution = 0;
				T_cellVolution.interrupt();
				for (int y1 = 0; y1 < TheAreaExistCell.length * 20; y1++) {
					for (int x1 = 0; x1 < TheAreaExistCell[0].length * 20; x1++) {
						bm_GameBG_copy.setPixel(y1, x1, Color.WHITE);
					}
				}
				iv_GameBG.setImageBitmap(bm_GameBG_copy);
				for (int y = 0; y < TheAreaExistCell.length; y++) {
					for (int x = 0; x < TheAreaExistCell[y].length; x++) {
						TheAreaExistCell[y][x] = 0;
					}
				}
				Toast.makeText(MainActivity.this, "清除完成", Toast.LENGTH_SHORT)
						.show();
			}
		});

	}// onCreate结束
}// 公有类结束
// 细胞图类---用于实现保存功能

class CellImage {
	static private int EvolutionCount = -1;// 用于存储演化次数，因为第一次自定义的那张细胞图不算是迭代出来的，所以需要去掉那张图，因此演化次数从-1开始
	private List<Cell> CellImage_List = new ArrayList<Cell>();// 用于存储迭代一次之后产生的一个细胞图中每个细胞的信息---用于实现保存的目的

	// 通过TheAreaExistCell也就是通过这个细胞存活区域的矩阵生成一个细胞图
	public CellImage(int[][] TheAreaExistCell) {
		EvolutionCount++;// 每生成一个细胞图演化次数加1
		for (int y = 0; y < TheAreaExistCell.length; y++) {// 50
			for (int x = 0; x < TheAreaExistCell[y].length; x++) {// 50
				Cell cell = new Cell();
				cell.Survival = TheAreaExistCell[y][x];
				cell.Coordinate_X = x;// 横坐标---数组的第二个下标--最大值为50---列
				cell.Coordinate_Y = y;// 纵坐标---数组的第一个下标--最大值为50---行
				cell.NeighbourCount = Utils.gainNeighbourCount(x, y,
						TheAreaExistCell);
				// test
				/*
				 * Log.i("test-TheAreaExistCelly:x--" + y + ":" + x,
				 * TheAreaExistCell[y][x] + ""); Log.i("test-cell--x",
				 * cell.Coordinate_X + ""); Log.i("test-cell--y",
				 * cell.Coordinate_Y + ""); Log.i("test-cell--NeighbourCount",
				 * cell.NeighbourCount + "");
				 */
				CellImage_List.add(cell);
			}
		}// 外层for循环结束
		Log.i("test_CellImage_List的长度", CellImage_List.size() + "");
	}// CellImage类构造方法结束

	public List<Cell> getCellImage_List() {
		return CellImage_List;
	}

	public static int getEvolutionCount() {
		return EvolutionCount;
	}
}// 细胞图类结束

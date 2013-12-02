package com.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ServiceComposition extends JFrame implements ActionListener {

	// 文件地址
	String fileName = "";
	// 存储所有服务的列表
	public List<Dag> dags = new ArrayList<Dag>();
	// 循环当前选中的服务
	public Set<Dag> curDags;
	public String input = "AE";
	public String output = "IJK";
	// s表示已经选中服务的输出参数的并集
	public String s = input;
	// 保存所选择的服务id,时间,选择的文件
	public String sid = "", tim;

	// 测试次数
	int testTime = 100, min = 10000000;
	String finalResult;
	// 存储所有结果
	List<Result> allResult = new ArrayList<Result>();
	Set<Result> results = new HashSet<Result>();

	long startTime, endTime;
	MyPanel myPanel = null;
	int curIndex = 0;// 当前放第几个结果
	// 文件选择器
	JFileChooser jc = new JFileChooser();

	public ServiceComposition() {
		// exc();
		// // 界面相关
		// myPanel = new MyPanel(allResult.get(0).getResult(), dags, tim,
		// fileName);
		JPanel jp = new JPanel();
		jc.setDialogTitle("选择文件");

		JButton choose = new JButton("选择文件");
		choose.setActionCommand("choose");
		choose.addActionListener(this);

		JButton exe = new JButton("运行");
		exe.setActionCommand("exe");
		exe.addActionListener(this);

		JButton jb = new JButton("上一个结果");
		jb.setActionCommand("previous");
		jb.addActionListener(this);

		JButton jb1 = new JButton("下一个结果");
		jb1.addActionListener(this);
		jb1.setActionCommand("next");

		jp.add(choose);
		jp.add(exe);
		jp.add(jb);
		jp.add(jb1);
		this.add(jp, BorderLayout.SOUTH);
		// this.add(myPanel);
		this.setTitle("结果图形化展示");
		this.setSize(900, 750);
		this.setLocation(200, 150);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public void exc() {
		startTime = System.currentTimeMillis();
		readFileByLines(fileName);
		System.out.println("数据处理后大小： " + dags.size());
		process();
		endTime = System.currentTimeMillis();
		tim = "程序运行时间为: " + (endTime - startTime) + "ms";
		System.out.println("最少服务组合Id为： " + finalResult);
		System.out.println(tim);

		// 保存所有结果到列表
		for (Result r : results) {
			if (r.getMin() <= min) {
				allResult.add(r);
			}
		}
	}

	/**
	 * 算法主过程
	 */
	public void process() {
		int ii = 0;
		// 重复测试testTime次找到最好结果
		while (ii++ < testTime) {
			s = input;
			sid = "";
			// 只要s没有包含所有输出的服务项
			while (!isContain(s, output)) {

				// 如果当前结果已经比已知最好的结果差了，那么则直接跳入下一次循环
				String[] curLen = sid.split(" ");
				if (curLen.length > min) {
					break;
				}

				curDags = new HashSet<Dag>();
				// 得到所有以s为输入集合能够调用到的服务集合，且不是属于最终输出服务集
				for (Dag dag : dags) {
					// 如果s包含服务中输入参数的所有项则一定能够调用该服务
					if (isContain(s, dag.getInput())) {
						// 如果当前服务节点输出项已经包含在已经得到的输出序列的话，则不扩展
						if (!isContain(s, dag.getOutput())) {
							curDags.add(dag);
						}
					}
				}

				int f = 0, max = -1, iss = 0;
				Dag tDag = null;
				List<Dag> maxDag = new ArrayList<Dag>();

				// 为了将列表随机打乱，这样每次测试能遍历不同的顺序能够得到不同的结果
				List<Dag> tolDags = new ArrayList<Dag>();
				tolDags.addAll(curDags);
				Collections.shuffle(tolDags);

				// 对于所有能调用的集合
				for (Dag dag : tolDags) {
					Set<Dag> tmpDags = new HashSet<Dag>();
					// 将当前服务所能调用的服务项加入到s中，重复的不加
					String tmpS = addString(s, dag.getOutput());

					// 如果调用该服务所得到的字符串已经包含所有的输出参数，则调用该服务就为一个最好的解
					if (isContain(sub_String(tmpS, dag.getInput()), output)) {
						iss = 1;
						tDag = dag;
						break;
					}

					// 得到后继服务集合
					for (Dag dag1 : dags) {
						// 如果s包含服务中输入参数的所有项则一定能够调用该服务
						if (isContain(tmpS, dag1.getInput())) {
							tmpDags.add(dag1);
						}
					}

					// s0/s
					String ss = sub_String(output, s);
					// 保存每个后继节点的匹配值
					int tmpSuc = 0;
					for (Dag dag2 : tmpDags) {
						// 表示后继服务的输入至少有一个元素引自于当前服务节点的输出，这才为当前节点的后继服务
						if (isContainSame(dag2.getInput(), dag.getOutput())) {
							tmpSuc += countSame(ss, dag2.getOutput());
						}
					}

					int dws = countSame(ss, dag.getOutput());
					// int hws = dws+successor.length();
					int hws = dws + tmpSuc;
					int gws = countSame(sub_String(s, input), output);
					f = gws + hws;
					dag.setF(f);
					if (max < f) {
						max = f;
					}
				}

				if (iss == 0) {
					for (Dag dag : curDags) {
						if (dag.getF() == max) {
							maxDag.add(dag);
						}
					}
					// 从最大匹配值中随机挑取一个值
					int id = (int) (Math.random() * maxDag.size());
					sid += maxDag.get(id).getId() + " ";
					s = addString(s, maxDag.get(id).getOutput());
				} else if (iss == 1) {
					sid += tDag.getId() + " ";
					s = addString(s, tDag.getOutput());
				}
			}
			// System.out.println("第" + ii + "次结果为:   " + solu);

			if (isContain(s, output)) {
				String[] tt = sid.split(" ");

				if (tt.length <= min) {
					min = tt.length;
					finalResult = sid;

					// 为了保存多种结果
					boolean inculde = false;
					for (Result r : results) {
						if (r.getResult().equals(sid)) {
							inculde = true;
							break;
						}
					}

					if (!inculde) {
						Result result = new Result();
						result.setMin(tt.length);
						result.setResult(sid);
						results.add(result);
					}
				}
			}
		}

	}

	/**
	 * 判断字符串s1中是否包含s2中的每个字符，是的话返回真
	 */
	public boolean isContain(String s1, String s2) {
		if (s1.equals("")) {
			return false;
		}

		for (int i = 0; i < s2.length(); i++) {
			if (s1.indexOf(s2.charAt(i)) == -1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断字符串s1中是否与s2包含相同的字符，是的话返回真
	 */
	public boolean isContainSame(String s1, String s2) {
		if (s1.equals("") || s2.equals("")) {
			return false;
		}

		for (int i = 0; i < s2.length(); i++) {
			if (s1.indexOf(s2.charAt(i)) != -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 字符串s1中加入s2中的不在s1中的每个字符，返回加后的新字符串
	 */
	public String addString(String s1, String s2) {
		String ss = s1;
		for (int i = 0; i < s2.length(); i++) {
			if (ss.indexOf(s2.charAt(i)) == -1) {
				ss += s2.charAt(i);
			}
		}
		return ss;
	}

	/**
	 * 字符串s1去掉在s2中的字符，返回新字符串
	 */
	public String sub_String(String s1, String s2) {
		String ss = s1;
		for (int i = 0; i < s2.length(); i++) {
			if (ss.indexOf(s2.charAt(i)) != -1) {
				ss = ss.replace(s2.charAt(i), ' ');
				ss = ss.replace(" ", "");
			}
		}
		return ss;
	}

	/**
	 * 判断字符串s1是否与s2相等不考虑字符串顺序,相等返回真
	 */
	public boolean isEqual(String s1, String s2) {
		return isContain(s1, s2) && isContain(s2, s1);
	}

	/**
	 * 得到字符串s1和s2相同字符的个数
	 */
	public int countSame(String s1, String s2) {
		if (s1.equals("")) {
			return 0;
		}

		int num = 0;
		for (int i = 0; i < s2.length(); i++) {
			if (s1.indexOf(s2.charAt(i)) != -1) {
				num++;
			}
		}
		return num;
	}

	/**
	 * 读取文件存取结果
	 */
	public void readFileByLines(String fileName) {
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tmp[], tempString = null, inpu, outp;
			Dag tmpDag = null;
			boolean is = false;
			Set<Dag> toDeleDags = new HashSet<Dag>();

			while ((tempString = reader.readLine()) != null) {
				is = false;
				// 分别设置每个服务的id，输入参数，输出参数,并加入到服务列表
				tmp = tempString.split("\\|");
				inpu = tmp[1].replace(" ", "");
				outp = tmp[2].replace(" ", "");

				// 进行输入数据的处理
				for (Dag d : dags) {
					// 如果输出参数相同的话保留输入参数个数少的D->bC, DE->BC则删除DE这组数据
					// 因为输入输出参数顺序不一样因此不能简单用equal判断是否相同
					if (isEqual(d.getOutput(), outp)) {
						if (isContain(d.getInput(), inpu)) {
							toDeleDags.add(d);
						} else if (isContain(inpu, d.getInput())) {
							is = true;
							break;
						}
					}
					// 如果输入参数相同的话保留输出参数个数多的D->BC, D->BCD则删除D->BC这组数据
					else if (isEqual(d.getInput(), inpu)) {
						if (isContain(outp, d.getOutput())) {
							toDeleDags.add(d);
						} else if (isContain(d.getOutput(), outp)) {
							is = true;
							break;
						}
					}
				}

				// 该数据情况已经被包含，则不添加
				if (is) {
					continue;
				}

				tmpDag = new Dag(tmp[0]);
				tmpDag.setInput(inpu);
				tmpDag.setOutput(outp);
				dags.add(tmpDag);
			}
			// 删除所有不可能扩展的服务节点
			dags.removeAll(toDeleDags);

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ServiceComposition();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		// 下一个结果
		if (arg0.getActionCommand().equals("next")) {
			// System.out.println("==========" + allResult.size());
			if (myPanel != null && curIndex + 1 < allResult.size()) {
				Result r = allResult.get(curIndex + 1);
				this.remove(myPanel);
				myPanel = new MyPanel(r.getResult(), dags, tim, fileName);
				System.out.println("最小服务组合为： " + r.getResult());
				this.add(myPanel);
				this.setVisible(true);
				this.setFocusable(true);
				myPanel.repaint();
				curIndex++;
			}
		}
		// 上一个结果
		else if (arg0.getActionCommand().equals("previous")) {
			// System.out.println("==========" + allResult.size());
			if (myPanel != null && curIndex - 1 >= 0) {
				Result r = allResult.get(curIndex - 1);
				this.remove(myPanel);
				myPanel = new MyPanel(r.getResult(), dags, tim, fileName);
				System.out.println("最小服务组合为： " + r.getResult());
				this.add(myPanel);
				this.setVisible(true);
				this.setFocusable(true);
				myPanel.repaint();
				curIndex--;
			}
		}
		// 选择文件
		else if (arg0.getActionCommand().equals("choose")) {
			int returnVal = jc.showOpenDialog(null);
			// 如果选择了打开的话
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File f = jc.getSelectedFile();
				fileName = f.getPath();
				System.out.println("当前执行文件为： " + fileName);
			}
		}
		// 运行
		else if (arg0.getActionCommand().equals("exe")) {
			if (fileName != null && !fileName.equals("")) {
				// 初始化值
				curIndex = 0;
				min = 10000000;
				dags.clear();
				allResult.clear();
				results.clear();
				// 执行结果
				exc();
				if (myPanel != null) {
					this.remove(myPanel);
				}
				myPanel = new MyPanel(allResult.get(0).getResult(), dags, tim,
						fileName);
				this.add(myPanel);
				this.setVisible(true);
				this.setFocusable(true);
				myPanel.repaint();
			}
		}
	}
}

/**
 * 画板类
 */
class MyPanel extends JPanel {

	List<Dag> drawDags = new ArrayList<Dag>();
	// 每个服务之间间隔
	int space = 100, lineLen = 60, rectLen = 40;
	Dag dag;
	String input, output, finalResult;
	char o[];
	int as[], asd[], k = 0, is = 0, tol;
	List<String> ms = new ArrayList<String>();

	public MyPanel(String finalResult, List<Dag> dags, String tim,
			String fileName) {
		// 初始输入参数加到绘制节点中
		Dag dd = new Dag("Input");
		dd.setOutput("AE");
		this.drawDags.add(dd);

		// 文件名
		ms.add("当前执行文件为： " + fileName);

		// 服务组合信息
		String mms = "最少服务组合为： ";
		ms.add(mms);
		String t[] = finalResult.split(" ");
		for (String s : t) {
			for (Dag d : dags) {
				if (d.getId().equals(s)) {
					this.drawDags.add(d);
					mms = "";
					mms += "ID: ";
					mms += d.getId();
					mms += "    Input: ";
					mms += d.getInput();
					mms += "    Ouput: ";
					mms += d.getOutput();
					ms.add(mms);
					break;
				}
			}
		}

		// 时间
		ms.add(tim);
		// 用来控制文字下方离图线的距离
		tol = ms.size();
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 900, 700);

		for (int mm = 0; mm < ms.size(); mm++) {
			// 绘制结果字符串
			g.setColor(Color.BLACK);
			g.drawString(ms.get(mm), 10, 20 + mm * 15);
		}

		for (int i = 0; i < drawDags.size(); i++) {
			dag = drawDags.get(i);
			// 画服务矩形节点
			g.setColor(Color.RED);
			g.draw3DRect(10 + space * i, 20 * tol + space, rectLen, rectLen,
					false);

			// 在服务矩形内部绘制服务id
			g.setColor(Color.BLACK);
			g.drawString(dag.getId(), 13 + space * i, 20 * tol + space + 25);

			// 绘制输入参数
			if (i != 0) {
				g.setColor(Color.BLACK);
				g.drawString(dag.getInput(), space * i + 20, 20 * tol + space
						- 5);
			}

			String q[] = dag.getOutput().split("");
			for (int m = 0; m < q.length; m++) {
				String qq = q[m];
				if (qq.equals("I") || qq.equals("J") || qq.equals("K")) {
					// 绘制输出参数,目的输出参数变红
					g.setColor(Color.RED);
					g.drawString(qq, lineLen + space * i + m * 10, 20 * tol
							+ space + 15);
				} else {
					// 绘制输出参数
					g.setColor(Color.BLACK);
					g.drawString(qq, lineLen + space * i + m * 10, 20 * tol
							+ space + 15);
				}
			}

			as = new int[27];
			asd = new int[27];

			if (i != drawDags.size() - 1) {
				input = drawDags.get(i + 1).getInput();
				char s[] = input.toCharArray();

				for (int u = 0; u < 27; u++) {
					as[u] = 0;
				}
				// 将要绘制的输入都转换到int数组从1到26
				for (int u = 0; u < s.length; u++) {
					as[s[u] - 'A']++;
				}

				is = 0;
				for (int kk = 0; kk < 27; kk++) {
					if (as[kk] == 0) {
						continue;
					}

					// 遍历前面哪个包含输入的服务项
					for (int j = 0; j <= i; j++) {
						for (int u = 0; u < 27; u++) {
							asd[u] = 0;
						}

						output = drawDags.get(j).getOutput();
						o = output.toCharArray();
						// 前面每个服务的输出项保存到asd中
						for (int u = 0; u < o.length; u++) {
							asd[o[u] - 'A']++;
						}

						// 如果包含的话
						if (as[kk] == asd[kk]) {
							as[kk] = 0;
							// 说明是直接前驱则画直线
							if (j == i) {
								// 画服务之间一条线
								g.setColor(Color.DARK_GRAY);
								g.drawLine(50 + space * i, 20 * tol + 20
										+ space, 50 + lineLen + space * i, 20
										* tol + 20 + space);
							} else {
								// 否则画一条竖横竖的直线连接
								g.setColor(Color.DARK_GRAY);
								g.drawLine(70 + space * j, 20 * tol + space
										+ 20, 70 + space * j, 20 * tol + space
										+ 100 + k * 15);
								// 长度 90+ space * j+ (lineLen * (i - j) +
								// (i - j + 1) * rectLen,整理后为130+100*i
								g.drawLine(70 + space * j, 20 * tol + space
										+ 100 + k * 15, 130 + i * space, 20
										* tol + space + 100 + k * 15);
								g.drawLine(130 + i * space, 20 * tol + space
										+ 40, 130 + i * space, 20 * tol + space
										+ 100 + k * 15);
								is = 1;
							}
							break;
						}
					}
				}// end for kk

				if (is == 1) {
					// 下一次画竖横竖的线时向下移动15*k距离，为了区分开每条横线
					k++;
				}
			}
		}
	}
}

class Result {
	private int min;
	private String result;

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}

/**
 * 每个服务数据结构采用有向图表示
 */
class Dag {
	private String id;
	private String input;
	private String output;
	private int f;

	public Dag(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public int getF() {
		return f;
	}

	public void setF(int f) {
		this.f = f;
	}
}

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

	// �ļ���ַ
	String fileName = "";
	// �洢���з�����б�
	public List<Dag> dags = new ArrayList<Dag>();
	// ѭ����ǰѡ�еķ���
	public Set<Dag> curDags;
	public String input = "AE";
	public String output = "IJK";
	// s��ʾ�Ѿ�ѡ�з������������Ĳ���
	public String s = input;
	// ������ѡ��ķ���id,ʱ��,ѡ����ļ�
	public String sid = "", tim;

	// ���Դ���
	int testTime = 100, min = 10000000;
	String finalResult;
	// �洢���н��
	List<Result> allResult = new ArrayList<Result>();
	Set<Result> results = new HashSet<Result>();

	long startTime, endTime;
	MyPanel myPanel = null;
	int curIndex = 0;// ��ǰ�ŵڼ������
	// �ļ�ѡ����
	JFileChooser jc = new JFileChooser();

	public ServiceComposition() {
		// exc();
		// // �������
		// myPanel = new MyPanel(allResult.get(0).getResult(), dags, tim,
		// fileName);
		JPanel jp = new JPanel();
		jc.setDialogTitle("ѡ���ļ�");

		JButton choose = new JButton("ѡ���ļ�");
		choose.setActionCommand("choose");
		choose.addActionListener(this);

		JButton exe = new JButton("����");
		exe.setActionCommand("exe");
		exe.addActionListener(this);

		JButton jb = new JButton("��һ�����");
		jb.setActionCommand("previous");
		jb.addActionListener(this);

		JButton jb1 = new JButton("��һ�����");
		jb1.addActionListener(this);
		jb1.setActionCommand("next");

		jp.add(choose);
		jp.add(exe);
		jp.add(jb);
		jp.add(jb1);
		this.add(jp, BorderLayout.SOUTH);
		// this.add(myPanel);
		this.setTitle("���ͼ�λ�չʾ");
		this.setSize(900, 750);
		this.setLocation(200, 150);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public void exc() {
		startTime = System.currentTimeMillis();
		readFileByLines(fileName);
		System.out.println("���ݴ�����С�� " + dags.size());
		process();
		endTime = System.currentTimeMillis();
		tim = "��������ʱ��Ϊ: " + (endTime - startTime) + "ms";
		System.out.println("���ٷ������IdΪ�� " + finalResult);
		System.out.println(tim);

		// �������н�����б�
		for (Result r : results) {
			if (r.getMin() <= min) {
				allResult.add(r);
			}
		}
	}

	/**
	 * �㷨������
	 */
	public void process() {
		int ii = 0;
		// �ظ�����testTime���ҵ���ý��
		while (ii++ < testTime) {
			s = input;
			sid = "";
			// ֻҪsû�а�����������ķ�����
			while (!isContain(s, output)) {

				// �����ǰ����Ѿ�����֪��õĽ�����ˣ���ô��ֱ��������һ��ѭ��
				String[] curLen = sid.split(" ");
				if (curLen.length > min) {
					break;
				}

				curDags = new HashSet<Dag>();
				// �õ�������sΪ���뼯���ܹ����õ��ķ��񼯺ϣ��Ҳ������������������
				for (Dag dag : dags) {
					// ���s���������������������������һ���ܹ����ø÷���
					if (isContain(s, dag.getInput())) {
						// �����ǰ����ڵ�������Ѿ��������Ѿ��õ���������еĻ�������չ
						if (!isContain(s, dag.getOutput())) {
							curDags.add(dag);
						}
					}
				}

				int f = 0, max = -1, iss = 0;
				Dag tDag = null;
				List<Dag> maxDag = new ArrayList<Dag>();

				// Ϊ�˽��б�������ң�����ÿ�β����ܱ�����ͬ��˳���ܹ��õ���ͬ�Ľ��
				List<Dag> tolDags = new ArrayList<Dag>();
				tolDags.addAll(curDags);
				Collections.shuffle(tolDags);

				// ���������ܵ��õļ���
				for (Dag dag : tolDags) {
					Set<Dag> tmpDags = new HashSet<Dag>();
					// ����ǰ�������ܵ��õķ�������뵽s�У��ظ��Ĳ���
					String tmpS = addString(s, dag.getOutput());

					// ������ø÷������õ����ַ����Ѿ��������е��������������ø÷����Ϊһ����õĽ�
					if (isContain(sub_String(tmpS, dag.getInput()), output)) {
						iss = 1;
						tDag = dag;
						break;
					}

					// �õ���̷��񼯺�
					for (Dag dag1 : dags) {
						// ���s���������������������������һ���ܹ����ø÷���
						if (isContain(tmpS, dag1.getInput())) {
							tmpDags.add(dag1);
						}
					}

					// s0/s
					String ss = sub_String(output, s);
					// ����ÿ����̽ڵ��ƥ��ֵ
					int tmpSuc = 0;
					for (Dag dag2 : tmpDags) {
						// ��ʾ��̷��������������һ��Ԫ�������ڵ�ǰ����ڵ����������Ϊ��ǰ�ڵ�ĺ�̷���
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
					// �����ƥ��ֵ�������ȡһ��ֵ
					int id = (int) (Math.random() * maxDag.size());
					sid += maxDag.get(id).getId() + " ";
					s = addString(s, maxDag.get(id).getOutput());
				} else if (iss == 1) {
					sid += tDag.getId() + " ";
					s = addString(s, tDag.getOutput());
				}
			}
			// System.out.println("��" + ii + "�ν��Ϊ:   " + solu);

			if (isContain(s, output)) {
				String[] tt = sid.split(" ");

				if (tt.length <= min) {
					min = tt.length;
					finalResult = sid;

					// Ϊ�˱�����ֽ��
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
	 * �ж��ַ���s1���Ƿ����s2�е�ÿ���ַ����ǵĻ�������
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
	 * �ж��ַ���s1���Ƿ���s2������ͬ���ַ����ǵĻ�������
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
	 * �ַ���s1�м���s2�еĲ���s1�е�ÿ���ַ������ؼӺ�����ַ���
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
	 * �ַ���s1ȥ����s2�е��ַ����������ַ���
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
	 * �ж��ַ���s1�Ƿ���s2��Ȳ������ַ���˳��,��ȷ�����
	 */
	public boolean isEqual(String s1, String s2) {
		return isContain(s1, s2) && isContain(s2, s1);
	}

	/**
	 * �õ��ַ���s1��s2��ͬ�ַ��ĸ���
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
	 * ��ȡ�ļ���ȡ���
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
				// �ֱ�����ÿ�������id������������������,�����뵽�����б�
				tmp = tempString.split("\\|");
				inpu = tmp[1].replace(" ", "");
				outp = tmp[2].replace(" ", "");

				// �����������ݵĴ���
				for (Dag d : dags) {
					// ������������ͬ�Ļ�����������������ٵ�D->bC, DE->BC��ɾ��DE��������
					// ��Ϊ�����������˳��һ����˲��ܼ���equal�ж��Ƿ���ͬ
					if (isEqual(d.getOutput(), outp)) {
						if (isContain(d.getInput(), inpu)) {
							toDeleDags.add(d);
						} else if (isContain(inpu, d.getInput())) {
							is = true;
							break;
						}
					}
					// ������������ͬ�Ļ�������������������D->BC, D->BCD��ɾ��D->BC��������
					else if (isEqual(d.getInput(), inpu)) {
						if (isContain(outp, d.getOutput())) {
							toDeleDags.add(d);
						} else if (isContain(d.getOutput(), outp)) {
							is = true;
							break;
						}
					}
				}

				// ����������Ѿ��������������
				if (is) {
					continue;
				}

				tmpDag = new Dag(tmp[0]);
				tmpDag.setInput(inpu);
				tmpDag.setOutput(outp);
				dags.add(tmpDag);
			}
			// ɾ�����в�������չ�ķ���ڵ�
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
		// ��һ�����
		if (arg0.getActionCommand().equals("next")) {
			// System.out.println("==========" + allResult.size());
			if (myPanel != null && curIndex + 1 < allResult.size()) {
				Result r = allResult.get(curIndex + 1);
				this.remove(myPanel);
				myPanel = new MyPanel(r.getResult(), dags, tim, fileName);
				System.out.println("��С�������Ϊ�� " + r.getResult());
				this.add(myPanel);
				this.setVisible(true);
				this.setFocusable(true);
				myPanel.repaint();
				curIndex++;
			}
		}
		// ��һ�����
		else if (arg0.getActionCommand().equals("previous")) {
			// System.out.println("==========" + allResult.size());
			if (myPanel != null && curIndex - 1 >= 0) {
				Result r = allResult.get(curIndex - 1);
				this.remove(myPanel);
				myPanel = new MyPanel(r.getResult(), dags, tim, fileName);
				System.out.println("��С�������Ϊ�� " + r.getResult());
				this.add(myPanel);
				this.setVisible(true);
				this.setFocusable(true);
				myPanel.repaint();
				curIndex--;
			}
		}
		// ѡ���ļ�
		else if (arg0.getActionCommand().equals("choose")) {
			int returnVal = jc.showOpenDialog(null);
			// ���ѡ���˴򿪵Ļ�
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File f = jc.getSelectedFile();
				fileName = f.getPath();
				System.out.println("��ǰִ���ļ�Ϊ�� " + fileName);
			}
		}
		// ����
		else if (arg0.getActionCommand().equals("exe")) {
			if (fileName != null && !fileName.equals("")) {
				// ��ʼ��ֵ
				curIndex = 0;
				min = 10000000;
				dags.clear();
				allResult.clear();
				results.clear();
				// ִ�н��
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
 * ������
 */
class MyPanel extends JPanel {

	List<Dag> drawDags = new ArrayList<Dag>();
	// ÿ������֮����
	int space = 100, lineLen = 60, rectLen = 40;
	Dag dag;
	String input, output, finalResult;
	char o[];
	int as[], asd[], k = 0, is = 0, tol;
	List<String> ms = new ArrayList<String>();

	public MyPanel(String finalResult, List<Dag> dags, String tim,
			String fileName) {
		// ��ʼ��������ӵ����ƽڵ���
		Dag dd = new Dag("Input");
		dd.setOutput("AE");
		this.drawDags.add(dd);

		// �ļ���
		ms.add("��ǰִ���ļ�Ϊ�� " + fileName);

		// ���������Ϣ
		String mms = "���ٷ������Ϊ�� ";
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

		// ʱ��
		ms.add(tim);
		// �������������·���ͼ�ߵľ���
		tol = ms.size();
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 900, 700);

		for (int mm = 0; mm < ms.size(); mm++) {
			// ���ƽ���ַ���
			g.setColor(Color.BLACK);
			g.drawString(ms.get(mm), 10, 20 + mm * 15);
		}

		for (int i = 0; i < drawDags.size(); i++) {
			dag = drawDags.get(i);
			// ��������νڵ�
			g.setColor(Color.RED);
			g.draw3DRect(10 + space * i, 20 * tol + space, rectLen, rectLen,
					false);

			// �ڷ�������ڲ����Ʒ���id
			g.setColor(Color.BLACK);
			g.drawString(dag.getId(), 13 + space * i, 20 * tol + space + 25);

			// �����������
			if (i != 0) {
				g.setColor(Color.BLACK);
				g.drawString(dag.getInput(), space * i + 20, 20 * tol + space
						- 5);
			}

			String q[] = dag.getOutput().split("");
			for (int m = 0; m < q.length; m++) {
				String qq = q[m];
				if (qq.equals("I") || qq.equals("J") || qq.equals("K")) {
					// �����������,Ŀ������������
					g.setColor(Color.RED);
					g.drawString(qq, lineLen + space * i + m * 10, 20 * tol
							+ space + 15);
				} else {
					// �����������
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
				// ��Ҫ���Ƶ����붼ת����int�����1��26
				for (int u = 0; u < s.length; u++) {
					as[s[u] - 'A']++;
				}

				is = 0;
				for (int kk = 0; kk < 27; kk++) {
					if (as[kk] == 0) {
						continue;
					}

					// ����ǰ���ĸ���������ķ�����
					for (int j = 0; j <= i; j++) {
						for (int u = 0; u < 27; u++) {
							asd[u] = 0;
						}

						output = drawDags.get(j).getOutput();
						o = output.toCharArray();
						// ǰ��ÿ������������浽asd��
						for (int u = 0; u < o.length; u++) {
							asd[o[u] - 'A']++;
						}

						// ��������Ļ�
						if (as[kk] == asd[kk]) {
							as[kk] = 0;
							// ˵����ֱ��ǰ����ֱ��
							if (j == i) {
								// ������֮��һ����
								g.setColor(Color.DARK_GRAY);
								g.drawLine(50 + space * i, 20 * tol + 20
										+ space, 50 + lineLen + space * i, 20
										* tol + 20 + space);
							} else {
								// ����һ����������ֱ������
								g.setColor(Color.DARK_GRAY);
								g.drawLine(70 + space * j, 20 * tol + space
										+ 20, 70 + space * j, 20 * tol + space
										+ 100 + k * 15);
								// ���� 90+ space * j+ (lineLen * (i - j) +
								// (i - j + 1) * rectLen,�����Ϊ130+100*i
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
					// ��һ�λ�����������ʱ�����ƶ�15*k���룬Ϊ�����ֿ�ÿ������
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
 * ÿ���������ݽṹ��������ͼ��ʾ
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

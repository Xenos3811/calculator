import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * �������ں�
 * 
 * @author so
 * 
 */
public class Calculator {

	private String expression;// ���ʽԭʼ��ʽ
	private Stack<String> ops = new Stack<String>();// ����ջ
	private Stack<String> vals = new Stack<String>();// ֵջ
	private HashMap<String, Method> map = new HashMap<String, Method>();// ���㷽��ջ
	private ArrayList<String> expressions = new ArrayList<String>();// �洢�������ʽ���Ԫ��
	private HashMap<Type, Class<?>> types = new HashMap<Type, Class<?>>();// �洢��������

	/**
	 * �Զ������㷽����
	 * 
	 * @author so
	 * 
	 */
	private static class DIYLIB {
		@SuppressWarnings("unused")
		public static double add(double a, double b) {
			return a + b;
		}

		@SuppressWarnings("unused")
		public static double divide(double a, double b) {
			return a / b;
		}

		@SuppressWarnings("unused")
		public static double subtract(double a, double b) {
			return a - b;
		}

		@SuppressWarnings("unused")
		public static double multiply(double a, double b) {
			return a * b;
		}
	}

	/**
	 * ������
	 * 
	 * @param expression
	 *            ������ʽ
	 */
	public Calculator(String expression) {
		this.expression = expression;
		this.initHeartMap();
		this.prepare();
	}

	/**
	 * ���س�ʼ�����Ŀ�
	 */
	private void initHeartMap() {
		try {

			types.put(Double.TYPE, Double.class);
			types.put(Integer.TYPE, Integer.class);
			types.put(Long.TYPE, Long.class);

			Method[] methods = Math.class.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				this.map.put(methods[i].getName(), methods[i]);
			}
			this.map.put("+",
					DIYLIB.class.getMethod("add", Double.TYPE, Double.TYPE));
			this.map.put("/",
					DIYLIB.class.getMethod("divide", Double.TYPE, Double.TYPE));
			this.map.put("-", DIYLIB.class.getMethod("subtract", Double.TYPE,
					Double.TYPE));
			this.map.put("*", DIYLIB.class.getMethod("multiply", Double.TYPE,
					Double.TYPE));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ϵͳ׼���׶� ��ֱ��ʽ
	 */
	private void prepare() {

		char[] cs = this.expression.toCharArray();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < cs.length; i++) {
			// ѭ�������ĸ
			for (int j = i; (j < cs.length)
					&& ((cs[j] >= 'A' && cs[j] <= 'Z') || (cs[j] >= 'a' && cs[j] <= 'z')); j++) {
				buffer.append(cs[j]);
				while (j + 1 < cs.length && cs[j + 1] >= '0'
						&& cs[j + 1] <= '9') {
					buffer.append(cs[j + 1]);
					j++;
				}
				i = j + 1;

			}
			// ѭ���������
			for (int k = i; (k < cs.length) && ((cs[k] >= '0' && cs[k] <= '9')); k++) {
				buffer.append(cs[k]);
				i = k;
			}
			// ѭ����������ַ�
			if (cs[i] == '+' || cs[i] == '-' || cs[i] == '*' || cs[i] == '+'
					|| cs[i] == '/' || cs[i] == '(' || cs[i] == ')') {
				if (buffer.length() != 0) {// ��֤�����֮ǰ�Ĳ����������Ƕ�����
					this.expressions.add(buffer.toString());// ��ӽ����������
					buffer.delete(0, buffer.length());// ����ַ�
				}
				buffer.append(cs[i]);
			}
			this.expressions.add(buffer.toString());// ��ӽ����������
			buffer.delete(0, buffer.length());// ����ַ�
		}

	}

	/**
	 * ϵͳ��ʼ����
	 * 
	 * @return ������
	 */
	public String start() {
		for (int i = 0; i < this.expressions.size(); i++) {
			String item = this.expressions.get(i);
			if (item.matches("^[/+-/*/]$") || item.matches("^[a-z]+[0-9]*$")) {// ��������
				this.ops.push(item);
			}
			if (item.matches("^[0-9]*$")) {// ��������
				this.vals.push(item);
			}
			if (item.equals(")")) {
				String op = this.ops.pop();
				this.doit(op);
			}
		}
		this._doit("^[/*/]$");
		this._doit("^[/+-]$");

		if (this.vals.size() == 1) {
			return this.vals.pop();
		}

		return "error";
	}

	/**
	 * �ֲ�����ʵ��2
	 * 
	 * @param regx
	 *            ��Ҫƥ�������
	 */
	private void _doit(String regx) {
		for (int k = 0; k < this.ops.size(); k++) {
			String op = this.ops.get(k);
			if (op.matches("^[/+-]$")) {
				Method method = this.map.get(op);
				try {
					if (k + 1 < this.vals.size()) {
						String val = method.invoke(method.getClass(),
								Double.valueOf(this.vals.get(k)),
								Double.valueOf(this.vals.get(k + 1)))
								.toString();
						this.vals.set(k, val);
						this.vals.remove(k + 1);
						this.ops.remove(k);
						k--;
					}
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * �ֲ�����ʵ��
	 * 
	 * @param op
	 *            ������
	 * @return ������
	 */
	private void doit(String op) {
		Method method = this.map.get(op);
		Class<?>[] types = method.getParameterTypes();
		Object[] objects = new Object[types.length];
		for (int j = types.length - 1; j >= 0; j--) {
			Object object = null;
			Class<?> number = this.types.get(types[j]);
			try {
				Method mt = number.getDeclaredMethod("valueOf", String.class);
				object = mt.invoke(mt.getClass(), this.vals.pop());
			} catch (NoSuchMethodException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
			objects[j] = object;

		}
		try {
			this.vals
					.push(method.invoke(method.getClass(), objects).toString());
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Calculator calculator = new Calculator(args[0]);
		System.out.println(calculator.start());
	}
}

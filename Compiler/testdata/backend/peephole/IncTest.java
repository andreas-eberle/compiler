class DecTest {
	public static void main(String[] args) {
		int i = f(5);
		i = i + 1;
		i = 1 + i;

		System.out.println(i);
	}

	public static int f(int i) {
		return i;
	}
}
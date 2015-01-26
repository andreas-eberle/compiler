class AddTest {
	public static void main(String[] args) {
		AddTest t = new AddTest();
		int i = 0;

		int a = t.recursiveFib(23);
		int b = -t.recursiveFib(38);
		int c = t.recursiveFib(34);
		int d = t.recursiveFib(4);

		while (i < 3000000) {
			d = d + t.longCalculation(a, b, c) + i;
			d = d + t.longCalculation(a + 1, b - 5, c + 11) + i;
			d = d + t.longCalculation(a + 2, b - 4, c - 22) + i;
			d = d + t.longCalculation(a + 3, b - 3, c - 42) + i;
			d = d + t.longCalculation(a + 4, b - 2, c + 1) + i;
			d = d + t.longCalculation(a + 5, b - 1, c) + i;
			i = i + 1;
		}

		System.out.println(d);
	}

	public int recursiveFib(int n) {
		if (n <= 2) {
			return 1;
		}

		return recursiveFib(n - 1) + recursiveFib(n - 2);
	}

	public int longCalculation(int a, int b, int c) {
		int d = b;
/*		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;
		d = 3453 / 234 + 345 * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30945) - 234 * c;

		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);	*/

		d = 3453 / ( 234 + 335) * d + a * b * c * 32 + 343 * (a + 2345 + b + c + 30947) - 234 * (c - b * d);
		
		return d;
	}
}

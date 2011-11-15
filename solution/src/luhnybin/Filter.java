package luhnybin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Filter {

  InputStream in;
  OutputStream out;
  List<Integer> buffer = new ArrayList<Integer>();
  List<Integer> digits = new ArrayList<Integer>();

  public static void main(String[] args) throws IOException {
    new Filter(System.in, System.out).process();
  }

  public Filter(InputStream in, OutputStream out) {
    this.in = in;
    this.out = out;
  }

  public void process() throws IOException {
    for (int b; (b = in.read()) != -1;) {
      if (!isCreditCardSymbol(b)) {
        flush();
        out.write(b);
        continue;
      }
      buffer.add(b);
      if (isDigit(b)) digits.add(b);
      if (digits.size() < 14) continue;
      int count = findLongestTrailingCreditCardNumber();
      maskTrailingDigitsInBuffer(count);
    }
    flush();
  }

  void flush() throws IOException {
    for (Integer b : buffer) out.write(b);
    buffer.clear();
    digits.clear();
  }

  void maskTrailingDigitsInBuffer(int count) {
    for (int i = buffer.size() - 1; i >= 0 && count > 0; i--)
      if (isDigit(buffer.get(i))) {
        buffer.set(i, (int)'X');
        count--;
      }
  }

  int findLongestTrailingCreditCardNumber() {
    int result = 0, sum = 0, limit = Math.min(digits.size(), 16);
    for (int i = 1; i <= limit; i++) {
      int digit = digits.get(digits.size() - i) - 48;
      if (i % 2 == 0) digit *= 2;
      sum += digit / 10 + digit % 10;
      if (i >= 14 && sum % 10 == 0) result = i;
    }
    return result;
  }

  boolean isCreditCardSymbol(int b) {
    return isDigit(b) || b == 32 || b == 45;
  }

  boolean isDigit(int b) {
    return b >= 48 && b <= 57;
  }
}

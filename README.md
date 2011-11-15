Coding Challenge: The Luhny Bin
===============================

> "To err is human; to forgive, divine." -Alexander Pope

Mistakes happen. At Square, we accept that human error is inevitable. We anticipate potential slip-ups and implement safety measures to mitigate&mdash;and oftentimes completely eliminate&mdash;any repercussions.

For example, Square's Luhn filter monitors logs and masks anything that looks like a credit card number. If a number like "4111 1111 1111 1111" were accidentally logged as part of an error message, our filter would replace it with "XXXX XXXX XXXX XXXX" and page an on call engineer.

The Luhn filter looks for sequences of digits that pass <a href="http://en.wikipedia.org/wiki/Luhn_algorithm">the Luhn check</a>, a simple checksum algorithm invented by Hans Peter Luhn in 1954. All valid credit card numbers pass the Luhn check, thereby enabling computer programs, like our log filter, to distinguish credit card numbers from random digit sequences.

The Luhn check works like this:

1. Starting from the *rightmost* digit and working left, double every second digit.
1. If a product has two digits, treat the digits independently.
1. Sum each individual digit, including the non-doubled digits.
1. Divide the result by 10.
1. If the remainder is 0, the number passed the Luhn check.

For example, "5678" passes the Luhn check:

1. Double every other digit: 10, 6, 14, 8
1. Sum the individual digits: (1 + 0) + 6 + (1 + 4) + 8 = 20
1. Divide the result by 10: 20 mod 10 = 0 <b><font color="green">Pass</font></b>

"6789" does not:

1. Double every other digit: 12, 7, 16, 9
1. Sum the individual digits: (1 + 2) + 7 + (1 + 6) + 9 = 26
1. Divide the result by 10: 26 mod 10 != 0 <b><font color="red">Fail</font></b>

Now for the challenge...
------------------------

Write a command line program that reads ASCII text from standard input, masks sequences of digits that look like credit card numbers, and writes the filtered text to standard output. For the purposes of this challenge, a credit card number:

- Consists of digits, spaces (`' '`) and hyphens (`'-'`).
- Has between 14 and 16 digits, inclusive.
- Passes the Luhn check.

If a sequence of digits looks like a credit card number, replace each digit with an `'X'`. Any characters, including digits, may flank a credit card number. *Beware.* Potential credit card numbers can overlap. A valid 16-digit number can even contain a valid 14 or 15-digit number. Your program must mask every digit.

I already wrote a test suite, so you can jump straight to the fun part: writing the algorithm. To participate:

1. Fork the [Luhny Bin GitHub repo](https://github.com/square/luhnybin).
2. Modify `mask.sh` to call your program.
3. Test your program by executing `run.sh`.
4. Once `run.sh` passes, post a link to your solution in the comments on [our blog](http://corner.squareup.com/2011/11/luhny-bin.html).

Windows users should use [Cygwin](http://www.cygwin.com/) to run the tests. Please make it easy for others to check out and run your solution.

The first time you execute `run.sh`, you'll see a test failure:

    $ ./run.sh 
    Running tests against mask.sh...
    
    .X
    
    Test #2 of 20 failed:
      Description:     valid 14-digit #
      Input:           56613959932537\n
      Expected result: XXXXXXXXXXXXXX\n
      Actual result:   56613959932537\n

Modify `mask.sh` and make the tests pass. [Line feeds](http://en.wikipedia.org/wiki/Newline) delineate the test cases. If you pass a number on the command line, `run.sh` will repeat the test suite the specified number of times; this is useful for performance comparisons. The tests aren't set in stone&mdash;if you have an idea for improving the test suite, please submit a pull request.

This isn't a contest, but an innovative solution could score you interviews at Square. I'm primarily interested to see how different programming languages stack up with regard to readability and performance.

Once we have enough interesting submissions, I'll summarize the results in a followup [blog](http://corner.squareup.com/) post and open source our own Java-based implementation. In the mean time, if you enjoy working with talented people on challenging problems like this, email your résumé to <a href="mailto:luhnybin@squareup.com">luhnybin@squareup.com</a>.

Good luck!

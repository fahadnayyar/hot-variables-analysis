package TestCases;

import java.util.Scanner;

public class Tester {

	/**
	 * @param args
	 */
	public static void happy(int n) {
		int x,y,z,t;
		x=2;
		y=10;
		z=3;
		t=z;
		Scanner sc = new Scanner(System.in);
        int input = sc.nextInt();
        if(input>0)
        {
        	x++;
        	y=2;
        	z=3;
        }
        else
        {
        	y=2;
        }
        System.out.println(y);
        while(x<y)
        {
        	t++;
        }
        System.out.println(y);
	}
}

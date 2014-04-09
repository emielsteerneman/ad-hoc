package diffiehellman;

import java.util.Random;

public class PrimeGenerator {
	
	private static long firstBound = 100000000;
	private static long secondBound = 1000000000;
		
	
	public static long generatePrime(){
		int primeCounter = 0;
		System.out.println("Calculating prime, please wait...");
		while(true){
			primeCounter++;
			Random r = new Random();
			long number = firstBound+((long)(r.nextDouble()*(secondBound-firstBound)));
			if(isPrime(number)){
				System.out.println("Tries: " + primeCounter);
				System.out.println("Prime found! :" + number);
				return number;				
			}
		}
	}

	
	public static boolean isPrime(long number){
		
        for(int i = 2; i < Math.sqrt(number); i++){
           if(number % i == 0){
               return false; 
           }
        }       
        return true; 
    }
}

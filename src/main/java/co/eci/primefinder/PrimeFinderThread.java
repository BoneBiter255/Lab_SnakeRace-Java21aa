package co.eci.primefinder;

import java.util.LinkedList;
import java.util.List;

/**
 * PrimeFinderThread: Hilo trabajador
 * 
 * Responsabilidades:
 * - Busca números primos en el rango [a, b)
 * - Se sincroniza con el hilo de control a través de PauseManager
 * - Usa wait() para pausarse sin consumir CPU (sin busy-waiting)
 */
public class PrimeFinderThread extends Thread{

	private int a, b;
	private List<Integer> primes;
	
	// Monitor compartido para sincronización (PauseManager)
	private PauseManager pauseManager;
	
	public PrimeFinderThread(int a, int b, PauseManager pauseManager) {
		super();
        this.primes = new LinkedList<>();
		this.a = a;
		this.b = b;
		this.pauseManager = pauseManager;
	}

    @Override
	public void run(){
        for (int i = a; i < b; i++){
            // Punto de sincronización crítico:
            // Verifica si debe pausarse y espera si es necesario
            pauseManager.checkPause();
            
            if (isPrime(i)){
                primes.add(i);
            }
        }
	}
	
	/**
	 * Determina si un número es primo
	 * Algoritmo simple pero efectivo
	 */
	private boolean isPrime(int n) {
	    boolean ans;
            if (n > 2) { 
                ans = n%2 != 0;
                for(int i = 3; ans && i*i <= n; i+=2 ) {
                    ans = n % i != 0;
                }
            } else {
                ans = n == 2;
            }
	    return ans;
	}

	public List<Integer> getPrimes() {
		return primes;
	}
	
}

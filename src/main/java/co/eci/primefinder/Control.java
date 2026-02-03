package co.eci.primefinder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Control: Hilo coordinador
 * 
 * Responsabilidades:
 * - Crear y iniciar los hilos trabajadores (PrimeFinderThread)
 * - Cada TMILISECONDS: pausar, mostrar reporte, esperar ENTER, reanudar
 * - Usa PauseManager para sincronización
 */
public class Control extends Thread {
    
    private final static int NTHREADS = 3;
    private final static int MAXVALUE = 300000000;
    private final static int TMILISECONDS = 5000;

    private final int NDATA = MAXVALUE / NTHREADS;

    private PrimeFinderThread pft[];
    
    // Gestor centralizado de pausa (monitor)
    private PauseManager pauseManager;
    
    private Control() {
        super();
        this.pauseManager = new PauseManager();
        this.pft = new  PrimeFinderThread[NTHREADS];

        int i;
        for(i = 0;i < NTHREADS - 1; i++) {
            PrimeFinderThread elem = new PrimeFinderThread(i*NDATA, (i+1)*NDATA, pauseManager);
            pft[i] = elem;
        }
        pft[i] = new PrimeFinderThread(i*NDATA, MAXVALUE + 1, pauseManager);
    }
    
    public static Control newControl() {
        return new Control();
    }

    @Override
    public void run() {
        // Iniciar todos los hilos trabajadores
        for(int i = 0;i < NTHREADS;i++ ) {
            pft[i].start();
        }
        
        // Hilo de control: cada TMILISECONDS pausa el trabajo
        try {
            while(true) {
                Thread.sleep(TMILISECONDS);
                pauseWorkers();
                showStatus();
                waitForUserInput();
                resumeWorkers();
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * PASO 1: Pausa todos los hilos trabajadores
     * Usa pauseManager.pause() que establece paused = true
     */
    private void pauseWorkers() {
        pauseManager.pause();
        System.out.println("\n[CONTROL] Pausando todos los hilos...");
    }
    
    /**
     * PASO 2: Muestra el estado actual de primos encontrados
     * Cuenta los primos en cada hilo trabajador
     */
    private void showStatus() {
        int totalPrimes = 0;
        for(int i = 0; i < NTHREADS; i++) {
            totalPrimes += pft[i].getPrimes().size();
        }
        System.out.println("[REPORTE] Total de primos encontrados: " + totalPrimes);
    }
    
    /**
     * PASO 3: Espera a que el usuario presione ENTER
     */
    private void waitForUserInput() {
        System.out.println("Presione ENTER para reanudar...");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            br.readLine();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * PASO 4: Reanuda todos los hilos trabajadores
     * Usa pauseManager.resume() que llama a notifyAll()
     */
    private void resumeWorkers() {
        pauseManager.resume();
        System.out.println("[CONTROL] Reanudando hilos...\n");
    }
}

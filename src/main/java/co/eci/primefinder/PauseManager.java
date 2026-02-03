package co.eci.primefinder;

/**
 * PauseManager: Gestor centralizado de la pausa usando el patrón productor-consumidor
 * 
 * LOCK (Monitor): El objeto PauseManager mismo (this)
 * CONDICIÓN: Variable "paused" de tipo boolean
 * 
 * Características:
 * - Usa synchronized() en métodos para sincronización automática
 * - wait() libera el lock y duerme sin consumir CPU (NO HAY BUSY-WAITING)
 * - notifyAll() despierta todos los hilos esperando
 * - Evita "lost wakeups" porque siempre verifica la condición en un while()
 */
public class PauseManager {
    
    // Variable de condición: determina si los trabajadores deben pausarse
    private boolean paused = false;
    
    /**
     * Los hilos trabajadores llaman a este método
     * Si está pausado, se bloquean aquí hasta que se reanude
     * 
     * SIN BUSY-WAITING: Usa wait() que duerme realmente
     */
    public synchronized void checkPause() {
        // IMPORTANTE: Usar while() en lugar de if()
        // Esto evita "lost wakeups" - si otro hilo cambió la condición,
        // el while lo verifica nuevamente
        while(paused) {
            try {
                // wait() hace 3 cosas:
                // 1. Libera el lock (pauseLock)
                // 2. Duerme el hilo (sin consumir CPU)
                // 3. Cuando se llama notify(), se despierta y reacquiere el lock
                this.wait();
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * El hilo de control (Main) llama a este método
     * Pausa todos los trabajadores
     */
    public synchronized void pause() {
        paused = true;
    }
    
    /**
     * El hilo de control llama a este después de procesar
     * Despierta todos los trabajadores esperando
     */
    public synchronized void resume() {
        paused = false;
        // notifyAll() despierta TODOS los hilos esperando en wait()
        // Preferimos notifyAll() en lugar de notify() para garantizar
        // que todos los trabajadores se despiertan (más seguro)
        this.notifyAll();
    }
    
    /**
     * Consulta segura del estado de pausa
     */
    public synchronized boolean isPaused() {
        return paused;
    }
}

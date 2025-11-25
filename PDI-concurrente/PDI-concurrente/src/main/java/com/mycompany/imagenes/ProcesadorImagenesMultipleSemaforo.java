package com.mycompany.imagenes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Procesa múltiples imágenes en paralelo usando un Semáforo para limitar a 10 hilos simultáneos
 * Usa Thread directo con join() en lugar de ExecutorService
 * @author andrespillajo
 */
public class ProcesadorImagenesMultipleSemaforo {
    
    private static final String CARPETA_ENTRADA = "imagenes";
    private static final String CARPETA_SALIDA = "imagenes_grises";
    private static final int NUMERO_HILOS_PARALELOS = 10; // Limitar hilos simultáneos para evitar OutOfMemoryError
    
    /**
     * Clase interna que envuelve ProcesadorImagen y maneja el semáforo
     */
    private static class ProcesadorImagenConSemaforo implements Runnable {
        private final File archivoEntrada;
        private final File carpetaSalida;
        private final Semaphore semaforo;
        
        public ProcesadorImagenConSemaforo(File archivoEntrada, File carpetaSalida, Semaphore semaforo) {
            this.archivoEntrada = archivoEntrada;
            this.carpetaSalida = carpetaSalida;
            this.semaforo = semaforo;
        }
        
        @Override
        public void run() {
            try {
                // Adquirir un permiso del semáforo (espera si ya hay 10 hilos activos)
                semaforo.acquire();
                
                try {
                    // Procesar la imagen
                    ProcesadorImagen procesador = new ProcesadorImagen(archivoEntrada, carpetaSalida);
                    procesador.run();
                } finally {
                    // Liberar el permiso cuando termine el procesamiento
                    semaforo.release();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Hilo interrumpido mientras esperaba el semáforo: " + archivoEntrada.getName());
            } catch (Exception e) {
                System.err.println("Error procesando " + archivoEntrada.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            // Obtener todas las imágenes de la carpeta de entrada
            File carpetaEntrada = new File(CARPETA_ENTRADA);
            File carpetaSalida = new File(CARPETA_SALIDA);
            
            // Verificar que existe la carpeta de entrada
            if (!carpetaEntrada.exists() || !carpetaEntrada.isDirectory()) {
                System.err.println("Error: La carpeta '" + CARPETA_ENTRADA + "' no existe.");
                System.out.println("Por favor, crea la carpeta y coloca las imágenes a procesar.");
                return;
            }
            
            // Crear carpeta de salida si no existe
            if (!carpetaSalida.exists()) {
                carpetaSalida.mkdirs();
            }
            
            // Obtener archivos de imagen
            File[] archivos = carpetaEntrada.listFiles((dir, name) -> {
                String nombreLower = name.toLowerCase();
                return nombreLower.endsWith(".png") || 
                       nombreLower.endsWith(".jpg") || 
                       nombreLower.endsWith(".jpeg");
            });
            
            if (archivos == null || archivos.length == 0) {
                System.out.println("No se encontraron imágenes en la carpeta '" + CARPETA_ENTRADA + "'");
                return;
            }
            
            String separador = "============================================================";
            System.out.println(separador);
            System.out.println("Procesando " + archivos.length + " imágenes en paralelo");
            System.out.println("Usando Semáforo para limitar a " + NUMERO_HILOS_PARALELOS + " hilos simultáneos");
            System.out.println(separador);
            
            long inicioTotal = System.nanoTime();
            
            // Crear un Semáforo con 10 permisos (permite 10 hilos simultáneos)
            Semaphore semaforo = new Semaphore(NUMERO_HILOS_PARALELOS);
            
            // Crear lista de hilos
            List<Thread> hilos = new ArrayList<>();
            
            // Crear un hilo por cada imagen
            for (File archivo : archivos) {
                Thread hilo = new Thread(new ProcesadorImagenConSemaforo(archivo, carpetaSalida, semaforo));
                hilos.add(hilo);
                hilo.start();
            }
            
            // Esperar a que todos los hilos terminen usando join()
            for (Thread hilo : hilos) {
                try {
                    hilo.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Error esperando hilo: " + e.getMessage());
                }
            }
            
            long finTotal = System.nanoTime();
            long tiempoTotal = (finTotal - inicioTotal) / 1_000_000;
            
            System.out.println(separador);
            System.out.println("Procesamiento completado!");
            System.out.println("Total de imágenes procesadas: " + archivos.length);
            System.out.println("Tiempo total: " + tiempoTotal + " ms");
            System.out.println("Tiempo promedio por imagen: " + (tiempoTotal / archivos.length) + " ms");
            System.out.println("Imágenes guardadas en: " + CARPETA_SALIDA);
            System.out.println(separador);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


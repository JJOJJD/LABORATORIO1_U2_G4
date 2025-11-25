package com.mycompany.imagenes;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Runnable que procesa una imagen individual convirtiéndola a escala de grises
 * @author andrespillajo
 */
public class ProcesadorImagen implements Runnable {
    
    private final File archivoEntrada;
    private final File carpetaSalida;
    
    public ProcesadorImagen(File archivoEntrada, File carpetaSalida) {
        this.archivoEntrada = archivoEntrada;
        this.carpetaSalida = carpetaSalida;
    }
    
    @Override
    public void run() {
        try {
            long inicio = System.nanoTime();
            
            // Cargar la imagen
            BufferedImage imagen = ImageIO.read(archivoEntrada);
            
            if (imagen == null) {
                System.err.println("Error al cargar: " + archivoEntrada.getName());
                return;
            }
            
            int altura = imagen.getHeight();
            int ancho = imagen.getWidth();
            
            System.out.println("[" + Thread.currentThread().getName() + "] Procesando: " + 
                             archivoEntrada.getName() + " (" + ancho + "x" + altura + ")");
            
            // Procesar la imagen usando múltiples hilos internos
            int numeroHilos = 4; // Dividir cada imagen en 4 partes
            Thread[] hilos = new Thread[numeroHilos];
            
            int filasPorHilo = altura / numeroHilos;
            
            for (int i = 0; i < numeroHilos; i++) {
                int inicioFila = i * filasPorHilo;
                int finFila = (i == numeroHilos - 1) ? altura : inicioFila + filasPorHilo;
                
                hilos[i] = new Thread(new FiltroGris(imagen, inicioFila, finFila));
                hilos[i].start();
            }
            
            // Esperar a que todos los hilos internos terminen
            for (Thread hilo : hilos) {
                hilo.join();
            }
            
            // Guardar la imagen procesada
            String nombreSalida = archivoEntrada.getName();
            String extension = nombreSalida.substring(nombreSalida.lastIndexOf(".") + 1);
            String nombreBase = nombreSalida.substring(0, nombreSalida.lastIndexOf("."));
            String nombreFinal = nombreBase + "_gris." + extension;
            
            File archivoSalida = new File(carpetaSalida, nombreFinal);
            ImageIO.write(imagen, extension, archivoSalida);
            
            long fin = System.nanoTime();
            long tiempo = (fin - inicio) / 1_000_000;
            
            System.out.println("[" + Thread.currentThread().getName() + "] Completado: " + 
                             nombreFinal + " (" + tiempo + " ms)");
            
        } catch (Exception e) {
            System.err.println("Error procesando " + archivoEntrada.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.imagenessinhilos;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author andrespillajo
 */
public class ImagenesSinHilos {

    public static void main(String[] args) {
        try {
            // Ruta de la carpeta con las imágenes
            File carpetaImagenes = new File("imagenes");
            
            if (!carpetaImagenes.exists() || !carpetaImagenes.isDirectory()) {
                System.out.println("La carpeta 'imagenes' no existe o no es un directorio válido.");
                return;
            }
            
            // Obtener todos los archivos JPG de la carpeta
            File[] archivos = carpetaImagenes.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg")
            );
            
            if (archivos == null || archivos.length == 0) {
                System.out.println("No se encontraron archivos JPG en la carpeta 'imagenes'.");
                return;
            }
            
            System.out.println("Se encontraron " + archivos.length + " imágenes para procesar.");
            System.out.println("Iniciando procesamiento secuencial...\n");
            
            long tiempoTotalInicio = System.nanoTime();
            int imagenesProcesadas = 0;
            int imagenesError = 0;
            
            // Procesar cada imagen secuencialmente
            for (int i = 0; i < archivos.length; i++) {
                File archivoEntrada = archivos[i];
                String nombreArchivo = archivoEntrada.getName();
                
                System.out.println("[" + (i + 1) + "/" + archivos.length + "] Procesando: " + nombreArchivo);
                
                try {
                    // Cargar la imagen
                    BufferedImage imagen = ImageIO.read(archivoEntrada);
                    
                    if (imagen == null) {
                        System.out.println("  ⚠ No se pudo cargar la imagen: " + nombreArchivo);
                        imagenesError++;
                        continue;
                    }
                    
                    // Obtener dimensiones de la imagen
                    int ancho = imagen.getWidth();
                    int alto = imagen.getHeight();
                    
                    long inicio = System.nanoTime();
                    
                    // Recorrer cada píxel de la imagen y convertir a escala de grises
                    for (int y = 0; y < alto; y++) {
                        for (int x = 0; x < ancho; x++) {
                            // Obtener el valor ARGB del píxel
                            int pixel = imagen.getRGB(x, y);

                            // Extraer componentes de color
                            int alpha = (pixel >> 24) & 0xff; // Componente Alpha
                            int red = (pixel >> 16) & 0xff;   // Componente Rojo
                            int green = (pixel >> 8) & 0xff;  // Componente Verde
                            int blue = pixel & 0xff;          // Componente Azul

                            // Calcular el promedio para escala de grises
                            int gris = (red + green + blue) / 3;

                            // Crear el nuevo color en escala de grises
                            int nuevoPixel = (alpha << 24) | (gris << 16) | (gris << 8) | gris;

                            // Asignar el nuevo color al píxel
                            imagen.setRGB(x, y, nuevoPixel);
                        }
                    }
                    
                    long fin = System.nanoTime();
                    long tiempoProcesamiento = (fin - inicio) / 1_000_000;

                    // Guardar la imagen resultante (sobrescribiendo la original)
                    ImageIO.write(imagen, "jpg", archivoEntrada);
                    
                    imagenesProcesadas++;
                    System.out.println("  ✓ Completado (" + ancho + "x" + alto + ") - Tiempo: " + tiempoProcesamiento + " ms");
                    
                } catch (Exception e) {
                    System.out.println("  ✗ Error al procesar " + nombreArchivo + ": " + e.getMessage());
                    imagenesError++;
                }
            }
            
            long tiempoTotalFin = System.nanoTime();
            long tiempoTotal = (tiempoTotalFin - tiempoTotalInicio) / 1_000_000;
            
            // Resumen final
            System.out.println("\n========================================");
            System.out.println("Procesamiento completado:");
            System.out.println("  - Imágenes procesadas: " + imagenesProcesadas);
            System.out.println("  - Errores: " + imagenesError);
            System.out.println("  - Tiempo total: " + tiempoTotal + " ms (" + (tiempoTotal / 1000.0) + " segundos)");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.out.println("Error general: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

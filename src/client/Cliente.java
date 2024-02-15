package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

// C:/Users/yefry/OneDrive/Documentos/ProjectTest/client/imagen.JPG
// C:/Users/yefry/OneDrive/Documentos/ProjectTest/client/Captura.JPG
// final int PUERTO = 54321;

public class Cliente {
	public static void main(String[] args) throws IOException {
		final String SERVIDOR = "localhost";
		final int PUERTO = 54321;

		Socket cliente = new Socket(SERVIDOR, PUERTO);

		BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
		PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true);
		Scanner scanner = new Scanner(System.in);

		// Preguntar al cliente qué operación desea realizar (subir o descargar)
		String opcion = entrada.readLine();
		System.out.println(opcion);
		System.out.print("Ingrese su opción (subir/descargar): ");
		String respuesta = scanner.nextLine();
		salida.println(respuesta);

		// Manejar la opción del cliente (subir o descargar)
		if (respuesta.equalsIgnoreCase("subir")) {
			System.out.print("Ingrese la ruta de la imagen a subir al servidor: ");
			String rutaImagen = scanner.nextLine();
			salida.println(rutaImagen);
			subirImagen(cliente, rutaImagen);
		} else if (respuesta.equalsIgnoreCase("descargar")) {
			try {
				descargarImagenes(cliente);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Opción inválida.");
		}

		// Cerrar los recursos
		scanner.close();
		salida.close();
		entrada.close();
		cliente.close();
	}

	private static void subirImagen(Socket cliente, String rutaImagen) throws IOException {
		PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true);

		// Leer y enviar la imagen al servidor
		FileInputStream archivoEntrada = new FileInputStream(rutaImagen);
		OutputStream salidaImagen = cliente.getOutputStream();
		byte[] buffer = new byte[1024];
		int bytesLeidos;
		while ((bytesLeidos = archivoEntrada.read(buffer)) != -1) {
			salidaImagen.write(buffer, 0, bytesLeidos);
		}
		archivoEntrada.close();

		System.out.println("Imagen desde '" + rutaImagen + "' enviada al servidor.");
	}

	private static void descargarImagenes(Socket cliente) throws IOException, ClassNotFoundException {
		// Recibir la lista de imágenes del servidor
		System.out.println("entro a descargarImagenes");
		ObjectInputStream entradaObjeto = new ObjectInputStream(cliente.getInputStream());
		System.out.println(cliente.getChannel().isOpen());
		String[] archivos = (String[]) entradaObjeto.readObject();
		entradaObjeto.close();

		// Mostrar la lista de imágenes disponibles
		System.out.println("Lista de imágenes en el servidor:");
		for (int i = 0; i < archivos.length; i++) {
			System.out.println((i + 1) + ". " + archivos[i]);
		}

		// Solicitar al usuario que seleccione una imagen para descargar
		Scanner scanner = new Scanner(System.in);
		System.out.print("Ingrese el número de la imagen que desea descargar: ");
		int opcion = scanner.nextInt();
		scanner.nextLine(); // Consumir el salto de línea después del entero

		// Verificar si la opción es válida
		if (opcion < 1 || opcion > archivos.length) {
			System.out.println("Opción inválida.");
			return;
		}

		// Enviar la solicitud al servidor
		PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true);
		salida.println(opcion);

		// Recibir la imagen seleccionada del servidor
		InputStream entradaImagen = cliente.getInputStream();
		FileOutputStream archivoSalida = new FileOutputStream(archivos[opcion - 1]);
		byte[] buffer = new byte[1024];
		int bytesLeidos;
		while ((bytesLeidos = entradaImagen.read(buffer)) != -1) {
			archivoSalida.write(buffer, 0, bytesLeidos);
		}
		archivoSalida.close();

		System.out.println("Imagen descargada con éxito.");
	}
}

package server;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Servidor {
	public static void main(String[] args) throws IOException {
		final int PUERTO = 54321;
		ServerSocket servidor = new ServerSocket(PUERTO);
		System.out.println("Servidor esperando conexiones...");

		while (true) {
			Socket cliente = servidor.accept();
			System.out.println("Cliente conectado desde: " + cliente.getInetAddress() + ":" + cliente.getPort());

			// Crear un hilo para manejar la conexión del cliente
			Thread clienteHandler = new Thread(new ClienteHandler(cliente));
			clienteHandler.start();
		}
	}
}

class ClienteHandler implements Runnable {
	private Socket cliente;

	public ClienteHandler(Socket cliente) {
		this.cliente = cliente;
	}

	@Override
	public void run() {
		try {
			// Leer la opción del cliente (subir o descargar)
			BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true);
			salida.println(
					"¿Desea subir una imagen al servidor o descargar las imágenes del servidor? (subir/descargar)");
			String opcion = entrada.readLine();

			if (opcion.equalsIgnoreCase("subir")) {
				subirImagen(cliente);
			} else if (opcion.equalsIgnoreCase("descargar")) {
				descargarImagenes(cliente);
			} else {
				System.out.println("Opción inválida.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				cliente.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void subirImagen(Socket cliente) throws IOException {
		// Recibir nombre de la imagen del cliente
		BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
		String nombreImagen = entrada.readLine();

		// Recibir y guardar la imagen
		InputStream entradaImagen = cliente.getInputStream();
		FileOutputStream archivoSalida = new FileOutputStream(
				"src/imagenes/imagen_recibida_" + System.currentTimeMillis() + ".jpg");
		byte[] buffer = new byte[1024];
		int bytesLeidos;
		while ((bytesLeidos = entradaImagen.read(buffer)) != -1) {
			archivoSalida.write(buffer, 0, bytesLeidos);
		}
		archivoSalida.close();

		System.out.println("Imagen '" + nombreImagen + "' recibida y guardada en el servidor.");
	}

	private void descargarImagenes(Socket cliente) throws IOException {
		// Obtener la lista de imágenes disponibles en el servidor
		File directorio = new File("src/imagenes");
		String[] archivos = directorio.list();

		// Enviar la lista de imágenes al cliente
		ObjectOutputStream salidaObjeto = new ObjectOutputStream(cliente.getOutputStream());
		salidaObjeto.writeObject(archivos);
		salidaObjeto.close();
	}
}

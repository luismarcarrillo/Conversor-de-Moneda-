// Importar gson
import com.google.gson.Gson;
import com.google.gson.JsonObject;
// Importaciones
import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    private static final String API_URL_BASE = "https://v6.exchangerate-api.com/v6/";
    private static final String API_KEY = "3e6670be396646341c40220c";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Gson gson = new Gson();

        while (true) {
            mostrarMenu();
            int categoria = obtenerOpcionValida(scanner);

            if (categoria == 9) {
                System.out.println("Gracias por usar el conversor de divisas.\n¡Vuelva pronto!");
                break;
            }

            String divisa = obtenerDivisa(categoria);
            if (divisa == null) {
                System.out.println("Por favor selecciona una opción válida");
                continue;
            }

            double monto = obtenerMontoValido(scanner);

            try {
                double conversion = realizarConversion(gson, divisa, monto, categoria);
                System.out.println("La conversión es: " + conversion);
            } catch (IOException | InterruptedException e) {
                System.err.println("Error en la conversión: " + e.getMessage());
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("""
                **************************
                Selecciona una opción dentro del menú:
                1 - USD a ARS
                2 - USD a BRL
                3 - USD a COP
                4 - USD a MXN
                5 - MXN a USD
                6 - BRL a USD
                7 - ARS a USD
                8 - COP a USD
                9 - Salir
                **************************
                """);
    }

    private static int obtenerOpcionValida(Scanner scanner) {
        int opcion = -1;
        while (true) {
            try {
                System.out.print("Introduce tu opción: ");
                opcion = scanner.nextInt();
                if (opcion >= 1 && opcion <= 9) {
                    break;
                } else {
                    System.out.println("Por favor selecciona una opción válida (1-9).");
                }
            } catch (InputMismatchException e) {
                System.out.println("Por favor introduce un número válido.");
                scanner.next(); // Limpiar el buffer del scanner
            }
        }
        return opcion;
    }

    private static double obtenerMontoValido(Scanner scanner) {
        double monto = 0;
        while (true) {
            try {
                System.out.print("Ingresa el monto a convertir: ");
                monto = scanner.nextDouble();
                if (monto > 0) {
                    break;
                } else {
                    System.out.println("El monto debe ser mayor que cero.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Por favor introduce un monto válido.");
                scanner.next(); // Limpiar el buffer del scanner
            }
        }
        return monto;
    }

    private static String obtenerDivisa(int categoria) {
        return switch (categoria) {
            case 1, 2, 3, 4 -> "USD";
            case 5 -> "MXN";
            case 6 -> "BRL";
            case 7 -> "ARS";
            case 8 -> "COP";
            default -> null;
        };
    }

    private static double realizarConversion(Gson gson, String divisa, double monto, int categoria) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(API_URL_BASE + API_KEY + "/latest/" + divisa);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonObject conversionRates = jsonObject.getAsJsonObject("conversion_rates");

        return switch (categoria) {
            case 1 -> monto * conversionRates.get("ARS").getAsDouble();
            case 2 -> monto * conversionRates.get("BRL").getAsDouble();
            case 3 -> monto * conversionRates.get("COP").getAsDouble();
            case 4 -> monto * conversionRates.get("MXN").getAsDouble();
            case 5, 6, 7, 8 -> monto * conversionRates.get("USD").getAsDouble();
            default -> throw new IllegalArgumentException("Categoría no válida: " + categoria);
        };
    }
}

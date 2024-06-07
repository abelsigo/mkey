package com.abel.mkey;

public class Singleton {

    // Instancia única del singleton
    private static Singleton instance;

    // ID de Google almacenado en el singleton
    private String googleId;

    // Constructor privado para evitar que se puedan crear instancias de esta clase fuera de ella misma
    private Singleton() {
        // Aquí puedes inicializar cualquier recurso necesario para tu singleton
    }

    // Método estático para obtener la instancia única del singleton
    public static synchronized Singleton getInstance() {
        // Si la instancia aún no ha sido creada, la crea
        if (instance == null) {
            instance = new Singleton();
        }
        // Devuelve la instancia única
        return instance;
    }

    // Método para establecer el ID de Google
    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    // Método para obtener el ID de Google
    public String getGoogleId() {
        return googleId;
    }

    // Métodos adicionales de la clase singleton
    public void doSomething() {
        // Método para realizar alguna acción específica del singleton
    }
}



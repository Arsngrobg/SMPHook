package dev.arsngrobg.smphook;

import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Javalin.create(config -> {
            config.routes.get("/", ctx -> ctx.result("Diddy blud"));
        }).start(8008);
    }
}

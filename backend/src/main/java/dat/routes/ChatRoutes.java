package dat.routes;

import dat.controllers.ChatController;
import dat.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.post;

public class ChatRoutes {

    private final ChatController chatController;

    public ChatRoutes(EntityManagerFactory emf) {
        this.chatController = new ChatController(emf);
    }

    protected EndpointGroup getRoutes() {
        return () -> post("/", chatController::chat, Role.USER);
    }
}

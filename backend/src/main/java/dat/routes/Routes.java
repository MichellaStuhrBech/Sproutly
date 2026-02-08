package dat.routes;

import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.path;
import io.javalin.http.Context;
import static io.javalin.apibuilder.ApiBuilder.get;


public class Routes {

    private final CreateUserRoutes createUserRoutes = new CreateUserRoutes();

    public EndpointGroup getRoutes() {
        return () -> {
                path("/createuser", createUserRoutes.getRoutes());

            };
        }

}

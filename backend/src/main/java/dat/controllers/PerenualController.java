package dat.controllers;

import dat.dtos.PerenualSpeciesDetailsDTO;
import dat.dtos.PerenualSpeciesDTO;
import dat.services.PerenualService;
import io.javalin.http.Context;

import java.util.List;

/**
 * Serves Perenual plant search and species details for logged-in users.
 */
public class PerenualController {

    private final PerenualService perenualService = new PerenualService();

    /**
     * GET /api/plants/search?q=... — search species by keyword.
     */
    public void search(Context ctx) {
        String q = ctx.queryParam("q");
        if (q == null || q.isBlank()) {
            ctx.json(List.of());
            return;
        }
        List<PerenualSpeciesDTO> results = perenualService.search(q.trim());
        ctx.json(results);
    }

    /**
     * GET /api/plants/species/:id — get full species details.
     */
    public void getSpeciesDetails(Context ctx) {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).json("Invalid species id");
            return;
        }
        PerenualSpeciesDetailsDTO details = perenualService.getDetails(id);
        if (details == null) {
            ctx.status(404).json("Species not found");
            return;
        }
        ctx.json(details);
    }
}

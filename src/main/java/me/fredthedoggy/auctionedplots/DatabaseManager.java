package me.fredthedoggy.auctionedplots;

import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    Gson gson = new Gson();
    DatabaseType db;
    Logger logger = AuctionedPlots.getInstance().getLogger();
    File file;

    public DatabaseManager(File file) {
        this.file = file;
        if (!file.isFile()) {
            try {
                file.createNewFile();
                save(new DatabaseType());
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.toString());
                return;
            }
        }
        try {
            db = load(new FileInputStream(file), DatabaseType.class);
            if (db == null) db = new DatabaseType();
            if (db.plots == null) db.plots = new ArrayList<>();
            save(db);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database Exists, But Cannot Be Opened. File Permissions Error?");
            return;
        }
    }

    public DatabaseType.Plot getPlot(String name) {
            for (DatabaseType.Plot plot : db.plots) {
                if (plot.plot.equals(name)) {
                    return plot;
                }
            }
        return null;
    }

    public List<DatabaseType.Plot> getPlots() {
        return db.plots;
    }

    public void setPlot(DatabaseType.Plot plot) {
        DatabaseType.Plot oldPlot = getPlot(plot.plot);
        if (oldPlot != null) db.plots.remove(oldPlot);
        db.plots.add(plot);
        save(db);
        System.out.println("Added " + plot.plot + " to Plot Database");
    }

    private <T> T load(final InputStream inputStream, final Class<T> clazz) {
        try {
            if (inputStream != null) {
                final Gson gson = new Gson();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                return gson.fromJson(reader, clazz);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T> void save(final T clazzInstance) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(gson.toJson(clazzInstance));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package de.uol.dssp.source;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.oul.dssp.source.model.AISMessageTO;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.selection.Selection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class that resolves queries to the interface
 */
@Component
public class CustomResolver implements GraphQLQueryResolver {


    /**
     * Returns a list of AIS Messages.
     * @return
     */
    public List<AISMessageTO> getAISMessage() {
        List<AISMessageTO> messages = new ArrayList<>();

        int counter = 0;
        for (Row row : DataSingleton.getInstance().getTable()) {
            if (counter > 1000)
                break;
            AISMessageTO.Builder builder = AISMessageTO.builder();
            AISMessageTO m = builder.setCallsign(row.getString("CallSign")).setCargo(row.getInt("Cargo"))
                    .setLat(row.getDouble("LAT")).setLon(row.getDouble("LON"))
                    .setVesselName(row.getString("VesselName")).setMmsi(row.getInt("MMSI"))
                    .setBaseDateTime(row.getDateTime("BaseDateTime").toString()).setSog(row.getDouble("SOG"))
                    .setCog(row.getDouble("COG")).setImo(row.getString("IMO"))
                    .setVesselType(row.getInt("VesselType")).setStatus(row.getInt("Status"))
                    .setLength(Double.valueOf(row.getInt("Length"))).setWidth(Double.valueOf(row.getInt("Width")))
                    .setDraft(row.getDouble("Draft")).setCargo(row.getInt("Cargo"))
                    .setTranscieverClass(row.getString("TranscieverClass")).build();
            messages.add(m);
            counter++;
        }

        return messages;
    }

    /**
     * Returns a list of filtered AIS messages (minmax filter on lat, lon, sog and timestamp)
     * @param filter
     * @return
     */
    public List<AISMessageTO> getAISMessage(String filter)  {
        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(filter, JsonObject.class);
        Float minLat = null;
        Float maxLat = null;
        Float minLon = null;
        Float maxLon = null;
        String timeStart = null;
        String timeEnd = null;
        Float minSog = null;
        Float maxSog = null;
        if (obj != null) {
            if (obj.has("latitude") && obj.get("latitude").getAsJsonObject().has("min"))
                minLat = obj.get("latitude").getAsJsonObject().get("min").getAsFloat();
            if (obj.has("latitude") && obj.get("latitude").getAsJsonObject().has("max"))
                maxLat = obj.get("latitude").getAsJsonObject().get("max").getAsFloat();
            if (obj.has("longitude") && obj.get("longitude").getAsJsonObject().has("min"))
                minLon = obj.get("longitude").getAsJsonObject().get("min").getAsFloat();
            if (obj.has("longitude") && obj.get("longitude").getAsJsonObject().has("max"))
                maxLon = obj.get("longitude").getAsJsonObject().get("max").getAsFloat();
            if (obj.has("timestamp") && obj.get("timestamp").getAsJsonObject().has("min"))
                timeStart = obj.get("timestamp").getAsJsonObject().get("min").getAsString();
            if (obj.has("timestamp") && obj.get("timestamp").getAsJsonObject().has("max"))
                timeEnd = obj.get("timestamp").getAsJsonObject().get("max").getAsString();
            if (obj.has("sog") && obj.get("sog").getAsJsonObject().has("min"))
                minSog = obj.get("sog").getAsJsonObject().get("min").getAsFloat();
            if (obj.has("sog") && obj.get("sog").getAsJsonObject().has("max"))
                maxSog = obj.get("sog").getAsJsonObject().get("max").getAsFloat();
        }
        List<AISMessageTO> messages = new ArrayList<>();
        Table data = DataSingleton.getInstance().getTable();

        if (minLat != null && maxLat != null && minLon != null && maxLon != null) {
            DoubleColumn lat = data.doubleColumn("LAT");
            Selection latRange = lat.isBetweenExclusive(minLat, maxLat);
            data = data.where(latRange);

            DoubleColumn lon = data.doubleColumn("LON");
            Selection lonRange = lon.isBetweenExclusive(minLon, maxLon);
            data = data.where(lonRange);

            //both
            if (timeStart != null && timeEnd != null) {
                //only time
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); //throws an error if invalid format
                LocalDateTime startDate = LocalDateTime.parse(timeStart, formatter);
                LocalDateTime endDate = LocalDateTime.parse(timeEnd, formatter);
                DateTimeColumn ts = data.dateTimeColumn("BaseDateTime");
                Selection tsRange = ts.isBetweenExcluding(startDate, endDate);
                data = data.where(tsRange);
            }
        } else if (timeStart != null && timeEnd != null) {
            //only time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); //throws an error if invalid format
            LocalDateTime startDate = LocalDateTime.parse(timeStart, formatter);
            LocalDateTime endDate = LocalDateTime.parse(timeEnd, formatter);
            DateTimeColumn ts = data.dateTimeColumn("BaseDateTime");
            Selection tsRange = ts.isBetweenExcluding(startDate, endDate);
            data = data.where(tsRange);
        } else if (minSog != null && maxSog != null) {
            DoubleColumn sog = data.doubleColumn("SOG");
            Selection sogRange = sog.isBetweenExclusive(minSog, maxSog);
            data = data.where(sogRange);
        } else {
            //nothing
            return this.getAISMessage();
        }

        int counter = 0;
        for (Row row : data) {
            if (counter > 1000)
                break;
            AISMessageTO.Builder builder = AISMessageTO.builder();
            AISMessageTO m = builder.setCallsign(row.getString("CallSign")).setCargo(row.getInt("Cargo"))
                    .setLat(row.getDouble("LAT")).setLon(row.getDouble("LON"))
                    .setVesselName(row.getString("VesselName")).setMmsi(row.getInt("MMSI"))
                    .setBaseDateTime(row.getDateTime("BaseDateTime").toString()).setSog(row.getDouble("SOG"))
                    .setCog(row.getDouble("COG")).setImo(row.getString("IMO"))
                    .setVesselType(row.getInt("VesselType")).setStatus(row.getInt("Status"))
                    .setLength(Double.valueOf(row.getInt("Length"))).setWidth(Double.valueOf(row.getInt("Width")))
                    .setDraft(row.getDouble("Draft")).setCargo(row.getInt("Cargo"))
                    .setTranscieverClass(row.getString("TranscieverClass")).build();
            messages.add(m);
            counter++;
        }


        return messages;
    }

    /**
     * Returns a list of available filters.
     * @return
     */
    public String getAvailableFilterMethods() {
        String result = "{\n" +
                "  \"AISMessage\" : {\n" +
                "    \"baseDateTime\" : [\"minmax\"],\n" +
                "    \"lat\" : [\"minmax\"],\n" +
                "    \"lon\" : [\"minmax\"]\n" +
                "  }\n" +
                "}";
        return result;
    }
}

package de.uol.dssp.source;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.oul.dssp.source.model.RadarTargetStateTO;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * GraphQL query resolver.
 */
@Component
public class CustomResolver implements GraphQLQueryResolver {
    // Obtain database credentials from config file.
    @Value("${db.user}")
    private String dbUser;
    @Value("${db.pass}")
    private String dbPassword;

    /**
     * Returns a list of Radar data (limited to 1000 results)
     * @return
     * @throws SQLException
     */
    public List<RadarTargetStateTO> getRadarTargetState() throws SQLException {
        String url = "jdbc:postgresql://mydbhost.com/database";
        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPassword);
        //props.setProperty("ssl", "true");
        Connection conn = DriverManager.getConnection(url, props);
        String query = "select id, position[0] as lat, position[1] as lon, sog, cog, COALESCE(to_char(timestamp , 'MM-DD-YYYY HH24:MI:SS'), '') as ts, radarid, theme from tracks where theme = 'RADAR'";
        query += " limit 1000";

        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(query);
        List<RadarTargetStateTO> radarStates = new ArrayList<>();
        while (rs.next()) {
            double sog = rs.getDouble("sog");
            double cog = rs.getDouble("cog");
            double lat = rs.getDouble("lat");
            double lon = rs.getDouble("lon");

            RadarTargetStateTO.Builder builder = RadarTargetStateTO.builder();
            String localDate = rs.getString("ts");

            radarStates.add(builder.setCog(cog).setSog(sog).setProviderID("urn:mrn:mcp:org:uol:database").setId("0").setTimestamp(localDate).setLatitude(lat).setLongitude(lon).setCrsEPSG("epsg:4362").build());

            System.out.println(sog + ", " + cog + ", " + localDate);
        }
        return radarStates;
    }


    /**
     * Returns a list of filtered radar data (minmax filter on lat, lon and timestamp is supported)
     * @param filter the filter
     * @return
     * @throws SQLException
     */
    public List<RadarTargetStateTO> getRadarTargetState(String filter) throws SQLException {
        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(filter, JsonObject.class);
        Float minLat = null;
        Float maxLat = null;
        Float minLon = null;
        Float maxLon = null;
        String timeStart = null;
        String timeEnd = null;
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
        }
        List<RadarTargetStateTO> messages = new ArrayList<>();
        String url = "jdbc:postgresql://mydbhost.com/database";
        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPassword);
        Connection conn = DriverManager.getConnection(url, props);
        String query = "";
        if (minLat != null && maxLat != null && minLon != null && maxLon != null) {
            //only position
            query = "select * from (select id, position[0] as lat, position[1] as lon, sog, cog, " +
                    "COALESCE(to_char(timestamp , 'MM-DD-YYYY HH24:MI:SS'), '') as ts from " +
                    "tracks where theme = 'RADAR' limit 1000) as result " +
                    "where lat <" + maxLat + " and lat > " + minLat + " and lon < " + maxLon + " and lon > " + minLon;
            //both
            if (timeStart != null && timeEnd != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); //throws an error if invalid format
                LocalDateTime startDate = LocalDateTime.parse(timeStart, formatter);
                LocalDateTime endDate = LocalDateTime.parse(timeEnd, formatter);

                query = "select * from (select id, position[0] as lat, position[1] as lon, sog, cog, " +
                        "COALESCE(to_char(timestamp , 'MM-DD-YYYY HH24:MI:SS'), '') as ts from " +
                        "tracks where theme = 'RADAR' and timestamp > '" + startDate.format(formatter) + "' and timestamp < '" + endDate.format(formatter) + "' limit 1000) as result " +
                        "where lat <" + maxLat + " and lat > " + minLat + " and lon < " + maxLon + " and lon > " + minLon;
            }
        } else if (timeStart != null && timeEnd != null) {
            //only time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); //throws an error if invalid format
            LocalDateTime startDate = LocalDateTime.parse(timeStart, formatter);
            LocalDateTime endDate = LocalDateTime.parse(timeEnd, formatter);
            query = "select id,  position[0] as lat, position[1] as lon,  sog, cog, " +
                    "COALESCE(to_char(timestamp , 'MM-DD-YYYY HH24:MI:SS'), '') as ts from " +
                    "tracks where theme = 'RADAR' and timestamp > '" + startDate.format(formatter) + "' and timestamp < '" + endDate.format(formatter) + "' limit 1000";
        } else {
            //nothing
            return this.getRadarTargetState();
        }
        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(query);
        List<RadarTargetStateTO> radarStates = new ArrayList<>();
        while (rs.next()) {
            double sog = rs.getDouble("sog");
            double cog = rs.getDouble("cog");
            double lat = rs.getDouble("lat");
            double lon = rs.getDouble("lon");

            RadarTargetStateTO.Builder builder = RadarTargetStateTO.builder();
            String localDate = rs.getString("ts");

            radarStates.add(builder.setCog(cog).setSog(sog).setProviderID("urn:mrn:mcp:org:uol:database").setId(null).setTimestamp(localDate).setLatitude(lat).setLongitude(lon).setCrsEPSG("epsg:4362").build());

            System.out.println(sog + ", " + cog + ", " + localDate);
        }
        return radarStates;
    }

    /**
     * Returns available filter methods.
     * @return
     */
    public String getAvailableFilterMethods(){
        String result = "{\n" +
                "  \"RadarTargetState\" : {\n" +
                "    \"timestamp\" : [\"minmax\"],\n" +
                "    \"latitude\" : [\"minmax\"],\n" +
                "    \"longitude\" : [\"minmax\"]\n" +
                "  }\n" +
                "}";
        return result;
    }
}

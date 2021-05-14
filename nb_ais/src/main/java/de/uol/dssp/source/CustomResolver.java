package de.uol.dssp.source;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.oul.dssp.source.model.*;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * GraphQL query resolver class.
 */
@Component
public class CustomResolver implements GraphQLQueryResolver {

    //Obtain database credentials from config file
    @Value("${db.user}")
    private String dbUser;
    @Value("${db.pass}")
    private String dbPass;

    /**
     * Returns a list of AIS dynamic Messages (limited to 1000)
     * @return
     * @throws SQLException
     */
    public List<AISDynamicMessageTO> getAISDynamicMessage() throws SQLException {
        String url = "jdbc:postgresql://mydbhost.com/database";
        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPass);
        Connection conn = DriverManager.getConnection(url, props);
        String query = "select id, mmsi, position[0] as lat, position[1] as lon, heading, sog, cog, " +
                "COALESCE(to_char(timestamp , 'MM-DD-YYYY HH24:MI:SS'), '') as ts, rot, navstatus, theme from " +
                "tracks where theme = 'AIS' and RIGHT(referenceid,1) = '0'";

        query += " limit 1000";

        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(query);
        List<AISDynamicMessageTO> messages = new ArrayList<>();
        while (rs.next()) {
            int mmsi = rs.getInt("mmsi");
            String navstatus = rs.getString("navstatus");
            double heading = rs.getDouble("heading");
            double rot = rs.getDouble("rot");
            double sog = rs.getDouble("sog");
            double cog = rs.getDouble("cog");
            double lat = rs.getDouble("lat");
            double lon = rs.getDouble("lon");
            String localDate = rs.getString("ts");


            AISDynamicMessageTO.Builder builder = AISDynamicMessageTO.builder();
            AISDynamicMessageTO message = builder.setCog(cog).setHeading(heading).setLatitude(lat).setLongitude(lon)
                    .setMmsi(mmsi).setTimestamp(localDate).setSog(sog)
                    .setNavstatus(navstatus == null ? AISNavStatusTO.NotDefined : AISNavStatusTO.valueOf(navstatus))
                    .setRot(rot).setProviderID("urn:mrn:org:uol:database").build();
            messages.add(message);

        }
        return messages;
    }

    /**
     * Returns a list of AIS static messages (limited to 1000)
     * @return
     * @throws SQLException
     */
    public List<AISStaticMessageTO> getAISStaticMessage() throws SQLException {
        String url = "jdbc:postgresql://mydbhost.com/database";
        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPass);
        Connection conn = DriverManager.getConnection(url, props);
        String query = "select id, callsign, mmsi, length, width, draught," +
                " COALESCE(to_char(eta, 'MM-DD-YYYY HH24:MI:SS'), '') as eta," +
                " COALESCE(to_char(timestamp, 'MM-DD-YYYY HH24:MI:SS'), '') as ts, shiptype, imo, destination, " +
                "name, bow, stern, port, starboard from tracks where theme = 'AIS' and RIGHT(referenceid,1) = '1'";

        query += " limit 1000";

        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(query);
        List<AISStaticMessageTO> messages = new ArrayList<>();
        while (rs.next()) {
            String mmsi = rs.getString("mmsi");
            String callsign = rs.getString("callsign");
            double length = rs.getDouble("length");
            double width = rs.getDouble("width");
            double bow = rs.getDouble("bow");
            double stern = rs.getDouble("stern");
            double port = rs.getDouble("port");
            double starboard = rs.getDouble("starboard");
            String name = rs.getString("name");
            double draught = rs.getDouble("draught");
            String timestamp = rs.getString("ts");
            String eta = rs.getString("eta");
            String shiptype = rs.getString("shiptype");
            String imo = rs.getString("imo");
            String destination = rs.getString("destination");


            AISStaticMessageTO.Builder builder = AISStaticMessageTO.builder();
            AISStaticMessageTO message = builder.setMmsi(mmsi).setCallsign(callsign).setLength(length).setWidth(width).setBow(bow).setStern(stern)
                    .setStaboard(starboard).setPort(port).setName(name).setDraught(draught).setTimestamp(timestamp)
                    .setEta(eta).setImo(imo).setDestination(destination)
                    .setVesselType(shiptype == null ? null : AISVesselTypeTO.valueOf(shiptype)).build();
            messages.add(message);

        }
        return messages;
    }

    /**
     * Returns a filtered list of AIS dynamic messages (minmax filters for lat, lon and timestamp are available)
     * @param filter
     * @return
     * @throws SQLException
     */

    public List<AISDynamicMessageTO> getAISDynamicMessage(String filter) throws SQLException {
        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(filter, JsonObject.class);
        Float minLat = null;
        Float maxLat = null;
        Float minLon = null;
        Float maxLon = null;
        Float minSog = null;
        Float maxSog = null;
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
            if (obj.has("sog") && obj.get("sog").getAsJsonObject().has("min"))
                minSog = obj.get("sog").getAsJsonObject().get("min").getAsFloat();
            if (obj.has("sog") && obj.get("sog").getAsJsonObject().has("max"))
                maxSog = obj.get("sog").getAsJsonObject().get("max").getAsFloat();

        }
        List<AISDynamicMessageTO> messages = new ArrayList<>();
        String url = "jdbc:postgresql://mydbhost.com/database";
        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPass);
        Connection conn = DriverManager.getConnection(url, props);
        String query = "";
        if (minLat != null && maxLat != null && minLon != null && maxLon != null) {
            //only position
            query = "select * from (select id, mmsi, position[0] as lat, position[1] as lon, heading, sog, cog, " +
                    "COALESCE(to_char(timestamp , 'MM-DD-YYYY HH24:MI:SS'), '') as ts, rot, navstatus, theme from " +
                    "tracks where theme = 'AIS' and RIGHT(referenceid,1) = '0' limit 1000) as result " +
                    "where lat <" + maxLat + " and lat > " + minLat + " and lon < " + maxLon + " and lon > " + minLon;
            //both
            if (timeStart != null && timeEnd != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); //throws an error if invalid format
                LocalDateTime startDate = LocalDateTime.parse(timeStart, formatter);
                LocalDateTime endDate = LocalDateTime.parse(timeEnd, formatter);

                query = "select * from (select id, mmsi, position[0] as lat, position[1] as lon, heading, sog, cog, " +
                        "COALESCE(to_char(timestamp , 'MM-DD-YYYY HH24:MI:SS'), '') as ts, rot, navstatus, theme from " +
                        "tracks where theme = 'AIS' and RIGHT(referenceid,1) = '0' and timestamp > '" + startDate.format(formatter) + "' and timestamp < '" + endDate.format(formatter) + "' limit 1000) as result " +
                        "where lat <" + maxLat + " and lat > " + minLat + " and lon < " + maxLon + " and lon > " + minLon;
            }
        } else if (timeStart != null && timeEnd != null) {
            //only time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); //throws an error if invalid format
            LocalDateTime startDate = LocalDateTime.parse(timeStart, formatter);
            LocalDateTime endDate = LocalDateTime.parse(timeEnd, formatter);
            query = "select id, mmsi, position[0] as lat, position[1] as lon, heading, sog, cog, " +
                    "COALESCE(to_char(timestamp , 'MM-DD-YYYY HH24:MI:SS'), '') as ts, rot, navstatus, theme from " +
                    "tracks where theme = 'AIS' and RIGHT(referenceid,1) = '0' and timestamp > '" + startDate.format(formatter) + "' and timestamp < '" + endDate.format(formatter) + "' limit 1000";

        } else if (minSog != null && maxSog != null) {
            query = "select * from (select id, mmsi, position[0] as lat, position[1] as lon, heading, sog, cog, " +
                    "COALESCE(to_char(timestamp , 'MM-DD-YYYY HH24:MI:SS'), '') as ts, rot, navstatus, theme from " +
                    "tracks where theme = 'AIS' and RIGHT(referenceid,1) = '0' limit 1000) as result " +
                    "where sog <" + maxSog + " and sog > " + minSog;

        } else {
            //nothing
            return this.getAISDynamicMessage();
        }
        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(query);
        while (rs.next()) {
            int mmsi = rs.getInt("mmsi");
            String navstatus = rs.getString("navstatus");
            double heading = rs.getDouble("heading");
            double rot = rs.getDouble("rot");
            double sog = rs.getDouble("sog");
            double cog = rs.getDouble("cog");
            double lat = rs.getDouble("lat");
            double lon = rs.getDouble("lon");
            String localDate = rs.getString("ts");


            AISDynamicMessageTO.Builder builder = AISDynamicMessageTO.builder();
            System.out.println("Navstatus is '" + navstatus + "'");

            AISDynamicMessageTO message = builder.setCog(cog).setHeading(heading).setLatitude(lat).setLongitude(lon)
                    .setMmsi(mmsi).setTimestamp(localDate).setSog(sog)
                    .setNavstatus((navstatus == null || navstatus.isEmpty()) ? AISNavStatusTO.NotDefined : AISNavStatusTO.valueOf(navstatus))
                    .setRot(rot).setProviderID("urn:mrn:mcp:org:uol:database").build();
            messages.add(message);

        }
        return messages;
    }

    /**
     * Returns available filter methods.
     * @return
     */
    public String getAvailableFilterMethods() {
        String result = "{\n" +
                "  \"AISDynamicMessage\" : {\n" +
                "    \"timestamp\" : [\"minmax\"],\n" +
                "    \"latitude\" : [\"minmax\"],\n" +
                "    \"longitude\" : [\"minmax\"]\n" +
                "  }\n" +
                "}";
        return result;
    }
}

package htv.springboot.browsers.firefox.bookmarks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookmarkService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String BOOKMARK_DB_LOCATION =
            "C:/Users/hthvu/AppData/Roaming/Mozilla/Firefox/Profiles/8zbdl71t.default-release/places.sqlite";
    private static final String BOOKMARK_DB_CONNECTION_STR = "jdbc:sqlite:" + BOOKMARK_DB_LOCATION;
    private static final String BOOKMARK_QUERY = "SELECT moz_places.id, moz_places.url " +
            "FROM  moz_bookmarks, (SELECT moz_bookmarks.id from moz_bookmarks " +
            "WHERE moz_bookmarks.title == '%s' AND moz_bookmarks.fk IS NULL) tag " +
            "LEFT OUTER JOIN moz_places " +
            "ON moz_places.id == moz_bookmarks.fk " +
            "WHERE moz_bookmarks.parent == tag.id";

    public String[] retrieveBookmarksWithTag(String tag) {
        try (
                Connection conn = DriverManager.getConnection(BOOKMARK_DB_CONNECTION_STR);
                Statement statement = conn.createStatement()) {
            statement.setQueryTimeout(30);

            ResultSet rs = statement.executeQuery(String.format(BOOKMARK_QUERY, tag));
            List<String> urlsList = new ArrayList<>();
            while (rs.next()) {
                urlsList.add(rs.getString("url"));
            }

            return urlsList.toArray(new String[0]);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return new String[0];
    }
}

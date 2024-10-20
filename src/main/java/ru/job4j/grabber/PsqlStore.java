package ru.job4j.grabber;

import ru.job4j.grabber.pojo.Post;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection connection;

    public PsqlStore(Properties config) {
        init(config);
    }

    private void init(Properties config) {
        try (InputStream input = PsqlStore.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            config.load(input);
            Class.forName(config.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            connection = DriverManager.getConnection(config.getProperty("jdbc.url"),
                    config.getProperty("jdbc.username"),
                    config.getProperty("jdbc.password"));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Post createPostFromResultSet(ResultSet set) {
        Post post = null;
        try {
            post = new Post(set.getInt("id"),
                    set.getString("title"),
                    set.getString("link"),
                    set.getString("description"),
                    set.getTimestamp("created").toLocalDateTime().withNano(0));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return post;
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement =
                     connection.prepareStatement("INSERT INTO post(name, text, link, created)"
                             + " VALUES(?, ?, ?, ?)"
                             + "ON CONFLICT (link) DO NOTHING")) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement =
                     connection.prepareStatement("select * from post")) {
            statement.execute();
            while (statement.getResultSet().next()) {
                posts.add(createPostFromResultSet(statement.getResultSet()));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT * FROM post WHERE id = ?")) {
            statement.setInt(1, id);
            statement.execute();
            post = createPostFromResultSet(statement.getResultSet());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    private static void runExample() {
        try (PsqlStore store = new PsqlStore(new Properties())) {
            Post post1 = new Post(1, "title_1", "https://stackoverflow.com", "descritption_1",
                    new Timestamp(System.currentTimeMillis()).toLocalDateTime());
            Post post2 = new Post(2, "title_2", "w.stackoverflow.com", "descritption_2",
                    new Timestamp(System.currentTimeMillis()).toLocalDateTime());
            Post post3 = new Post(3, "title_3", "http://stackoverflow.com", "descritption_3",
                    new Timestamp(System.currentTimeMillis()).toLocalDateTime());
            Post post4 = new Post(4, "title_4", "www.stackoverflow.com", "descritption_4",
                    new Timestamp(System.currentTimeMillis()).toLocalDateTime());
            store.save(post1);
            store.save(post2);
            store.save(post3);
            store.save(post4);
            store.save(post4);
            store.getAll().forEach(System.out::println);
            System.out.println();
            System.out.println(store.findById(1));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        runExample();
    }
}

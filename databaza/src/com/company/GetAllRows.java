package com.company;

import java.sql.*;
import java.util.Random;

public class GetAllRows{

    private Connection con;

    private String nameOfTable = "TAB_smarzik";

    Random random = new Random();

    public GetAllRows(Connection con){
        this.con = con;
    }

    public void create() throws SQLException {
        String query = "CREATE TABLE " + nameOfTable +
                " (id INTEGER not NULL, " +
                " name VARCHAR(255), " +
                " length INTEGER, " +
                " rating INTEGER, " +
                " PRIMARY KEY ( id ))";

        try (Statement statement = con.createStatement()) {
            statement.executeUpdate(query);
        }
        catch (SQLException e) {
            throw e;
        }
    }

    public void drop() throws SQLException {
        String query = "DROP TABLE " + nameOfTable;

        try (Statement statement = con.createStatement()) {
            statement.executeUpdate(query);
        }
        catch (SQLException e) {
            throw e;
        }
    }

    public void insert100() throws SQLException {
        String query = "INSERT INTO " + nameOfTable + " VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {

            for(int i = 0; i < 100; i++){
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, "Film-" + i);
                int length = random.nextInt(240) + 60;
                preparedStatement.setInt(3, length);
                int rating = random.nextInt(11);
                preparedStatement.setInt(4, rating);
                preparedStatement.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw e;
        }
    }

    public void selectFilms(int minRating) throws SQLException {
        String query = "SELECT * FROM " + nameOfTable + " WHERE rating >= ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, minRating);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int length = resultSet.getInt("length");
                int rating = resultSet.getInt("rating");
                System.out.println(id + " : " + name + " - " + length + " - " + rating);
            }
        }
        catch (SQLException e) {
            throw e;
        }
    }

    public void changeSelectedFilms(int minRating) throws SQLException {
        String query = "UPDATE " + nameOfTable + " SET name = ? WHERE rating >= ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {

            preparedStatement.setString(1, "good film");
            preparedStatement.setInt(2, minRating);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            throw e;
        }
    }

    public void deleteRestOfFilms(int minRating) throws SQLException {
        String query = "DELETE FROM " + nameOfTable + " WHERE rating < ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, minRating);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            throw e;
        }
    }

    public void getRemainingRows() throws SQLException {
        String query = "SELECT COUNT(*) AS number FROM " + nameOfTable;

        try (Statement statement = con.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()){
                int rows = resultSet.getInt("number");
                System.out.println();
                System.out.println("POCET ZOSTAVAJUCICH RIADKOV:  " + rows);
            }
        }
        catch (SQLException e) {
            throw e;
        }
    }

    public void getFilms() throws SQLException {
        String query = "SELECT * FROM " + nameOfTable;

        try (Statement statement = con.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int length = resultSet.getInt("length");
                int rating = resultSet.getInt("rating");
                System.out.println(id + " : " + name + " - " + length + " - " + rating);
            }
        }
        catch (SQLException e) {
            throw e;
        }
    }

    public static void main(String[] args) {
        System.out.println("Getting All Rows from a table!\n");
        Connection con = null;
        String url = "jdbc:mysql://kempelen.dai.fmph.uniba.sk:3306/";
        String db = "ee";
        String driver = "com.mysql.jdbc.Driver";
        String user = "ee";
        String pass = "pivo";

        try{
            Class.forName(driver).newInstance();
            //characterEncoding - fix bug
            con = DriverManager.getConnection(url+db + "?characterEncoding=latin1", user, pass);

            GetAllRows getAllRows = new GetAllRows(con);

            try{
                getAllRows.create();
                getAllRows.insert100();
                System.out.println("FILMY S HODNOTENIM ASPON 8: ");
                getAllRows.selectFilms(8);
                getAllRows.changeSelectedFilms(8);
                getAllRows.deleteRestOfFilms(8);
                getAllRows.getRemainingRows();
                System.out.println("FILMY PO VYMAZANI: ");
                getAllRows.getFilms();
                getAllRows.drop();

            }
            catch (SQLException s){
                System.out.println("SQL code does not execute.");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}

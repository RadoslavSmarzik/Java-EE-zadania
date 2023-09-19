package com.company;

import java.sql.*;
import java.util.Random;

public class GetAllRowsPokrocile {

    private Connection con;

    private String nameOfTable = "TAB_smarzik";

    Random random = new Random();

    public GetAllRowsPokrocile(Connection con){
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

    public void procedureInsert() throws SQLException {
        String dropProcedure = "DROP PROCEDURE IF EXISTS NEW_FILM";

        String createProcedure = "CREATE PROCEDURE NEW_FILM (IN id integer, IN name varchar(255), IN length integer, IN rating integer) " +
                "BEGIN "+
                " INSERT INTO " + nameOfTable + " VALUES (id, name, length, rating);" +
                "END";

        try (Statement stmtDrop = con.createStatement()) {
            stmtDrop.execute(dropProcedure);
        }
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(createProcedure);
        }
    }

    public void insert100() throws SQLException {
        try (CallableStatement cstmt = con.prepareCall("{call NEW_FILM(?,?,?,?)}")) {
            con.setAutoCommit(false);
            for(int i = 0; i < 100; i++){
                cstmt.setInt(1, i);
                cstmt.setString(2, "Film-" + i);
                int length = random.nextInt(240) + 60;
                cstmt.setInt(3, length);
                int rating = random.nextInt(11);
                cstmt.setInt(4, rating);
                cstmt.executeUpdate();
            }
            con.commit();
            con.setAutoCommit(true);

        } catch (SQLException e) {
            con.rollback();
            e.printStackTrace();
        }
    }

    public void procedureSelect() throws SQLException {
        String dropProcedure = "DROP PROCEDURE IF EXISTS SELECT_FILMS";

        String createProcedure =
                "CREATE PROCEDURE SELECT_FILMS(IN minRating integer) " +
                        "BEGIN " +
                        "SELECT * FROM " + nameOfTable + " WHERE rating >= minRating;" +
                        "END";

        try (Statement stmtDrop = con.createStatement()) {
            stmtDrop.execute(dropProcedure);
        }
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(createProcedure);
        }
    }

    public void selectFilms(int minRating) {
        try (CallableStatement cstmt = con.prepareCall("{call SELECT_FILMS(?)}")) {
            cstmt.setInt(1, minRating);
            ResultSet resultSet = cstmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int length = resultSet.getInt("length");
                int rating = resultSet.getInt("rating");
                System.out.println(id + " : " + name + " - " + length + " - " + rating);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void procedureChangeFilmName() throws SQLException {
        String dropProcedure = "DROP PROCEDURE IF EXISTS CHANGE_FILM_NAME";

        String createProcedure =
                "CREATE PROCEDURE CHANGE_FILM_NAME(IN minRating integer, IN newName varchar(255)) " +
                        "BEGIN " +
                        "UPDATE " + nameOfTable + " SET name = newName WHERE rating >= minRating;" +
                        "END";

        try (Statement stmtDrop = con.createStatement()) {
            stmtDrop.execute(dropProcedure);
        }
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(createProcedure);
        }
    }

    public void procedureDeleteFilms() throws SQLException {
        String dropProcedure = "DROP PROCEDURE IF EXISTS DELETE_FILMS";

        String createProcedure =
                "CREATE PROCEDURE DELETE_FILMS(IN minRating integer) " +
                        "BEGIN " +
                        "DELETE FROM " + nameOfTable + " WHERE rating < minRating;" +
                        "END";

        try (Statement stmtDrop = con.createStatement()) {
            stmtDrop.execute(dropProcedure);
        }
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(createProcedure);
        }
    }

    public void changeAndDelete(int minRating) throws SQLException {
        try (CallableStatement cstmt = con.prepareCall("{call CHANGE_FILM_NAME(?,?)}");
             CallableStatement cstmt1 = con.prepareCall("{call DELETE_FILMS(?)}")) {
            con.setAutoCommit(false);

            cstmt.setInt(1, minRating);
            cstmt.setString(2, "good film");
            cstmt.executeQuery();

            cstmt1.setInt(1, minRating);
            cstmt1.executeQuery();

            con.commit();
            con.setAutoCommit(true);

        } catch (SQLException e) {
            con.rollback();
            e.printStackTrace();
        }
    }

    public void procedureRemainingRows() throws SQLException {
        String dropProcedure = "DROP PROCEDURE IF EXISTS GET_REMAINING_ROWS";

        String createProcedure =
                "CREATE PROCEDURE GET_REMAINING_ROWS(OUT number integer) " +
                        "BEGIN " +
                        "SELECT COUNT(*) into number FROM " + nameOfTable + ";" +
                        "select number; " +
                        "END";

        try (Statement stmtDrop = con.createStatement()) {
            stmtDrop.execute(dropProcedure);
        }
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(createProcedure);
        }
    }

    public void getRemainingRows() throws SQLException {
        try (CallableStatement cstmt = con.prepareCall("{call GET_REMAINING_ROWS(?)}")) {
            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.executeQuery();
            int rowsCount = cstmt.getInt(1);
            System.out.println();
            System.out.println("POCET ZOSTAVAJUCICH RIADKOV:  " + rowsCount);
        }
        catch (SQLException e) {
            e.printStackTrace();
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

            GetAllRowsPokrocile getAllRowsPokrocile = new GetAllRowsPokrocile(con);

            try{

                getAllRowsPokrocile.create();
                getAllRowsPokrocile.procedureInsert();
                getAllRowsPokrocile.procedureChangeFilmName();
                getAllRowsPokrocile.procedureDeleteFilms();
                getAllRowsPokrocile.procedureRemainingRows();
                getAllRowsPokrocile.procedureSelect();

                getAllRowsPokrocile.insert100();
                System.out.println("FILMY S HODNOTENIM ASPON 8: ");
                getAllRowsPokrocile.selectFilms(8);
                getAllRowsPokrocile.changeAndDelete(8);

                getAllRowsPokrocile.getRemainingRows();

                getAllRowsPokrocile.drop();

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

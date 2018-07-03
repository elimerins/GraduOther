import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Console;
import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GachonDatabase {
    private static final String DB_NAME = "gtg";
    private static final String GN_TABLE_NAME = "gn";
    private static final String MJ_TABLE_NAME = "mj";
    private static final String COURSE_TABLE_NAME = "course";

    private static Connection conn;
    private PreparedStatement createPstmt;
    private PreparedStatement insertPstmt;
    private PreparedStatement selectPstmt;

    private static final String BASE_URL = "http://203.249.126.126:9090/servlets/timetable";

    public GachonDatabase() {
        conn = null;
        insertPstmt = null;
        selectPstmt = null;
    }

    public static boolean setConn(String addr, String user, String password) {
        String url = String.format("jdbc:mysql://%s:3306/?serverTimezone=UTC", addr);

        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean checkConn() {
        try {
            if (conn == null || !conn.isValid(5))
                return false;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean createDatabase() {
        String query = String.format("CREATE DATABASE IF NOT EXISTS %s", DB_NAME);

        try {
            createPstmt = conn.prepareStatement(query);
            createPstmt.executeUpdate();
            createPstmt.close();
        } catch (SQLException e) {
            System.err.println("createDatabase error!");
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean createTables() {
        String query;
        boolean ret = true;

        try {
            // The table for General Education
            query = String.format("CREATE TABLE IF NOT EXISTS %s.`%s` (" +
                    "`cor_cd` VARCHAR(10) NOT NULL, " +
                    "`name` VARCHAR(45) NULL, " +
                    "PRIMARY KEY (`cor_cd`))", DB_NAME, GN_TABLE_NAME);

            createPstmt = conn.prepareStatement(query);
            createPstmt.executeUpdate();
            createPstmt.close();

            // The table for Major
            query = String.format("CREATE TABLE IF NOT EXISTS %s.`%s` (" +
                    "`univ_cd` VARCHAR(10) NOT NULL, " +
                    "`maj_cd` VARCHAR(10) NOT NULL, " +
                    "`name` VARCHAR(45) NULL, " +
                    "PRIMARY KEY (`maj_cd`))", DB_NAME, MJ_TABLE_NAME);

            createPstmt = conn.prepareStatement(query);
            createPstmt.executeUpdate();
            createPstmt.close();

            // The table for Course
            query = String.format("CREATE TABLE IF NOT EXISTS %s.`%s` (" +
                            "  `year` INT NOT NULL," +
                            "  `semester` INT NOT NULL," +
                            "  `code` VARCHAR(10) NOT NULL," +
                            "  `title` VARCHAR(100) NOT NULL," +
                            "  `classification` VARCHAR(45) NOT NULL," +
                            "  `credit` INT NOT NULL," +
                            "  `quota` INT NOT NULL," +
                            "  `time` VARCHAR(100)," +
                            "  `instructor` VARCHAR(45) NOT NULL," +
                            "  `room` VARCHAR(45)," +
                            "  `grade` VARCHAR(100) NOT NULL," +
                            "  `syllabus` VARCHAR(100) NULL," +
                            "  `maj_cd` VARCHAR(10)," +
                            "  `cor_cd` VARCHAR(10)," +
                            "  PRIMARY KEY (`year`, `semester`, `code`)," +
                            "  FOREIGN KEY (`maj_cd`) REFERENCES %s(`maj_cd`)," +
                            "  FOREIGN KEY (`cor_cd`) REFERENCES %s(`cor_cd`))",
                    DB_NAME, COURSE_TABLE_NAME, MJ_TABLE_NAME, GN_TABLE_NAME);

            createPstmt = conn.prepareStatement(query);
            createPstmt.executeUpdate();
            createPstmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            ret = false;
        }

        return ret;
    }

    public boolean insertCodes() {
        Document doc;
        String url;
        String query;

        try {
            conn.setCatalog(DB_NAME);

            // General educations Routine
            query = String.format("INSERT INTO `%s` (cor_cd, name) " +
                    "VALUES(?, ?)", GN_TABLE_NAME);
            insertPstmt = conn.prepareStatement(query);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        try {
            // Use Jsoup for crawling
            url = BASE_URL + "?attribute=top&lang=ko";
            doc = Jsoup.connect(url).get();

            for (Element gnOpt : doc.select("select[name='cor_cd']").select("option")) {
                if (gnOpt.text().contains("폐기"))
                    continue;

                try {
                    // 1: cor_cd, 2: name
                    insertPstmt.setString(1, gnOpt.attr("value"));
                    insertPstmt.setString(2, gnOpt.text());
                    insertPstmt.executeUpdate();
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            }


            try {
                insertPstmt.close();
                // Majors Routine
                query = String.format("INSERT INTO `%s` (univ_cd, maj_cd, name) " +
                        "VALUES(?, ?, ?)", MJ_TABLE_NAME);
                insertPstmt = conn.prepareStatement(query);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }

            Map<String, String> body = Collections.synchronizedMap(new HashMap<>());
            body.put("attribute", "top");
            body.put("flag", "A");
            body.put("lang", "ko");
            body.put("year", "");
            body.put("hakgi", "");
            body.put("isu_cd", "");
            body.put("cor_cd", "");
            body.put("univ_cd", "");
            body.put("maj_cd", "");

            // The code of searching majors is 1
            body.put("isu_cd", "1");

            String codeUrl = BASE_URL;

            for (Element univOpt : doc.select("select[name='univ_cd']").select("option")) {
                if (univOpt.text().contains("폐기"))
                    continue;

                body.put("univ_cd", univOpt.attr("value"));
                doc = Jsoup.connect(codeUrl)
                        .data(body)
                        .post();

                for (Element majOpt : doc.select("select[name='maj_cd']").select("option")) {
                    if (majOpt.text().contains("폐기") || majOpt.text().contains("(야)"))
                        continue;

                    try {
                        // 1: univ_cd, 2: maj_cd, 3: name
                        insertPstmt.setString(1, univOpt.attr("value"));
                        insertPstmt.setString(2, majOpt.attr("value"));
                        insertPstmt.setString(3, majOpt.text());
                        insertPstmt.executeUpdate();
                    } catch (SQLException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }

            try {
                insertPstmt.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return true;
    }

    public boolean insertCourses() {
        Document topDoc = null;
        Document listDoc = null;

        String url = null;
        String insertQuery = null;
        String selectMjQuery = null;
        String selectGnQuery = null;
        PreparedStatement ps = null;

        ResultSet mjRs = null;
        ResultSet gnRs = null;

        try {
            conn.setCatalog(DB_NAME);

            insertQuery = String.format("INSERT INTO `%s` (year, semester, code, title, classification, credit, quota, " +
                            "time, instructor, room, grade, syllabus, maj_cd, cor_cd) " +
                            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    COURSE_TABLE_NAME);
            insertPstmt = conn.prepareStatement(insertQuery);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Use Jsoup for crawling
        url = BASE_URL + "?attribute=top&lang=ko";
        try {
            topDoc = Jsoup.connect(url).get();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        Map<String, String> body = Collections.synchronizedMap(new HashMap<>());
        body.put("attribute", "top");
        body.put("flag", "A");
        body.put("lang", "ko");
        body.put("year", "");
        body.put("hakgi", "");
        body.put("isu_cd", "");
        body.put("cor_cd", "");
        body.put("univ_cd", "");
        body.put("maj_cd", "");

        url = BASE_URL + "?attribute=lists";

        try {
            // For Majors
            selectMjQuery = String.format("SELECT univ_cd, maj_cd, name FROM `%s`", MJ_TABLE_NAME);
            selectPstmt = conn.prepareStatement(selectMjQuery);

            mjRs = selectPstmt.executeQuery();

            // For General Educations
            selectGnQuery = String.format("SELECT cor_cd, name FROM `%s`", GN_TABLE_NAME);
            ps = conn.prepareStatement(selectGnQuery);

            gnRs = ps.executeQuery();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Insert Query loop
        for (Element yearOpt : topDoc.select("select[name='year']").select("option")) {
            Integer year = Integer.valueOf(yearOpt.attr("value"));

            System.out.println(String.format("[INFO:year] %d Start!", year));
            if (year < 2018)
                break;

            for (Element hakgiOpt : topDoc.select("select[name='hakgi']").select("option")) {
                Integer hakgi = Integer.valueOf(hakgiOpt.attr("value"));

                System.out.println(String.format("[INFO:hakgi] %d Start!", hakgi));
                if (hakgi != 10)
                    continue;

                String univ_cd;
                String maj_cd;
                String cor_cd;
                Elements table;

                body.put("year", year.toString());
                body.put("hakgi", hakgi.toString());

                // Major classification code
                body.put("isu_cd", "1");

                try {
                    // The loop for majors
                    while (mjRs.next()) {
                        univ_cd = mjRs.getString("univ_cd");
                        maj_cd = mjRs.getString("maj_cd");

                        System.out.println(mjRs.getString("name") + " Start!");

                        body.put("univ_cd", univ_cd);
                        body.put("maj_cd", maj_cd);

                        try {
                            listDoc = Jsoup.connect(url)
                                    .data(body)
                                    .post();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        table = listDoc.select("table")
                                .select("table")
                                .eq(2);

                        // insert the subjects
                        for (Element tr : table.select("tr")) {
                            // Pass column labels
                            if (tr.elementSiblingIndex() == 0)
                                continue;

                            try {
                                // year, semester, code, title, classification, credit,
                                // quota, time, instructor, room, grade, syllabus, maj_cd, cor_cd (14 columns)
                                insertPstmt.setInt(1, year);
                                insertPstmt.setInt(2, hakgi);
                                // pass the index 0(row number of table)
                                insertPstmt.setString(3, tr.select("td").eq(1).text());
                                insertPstmt.setString(4, textFilter(tr.select("td").eq(2).text()));
                                // pass the index 3(trailer link)
                                insertPstmt.setString(5, tr.select("td").eq(4).text());
                                insertPstmt.setInt(6, Integer.parseInt(tr.select("td").eq(5).text()));
                                insertPstmt.setInt(7, Integer.parseInt(tr.select("td").eq(6).text()));
                                insertPstmt.setString(8, tr.select("td").eq(7).text());
                                insertPstmt.setString(9, tr.select("td").eq(8).text());
                                insertPstmt.setString(10, tr.select("td").eq(9).text());
                                insertPstmt.setString(11, tr.select("td").eq(10).text());
                                // The format of syllabus is a javascript function
                                insertPstmt.setString(12, tr.select("td").eq(1).select("a")
                                        .attr("href"));
                                insertPstmt.setString(13, maj_cd);
                                insertPstmt.setString(14, null);

                                insertPstmt.executeUpdate();
                            } catch (SQLException e) {
                                System.err.println(e.getMessage());
                            } catch (NumberFormatException ne) {
                                System.err.println(ne.getMessage());
                            }
                        }

                        System.out.println(mjRs.getString("name") + " End!");
                    }

                    mjRs.beforeFirst();
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }

                // The classification code for general educations
                body.put("isu_cd", "2");

                try {
                    // The loop for general educations
                    while (gnRs.next()) {
                        cor_cd = gnRs.getString("cor_cd");

                        System.out.println(gnRs.getString("name") + " Start!");

                        body.put("cor_cd", cor_cd);

                        try {
                            listDoc = Jsoup.connect(url)
                                    .data(body)
                                    .post();
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }

                        table = listDoc.select("table")
                                .select("table")
                                .eq(2);

                        // insert the subjects
                        for (Element tr : table.select("tr")) {
                            // Pass column labels
                            if (tr.elementSiblingIndex() == 0)
                                continue;

                            try {
                                // year, semester, code, title, classification, credit,
                                // quota, time, instructor, room, grade, syllabus, maj_cd, cor_cd (14 columns)
                                insertPstmt.setInt(1, year);
                                insertPstmt.setInt(2, hakgi);
                                // pass the index 0(row number of table)
                                insertPstmt.setString(3, tr.select("td").eq(1).text());
                                insertPstmt.setString(4, textFilter(tr.select("td").eq(2).text()));
                                // pass the index 3(trailer link)
                                insertPstmt.setString(5, tr.select("td").eq(4).text());
                                insertPstmt.setInt(6, Integer.parseInt(tr.select("td").eq(5).text()));
                                insertPstmt.setInt(7, Integer.parseInt(tr.select("td").eq(6).text()));
                                insertPstmt.setString(8, tr.select("td").eq(7).text());
                                insertPstmt.setString(9, tr.select("td").eq(8).text());
                                insertPstmt.setString(10, tr.select("td").eq(9).text());
                                insertPstmt.setString(11, tr.select("td").eq(10).text());
                                // The format of syllabus is a javascript function
                                insertPstmt.setString(12, tr.select("td").eq(1).select("a")
                                        .attr("href"));
                                insertPstmt.setString(13, null);
                                insertPstmt.setString(14, cor_cd);

                                insertPstmt.executeUpdate();
                            } catch (SQLException e) {
                                System.err.println(e.getMessage());
                            } catch (NumberFormatException ne) {
                                System.err.println(ne.getMessage());
                            }
                        }

                        System.out.println(gnRs.getString("name") + " Start!");
                    }

                    gnRs.beforeFirst();
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            }
        }


        try {
            gnRs.close();
            mjRs.close();
            ps.close();
            selectPstmt.close();
        } catch (SQLException e) {
            System.err.printf("[close error] ");
            System.err.println(e.getMessage());
        }

        return true;
    }

    public String textFilter(String text) {
        return text.replaceAll("(&nbsp;)", "");
    }

    public static void main(String[] args) {
        GachonDatabase gd = new GachonDatabase();
        Console console = System.console();

        System.out.print("DB USER: ");
        String user = console.readLine();
        System.out.print("PASSWORD: ");
        String password = String.valueOf(console.readPassword());


        gd.setConn("localhost", user, password);
        gd.createDatabase();
        gd.createTables();
        gd.insertCodes();
        gd.insertCourses();
    }
}

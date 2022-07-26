package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:./src/carsharing/db/carsharing";

    public static void main(String[] args) {
        // write your code here

        try {
            Class.forName(JDBC_DRIVER);
            createEmptyTables();

            int mainOpt = mainMenu();
            System.out.println();
            while (mainOpt != 0) {
                switch (mainOpt) {
                    case 1:
                        manger();
                        break;
                    case 2:
                        customer();
                        break;
                    case 3:
                        createCustomer();
                        break;
                }
                System.out.println();
                mainOpt = mainMenu();
            }
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch(Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    public static int mainMenu() {
        System.out.println("1. Log in as a manager");
        System.out.println("2. Log in as a customer");
        System.out.println("3. Create a customer");
        System.out.println("0. Exit");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }

    public static void customer() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);

        int customerOpt = customerList(conn);
        System.out.println();
        int customerMenuOpt;
        while (customerOpt != 0) {
            customerMenuOpt = customerMenu();
            System.out.println();
            while (customerMenuOpt != 0) {
                switch (customerMenuOpt) {
                    case 1:
                        rentCar(conn, customerOpt);
                        break;
                    case 2:
                        returnCar(conn, customerOpt);
                        break;
                    case 3:
                        myCar(conn, customerOpt);
                        break;
                }
                System.out.println();
                customerMenuOpt = customerMenu();
                System.out.println();
            }
            customerOpt = 0;
        }

        conn.close();
    }

    public static int customerList(Connection conn) throws SQLException {
        int customerOpt = 0;
        Statement stat = conn.createStatement();
        String sql = "SELECT * FROM CUSTOMER ORDER BY ID;";
        ResultSet rs = stat.executeQuery(sql);
        if (rs.next()) {
            System.out.println("Customer list:");
            int i = rs.getInt("ID");
            String customerName = rs.getString("NAME");
            System.out.println(i + ". " + customerName);
            while (rs.next()) {
                i = rs.getInt("ID");
                customerName = rs.getString("NAME");
                System.out.println(i + ". " + customerName);
            }
            System.out.println("0. Back");
            Scanner scanner = new Scanner(System.in);
            customerOpt = scanner.nextInt();
        } else {
            System.out.println("The customer list is empty!");
        }
        stat.close();
        return customerOpt;
    }

    public static int customerMenu() {
        System.out.println("1. Rent a car");
        System.out.println("2. Return a rented car");
        System.out.println("3. My rented car");
        System.out.println("0. Back");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }

    public static void rentCar(Connection conn, int customerID) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "SELECT RENTED_CAR_ID IS NULL " +
                "FROM CUSTOMER " +
                "WHERE ID=" + customerID + ";";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        boolean rent = !rs.getBoolean(1);
        stmt.close();
        if (rent) {
            System.out.println("You've already rented a car!");
        } else {
            int companyOpt = chooseCompany(conn);
            System.out.println();
            if (companyOpt != 0) {
                rent = chooseCar(conn, customerID, companyOpt);
                System.out.println();
            }
            while (companyOpt != 0 && !rent) {
                companyOpt = chooseCompany(conn);
                System.out.println();
                rent = chooseCar(conn, customerID, companyOpt);
                System.out.println();
            }
        }
    }

    public static int chooseCompany(Connection conn) throws SQLException {
        int companyOpt = 0;
        Statement stat = conn.createStatement();
        String sql = "SELECT * FROM COMPANY ORDER BY ID;";
        ResultSet rs = stat.executeQuery(sql);
        if (rs.next()) {
            System.out.println("Choose a company:");
            int i = rs.getInt("ID");
            String companyName = rs.getString("NAME");
            System.out.println(i + ". " + companyName);
            while (rs.next()) {
                i = rs.getInt("ID");
                companyName = rs.getString("NAME");
                System.out.println(i + ". " + companyName);
            }
            System.out.println("0. Back");
            Scanner scanner = new Scanner(System.in);
            companyOpt = scanner.nextInt();
        } else {
            System.out.println("The company list is empty!");
        }
        stat.close();
        return companyOpt;
    }

    public static boolean chooseCar(Connection conn, int customerID, int companyID) throws SQLException {
        boolean rent = false;
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM CAR " +
                "WHERE COMPANY_ID=" + companyID +
                "ORDER BY ID;";
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            ArrayList<String> carArray = new ArrayList<>();
            ArrayList<Integer> carIDArray = new ArrayList<>();
            System.out.println("Choose a car:");
            int i = 0;
            String carName = rs.getString("NAME");
            int carID = rs.getInt("ID");
            boolean rented = rs.getBoolean("RENTED");
            if (!rented) {
                i++;
                carArray.add(carName);
                carIDArray.add(carID);
                System.out.println(i + ". " + carName);
            }
            while (rs.next()) {
                carName = rs.getString("NAME");
                carID = rs.getInt("ID");
                rented = rs.getBoolean("RENTED");
                if (!rented) {
                    i++;
                    carArray.add(carName);
                    carIDArray.add(carID);
                    System.out.println(i + ". " + carName);
                }
            }
            if (i == 0) {
                sql = "SELECT NAME FROM COMPANY WHERE ID=" + companyID + ";";
                rs = stmt.executeQuery(sql);
                rs.next();
                String companyName = rs.getString("NAME");
                System.out.println("No available cars in the '" + companyName + "' company");
            } else {
                System.out.println("0. Back");
                Scanner scanner = new Scanner(System.in);
                int carOpt = scanner.nextInt();
                if (carOpt != 0) {
                    int chosenID = carIDArray.get(carOpt - 1);
                    sql = "UPDATE CUSTOMER SET RENTED_CAR_ID=" + chosenID + " WHERE ID=" + customerID + ";";
                    stmt.executeUpdate(sql);
                    sql = "UPDATE CAR SET RENTED=1 WHERE ID=" + chosenID + ";";
                    stmt.executeUpdate(sql);
                    System.out.println("You rented '" + carArray.get(carOpt - 1) + "'");
                    rent = true;
                }
            }
        } else {
            System.out.println("The car list is empty!");
        }
        stmt.close();
        return rent;
    }

    public static void returnCar(Connection conn, int customerID) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "SELECT RENTED_CAR_ID IS NULL " +
                "FROM CUSTOMER " +
                "WHERE ID=" + customerID + ";";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        boolean noRent = rs.getBoolean(1);
        if (noRent) {
            System.out.println("You didn't rent a car!");
        } else {
            sql = "SELECT RENTED_CAR_ID FROM CUSTOMER WHERE ID=" + customerID + ";";
            rs = stmt.executeQuery(sql);
            rs.next();
            int carID = rs.getInt("RENTED_CAR_ID");
            sql = "UPDATE CAR SET RENTED=0 WHERE ID=" + carID + ";";
            stmt.executeUpdate(sql);
            sql = "UPDATE CUSTOMER SET RENTED_CAR_ID=RENTED_CAR_ID+NULL WHERE ID=" + customerID + ";";
            stmt.executeUpdate(sql);
            System.out.println("You've returned a rented car!");
        }
        stmt.close();
    }

    public static void myCar(Connection conn, int customerID) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "SELECT RENTED_CAR_ID IS NULL " +
                "FROM CUSTOMER " +
                "WHERE ID=" + customerID + ";";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        boolean noRent = rs.getBoolean(1);
        if (noRent) {
            System.out.println("You didn't rent a car!");
        } else {
            sql = "SELECT RENTED_CAR_ID FROM CUSTOMER WHERE ID=" + customerID + ";";
            rs = stmt.executeQuery(sql);
            rs.next();
            int carID = rs.getInt("RENTED_CAR_ID");
            sql = "SELECT * FROM CAR WHERE ID=" + carID + ";";
            rs = stmt.executeQuery(sql);
            rs.next();
            String carName = rs.getString("NAME");
            int companyID = rs.getInt("COMPANY_ID");
            sql = "SELECT NAME FROM COMPANY WHERE ID=" + companyID + ";";
            rs = stmt.executeQuery(sql);
            rs.next();
            String companyName = rs.getString("NAME");
            System.out.println("Your rented car:");
            System.out.println(carName);
            System.out.println("Company:");
            System.out.println(companyName);
        }
        stmt.close();
    }

    public static void createCustomer() throws SQLException {
        Connection conn = DriverManager.getConnection((DB_URL));

        System.out.println("Enter the customer name:");
        Scanner scanner = new Scanner(System.in);
        String customerName = scanner.nextLine();
        Statement stmt = conn.createStatement();
        String sql =  "INSERT INTO CUSTOMER (NAME) " +
                "VALUES ('" + customerName + "');";
        stmt.executeUpdate(sql);
        System.out.println("The customer was added!");

        stmt.close();

        conn.close();
    }

    public static void manger() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);

        int mangerOpt = mangerMenu();
        System.out.println();
        while (mangerOpt != 0) {
            switch (mangerOpt) {
                case 1:
                    companyManage(conn);
                    break;
                case 2:
                    createCompany(conn);
                    break;
            }
            System.out.println();
            mangerOpt = mangerMenu();
            System.out.println();
        }

        conn.close();
    }

    public static int mangerMenu() {
        System.out.println("1. Company list");
        System.out.println("2. Create a company");
        System.out.println("0. Back");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }

    public static void createCompany(Connection conn) throws SQLException {
        System.out.println("Enter the company name:");
        Scanner scanner = new Scanner(System.in);
        String tableNameStr = scanner.nextLine();
        //String tableName = tableNameStr.replaceAll(" ", "_");

        Statement stmt = conn.createStatement();
        String sql =  "INSERT INTO COMPANY (NAME) " +
                "VALUES ('" + tableNameStr + "');";
        stmt.executeUpdate(sql);
        System.out.println("The company was created!");
        stmt.close();
    }

    public static void companyManage(Connection conn) throws SQLException {
        int companyOpt = companyList(conn);
        int companyMenuOpt;
        while (companyOpt != 0) {
            companyMenuOpt = companyMenu();
            System.out.println();
            while (companyMenuOpt != 0) {
                switch (companyMenuOpt) {
                    case 1:
                        carList(conn, companyOpt);
                        break;
                    case 2:
                        createCar(conn, companyOpt);
                        break;
                }
                System.out.println();
                companyMenuOpt = companyMenu();
                System.out.println();
            }
            companyOpt = 0;
        }
    }

    public static int companyList(Connection conn) throws SQLException {
        int companyOpt = 0;
        Statement stat = conn.createStatement();
        String sql = "SELECT * FROM COMPANY ORDER BY ID;";
        ResultSet rs = stat.executeQuery(sql);
        if (rs.next()) {
            String companyName;
            ArrayList<String> companyArray = new ArrayList<>();
            System.out.println("Choose a company:");
            int i = rs.getInt("ID");
            companyName = rs.getString("NAME");
            companyArray.add(companyName);
            System.out.println(i + ". " + companyName);
            while (rs.next()) {
                i = rs.getInt("ID");
                companyName = rs.getString("NAME");
                companyArray.add(companyName);
                System.out.println(i + ". " + companyName);
            }
            System.out.println("0. Back");
            Scanner scanner = new Scanner(System.in);
            companyOpt = scanner.nextInt();
            if (companyOpt != 0) {
                System.out.println();
                System.out.println("'" + companyArray.get(companyOpt - 1) + "' company");
            }
        } else {
            System.out.println("The company list is empty!");
        }
        stat.close();
        return companyOpt;
    }

    public static int companyMenu() {
        System.out.println("1. Car list");
        System.out.println("2. Create a car");
        System.out.println("0. Back");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }

    public static void carList(Connection conn, int companyID) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "SELECT NAME FROM CAR " +
                "WHERE COMPANY_ID=" + companyID +
                "ORDER BY ID;";
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            System.out.println("Car list:");
            int i = 1;
            String carName = rs.getString("NAME");
            System.out.println(i + ". " + carName);
            while (rs.next()) {
                i++;
                carName = rs.getString("NAME");
                System.out.println(i + ". " + carName);
            }
        } else {
            System.out.println("The car list is empty!");
        }
        stmt.close();
    }

    public static void createCar(Connection conn, int companyID) throws SQLException {
        System.out.println("Enter the car name:");
        Scanner scanner = new Scanner(System.in);
        String carName = scanner.nextLine();
        Statement stmt = conn.createStatement();
        String sql = "INSERT INTO CAR (NAME, COMPANY_ID, RENTED) " +
                "VALUES ('" + carName + "', " + companyID +", 0);";
        stmt.executeUpdate(sql);
        System.out.println("The car was added!");
        stmt.close();
    }

    public static void createEmptyTables() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);
        Statement statement = connection.createStatement();
        String SQL = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES;";
        ResultSet result = statement.executeQuery(SQL);
        String tableName;
        boolean companyExists = false;
        boolean carExists = false;
        boolean customerExists = false;
        while (result.next()) {
            tableName = result.getString("TABLE_NAME");
            if ("COMPANY".equals(tableName)) {
                companyExists = true;
            } else if ("CAR".equals(tableName)) {
                carExists = true;
            } else if ("CUSTOMER".equals(tableName)) {
                customerExists = true;
            }
        }
        if (!companyExists) {
            SQL = "CREATE TABLE COMPANY " +
                    " (ID INT IDENTITY(1, 1), " +
                    " NAME VARCHAR(255) NOT NULL UNIQUE, " +
                    " PRIMARY KEY (ID));";
            statement.executeUpdate(SQL);
        }
        if (!carExists) {
            SQL = "CREATE TABLE CAR " +
                    " (ID INT IDENTITY(1, 1), " +
                    " NAME VARCHAR(255) NOT NULL UNIQUE, " +
                    " COMPANY_ID INT NOT NULL, " +
                    " RENTED BOOL, " +
                    " PRIMARY KEY (ID), " +
                    " FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID));";
            statement.executeUpdate(SQL);
        }
        if (!customerExists) {
            SQL = "CREATE TABLE CUSTOMER " +
                    " (ID INT IDENTITY(1, 1), " +
                    " NAME VARCHAR(255) NOT NULL UNIQUE, " +
                    " RENTED_CAR_ID INT, " +
                    " PRIMARY KEY (ID), " +
                    " FOREIGN KEY (RENTED_CAR_ID) REFERENCES CAR(ID));";
            statement.executeUpdate(SQL);
        }
        SQL = "ALTER TABLE COMPANY ALTER COLUMN ID RESTART WITH 1;";
        statement.executeUpdate(SQL);
        SQL = "ALTER TABLE CAR ALTER COLUMN ID RESTART WITH 1;";
        statement.executeUpdate(SQL);
        SQL = "ALTER TABLE CUSTOMER ALTER COLUMN ID RESTART WITH 1;";
        statement.executeUpdate(SQL);
        statement.close();
        connection.close();
    }
}


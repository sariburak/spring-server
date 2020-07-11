package com.srdc.staj.burak.assign2.springserver;

import org.springframework.stereotype.Component;
import srdj.staj.burak.Message;
import srdj.staj.burak.User;

import javax.xml.crypto.Data;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.sql.*;
import java.util.ArrayList;

@Component
public class DataBase {
    private Connection conn;
    String db_admin;
    String db_admin_password;
    String url;

    public void setDb_admin(String db_admin) {
        this.db_admin = db_admin;
    }

    public void setDb_admin_password(String db_admin_password) {
        this.db_admin_password = db_admin_password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Connection ConnectDB(){
        try {
            conn = DriverManager.getConnection(url, db_admin, db_admin_password);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        System.out.println("Connection established");
        return conn;
    }

    public void createUser(User user) throws SQLException {
        Statement stmt = null;

        stmt = conn.createStatement();

        String sql = "insert into users (user_name, password, name, surname, gender, birthdate, email)" +
                " values ('" + user.getUsername() + "' , '" + user.getPassword() + "', '" + user.getName() + "', '" + user.getSurname() + "', '" +
                user.getGender().toString() + "', '" + user.getBirthdate() + "', '" + user.getEmail() + "')";

        stmt.executeUpdate(sql);

    }

    public ArrayList<User> userList() throws SQLException {
        PreparedStatement stmt = null;
        String stmtString =
                "select * from users";


        stmt = conn.prepareStatement(stmtString);
        ResultSet rs = stmt.executeQuery();

        ArrayList<User> Result = new ArrayList<>();
        while(rs.next()){
            User found = construcUserFromRS(rs);

            Result.add(found);
        }
        return Result;
    }

    private User construcUserFromRS(ResultSet rs) throws SQLException {
        User found = new User();
        found.setUsername(rs.getString("user_name"));
        found.setPassword(rs.getString("password"));
        found.setBirthdate(rs.getString("birthdate"));
        found.setName(rs.getString("name"));
        found.setSurname(rs.getString("surname"));
        found.setGender(rs.getString("gender"));
        found.setEmail(rs.getString("email"));
        found.setIsAdmin(rs.getInt("is_admin") == 1);
        return found;
    }

    public User getUserFromDB(String user_id, String password) throws SQLException {

        String getUserString =
                "select * from users where " +
                        " user_name = ? AND " +
                        " password = ?";

        PreparedStatement stmt = conn.prepareStatement(getUserString);
        stmt.setString(1, user_id);
        stmt.setString(2,password);
        ResultSet rs = stmt.executeQuery();
        //Need a return statement
        if(rs.next() == false){
            return null;
        }else{
            //Construct user with given info
            User found = construcUserFromRS(rs);
            return found;
        }

    }

    public User getUserFromDB(String user_id) throws SQLException {

        String getUserString =
                "select * from users where " +
                        " user_name = ?";

        PreparedStatement stmt = conn.prepareStatement(getUserString);
        stmt.setString(1, user_id);
        ResultSet rs = stmt.executeQuery();
        //Need a return statement
        if(rs.next() == false){
            return null;
        }else{
            //Construct user with given info
            User found = construcUserFromRS(rs);
            return found;
        }

    }

    public void deleteUser(String user) throws SQLException, UserNotFoundException {
        Statement stmt = null;

        stmt = conn.createStatement();
        String sql = "delete from users" +
                " where user_name = " + "'" + user + "'";
        int rs = stmt.executeUpdate(sql);
        if(rs == 0){
            throw new UserNotFoundException();
        }
    }

    public ArrayList<Message> getInbox(String user) throws SQLException, DatatypeConfigurationException {
        PreparedStatement stmt = null;
        String statementString= "select *" +
                " from messages" +
                " where receiver = ?";
        return getMessages(user, statementString);
    }

    public ArrayList<Message> getOutbox(String user) throws SQLException, DatatypeConfigurationException {
        PreparedStatement stmt = null;
        String statementString= "select *" +
                " from messages" +
                " where sender = ?";


        return getMessages(user, statementString);
    }

    private ArrayList<Message> getMessages(String user, String statementString) throws SQLException, DatatypeConfigurationException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement(statementString);
        stmt.setString(1, user);

        ResultSet rs = stmt.executeQuery();

        ArrayList<Message> Result = new ArrayList<>();
        while(rs.next()){
            Message found = new Message();
            found.setContent(rs.getString("message"));

            Date date = rs.getDate("date");
            found.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(date.toString()));

            found.setReceiver(rs.getString("receiver"));
            found.setSender(rs.getString("sender"));
            found.setTitle(rs.getString("title"));

            Result.add(found);
        }
        for (Message message : Result) {
            System.out.println(message.getContent());
        }
        return Result;
    }

    public void createMessage(Message message) throws SQLException, UserNotFoundException {
        PreparedStatement stmt = null;
        String statementString =
                "insert into messages (message, receiver, sender, date, title) " +
                        "values (?, ? ,?, ?, ?)";

        stmt = conn.prepareStatement(statementString);
        stmt.setString(1, message.getContent());
        stmt.setString(2, message.getReceiver());
        stmt.setString(3, message.getSender());
        stmt.setDate(4, Date.valueOf(String.format(String.valueOf(message.getDate()), "dd-mm-yyyy")));
        stmt.setString(5, message.getTitle());

        int rs = stmt.executeUpdate();
        if(rs == 0) throw new UserNotFoundException();
    }

    public void deleteMessage(Message message) throws SQLException {
        PreparedStatement stmt = null;
        String statementString =
                "delete from messages " +
                        " where sender = ? AND " +
                        " receiver = ? AND" +
                        " message = ?";

        stmt = conn.prepareStatement(statementString);
        stmt.setString(1, message.getSender());
        stmt.setString(2, message.getReceiver());
        stmt.setString(3, message.getSender());

        stmt.executeUpdate();
    }

    public void updateUser(User user) throws UserNotFoundException, SQLException {
        PreparedStatement stmt = null;
        String statementString = "update users" +
                " set password = ?," +
                " birthdate = ?," +
                " name = ?," +
                " surname = ?," +
                " gender = ?," +
                " email = ?" +
                " where user_name = ?";


        stmt = conn.prepareStatement(statementString);
        stmt.setString(1, user.getPassword());
        stmt.setDate(2, Date.valueOf(user.getBirthdate()));
        stmt.setString(3, user.getName());
        stmt.setString(4, user.getSurname());
        stmt.setString(5, String.valueOf(user.getGender()));
        stmt.setString(6, user.getEmail());
        stmt.setString(7, user.getUsername());
        int rs = stmt.executeUpdate();
        if(rs == 0){
            throw new UserNotFoundException();
        }

    }

    /*public static void main(String[] args) {
        DataBase DB = new DataBase();
        DB.setDb_admin("postgres");
        DB.setDb_admin_password("8853116");
        DB.setUrl("jdbc:postgresql://localhost:5432/MessagingTool");
        DB.ConnectDB();

        try {
            ArrayList<Message> messages = DB.getInbox("buraksari");
            for (Message message : messages) {
                System.out.println(message.getContent());
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
    }*/

}

class UserNotFoundException extends Exception{
    public UserNotFoundException(){super("Person not found");}
}


/* insert into users (user_name, password, name, surname, birthdate, gender, email, is_admin)
	values ('sariburak', '123456789', 'Burak', 'Sari', '29-08-2000', 'M','mail@mail.com', 1);*/

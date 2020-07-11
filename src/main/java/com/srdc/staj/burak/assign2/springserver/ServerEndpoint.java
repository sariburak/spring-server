package com.srdc.staj.burak.assign2.springserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import srdj.staj.burak.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.sql.Array;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Endpoint
public class ServerEndpoint {
    private static final String NAMESPACE_URI = "SRDJ/staj/burak";

    private DataBase DB;

    @Autowired
    ServerEndpoint(DataBase db){
        db.setDb_admin("postgres");
        db.setDb_admin_password("8853116");
        db.setUrl("jdbc:postgresql://localhost:5432/MessagingTool");
        db.ConnectDB();
        this.DB = db;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "loginRequest")
    @ResponsePayload
    public LoginResponse login(@RequestPayload LoginRequest request){
        LoginResponse response = new LoginResponse();
        User user = null;
        try {
            user = DB.getUserFromDB(request.getUsername(), request.getPassword());
            response.setUser(user);
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "sendMessageRequest")
    @ResponsePayload
    public SendMessageResponse send(@RequestPayload SendMessageRequest request){
        SendMessageResponse response = new SendMessageResponse();
        try {
            Message message = new Message();
            message.setTitle(request.getTitle());
            message.setContent(request.getContent());
            message.setSender(request.getSender());
            message.setReceiver(request.getReceiver());
            message.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(LocalDate.now().toString()));
            DB.createMessage(message);
            response.setResponse(true);
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart ="logoutRequest")
    @ResponsePayload
    public LogoutResponse logout(@RequestPayload LogoutRequest request){
        LogoutResponse response = new LogoutResponse();
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getInboxRequest")
    @ResponsePayload
    public GetInboxResponse getInbox(@RequestPayload GetInboxRequest request){
        GetInboxResponse response = new GetInboxResponse();
        try {
            ArrayList<Message> messages = DB.getInbox(request.getUsername());
            for (Message message : messages) {
                response.getMessages().add(message);
            }
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getOutboxRequest")
    @ResponsePayload
    public GetOutboxResponse getOutbox(@RequestPayload GetOutboxRequest request){
        GetOutboxResponse response = new GetOutboxResponse();

        try {
            ArrayList<Message> inbox = DB.getInbox(request.getUsername());
            for (Message message : inbox) {
                response.getMessages().add(message);
            }
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUserListRequest")
    @ResponsePayload
    public GetUserListResponse userList(){
        GetUserListResponse response = new GetUserListResponse();

        try {
            ArrayList<User> users = DB.userList();
            for (User user : users) {
                response.getUsers().add(user);
            }
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return response;
    }


    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateUserRequest")
    @ResponsePayload
    public UpdateUserResponse updateUser(@RequestPayload UpdateUserRequest request){
        UpdateUserResponse response = new UpdateUserResponse();
        User user = null;
        try {
            user = DB.getUserFromDB(request.getUsername());
            switch (request.getAction().toLowerCase()){
                case "password":
                    user.setPassword(request.getValue());
                    break;
                case "name":
                    user.setName(request.getValue());
                    break;
                case "surname":
                    user.setSurname(request.getValue());
                    break;
                case "birthdate":
                    user.setBirthdate(request.getValue());
                    break;
                case "gender":
                    user.setGender(request.getValue());
                    break;
                case "email":
                    user.setEmail(request.getValue());
                    break;
                default:
                    throw new InvalidCommandException();
            }
            DB.updateUser(user);
            response.setResponse(true);
            return response;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidCommandException e) {
            e.printStackTrace();
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createUserRequest")
    @ResponsePayload
    public CreateUserResponse createUser(@RequestPayload CreateUserRequest request){
        CreateUserResponse response = new CreateUserResponse();
        try {
            DB.createUser(request.getUser());
            response.setResponse(true);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return response;
    }
}
